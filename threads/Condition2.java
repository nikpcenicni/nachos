package nachos.threads;

import nachos.machine.*;
import java.util.LinkedList;

/**
 * An implementation of condition variables that disables interrupt()s for
 * synchronization.
 *
 * <p>
 * You must implement this.
 *S
 * @see	nachos.threads.Condition
 */
public class Condition2 {
	/**
	 * Allocate a new condition variable.
	 *
	 * @param	conditionLock	the lock associated with this condition
	 *				variable. The current thread must hold this
	 *				lock whenever it uses <tt>sleep()</tt>,
	 *				<tt>wake()</tt>, or <tt>wakeAll()</tt>.
	 */

	public Condition2(Lock conditionLock) {
		this.conditionLock = conditionLock;

		// waitQueue = new LinkedList<Lock>();
		waitQueue = new LinkedList<KThread>();
	}

	/**
	 * Atomically release the associated lock and go to sleep on this condition
	 * variable until another thread wakes it using <tt>wake()</tt>. The
	 * current thread must hold the associated lock. The thread will
	 * automatically reacquire the lock before <tt>sleep()</tt> returns.
	 */
	public void sleep() {
		/*Lib.assertTrue(conditionLock.isHeldByCurrentThread());
		
		boolean status = Machine.interrupt().disable(); 
		
		conditionLock.release();
		waitQueue.add(KThread.currentThread());
		KThread.sleep();
		conditionLock.acquire();
		Machine.interrupt().restore(status);*/
		
		Lib.assertTrue(conditionLock.isHeldByCurrentThread());
		conditionLock.release();

		boolean intStatus = Machine.interrupt().disable(); 

		waitQueue.add(KThread.currentThread());  
		KThread.sleep(); 
		Machine.interrupt().restore(intStatus); 

		conditionLock.acquire();
	}

	/**
	 * Wake up at most one thread sleeping on this condition variable. The
	 * current thread must hold the associated lock.
	 */
	public void wake() {
		
		Lib.assertTrue(conditionLock.isHeldByCurrentThread());
		
		boolean status = Machine.interrupt().disable(); 
		
		if (!waitQueue.isEmpty())
		{
			(waitQueue.removeFirst()).ready();
		}	
		Machine.interrupt().restore(status);
	}

	/**
	 * Wake up all threads sleeping on this condition variable. The current
	 * thread must hold the associated lock.
	 */
	public void wakeAll() {
		
		Lib.assertTrue(conditionLock.isHeldByCurrentThread());
		while (!waitQueue.isEmpty()){
			wake();               
		}
	}

	
	/*
	 * Invoked by the ThreadedKernel class to ensure correct implementation
	 */
	public static void selfTest(){    
		Test1();
		Test2();
		new Test3();
		//Test4();
	}
	
	/*
	 * First test begins by testing locks as well as simple debugging of sleep,
	 * wake and wakeAll function using 10 threads. 
	 */
	public static void Test1(){
		
		System.out.print("\nCommencing Condition2 Test 1: Sleep & Wake \n\n");
		Lock lock = new Lock();
	    Condition2 condition = new Condition2(lock); 

	    KThread threads[] = new KThread[10];
		for (int i=0; i<10; i++) { //initialize 10 threads to be tested
	         threads[i] = new KThread(new test1Locks(lock, condition));//calls test1Locks to execute thread functionality
	         threads[i].setName("Thread-" + i).fork();
		}

	    KThread.yield(); // relinquish processor
	    
	    lock.acquire();

	    System.out.print("------------Testing wake------------\n");	
	    condition.wake();
	    System.out.print("------------Wake Successful------------\n");
	    System.out.print("------------Testing wakeAll------------\n");	
	    condition.wakeAll();
	    System.out.print("------------Wakeall Successful------------\n");
	    lock.release();

	    System.out.print("\n Terminating Condition2 Test 1 by releasing Locks. \n\n");	

	    threads[9].join(); // wait until threads terminate
	    System.out.print("\n Condition2 Test 1: Successful \n\n");

	}
	
