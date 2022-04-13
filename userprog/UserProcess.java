package nachos.userprog;

import nachos.machine.*;
import nachos.threads.*;
import nachos.userprog.*;

import java.io.EOFException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.NoSuchElementException;


/**
 * Encapsulates the state of a user process that is not contained in its
 * user thread (or threads). This includes its address translation state, a
 * file table, and information about the program being executed.
 *
 * <p>
 * This class is extended by other classes to support additional functionality
 * (such as additional syscalls).
 *
 * @see	nachos.vm.VMProcess
 * @see	nachos.network.NetProcess
 */
public class UserProcess {
    /**
     * Allocate a new process.
     */
    public UserProcess() {
		int numPhysPages = Machine.processor().getNumPhysPages();
		pageTable = new TranslationEntry[numPhysPages];
		for (int i=0; i<numPhysPages; i++)
		    pageTable[i] = new TranslationEntry(i,i, true,false,false,false);
        this.pid = UserKernel.pid;
        ++UserKernel.pid;
        this.fd = new OpenFile[16];
        this.fd[0] = UserKernel.console.openForReading();
        this.fd[1] = UserKernel.console.openForWriting();
        this.statusLock = new Lock();
        this.joinCond = new Condition(this.statusLock);
        this.exitStatus = null;
    }
    
    /**
     * Allocate and return a new process of the correct class. The class name
     * is specified by the <tt>nachos.conf</tt> key
     * <tt>Kernel.processClassName</tt>.
     *
     * @return	a new process of the correct class.
     */
    public static UserProcess newUserProcess() {
    	return (UserProcess)Lib.constructObject(Machine.getProcessClassName());
    }

    /**
     * Execute the specified program with the specified arguments. Attempts to
     * load the program, and then forks a thread to run it.
     *
     * @param	name	the name of the file containing the executable.
     * @param	args	the arguments to pass to the executable.
     * @return	<tt>true</tt> if the program was successfully executed.
     */
    public boolean execute(String name, String[] args) {
		if (!load(name, args))
		    return false;
		
		new UThread(this).setName(name).fork();
	
		return true;
    }

    /**
     * Save the state of this process in preparation for a context switch.
     * Called by <tt>UThread.saveState()</tt>.
     */
    public void saveState() {
    }

    /**
     * Restore the state of this process after a context switch. Called by
     * <tt>UThread.restoreState()</tt>.
     */
    public void restoreState() {
    	Machine.processor().setPageTable(pageTable);
    }

    /**
     * Read a null-terminated string from this process's virtual memory. Read
     * at most <tt>maxLength + 1</tt> bytes from the specified address, search
     * for the null terminator, and convert it to a <tt>java.lang.String</tt>,
     * without including the null terminator. If no null terminator is found,
     * returns <tt>null</tt>.
     *
     * @param	vaddr	the starting virtual address of the null-terminated
     *			string.
     * @param	maxLength	the maximum number of characters in the string,
     *				not including the null terminator.
     * @return	the string read, or <tt>null</tt> if no null terminator was
     *		found.
     */
    public String readVirtualMemoryString(int vaddr, int maxLength) {
		Lib.assertTrue(maxLength >= 0);
	
		byte[] bytes = new byte[maxLength+1];
	
		int bytesRead = readVirtualMemory(vaddr, bytes);
	
		for (int length=0; length<bytesRead; length++) {
		    if (bytes[length] == 0)
			return new String(bytes, 0, length);
		}
	
		return null;
    }

    /**
     * Transfer data from this process's virtual memory to all of the specified
     * array. Same as <tt>readVirtualMemory(vaddr, data, 0, data.length)</tt>.
     *
     * @param	vaddr	the first byte of virtual memory to read.
     * @param	data	the array where the data will be stored.
     * @return	the number of bytes successfully transferred.
     */
    public int readVirtualMemory(int vaddr, byte[] data) {
    	return readVirtualMemory(vaddr, data, 0, data.length);
    }

