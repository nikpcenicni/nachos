package nachos.threads;

import nachos.machine.*;
import java.util.ArrayList;

/**
 * A KThread is a thread that can be used to execute Nachos kernel code. Nachos
 * allows multiple threads to run concurrently.
 *
 * To create a new thread of execution, first declare a class that implements
 * the <tt>Runnable</tt> interface. That class then implements the <tt>run</tt>
 * method. An instance of the class can then be allocated, passed as an
 * argument when creating <tt>KThread</tt>, and forked. For example, a thread
 * that computes pi could be written as follows:
 *
 * <p><blockquote><pre>
 * class PiRun implements Runnable {
 *     public void run() {
 *         // compute pi
 *         ...
 *     }
 * }
 * </pre></blockquote>
 * <p>The following code would then create a thread and start it running:
 *
 * <p><blockquote><pre>
 * PiRun p = new PiRun();
 * new KThread(p).fork();
 * </pre></blockquote>
 */
public class KThread {
    /**
     * Get the current thread.
     *
     * @return	the current thread.
     */
    public static KThread currentThread() {
        Lib.assertTrue(currentThread != null);
        return currentThread;
    }
    
    /**
     * Allocate a new <tt>KThread</tt>. If this is the first <tt>KThread</tt>,
     * create an idle thread as well.
     */
    public KThread() {
        if (currentThread != null) {
            tcb = new TCB();
        }	    
        else {
            readyQueue = ThreadedKernel.scheduler.newThreadQueue(false);
            readyQueue.acquire(this);	 
            
            currentThread = this;
            tcb = TCB.currentTCB();
            name = "main";
            restoreState();

            createIdleThread();
        }
    }

    /**
     * Allocate a new KThread.
     *
     * @param	target	the object whose <tt>run</tt> method is called.
     */
    public KThread(Runnable target) {
        this();
        this.target = target;
    }

    /**
     * Set the target of this thread.
     *
     * @param	target	the object whose <tt>run</tt> method is called.
     * @return	this thread.
     */
    public KThread setTarget(Runnable target) {
        Lib.assertTrue(status == statusNew);
        
        this.target = target;
        return this;
    }

    /**
     * Set the name of this thread. This name is used for debugging purposes
     * only.
     *
     * @param	name	the name to give to this thread.
     * @return	this thread.
     */
    public KThread setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get the name of this thread. This name is used for debugging purposes
     * only.
     *
     * @return	the name given to this thread.
     */     
    public String getName() {
	    return name;
    }

    /**
     * Get the full name of this thread. This includes its name along with its
     * numerical ID. This name is used for debugging purposes only.
     *
     * @return	the full name given to this thread.
     */
    public String toString() {
	    return (name + " (#" + id + ")");
    }

    /**
     * Deterministically and consistently compare this thread to another
     * thread.
     */
    public int compareTo(Object o) {
        KThread thread = (KThread) o;

        if (id < thread.id)
            return -1;
        else if (id > thread.id)
            return 1;
        else
            return 0;
    }

    /**
     * Causes this thread to begin execution. The result is that two threads
     * are running concurrently: the current thread (which returns from the
     * call to the <tt>fork</tt> method) and the other thread (which executes
     * its target's <tt>run</tt> method).
     */
    public void fork() {
        Lib.assertTrue(status == statusNew);
        Lib.assertTrue(target != null);
        
        Lib.debug(dbgThread,
            "Forking thread: " + toString() + " Runnable: " + target);

        boolean intStatus = Machine.interrupt().disable();

        tcb.start(new Runnable() {
            public void run() {
                runThread();
            }
	    });

        ready();
        
        Machine.interrupt().restore(intStatus);
    }

    private void runThread() {
        begin();
        target.run();
        finish();
    }

    private void begin() {
        Lib.debug(dbgThread, "Beginning thread: " + toString());
        
        Lib.assertTrue(this == currentThread);

        restoreState();

        Machine.interrupt().enable();
    }

