package nachos.threads;

import java.util.concurrent.locks.Lock;

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
        handShake = true;
        waitToListenQueue = 0;
        lock = new Lock();
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
        this.message = (word);
        while (waitToListen){
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
        int message = this.message;
        lock.release();
        return message;
    }

    private boolean handShake;
    private int waitToListenQueue;
    private int message;
    private Lock lock;
    private Condition2 waitForHandShake;
    private Condition2 waitToSpeak;
    private Condition2 waitToListen;

}