    /**
     * Transfer data from this process's virtual memory to the specified array.
     * This method handles address translation details. This method must
     * <i>not</i> destroy the current process if an error occurs, but instead
     * should return the number of bytes successfully copied (or zero if no
     * data could be copied).
     *
     * @param	vaddr	the first byte of virtual memory to read.
     * @param	data	the array where the data will be stored.
     * @param	offset	the first byte to write in the array.
     * @param	length	the number of bytes to transfer from virtual memory to
     *			the array.
     * @return	the number of bytes successfully transferred.
     */
    public int readVirtualMemory(int vaddr, byte[] data, int offset, int length) {
    	Lib.assertTrue(offset >= 0 && length >= 0 && offset+length <= data.length);
    	
    	// Acquire lock
    	lock.acquire();
    	
		byte[] memory = Machine.processor().getMemory();
		
		// Initilize variables for return
		int amtBytes = accessMemory(vaddr, data, offset, length1, true);
		int length1 = Math.min(length, pageSize - vaddr % pageSize);
		int numOfPages = ((length + vaddr % pageSize) / pageSize) + 1;
		
		// Check if number of pages is greater than 1
		if(numOfPages > 1){
			// Loop through and add to amount
			for(int i = 1; i < numOfPages; i++){
				amtBytes += accessMemory((vaddr/pageSize + i * pageSize), data, offset + amtBytes, Math.min(length - amtBytes, pageSize), true);
			}
		}
		
		// Release lock
		lock.release();
		
		// Return amount of bytes
		return amtBytes;
    }

    /**
     * Transfer all data from the specified array to this process's virtual
     * memory.
     * Same as <tt>writeVirtualMemory(vaddr, data, 0, data.length)</tt>.
     *
     * @param	vaddr	the first byte of virtual memory to write.
     * @param	data	the array containing the data to transfer.
     * @return	the number of bytes successfully transferred.
     */
    public int writeVirtualMemory(int vaddr, byte[] data) {
    	return writeVirtualMemory(vaddr, data, 0, data.length);
    }

    /**
     * Transfer data from the specified array to this process's virtual memory.
     * This method handles address translation details. This method must
     * <i>not</i> destroy the current process if an error occurs, but instead
     * should return the number of bytes successfully copied (or zero if no
     * data could be copied).
     *
     * @param	vaddr	the first byte of virtual memory to write.
     * @param	data	the array containing the data to transfer.
     * @param	offset	the first byte to transfer from the array.
     * @param	length	the number of bytes to transfer from the array to
     *			virtual memory.
     * @return	the number of bytes successfully transferred.
     */
    public int writeVirtualMemory(int vaddr, byte[] data, int offset, int length) {
		Lib.assertTrue(offset >= 0 && length >= 0 && offset+length <= data.length);
		
		// Acquire lock
		lock.aquire();
	
		byte[] memory = Machine.processor().getMemory();
		
		// Initialize variables for return
		int amtBytes = accessMemory(vaddr, data, offset, length1, false);
		int length1 = Math.min(length, pageSize - vaddr % pageSize);
		int numOfPages = ((length + vaddr % pageSize) / pageSize) + 1;
		
		// Check if number of pages is greater than 1
		if(numOfPages > 1){
			// Loop through and add to amount
			for(int i = 1; i < numOfPages; i++){
				amtBytes += accessMemory((vaddr / pageSize + i * pageSize), data, offset+amtBytes, Math.min(length - amtBytes, pageSize), false);
			}
		}
		
		// Release Lock
		lock.release();
		
		// Return amount of bytes
		return amtBytes;
    }