    /**
     * Finish the current thread and schedule it to be destroyed when it is
     * safe to do so. This method is automatically called when a thread's
     * <tt>run</tt> method returns, but it may also be called directly.
     *
     * The current thread cannot be immediately destroyed because its stack and
     * other execution state are still in use. Instead, this thread will be
     * destroyed automatically by the next thread to run, when it is safe to
     * delete this thread.
     */
    public static void finish() {
        Lib.debug(dbgThread, "Finishing thread: " + currentThread.toString());
        
        
        Machine.interrupt().disable();

        Machine.autoGrader().finishingCurrentThread();

        if (currentThread.joined)
            currentThread.waitQueue.nextThread().ready();

        Lib.assertTrue(toBeDestroyed == null);
        toBeDestroyed = currentThread;
        
        currentThread.status = statusFinished;
        
        sleep();
    }

    /**
     * Relinquish the CPU if any other thread is ready to run. If so, put the
     * current thread on the ready queue, so that it will eventually be
     * rescheuled.
     *
     * <p>
     * Returns immediately if no other thread is ready to run. Otherwise
     * returns when the current thread is chosen to run again by
     * <tt>readyQueue.nextThread()</tt>.
     *
     * <p>
     * Interrupts are disabled, so that the current thread can atomically add
     * itself to the ready queue and switch to the next thread. On return,
     * restores interrupts to the previous state, in case <tt>yield()</tt> was
     * called with interrupts disabled.
     */
    public static void yield() {
        Lib.debug(dbgThread, "Yielding thread: " + currentThread.toString());
        
        Lib.assertTrue(currentThread.status == statusRunning);
        
        boolean intStatus = Machine.interrupt().disable();

        currentThread.ready();

        runNextThread();
        
        Machine.interrupt().restore(intStatus);
    }

    /**
     * Relinquish the CPU, because the current thread has either finished or it
     * is blocked. This thread must be the current thread.
     *
     * <p>
     * If the current thread is blocked (on a synchronization primitive, i.e.
     * a <tt>Semaphore</tt>, <tt>Lock</tt>, or <tt>Condition</tt>), eventually
     * some thread will wake this thread up, putting it back on the ready queue
     * so that it can be rescheduled. Otherwise, <tt>finish()</tt> should have
     * scheduled this thread to be destroyed by the next thread to run.
     */
    public static void sleep() {
        Lib.debug(dbgThread, "Sleeping thread: " + currentThread.toString());
        
        Lib.assertTrue(Machine.interrupt().disabled());

        if (currentThread.status != statusFinished)
            currentThread.status = statusBlocked;

        runNextThread();
    }

    /**
     * Moves this thread to the ready state and adds this to the scheduler's
     * ready queue.
     */
    public void ready() {
        Lib.debug(dbgThread, "Ready thread: " + toString());
        
        Lib.assertTrue(Machine.interrupt().disabled());
        Lib.assertTrue(status != statusReady);
        
        status = statusReady;
        if (this != idleThread)
            readyQueue.waitForAccess(this);
        
        Machine.autoGrader().readyThread(this);
    }

    /**
     * Waits for this thread to finish. If this thread is already finished,
     * return immediately. This method must only be called once; the second
     * call is not guaranteed to return. This thread must not be the current
     * thread.
     */
    public void join() {
        //default calls
        Lib.debug(dbgThread, "Joining to thread: " + toString());
        Lib.assertTrue(this != currentThread);

        // check if the thread that is being joine has already completed or if it has already been joined
        if (this.status == statusFinished) return;
        else if (currentThread.usedID.contains(this.id)) return;

        // stores the machine state and disabled the interupts to be restored later
        boolean mStatus = Machine.interrupt().disable();
               
        //  check if the wait queue is not initalized and creates a new queue for waiting threads.
        if (this.waitQueue == null) {
            this.waitQueue = ThreadedKernel.scheduler.newThreadQueue(false);
            this.waitQueue.acquire(this);
        }

        // store that this thread has been joined and its id to prevent it from being joined through another thread
        this.usedID.add(currentThread.id);
        this.joined = true;
        for(int i = 0; i < currentThread.usedID.size(); i++){
            this.usedID.add(currentThread.usedID.get(i));
        }

        // add the thread to the wait queue to wait until it can be processed 
        this.waitQueue.waitForAccess(KThread.currentThread());

        // set the thread to sleep and restore the machine status stored earlier
        KThread.sleep();
        Machine.interrupt().restore(mStatus);
    }

