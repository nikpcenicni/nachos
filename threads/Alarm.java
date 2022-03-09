package nachos.threads;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.Comparator;

import nachos.machine.*;

/**
 * Uses the hardware timer to provide preemption, and to allow threads to sleep
 * until a certain time.
 */
public class Alarm {
    /**
     * Allocate a new Alarm. Set the machine's timer interrupt handler to this
     * alarm's callback.
     *
     * <p><b>Note</b>: Nachos will not function correctly with more than one
     * alarm.
     */
    public Alarm() {
	Machine.timer().setInterruptHandler(new Runnable() {
		public void run() { timerInterrupt(); }
	    });
    }

    /**
     * The timer interrupt handler. This is called by the machine's timer
     * periodically (approximately every 500 clock ticks). Causes the current
     * thread to yield, forcing a context switch if there is another thread
     * that should be run.
     */
    public void timerInterrupt() {
    boolean intStatus = Machine.interrupt().disable();
    
    waitThread nextThread;
    
    while((nextThread = waitQueue.peek()) != null && nextThread.wakeTime() <= Machine.timer().getTime())
    	waitQueue.poll().thread().ready();
    
    Machine.interrupt().restore(intStatus);
	KThread.currentThread().yield();
    }

    /**
     * Put the current thread to sleep for at least <i>x</i> ticks,
     * waking it up in the timer interrupt handler. The thread must be
     * woken up (placed in the scheduler ready set) during the first timer
     * interrupt where
     *
     * <p><blockquote>
     * (current time) >= (WaitUntil called time)+(x)
     * </blockquote>
     *
     * @param	x	the minimum number of clock ticks to wait.
     *
     * @see	nachos.machine.Timer#getTime()
     */
    public void waitUntil(long x) {
    
	// for now, cheat just to get something working (busy waiting is bad)
	long wakeTime = Machine.timer().getTime() + x;
	
	boolean intStatus = Machine.interrupt().disable();
	
	waitQueue.add(new waitThread(wakeTime, KThread.currentThread()));
	
	KThread.sleep();
	
	Machine.interrupt().restore(intStatus);
	
	while (wakeTime > Machine.timer().getTime())
	    KThread.yield();
    }


	private class waitThread implements Comparable<waitThread> {
		
		private KThread thread = null;
		private long wakeTime = -1;
	
		public waitThread(long wakeTime, KThread thread){
			Lib.assertTrue(Machine.interrupt().disabled());
			this.wakeTime = wakeTime;
			this.thread = thread;
		}
		public int compareTo(waitThread waitThread){
			if(this.wakeTime < waitThread.wakeTime)
				return -1;
			else if(this.wakeTime > waitThread.wakeTime)
				return 1;
			else
				return thread.compareTo(waitThread.thread);
		}
		
		public long wakeTime(){
			return wakeTime;
		}
		
		public KThread thread(){
			return thread;
		}
	}

	public static void selfTest(){
		
		System.out.println();
		System.out.println("------------ Alarm Self Tests -------------\n");
		test1();
		test2();
		test3();
		test4();
		System.out.println();
		System.out.println("------------ All Alarm Tests Completed -------------\n");
	
	}
	
	private static void test1(){
		Alarm tester = new Alarm();
		KThread thread = new KThread();
		thread.setTarget(new Runnable() { 
			public void run() {  
				System.out.println("Test 1 - Large Delay (100): Starting");
				long time = Machine.timer().getTime();
				tester.waitUntil(100);	
				long waitTime = Machine.timer().getTime() - time;
				System.out.println("Test 1: Complete. Wait Time: " + waitTime);
				System.out.println();	
			}
		});
		thread.fork();
		thread.join();
	}
	
	private static void test2(){
		Alarm tester = new Alarm();
		KThread thread = new KThread();
		thread.setTarget(new Runnable() { 
			public void run() {  
				System.out.println("Test 2 - Small Delay (10): Starting");
				long time = Machine.timer().getTime();
				tester.waitUntil(10);	
				long waitTime = Machine.timer().getTime() - time;
				System.out.println("Test 2: Complete. Wait Time: " + waitTime);
				System.out.println();	
			}
		});
		thread.fork();
		thread.join();
	}

	private static void test3(){
		Alarm tester = new Alarm();
		KThread thread = new KThread();
		thread.setTarget(new Runnable() { 
			public void run() {  
				System.out.println("Test 3 - Very Large Delay (1000): Starting");
				long time = Machine.timer().getTime();
				tester.waitUntil(1000);	
				long waitTime = Machine.timer().getTime() - time;
				System.out.println("Test 3: Complete. Wait Time: " + waitTime);	
				System.out.println();
			}
		});
		thread.fork();
		thread.join();
	}
	
	private static void test4(){
		Alarm tester = new Alarm();
		KThread thread = new KThread();
		thread.setTarget(new Runnable() { 
			public void run() {  
				System.out.println("Test 4 - Very Small Delay (1): Starting");
				long time = Machine.timer().getTime();
				tester.waitUntil(1);	
				long waitTime = Machine.timer().getTime() - time;
				System.out.println("Test 4: Complete. Wait Time: " + waitTime);	
			}
		});
		thread.fork();
		thread.join();
	}
	
	java.util.PriorityQueue<waitThread> waitQueue = new java.util.PriorityQueue<waitThread>();
}