    /**
     * Load the executable with the specified name into this process, and
     * prepare to pass it the specified arguments. Opens the executable, reads
     * its header information, and copies sections and arguments into this
     * process's virtual memory.
     *
     * @param	name	the name of the file containing the executable.
     * @param	args	the arguments to pass to the executable.
     * @return	<tt>true</tt> if the executable was successfully loaded.
     */
    private boolean load(String name, String[] args) {
		Lib.debug(dbgProcess, "UserProcess.load(\"" + name + "\")");
		
		OpenFile executable = ThreadedKernel.fileSystem.open(name, false);
		if (executable == null) {
		    Lib.debug(dbgProcess, "\topen failed");
		    return false;
		}
	
		try {
		    coff = new Coff(executable);
		}
		catch (EOFException e) {
		    executable.close();
		    Lib.debug(dbgProcess, "\tcoff load failed");
		    return false;
		}
	
		// make sure the sections are contiguous and start at page 0
		numPages = 0;
		for (int s=0; s<coff.getNumSections(); s++) {
		    CoffSection section = coff.getSection(s);
		    if (section.getFirstVPN() != numPages) {
			coff.close();
			Lib.debug(dbgProcess, "\tfragmented executable");
			return false;
		    }
		    numPages += section.getLength();
		}
	
		// make sure the argv array will fit in one page
		byte[][] argv = new byte[args.length][];
		int argsSize = 0;
		for (int i=0; i<args.length; i++) {
		    argv[i] = args[i].getBytes();
		    // 4 bytes for argv[] pointer; then string plus one for null byte
		    argsSize += 4 + argv[i].length + 1;
		}
		if (argsSize > pageSize) {
		    coff.close();
		    Lib.debug(dbgProcess, "\targuments too long");
		    return false;
		}
	
		// program counter initially points at the program entry point
		initialPC = coff.getEntryPoint();	
	
		// next comes the stack; stack pointer initially points to top of it
		numPages += stackPages;
		initialSP = numPages*pageSize;
	
		// and finally reserve 1 page for arguments
		numPages++;
	
		if (!loadSections())
		    return false;
	
		// store arguments in last page
		int entryOffset = (numPages-1)*pageSize;
		int stringOffset = entryOffset + args.length*4;
	
		this.argc = args.length;
		this.argv = entryOffset;
		
		for (int i=0; i<argv.length; i++) {
		    byte[] stringOffsetBytes = Lib.bytesFromInt(stringOffset);
		    Lib.assertTrue(writeVirtualMemory(entryOffset,stringOffsetBytes) == 4);
		    entryOffset += 4;
		    Lib.assertTrue(writeVirtualMemory(stringOffset, argv[i]) ==
			       argv[i].length);
		    stringOffset += argv[i].length;
		    Lib.assertTrue(writeVirtualMemory(stringOffset,new byte[] { 0 }) == 1);
		    stringOffset += 1;
		}
	
		return true;
    }

    /**
     * Allocates memory for this process, and loads the COFF sections into
     * memory. If this returns successfully, the process will definitely be
     * run (this is the last step in process initialization that can fail).
     *
     * @return	<tt>true</tt> if the sections were successfully loaded.
     */
    protected boolean loadSections() {
    	
    	// Check to see if there's enough memory to start a new process
		if (numPages > Machine.processor().getNumPhysPages()) {
			// Close the Coff
			coff.close();
			
			// Print not enough memory to console
		    System.out.println("Not Enough Memory!");
		    
		    // Return false
		    return false;
		}
		
		// Store page table in variable
		pageTable = ((UserKernel)Kernel.kernel).getPages(numPages);
		
		if (pageTable == 0) {
			// Close the coff
			coff.close();
			
			// Return false
			return false;
		}
		
		for (int i = 0; i < pageTable.length; i++) {
			pageTable[i].vpn = i;
		}
		
		// Load sections
		for (int s = 0; s < coff.getNumSections(); s++) {
		    CoffSection section = coff.getSection(s);
		    
		    Lib.debug(dbgProcess, "\tinitializing " + section.getName()
			      + " section (" + section.getLength() + " pages)");
	
		    for (int i = 0; i < section.getLength(); i++) {
			int vpn = section.getFirstVPN()+i;
	
			// for now, just assume virtual addresses=physical addresses
			section.loadPage(i, vpn);
		    }
		}
		
		// Return true
		return true;
    }

    /**
     * Release any resources allocated by <tt>loadSections()</tt>.
     */
    protected void unloadSections() {
    	// Release the page table
    	((UserKernel)Kernel.kernel).releasePageTable(pageTable);
    }    