    /**
     * Create the idle thread. Whenever there are no threads ready to be run,
     * and <tt>runNextThread()</tt> is called, it will run the idle thread. The
     * idle thread must never block, and it will only be allowed to run when
     * all other threads are blocked.
     *
     * <p>
     * Note that <tt>ready()</tt> never adds the idle thread to the ready set.
     */
    private static void createIdleThread() {
        Lib.assertTrue(idleThread == null);
        
        idleThread = new KThread(new Runnable() {
            public void run() { while (true) yield(); }
        });
        idleThread.setName("idle");

        Machine.autoGrader().setIdleThread(idleThread);
        
        idleThread.fork();
    }
    
    /**
     * Determine the next thread to run, then dispatch the CPU to the thread
     * using <tt>run()</tt>.
     */
    private static void runNextThread() {
        KThread nextThread = readyQueue.nextThread();
        if (nextThread == null)
            nextThread = idleThread;

        nextThread.run();
    }

    /**
     * Dispatch the CPU to this thread. Save the state of the current thread,
     * switch to the new thread by calling <tt>TCB.contextSwitch()</tt>, and
     * load the state of the new thread. The new thread becomes the current
     * thread.
     *
     * <p>
     * If the new thread and the old thread are the same, this method must
     * still call <tt>saveState()</tt>, <tt>contextSwitch()</tt>, and
     * <tt>restoreState()</tt>.
     *
     * <p>
     * The state of the previously running thread must already have been
     * changed from running to blocked or ready (depending on whether the
     * thread is sleeping or yielding).
     *
     * @param	finishing	<tt>true</tt> if the current thread is
     *				finished, and should be destroyed by the new
     *				thread.
     */
    private void run() {
        Lib.assertTrue(Machine.interrupt().disabled());

        Machine.yield();

        currentThread.saveState();

        Lib.debug(dbgThread, "Switching from: " + currentThread.toString()
            + " to: " + toString());

        currentThread = this;

        tcb.contextSwitch();

        currentThread.restoreState();
    }

    /**
     * Prepare this thread to be run. Set <tt>status</tt> to
     * <tt>statusRunning</tt> and check <tt>toBeDestroyed</tt>.
     */
    protected void restoreState() {
        Lib.debug(dbgThread, "Running thread: " + currentThread.toString());
        
        Lib.assertTrue(Machine.interrupt().disabled());
        Lib.assertTrue(this == currentThread);
        Lib.assertTrue(tcb == TCB.currentTCB());

        Machine.autoGrader().runningThread(this);
        
        status = statusRunning;

        if (toBeDestroyed != null) {
            toBeDestroyed.tcb.destroy();
            toBeDestroyed.tcb = null;
            toBeDestroyed = null;
        }
    }

    /**
     * Prepare this thread to give up the processor. Kernel threads do not
     * need to do anything here.
     */
    protected void saveState() {
        Lib.assertTrue(Machine.interrupt().disabled());
        Lib.assertTrue(this == currentThread);
        }

        private static class PingTest implements Runnable {
        PingTest(int which) {
            this.which = which;
        }
        
        public void run() {
            for (int i=0; i<5; i++) {
            System.out.println("*** thread " + which + " looped "
                    + i + " times");
            currentThread.yield();
            }
        }

        private int which;
    }

