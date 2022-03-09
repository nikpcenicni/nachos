package nachos.threads;

import java.util.concurrent.locks.Lock;

import javax.xml.bind.Marshaller.Listener;

import nachos.machine.*;

/**
 * A <i>communicator</i> allows threads to synchronously exchange 32-bit
 * messages. Multiple threads can be waiting to <i>speak</i>,
 * and multiple threads can be waiting to <i>listen</i>. But there should never
 * be a time when both a speaker and a listener are waiting, because the two
 * threads can be paired off at this point.
 */
public class Communicator {
    /**
     * Allocate a new communicator.
     */
    public Communicator() {
        handShake = false;
        waitToListenQueue = 0;
        lock = new nachos.threads.Lock();
        waitForHandShake = new Condition2(lock);
        waitToSpeak = new Condition2(lock);
        waitToListen = new Condition2(lock);
    }

    /**
     * Wait for a thread to listen through this communicator, and then transfer
     * <i>word</i> to the listener.
     *
     * <p>
     * Does not return until this thread is paired up with a listening thread.
     * Exactly one listener should receive <i>word</i>.
     *
     * @param	word	the integer to transfer.
     */
    public void speak(int word) {
        lock.acquire();
        while(handShake){
            waitToSpeak.sleep();
        }

        handShake = true;
        this.message = word;
        while (waitToListenQueue == 0){
            waitForHandShake.sleep();
        }

        waitToListen.wake();
        waitForHandShake.sleep();
        handShake = false;
        waitToSpeak.wake();
        lock.release();
    }

    /**
     * Wait for a thread to speak through this communicator, and then return
     * the <i>word</i> that thread passed to <tt>speak()</tt>.
     *
     * @return	the integer transferred.
     */    
    public int listen() {
        lock.acquire();
        waitToListenQueue++;

        if (waitToListenQueue == 1 && handShake)
            waitForHandShake.wake();
        
        waitToListen.sleep();
        waitForHandShake.wake();
        waitToListenQueue--;
        int word = this.message;
        lock.release();
        return word;
    }

    /**
     * Tests whether this module is working.
     */
    public static void selfTest() {
        System.out.println("------------ Communicator Self Tests -------------\n");
    
        speakerWaitTest();
        listenerWaitTest();
        multipleSpeakersWaitTest();
        multipleListenersWaitTest();

        System.out.println("\n-------- All Communicator Tests Completed --------"); 
    }
    
    /**
     * Tests if a speaker will wait for the listener to recieve the message
     */
    public static void speakerWaitTest(){
        System.out.println("Speaker Wait Test: Starting");
        // create a new communicator object for passing the message
        Communicator speaker = new Communicator();

        // create a new thread and have it send the message 101 
        KThread thread1 = new KThread();
        thread1.setName("Thread 1");
        thread1.setTarget(new Runnable(){
            public void run() {
                speaker.speak(101);
            }
        });

        // create a new thread as the listener and have it listen to the message from thread 1
        KThread thread2 = new KThread();
        thread2.setName("Thread 2");
        thread2.setTarget(new Runnable() {
            public void run() {
                System.out.println("Speaker Wait Test: " + thread2.getName() + " listening to " + thread1.getName() + " and heard " + speaker.listen());
            }
        });

        // fork and join the threads
        thread1.fork();
        thread2.fork();
        thread1.join();
        System.out.println("Speaker Wait Test: Completed successfully");
    }

    /**
     * Test to see if the listener will wait for the speaker to send a message
     */
    public static void listenerWaitTest(){
        System.out.println("Listener Wait Test: Starting");
        
        // create a new communicator object called speaker
        Communicator speaker = new Communicator();


        // create 2 new threads for speaking and listening.
        KThread thread1 = new KThread();
        thread1.setName("Thread 1");

        KThread thread2 = new KThread();
        thread2.setName("Thread 2");

        // set the first thread to listen for the speaker on run
        thread1.setTarget(new Runnable(){
            public void run() {
                System.out.println("Speaker Wait Test: " + thread1.getName() + " listening to " + thread2.getName() + " and heard " + speaker.listen());
            }
        });

        // set the second thread to speak 1000 on run
        thread2.setTarget(new Runnable() {
            public void run() {
                speaker.speak(1000);
            }
        });

        // fork and join the threads to start them
        thread1.fork();
        thread2.fork();
        thread2.join();
        System.out.println("Listener Wait Test: Completed successfully");
    }