    /**
     * Initialize the processor's registers in preparation for running the
     * program loaded into this process. Set the PC register to point at the
     * start function, set the stack pointer register to point at the top of
     * the stack, set the A0 and A1 registers to argc and argv, respectively,
     * and initialize all other registers to 0.
     */
    public void initRegisters() {
		Processor processor = Machine.processor();
	
		// by default, everything's 0
		for (int i=0; i<processor.numUserRegisters; i++)
		    processor.writeRegister(i, 0);
	
		// initialize PC and SP according
		processor.writeRegister(Processor.regPC, initialPC);
		processor.writeRegister(Processor.regSP, initialSP);
	
		// initialize the first two argument registers to argc and argv
		processor.writeRegister(Processor.regA0, argc);
		processor.writeRegister(Processor.regA1, argv);
    }

    private int isInTable(String filename) {
        for (int i = 0; i < this.fd.length; i++) {
          OpenFile currFile = this.fd[i];
          if (currFile != null && filename == currFile.getName())
            return i;
        }
        return -1;
      }

    /**
     * Handle the halt() system call. 
     */
    private int handleHalt() {
	
		Machine.halt();
		
		Lib.assertNotReached("Machine.halt() did not halt machine!");
		return 0;
    }

    private int handleExec(int file, int argc, int argv){
    	String fileName = null;
		fileName = readVirtualMemoryString(file, 256);
		
		if(fileName == null)
		{
			System.out.println("handleExec: Could not read filename from Virtual Memory");
			return -1;
		}
		if (argc < 0) {
			System.out.println("handleExec: argc < 0");
			return -1;
		}
		
		String extension = fileName.toLowerCase().substring(fileName.length() - 5);
		if(!".coff".equals(extension)){
			System.out.println("handleExec: File extension incorrect (must be .coff)");
			return -1;
		}
		
		String[] args = new String[argc];
		
		byte[] temp = new byte[4];

		for (int i = 0; i < argc; i++) 
		{
			if(readVirtualMemory(argv+i*4, temp) != temp.length){
				System.out.println("handleExec: Error reading arg ");
				return -1;
			}
			args[i] = readVirtualMemoryString(Lib.bytesToInt(temp, 0),256);
			
			if (args[i] == null)
			{
				System.out.println("handleExec: Error reading arg ");
				return -1;
			}
		}
		
		UserProcess child = newUserProcess();
		
		children.put(child.pID, child);
		
		child.parent = this;
		
		boolean insertProgram = child.execute(fileName, args);
		
		if(insertProgram) {
			return child.pID;
		}
		
		return -1;
    }
    
	private int handleJoin(int processID, int status) {
	
		if(status < 0 || processID < 0)
			return -1;
		
		UserProcess child;
		
		if(children.containsKey(processID))
			child = children.get(processID);
		else {
			System.out.println("handleJoin: Error processID not found or has already joined with a parent");
			return -1;
		}
		
		child.statusLock.acquire();
		
		Integer childStatus = child.exitStatus;

		if (childStatus == null)
		{
			statusLock.acquire();
			child.statusLock.release();
			joinCond.sleep();
			statusLock.release();
			
			child.statusLock.acquire();
			childStatus = child.exitStatus;

		}
		
		child.statusLock.release();
		children.remove(processID);
			
		byte[] statuses = Lib.bytesFromInt(childStatus.intValue());
		writeVirtualMemory(status, statuses);
		
		if (childStatus.intValue() == 0)
			return 1;
		else		
			return 0;
		
	}
    
    private int handleExit(int status){
    	unloadSections();
    	for(int i = 2; i < fd.length; i++){
    		if(fd[i] != null)
    			fd[i].close();
    	}
    	statusLock.acquire();
    	exitStatus = status;
    	statusLock.release();
    	
    	parentMutex.P();
    	if(parent != null){
    		parent.statusLock.acquire();
    		parent.joinCond.wakeAll();
    		parent.statusLock.release();
    	}
    	parentMutex.V();
    	
    	for(UserProcess aChild : children.values()){
    		aChild.parentMutex.P();
    		aChild.parent = null;
    		aChild.parentMutex.V();
    	}
    	
    	decProcessCount();
    	UThread.finish();
    	return status;
    }
    