    /**
     * Tests whether this module is working.
     */
    public static void selfTest() {
        //default self test code
        Lib.debug(dbgThread, "Enter KThread.selfTest");

        new KThread(new PingTest(1)).setName("forked thread").fork();
        new PingTest(0).run();

        // custom test code
        System.out.println("------------ KThread Self Tests -------------\n");
      
        selfJoinTest();
        joinFinishedThreadTest();
        nestedJoinTest();
        multipleThreadJoins();

        System.out.println("\n-------- All KThread Tests Completed --------\n");

    }

    /**
     * Test if the thread is not able to join itself
     */
    public static void selfJoinTest() {
        // creates thread to attempt to join itself
        KThread joinee = new KThread();
        joinee.setName("Joinee");
        System.out.println("Self Join Test: Started");

        // set the threads target to itself so on fork it will attempt to join itself and print a message if it is unable to join itself
        joinee.setTarget(new Runnable() {
            public void run() {
                String result = "unsuccesfully";
                try {
                    System.out.println("Self Join Test: Joining self");
                    joinee.join();
                } catch (Error e) {
                   result =  "succcesfully";
                }
                System.out.println("Self Join Test: Completed " + result);
            }
        });
        // kick off the thread and join it to watch what happens
        joinee.fork();
        joinee.join();
    }

    /**
     * Tests if a thread is able to join another thread that has already been completed 
     */
    public static void joinFinishedThreadTest(){
        // create 2 threads one that will join the other, and one that will finish before being joined 
        System.out.println("Finished Thread Join Test: Starting");
        KThread finished = new KThread();
        finished.setName("Finished Thread");
        KThread joiner = new KThread();
        joiner.setName("Joiner Thread");

        // Run the finished thread 
        finished.setTarget(new Runnable() {
            public void run() {
                System.out.println("Finished Thread Join Test: Finished thread done running");
            }
        });

        // join the finished thread from the joiner thread 
        joiner.setTarget(new Runnable() {
            public void run() {
                System.out.println("Finished Thread Join Test: Preparing to join finished thread");
                finished.join();
            }
        });

        // start the threads and join the joiner to see the results 
        finished.fork();
        joiner.fork();
        joiner.join();

        System.out.println("Finished Thread Join Test: Completed succesfully");
    }

    // test if thread is able to join itself through another thread
    public static void nestedJoinTest() {
        System.out.println("Nested Join Test: Starting");

        //create threads to join through secondary thread
        KThread thread1 = new KThread();
        thread1.setName("Joinee");
        KThread thread2 = new KThread();
        thread2.setName("Joiner");

        // set the first thread to start the second thread and join the second thread
        thread1.setTarget(new Runnable() {
            public void run() {
                System.out.println("Nested Join Test: Join thread 2 from thread 1");
                thread2.fork();
                thread2.join();
            }
        });
        // set the second thread to join the first thread, no need for fork as it is already running
        thread2.setTarget(new Runnable() {
            public void run() {
                System.out.println("Nested Join Test: Join thread 1 from thread 2");
                thread1.join();
            }
        });

        //start nested thread join
        thread1.fork();
        thread1.join();
        System.out.println("Nested Join Test: Completed successfully");
    }