    /**
     * Test multiple speakers are able to wait for multiple listeners 
     */
    public static void multipleSpeakersWaitTest() {
        System.out.println("Multiple Speakers Wait Test: Started");
        // create a communicator object to pass the messages
        Communicator speaker = new Communicator();

        // create 4 threads for speaking and 4 threads for listening 
        KThread thread1 = new KThread();
        thread1.setName("Thread 1");
        KThread thread2 = new KThread();
        thread2.setName("Thread 2");
        KThread thread3 = new KThread();
        thread3.setName("Thread 3");
        KThread thread4 = new KThread();
        thread4.setName("Thread 4");
        KThread thread5 = new KThread();
        thread5.setName("Thread 5");
        KThread thread6 = new KThread();
        thread6.setName("Thread 6");
        KThread thread7 = new KThread();
        thread7.setName("Thread 7");
        KThread thread8 = new KThread();
        thread8.setName("Thread 8");


        // set the first 4 threads the speak binary numbers 1 - 4
        thread1.setTarget(new Runnable(){
            public void run() {
                speaker.speak(1);
            }
        });

        thread2.setTarget(new Runnable(){
            public void run() {
                speaker.speak(10);
            }
        });

        thread3.setTarget(new Runnable(){
            public void run() {
                speaker.speak(11);
            }
        });

        thread4.setTarget(new Runnable(){
            public void run() {
                speaker.speak(100);
            }
        });

        // set the last 4 threads to listen to the message from the respective speaker threads
        thread5.setTarget(new Runnable(){
            public void run() {
                System.out.println("Multiple Speakers Wait Test: " + thread5.getName() + " listening to " + thread1.getName() + " and heard " + speaker.listen());
            }
        });
        thread6.setTarget(new Runnable(){
            public void run() {
                System.out.println("Multiple Speakers Wait Test: " + thread6.getName() + " listening to " + thread2.getName() + " and heard " + speaker.listen());
            }
        });
        thread7.setTarget(new Runnable(){
            public void run() {
                System.out.println("Multiple Speakers Wait Test: " + thread7.getName() + " listening to " + thread3.getName() + " and heard " + speaker.listen());
            }
        });
        thread8.setTarget(new Runnable(){
            public void run() {
                System.out.println("Multiple Speakers Wait Test: " + thread8.getName() + " listening to " + thread4.getName() + " and heard " + speaker.listen());
            }
        });

        // start all 8 threads and join the first 4 to watch progress
        thread1.fork();
        thread2.fork();
        thread3.fork();
        thread4.fork();
        thread5.fork();
        thread6.fork();
        thread7.fork();
        thread8.fork();

        thread1.join();
        thread2.join();
        thread3.join();
        thread4.join();

        System.out.println("Multiple Speakers Wait Test: Completed successfully");
    }

    /**
     * Test if multiple listeners will wait for multiple speakers 
     */
    public static void multipleListenersWaitTest() {
        System.out.println("Multiple Listeners Wait Test: Started");
        // create a communicator object named speaker
        Communicator speaker = new Communicator();

        // create 4 threads for speaking and 4 threads for listening 
        KThread thread1 = new KThread();
        thread1.setName("Thread 1");
        KThread thread2 = new KThread();
        thread2.setName("Thread 2");
        KThread thread3 = new KThread();
        thread3.setName("Thread 3");
        KThread thread4 = new KThread();
        thread4.setName("Thread 4");
        KThread thread5 = new KThread();
        thread5.setName("Thread 5");
        KThread thread6 = new KThread();
        thread6.setName("Thread 6");
        KThread thread7 = new KThread();
        thread7.setName("Thread 7");
        KThread thread8 = new KThread();
        thread8.setName("Thread 8");

        // set the first 4 threads to listen to the speaker object
        thread1.setTarget(new Runnable(){
            public void run() {
                System.out.println("Multiple Listeners Wait Test: " + thread1.getName() + " listening to " + thread5.getName() + " and heard " + speaker.listen());
            }
        });
        thread2.setTarget(new Runnable(){
            public void run() {
                System.out.println("Multiple Listeners Wait Test: " + thread2.getName() + " listening to " + thread6.getName() + " and heard " + speaker.listen());
            }
        });
        thread3.setTarget(new Runnable(){
            public void run() {
                System.out.println("Multiple Listeners Wait Test: " + thread3.getName() + " listening to " + thread7.getName() + " and heard " + speaker.listen());
            }
        });
        thread4.setTarget(new Runnable(){
            public void run() {
                System.out.println("Multiple Listeners Wait Test: " + thread4.getName() + " listening to " + thread8.getName() + " and heard " + speaker.listen());
            }
        });

        // set the last 4 threads to speak the binary numbers 1 - 4
        thread5.setTarget(new Runnable(){
            public void run() {
                speaker.speak(1);
            }
        });

        thread6.setTarget(new Runnable(){
            public void run() {
                speaker.speak(10);
            }
        });

        thread7.setTarget(new Runnable(){
            public void run() {
                speaker.speak(11);
            }
        });

        thread8.setTarget(new Runnable(){
            public void run() {
                speaker.speak(100);
            }
        });

        // start all 8 threads 
        thread1.fork();
        thread2.fork();
        thread3.fork();
        thread4.fork();
        thread5.fork();
        thread6.fork();
        thread7.fork();
        thread8.fork();

        // join the first 4 threads
        thread1.join();
        thread2.join();
        thread3.join();
        thread4.join();

        System.out.println("Multiple Listeners Wait Test: Completed successfully");

    }

    private boolean handShake;
    private int waitToListenQueue;
    private int message;
    private nachos.threads.Lock lock;
    private Condition2 waitForHandShake;
    private Condition2 waitToSpeak;
    private Condition2 waitToListen;

}