    private int handleCreate(int file){
        String fileName = null;
        fileName = readVirtualMemoryString(file, 256);
        if (fileName == null) {
            Lib.debug(dbgProcess, "HandleCreate: Could not get filename from memory");
            return -1;
        }

        OpenFile thisFile = ThreadedKernel.fileSystem.open(fileName, true);
        if (thisFile == null) {
            Lib.debug(dbgProcess, "HandleCreate: Unable to open file in filesystem");
            return -1;
        } else {
            int i;
            for (i = 2; i < this.fd.length; i++){
                if (this.fd[i] == null) {
                    this.fd[i] = thisFile;
                    return i;
                }
            }
            if (i == this.fd.length) {
                Lib.debug(dbgProcess, "HandleCreate: No space in file descriptor");
                return -1;
            }
        }
        return -1;
    }
    
    private int handleOpen(int file) {
        String filename = null;
        filename = readVirtualMemoryString(file, 256);
        if (filename == null) {
            Lib.debug(dbgProcess, "HandleOpen: Could not read filename from memory");
            return -1;
        }
        OpenFile thisFile = ThreadedKernel.fileSystem.open(filename, false);
        if (thisFile == null) {
            Lib.debug(dbgProcess, "HandleOpen: Could not open file from filesystem");
            return -1;
        } else {
            int i;
            for (i = 2; i < this.fd.length; i++){
                if (this.fd[i] == null){
                    this.fd[i] = thisFile;
                    return i;
                }
            }
            if (i == this.fd.length) {
                Lib.debug(dbgProcess, "HandleOpen: No more space in file descriptor");
                return -1;
            }
        }
        return -1;
    }

    private int handleClose(int file){
        if (file < 0 || file > 15) {
            Lib.debug(dbgProcess, "HandleClose: Trying to close the file " + file + " which is outside of range");
            return -1;
        }
        OpenFile thisFile = this.fd[file];
        if (thisFile == null) {
            Lib.debug(dbgProcess, "HandleClose: Trying to close a file that does not exist");
            return -1;
        } else {
            thisFile.close();
            this.fd[file] = null;
            return 0;
        }
    }
    
    private int handleRead(int file, int bufferAddr, int count) {
    	// Lib.debug(dbgProcess, "Function handleRead() Executing..."); 
    	// Lib.debug(dbgProcess, "File Handle: " + file);                      
	    // Lib.debug(dbgProcess, "Buffer Address: " + bufferAddr);                   
	    // Lib.debug(dbgProcess, "Buffer Size: " + count);
    	
	    // if((file >= 16 || file < 0) || count < 0 ||
		// 		fd[file].file == null) {
		// 	return -1;
		// }
	    
        // byte[] newBuffer = new byte[count];
	    // OpenFile f = fd[file];                                  

        // int result = f.file.read(f.position, newBuffer, 0, count);  
        
        // if (result <= 0) {                                           
        //     return -1;                                                   
        // }                                                                
        // else {                                                             
        //     int output = writeVirtualMemory(bufferAddr, newBuffer);
        //     	if(file >= 2){
        //     		f.position = f.position + output;
        //     	}                                      
        //     return result;                                                 
        //}    
        
        if (file < 0 || file == 1 || file > 15) {
            Lib.debug(dbgProcess, "HandleRead: Trying to read from the file " + file + " which is outside of range");
            return -1;
        }
        OpenFile f = this.fd[file];
        if (f == null) {
            Lib.debug(dbgProcess, "HandleRead: Trying to read from a file that does not exist");
            return -1;
        }
        byte[] buffer = new byte[pageSize];
        int leftToRead = count;
        int totalRead = 0;
        int read = 0;

        while (leftToRead > pageSize){
            read = f.read(bufferAddr, buffer, 0, pageSize);
            if (read == -1) {
                Lib.debug(dbgProcess, "HandleRead: Could not read from file");
                return -1;
            } else if (read == 0) {
                return totalRead;
            }
            int readBytes = writeVirtualMemory(bufferAddr, buffer, 0, read);
            if (read != readBytes) {
                Lib.debug(dbgProcess, "HandleRead: Read and write didnt match");
                return -1;
            }
            bufferAddr += readBytes;
            totalRead += readBytes;
            leftToRead -= readBytes;
        }
        read = f.read(buffer, 0, leftToRead);
        if (read == -1) {
            Lib.debug(dbgProcess, "HandleRead: Could not read from file");
            return -1;
        } 
        int readBytes = writeVirtualMemory(bufferAddr, buffer, 0, read);
        if (read != readBytes) {
            Lib.debug(dbgProcess, "HandleRead: Read and write didnt match");
            return -1;
        }
        totalRead += readBytes;
        return totalRead;
    }                                                                      
        