	/*
	 * Implements threads for testing conditions and then terminating
	 */
	private static class test1Locks implements Runnable {
		test1Locks(Lock lock, Condition2 condition) { //initialize condition variable and lock
		    this.condition = condition;
	        this.lock = lock;
		}
		
		public void run() {
	        lock.acquire();
	        System.out.print("Acquired lock on: " + KThread.currentThread().getName() + "\n");	
	        condition.sleep();
	        System.out.print("Lock Acquired for Release on: " +KThread.currentThread().getName() + "\n");	
	        lock.release();
	        System.out.print("Released lock on: " +KThread.currentThread().getName() + "\n");	
		}

	    private Lock lock; 
	    private Condition2 condition; 
	}

	/*
	 * Uses a linked list to test performance and reliability of Condition2 testing.
	 * Context Switching is also tested via producer and consumer threads. 
	 */
	public static void Test2(){
		
		System.out.print("\nCommencing Condition2 Test 2: Context Switching \n\n");
		Lock lock = new Lock();
	    Condition2 cond = new Condition2(lock); 
	    
	    LinkedList<Integer> intList = new LinkedList<>();
	    
	    KThread consumer = new KThread( new Runnable () {
            public void run() {
                lock.acquire();
                while(intList.isEmpty()){
                    cond.sleep();
                }
                System.out.print("\nEnsuring list size = 50\n");	
                Lib.assertTrue(intList.size() == 50);
                System.out.print("\nList size = 50\n");	
                
                while(!intList.isEmpty()) {
                    KThread.yield();
                    System.out.println("Thread: " + intList.removeFirst() + " was Removed");
                }
                lock.release();
            }
        });

        KThread producer = new KThread( new Runnable () {
            public void run() {
                lock.acquire();
                for (int i = 0; i < 50; i++) {
                    intList.add(i);
                    System.out.println("Thread: " + i + " was Added");
                    KThread.yield();
                }
                cond.wake();
                lock.release();
            }
        });

        consumer.setName("Consumer");
        producer.setName("Producer");

        consumer.fork();
        producer.fork();
        
        //Wait until all processes finish execution
        
        consumer.join();
        System.out.print("\nTerminating Condition2 Test 2 \n\n");	
        producer.join();
        
        System.out.print("\nCondition2 Test 2: Successful \n\n");
    }
	
	/*
	 * Test conducts by executing threads in a procedural manner. In short, each thread
	 * will run alternatively (i.e. after one another) via condition implementation
	 */
	private static class Test3{
		
		private static Lock lock;
		private static Condition2 cond;
		
		/*
		 * Initialize 5 pings and 5 pong threads. 
		 */
		private static class pingTester implements Runnable{
			public void run(){
				lock.acquire();
				for (int i = 0; i < 10; i++) {
					System.out.println(KThread.currentThread().getName());
					cond.wake();   // signal
					cond.sleep();  // wait
				}
				lock.release();
			}
		}
		
		public Test3(){
			System.out.print("\nCommencing Condition2 Test 3: InterLocked Processes \n\n");
			lock = new Lock();
			cond = new Condition2(lock);

			KThread ping = new KThread(new pingTester());
			ping.setName("ping");
			KThread pong = new KThread(new pingTester());
			pong.setName("pong");

			ping.fork();
			pong.fork();
			/*
			 * In order for pon to execute, it must wait until ping is terminated 
			 * via join and vice versa. Otherwise, an indefinite block may occur.
			 */
			for (int i = 0; i < 50; i++) { KThread.currentThread().yield(); }
			System.out.print("\nCondition2 Test 3: Successful \n\n");
		}
	}
	
	private Lock conditionLock; // lock to implement condition
	private LinkedList<KThread> waitQueue; //A number of threads sleeping on condition


}
