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

	public static void selfTest(){    
		Test1();
		//Test2();
	}
	
	public static void Test1(){
		
		System.out.print("\nCommencing Condition2 Test 1: Sleep & Wake \n\n");
		Lock lock = new Lock();
	    Condition2 condition = new Condition2(lock); 

	    KThread threads[] = new KThread[10];
		for (int i=0; i<10; i++) {
	         threads[i] = new KThread(new test1Locks(lock, condition));
	         threads[i].setName("Thread-" + i).fork();
		}

	    KThread.yield();
	    
	    lock.acquire();

	    System.out.print("------------Testing wake------------\n");	
	    condition.wake();
	    System.out.print("------------Wake Successful------------\n");
	    System.out.print("------------Testing wakeAll------------\n");	
	    condition.wakeAll();
	    System.out.print("------------Wakeall Successful------------\n");
	    lock.release();

	    System.out.print("\n Terminating Condition2 Test 1 by releasing Locks. \n\n");	

	    threads[9].join();
	    

//		System.out.print("\nCommencing Condition2 Test 2: Empty Queue \n\n");
//		KThread threads[] = new KThread[10];
//	    lock.acquire();
//	    System.out.print("------------Testing wake on Empty------------\n");	
//	    condition.wake();
//	    System.out.print("------------Wake Successful------------\n");
//	    System.out.print("------------Testing wakeAll on Empty------------\n");	
//	    condition.wakeAll();
//	    System.out.print("------------Wakeall Successful------------\n");
//	    lock.release();
//	    System.out.print("\n Terminating Condition2 Test 1 by releasing Locks. \n\n");	
//
//		
//		

	}
	
	private static class test1Locks implements Runnable {
		test1Locks(Lock lock, Condition2 condition) {
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

	public static void Test2(){
		
		System.out.print("\nCommencing Condition2 Test 2: Empty Queue \n\n");
		Lock lock = new Lock();
	    Condition2 condition = new Condition2(lock); 
	    
	   
	}
	
	
	private Lock conditionLock; // lock to implement condition
	private LinkedList<KThread> waitQueue; //A number of threads sleeping on condition
}