    private int handleWrite(int file, int bufferAddr, int count) {
    	// Lib.debug(dbgProcess, "Function handleWrite() Executing..."); 
    	// Lib.debug(dbgProcess, "File Handle: " + file);                      
	    // Lib.debug(dbgProcess, "Buffer Address: " + bufferAddr);                   
	    // Lib.debug(dbgProcess, "Buffer Size: " + count);
	    
	    // if((file >= 16 || file < 0) || count < 0 ||
		// 	fd[file].file == null) {
		// 	return -1;
		// }
	    
	    // byte[] newBuffer = new byte[count];
	    // OpenFile f = fd[file];    
	    
	    // int numOfBytes = readVirtualMemory(bufferAddr, newBuffer);
	    // int result = f.file.read(f.position, newBuffer, 0, count);
	    
	    // if (result <= 0) {                                           
        //     return -1;                                                   
        // }                                                                
        // else {                                                             
        //     f.position = f.position + output;                               
        //     return result;                                                 
        // }   
        
        if(file == 0){
            Lib.debug(dbgProcess, "HandleWrite: Trying to write to stdin");
            return -1;
        }
        if (file < 1 || file > 15){
            Lib.debug(dbgProcess, "HandleWrite: Trying to write to file " + file + " which is outside of range");
            return -1;
        }
        OpenFile f = this.fd[file];
        if (f == null){
            Lib.debug(dbgProcess, "HandleWrite: Trying to write to file " + file + " which does not exist");
            return -1;
        }

        byte[] buffer = new byte[pageSize];
        int leftToWrite = count;
        int totalWrote = 0;
        int written = 0;
        while (leftToWrite > pageSize) {
            written = readVirtualMemory(bufferAddr, buffer);
            int write = f.write(buffer, 0, written);
            if (written != write) {
                Lib.debug(dbgProcess, "HandleWrite: Not all bytes were written");
            }
            if (write == -1){
                Lib.debug(dbgProcess, "HandleWrite: Error writing to file");
                return -1;
            }
            else if (write == 0){
                return totalWrote;
            }
            bufferAddr += write;
            totalWrote += write;
            leftToWrite -= write;
        }
        written = readVirtualMemory(bufferAddr, buffer, 0, leftToWrite);
        int write = f.write(buffer, 0, written);
        if (written != write) {
            Lib.debug(dbgProcess, "HandleWrite: Not all bytes were written");
        }
        if (write == -1){
            Lib.debug(dbgProcess, "HandleWrite: Error writing to file");
            return -1;
        }
        
        totalWrote += write;
        return totalWrote;
        
    }      
	   

    private int handleUnlink(int file) {
        String filename = readVirtualMemoryString(file, 256);
        if (filename == null) {
            Lib.debug(dbgProcess, "HandleUnlink: Could not read file name from virtual memory");
            return -1;
        }
        int indx = isInTable(filename);
        if (indx != -1) {
            handleClose(indx);
        }
        if (ThreadedKernel.fileSystem.remove(filename)){
            return 0;
        }
        return -1;
    }



    private static final int
        syscallHalt = 0,
        syscallExit = 1,
        syscallExec = 2,
        syscallJoin = 3,
        syscallCreate = 4,
        syscallOpen = 5,
        syscallRead = 6,
        syscallWrite = 7,
        syscallClose = 8,
        syscallUnlink = 9;

