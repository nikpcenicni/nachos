package nachos.userprog;

import nachos.machine.*;
import nachos.threads.*;
import nachos.userprog.*;

import java.util.LinkedList; 

/**
 * A kernel that can support multiple user processes.
 */
public class UserKernel extends ThreadedKernel {
	
	// Data fields
	public LinkedList<TranslationEntry> pageLinkedList;
    public Lock lock;
    
    /**
     * Allocate a new user kernel.
     */
    public UserKernel() {
    	super();
    	pageLinkedList = new LinkedList<TranslationEntry>();
    }
    
    /**
     * Initialize this kernel. Creates a synchronized console and sets the
     * processor's exception handler and page list
     */
    public void initialize(String[] args) {
		super.initialize(args);
	
		console = new SynchConsole(Machine.console());
		
		Machine.processor().setExceptionHandler(new Runnable() {
			public void run() { exceptionHandler(); }
		});
		
		int numberOfPages = Machine.processor().getNumPhysPages();
		
		for (int i = 0; i < numberOfPages; i++) {
			pageLinkedList.add(new TranslationEntry(0,i,false,false,false,false));
		}
		
		lock = new Lock();
    }

    /**
     * Test the console device.
     */	
    public void selfTest() {
		super.selfTest();
	
		System.out.println("Testing the console device. Typed characters");
		System.out.println("will be echoed until q is typed.");
	
		char c;
	
		do {
		    c = (char) console.readByte(true);
		    console.writeByte(c);
		}
		while (c != 'q');
	
		System.out.println("");
    }

    /**
     * Returns the current process.
     *
     * @return	the current process, or <tt>null</tt> if no process is current.
     */
    public static UserProcess currentProcess() {
		if (!(KThread.currentThread() instanceof UThread))
		    return null;
		
		return ((UThread) KThread.currentThread()).process;
    }

    /**
     * The exception handler. This handler is called by the processor whenever
     * a user instruction causes a processor exception.
     *
     * <p>
     * When the exception handler is invoked, interrupts are enabled, and the
     * processor's cause register contains an integer identifying the cause of
     * the exception (see the <tt>exceptionZZZ</tt> constants in the
     * <tt>Processor</tt> class). If the exception involves a bad virtual
     * address (e.g. page fault, TLB miss, read-only, bus error, or address
     * error), the processor's BadVAddr register identifies the virtual address
     * that caused the exception.
     */
    public void exceptionHandler() {
		Lib.assertTrue(KThread.currentThread() instanceof UThread);
	
		UserProcess process = ((UThread) KThread.currentThread()).process;
		int cause = Machine.processor().readRegister(Processor.regCause);
		process.handleException(cause);
    }

    /**
     * Start running user programs, by creating a process and running a shell
     * program in it. The name of the shell program it must run is returned by
     * <tt>Machine.getShellProgramName()</tt>.
     *
     * @see	nachos.machine.Machine#getShellProgramName
     */
    public void run() {
		super.run();
	
		UserProcess process = UserProcess.newUserProcess();
		
		String shellProgram = Machine.getShellProgramName();	
		Lib.assertTrue(process.execute(shellProgram, new String[] { }));
	
		KThread.currentThread().finish();
    }

    /**
     * Terminate this kernel. Never returns.
     */
    public void terminate() {
    	super.terminate();
    }
    
    /**
     * getPages(int numberOfPages)
     * @param numberOfPages the total number of pages
     * @return the TranslationEntry array
     */
    public TranslationEntry[] getPages(int numberOfPages) {
    	
    	lock.acquire();
    	TranslationEntry[] pages = new TranslationEntry[numberOfPages];
    	
    	if((pageLinkedList.isEmpty() != true) && (pageLinkedList.size() >= numberOfPages)) {
    		for(int i = 0; i < numberOfPages; i++){
    			pages[i] = pageLinkedList.remove();
    			pages[i].valid = true;
    		}
    	}
    	lock.release();
		return pages;
    }
    
    public void resetPages (TranslationEntry[] userPages) {
    	lock.acquire();
    	
    	for(int i = 0; i < userPages.length; i++){
    		userPages[i].valid = false;
    		pageLinkedList.add(userPages[i]);
    	}
    	
    	lock.release();
    }
  
    public void releasePageTable(TranslationEntry[] pageTable){
    	pageLock.acquire();
    	
    	for(int i=0; i<pageTable.length; ++i){
    		pageTable[i].valid = false;
    		freePageList.add(pageTable[i]);
    	}
    	
    	pageLock.release();
    }
    
    
    
    
    
    
    
    
    /** Globally accessible reference to the synchronized console. */
    public static SynchConsole console;
    
    public static int processCount;
    public static Semaphore pCountMutex;

    // dummy variables to make javac smarter
    private static Coff dummy1 = null;
    static int pid;
    
    public LinkedList<TranslationEntry> freePageList;
    public Lock pageLock;
}