    /**
     * Create multiple threads that will be joined and multiple threads to join
     * this will act as a performance test to see how it is able to handle multiple
     * threads being joined simitaneously 
     */
    public static void multipleThreadJoins() {
        System.out.println("Multiple Threads Join Test: Started");

        // create 5 threads that will join the other threads 
        KThread thread1 = new KThread();
        KThread thread2 = new KThread();
        KThread thread3 = new KThread();
        KThread thread4 = new KThread();
        KThread thread5 = new KThread();
        thread1.setName("Thread 1");
        thread2.setName("Thread 2");
        thread3.setName("Thread 3");
        thread4.setName("Thread 4");
        thread5.setName("Thread 5");

        // create 5 threads that will be joined 
        KThread joinThread1 = new KThread();
        KThread joinThread2 = new KThread();
        KThread joinThread3 = new KThread();
        KThread joinThread4 = new KThread();
        KThread joinThread5 = new KThread();
        joinThread1.setName("Join Thread 1");
        joinThread2.setName("Join Thread 2");
        joinThread3.setName("Join Thread 3");
        joinThread4.setName("Join Thread 4");
        joinThread5.setName("Join Thread 5");

       // set the 5 threads to join their respective join thread.
        thread1.setTarget(new Runnable(){
            public void run() {
                System.out.println("Multiple Threads Join Test: " + thread1.getName() + " joining " + joinThread1.getName());
                joinThread1.fork();
                joinThread1.join();
            }
        });
        thread2.setTarget(new Runnable(){
            public void run() {
                System.out.println("Multiple Threads Join Test: " + thread2.getName() + " joining " + joinThread2.getName());
                joinThread2.fork();
                joinThread2.join();
            }
        });
        thread3.setTarget(new Runnable(){
            public void run() {
                System.out.println("Multiple Threads Join Test: " + thread3.getName() + " joining " + joinThread3.getName());
                joinThread3.fork();
                joinThread3.join();
            }
        });
        thread4.setTarget(new Runnable(){
            public void run() {
                System.out.println("Multiple Threads Join Test: " + thread4.getName() + " joining " + joinThread4.getName());
                joinThread4.fork();
                joinThread4.join();
            }
        });
        thread5.setTarget(new Runnable(){
            public void run() {
                System.out.println("Multiple Threads Join Test: " + thread5.getName() + " joining " + joinThread5.getName());
                joinThread5.fork();
                joinThread5.join();
            }
        });

        // set the 5 join threads target to print that they are running
        joinThread1.setTarget(new Runnable(){
            public void run() {
                System.out.println("Multiple Threads Join Test: " + joinThread1.getName() + " running");
            }
        });
        joinThread2.setTarget(new Runnable(){
            public void run() {
                System.out.println("Multiple Threads Join Test: " + joinThread2.getName() + " running");
            }
        });
        joinThread3.setTarget(new Runnable(){
            public void run() {
                System.out.println("Multiple Threads Join Test: " + joinThread3.getName() + " running");
            }
        });
        joinThread4.setTarget(new Runnable(){
            public void run() {
                System.out.println("Multiple Threads Join Test: " + joinThread4.getName() + " running");
            }
        });
        joinThread5.setTarget(new Runnable(){
            public void run() {
                System.out.println("Multiple Threads Join Test: " + joinThread5.getName() + " running");
            }
        });

        // start and join the 5 threads that are joining the other threads
        thread1.fork();
        thread1.join();
        thread2.fork();
        thread2.join();
        thread3.fork();
        thread3.join();
        thread4.fork();
        thread4.join();
        thread5.fork();
        thread5.join();
        System.out.println("Multiple Threads Join Test: Completed successfully");
    }

    
    private static final char dbgThread = 't';

    /**
     * Additional state used by schedulers.
     *
     * @see	nachos.threads.PriorityScheduler.ThreadState
     */
    public Object schedulingState = null;

    private static final int statusNew = 0;
    private static final int statusReady = 1;
    private static final int statusRunning = 2;
    private static final int statusBlocked = 3;
    private static final int statusFinished = 4;

    /**
     * The status of this thread. A thread can either be new (not yet forked),
     * ready (on the ready queue but not running), running, or blocked (not
     * on the ready queue and not running).
     */
    private int status = statusNew;
    private String name = "(unnamed thread)";
    private Runnable target;
    private TCB tcb;
    private static Lock lock = new Lock();

    /**
     * Unique identifer for this thread. Used to deterministically compare
     * threads.
     */
    private int id = numCreated++;
    /** Number of times the KThread constructor was called. */
    private static int numCreated = 0;

    private ThreadQueue waitQueue = null;
    private ArrayList<Integer> usedID = new ArrayList<Integer>();

    private boolean joined = false;

    private static ThreadQueue readyQueue = null;
    private static KThread currentThread = null;
    private static KThread toBeDestroyed = null;
    private static KThread idleThread = null;
}