    /**
     * Handle a syscall exception. Called by <tt>handleException()</tt>. The
     * <i>syscall</i> argument identifies which syscall the user executed:
     *
     * <table>
     * <tr><td>syscall#</td><td>syscall prototype</td></tr>
     * <tr><td>0</td><td><tt>void halt();</tt></td></tr>
     * <tr><td>1</td><td><tt>void exit(int status);</tt></td></tr>
     * <tr><td>2</td><td><tt>int  exec(char *name, int argc, char **argv);
     * 								</tt></td></tr>
     * <tr><td>3</td><td><tt>int  join(int pid, int *status);</tt></td></tr>
     * <tr><td>4</td><td><tt>int  creat(char *name);</tt></td></tr>
     * <tr><td>5</td><td><tt>int  open(char *name);</tt></td></tr>
     * <tr><td>6</td><td><tt>int  read(int fd, char *buffer, int size);
     *								</tt></td></tr>
     * <tr><td>7</td><td><tt>int  write(int fd, char *buffer, int size);
     *								</tt></td></tr>
     * <tr><td>8</td><td><tt>int  close(int fd);</tt></td></tr>
     * <tr><td>9</td><td><tt>int  unlink(char *name);</tt></td></tr>
     * </table>
     * 
     * @param	syscall	the syscall number.
     * @param	a0	the first syscall argument.
     * @param	a1	the second syscall argument.
     * @param	a2	the third syscall argument.
     * @param	a3	the fourth syscall argument.
     * @return	the value to be returned to the user.
     */
    public int handleSyscall(int syscall, int a0, int a1, int a2, int a3) {
		switch (syscall) {
		    case syscallHalt:
		        return handleHalt();
	        case syscallCreate:
	            return handleCreate(a0);
	        case syscallOpen:
	            return handleOpen(a0);
	        case syscallClose:
	            return handleClose(a0);
	        case syscallWrite:
	            return handleWrite(a0, a1, a2);
	        case syscallRead:
	            return handleRead(a0, a1, a2);
	        case syscallUnlink:
	            return handleUnlink(a0);
	
	
		default:
		    Lib.debug(dbgProcess, "Unknown syscall " + syscall);
		    Lib.assertNotReached("Unknown system call!");
		}
		return 0;
    }

    /**
     * Handle a user exception. Called by
     * <tt>UserKernel.exceptionHandler()</tt>. The
     * <i>cause</i> argument identifies which exception occurred; see the
     * <tt>Processor.exceptionZZZ</tt> constants.
     *
     * @param	cause	the user exception that occurred.
     */
    public void handleException(int cause) {
		Processor processor = Machine.processor();
	
		switch (cause) {
		case Processor.exceptionSyscall:
		    int result = handleSyscall(processor.readRegister(Processor.regV0),
					       processor.readRegister(Processor.regA0),
					       processor.readRegister(Processor.regA1),
					       processor.readRegister(Processor.regA2),
					       processor.readRegister(Processor.regA3)
					       );
		    processor.writeRegister(Processor.regV0, result);
		    processor.advancePC();
		    break;				       
					       
		default:
		    Lib.debug(dbgProcess, "Unexpected exception: " +
			      Processor.exceptionNames[cause]);
		    Lib.assertNotReached("Unexpected exception");
		}
    }
    
    public void decProcessCount(){
    	UserKernel.pCountMutex.P();
    	
    	if(--UserKernel.processCount == 0)
    		Kernel.kernel.terminate();
    	
    	UserKernel.pCountMutex.V();
    }
    
    protected OpenFile[] fd;

    /** The program being run by this process. */
    protected Coff coff;

    /** This process's page table. */
    protected TranslationEntry[] pageTable;
    /** The number of contiguous pages occupied by the program. */
    protected int numPages;

    /** The number of pages in the program's stack. */
    protected final int stackPages = 8;
    
    private int initialPC, initialSP;
    private int argc, argv;
	
    private static final int pageSize = Processor.pageSize;
    private static final char dbgProcess = 'a';
    
    protected int pID;
    
    protected Hashtable<Integer, UserProcess> children = new Hashtable<Integer, UserProcess>();
    
    protected UserProcess parent;
    protected Semaphore parentMutex = new Semaphore(1);
    
    protected Integer exitStatus;
    protected Lock statusLock;
    protected Condition joinCond;
}
