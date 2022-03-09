package nachos.threads;

import nachos.machine.*;

public class ReactWater{

    // Counting Variables
    private static int hydrogenCount;
    private static int oxygenCount;
    
    // Lock and Condition Variables
    Condition2 hydrogenCondition;
    Condition2 oxygenCondition;
    Lock lock;

    /** 
     *   Constructor of ReactWater
     *   Default, initializes all data field variables
     **/
    public ReactWater() {

        lock         = new Lock();
        hydrogenCondition = new Condition2(lock);
        oxygenCondition   = new Condition2(lock);
        hydrogenCount = oxygenCount = 0;

    } // end of ReactWater()

    /** 
     *   When H element comes, if there already exist another H element 
     *   and an O element, then call the method of Makewater(). Or let 
     *   H element wait in line. 
     **/ 
    public void hReady() {

        lock.acquire();
        ++hydrogenCount; // add hydrogen count

        // Check for existing H and O elements
        // If 2 H and 1 O then call MakeWater()
        if (hydrogenCount == 2 && oxygenCount == 1){
            hydrogenCondition.wake();
            oxygenCondition.wake();
            Makewater();
            lock.release();
            return;
        }
        // Let H element wait in line
        else{
            hydrogenCondition.sleep();
        }
    } // end of hReady()
 
    /** 
     *   When O element comes, if there already exist another two H
     *   elements, then call the method of Makewater(). Or let O element
     *   wait in line. 
     **/ 
    public void oReady() {

        lock.acquire();
        ++oxygenCount; // add oxygen count

        // Check for existing H elements
        // If 2 H then call MakeWater()
        if (hydrogenCount == 2){
            hydrogenCondition.wake();
            oxygenCondition.wake();
            Makewater();
            lock.release();
            return;
        }
        // Let O element wait in line
        else{
            oxygenCondition.sleep();
        }
    } // end of oReady()
    
    /** 
     *   Print out the message of "water was made!".
     **/
    public void Makewater() {

        // Reset counters
        hydrogenCount -= 2;
        --oxygenCount;

        System.out.println("Water was made!");
        
    } // end of Makewater()


	/**
	 * selfTest()
	 * A method to call the ReactWater() test methods
	 */
	public static void selfTest() {
		System.out.println("------------ ReactWater Self Tests -------------");
		test1();
		test2();
		test3();
		test4();
		System.out.println("-------- All ReactWater Tests Completed --------"); 
	} // end of selfTest()
	
	
	/**
	 * test1()
	 * A method to test the ReactWater() class
	 */
	public static void test1() {
		
		/* Variable Initialization */
		ReactWater reactWaterTest    = new ReactWater(); // ReactWater object for testing
		KThread hydrogen0, hydrogen1 = new KThread();    // 2 Hydrogen kthreads for testing	
		KThread oxygen0              = new KThread();    // 1 Oxygen kthread for testing
		
		/* Test Case 1: One Water Molecule: 2 H and 1 0 */
		hydrogen0.setTarget(new Runnable() { public void run() { reactWaterTest.hReady(); } });
		
		hydrogen1.setTarget(new Runnable() { public void run() { reactWaterTest.hReady(); } });
		
		oxygen0.setTarget(new Runnable() { public void run() { reactWaterTest.oReady(); } });
		
		hydrogen0.fork();
		hydrogen1.fork();
		oxygen0.fork();
		oxygen0.join();
	} // end of test1()
	
	/**
	 * test2()
	 * A method to test the ReactWater() class
	 */
	public static void test2() {
		
		/* Variable Initialization */
		ReactWater reactWaterTest                  = new ReactWater(); // ReactWater object for testing
		KThread hydrogen0, hydrogen1               = new KThread();    // 2 Hydrogen kthreads for testing
		KThread oxygen0, oxygen1, oxygen2, oxygen3 = new KThread();    // 4 Oxygen kthreads for testing
		
		/* Test case 2: One Water Molecule: 2 H and 4 O */
		hydrogen0.setTarget(new Runnable() { public void run() { reactWaterTest.hReady(); } });
		
		hydrogen1.setTarget(new Runnable() { public void run() { reactWaterTest.hReady(); } });
		
		oxygen0.setTarget(new Runnable() { public void run() { reactWaterTest.oReady(); } });
		
		oxygen1.setTarget(new Runnable() { public void run() { reactWaterTest.oReady(); } });
		
		oxygen2.setTarget(new Runnable() { public void run() { reactWaterTest.oReady(); } });
		
		oxygen3.setTarget(new Runnable() { public void run() { reactWaterTest.oReady(); } });
		
		hydrogen0.fork();
		hydrogen1.fork();
		oxygen0.fork();
		oxygen1.fork();
		oxygen2.fork();
		oxygen3.fork();
		oxygen0.join();
	} // end of test2()
	
	
	/**
	 * test3()
	 * A method to test the ReactWater() class
	 */
	public static void test3() {
		
		/* Variable Initialization */
		ReactWater reactWaterTest                          = new ReactWater(); // ReactWater object for testing
		KThread hydrogen0, hydrogen1, hydrogen2, hydrogen3 = new KThread();    // 4 Hydrogen kthreads for testing
		KThread oxygen0                                    = new KThread();    // 1 Oxygen kthreads for testing
		
		/* Test case 3: One Water Molecule: 4 H and 1 O */
		hydrogen0.setTarget(new Runnable() { public void run() { reactWaterTest.hReady(); } });
		
		hydrogen1.setTarget(new Runnable() { public void run() { reactWaterTest.hReady(); } });
		
		hydrogen2.setTarget(new Runnable() { public void run() { reactWaterTest.hReady(); } });
		
		hydrogen3.setTarget(new Runnable() { public void run() { reactWaterTest.hReady(); } });
		
		oxygen0.setTarget(new Runnable() { public void run() { reactWaterTest.oReady(); } });
		
		hydrogen0.fork();
		hydrogen1.fork();
		hydrogen2.fork();
		hydrogen3.fork();
		oxygen0.fork();
		oxygen0.join();
	} // end of test3()
	
	
	/**
	 * test4()
	 * A method to test the ReactWater() class
	 */
	public static void test4() {
		
		/* Variable Initialization */
		ReactWater reactWaterTest                          = new ReactWater(); // ReactWater object for testing
		KThread hydrogen0, hydrogen1, hydrogen2, hydrogen3 = new KThread();    // 4/7 Hydrogen kthreads for testing
		KThread hydrogen4, hydrogen5, hydrogen6            = new KThread();    // 3/7 Hydrogen kthreads for testing
		KThread oxygen0, oxygen1, oxygen2, oxygen3         = new KThread();    // 4 Oxygen kthreads for testing
		
		/* Test case 4: Three Water Molecules: 7 H and 4 O */
		hydrogen0.setTarget(new Runnable() { public void run() { reactWaterTest.hReady(); } });
		
		hydrogen1.setTarget(new Runnable() { public void run() { reactWaterTest.hReady(); } });
		
		hydrogen2.setTarget(new Runnable() { public void run() { reactWaterTest.hReady(); } });
		
		hydrogen3.setTarget(new Runnable() { public void run() { reactWaterTest.hReady(); } });
		
		hydrogen4.setTarget(new Runnable() { public void run() { reactWaterTest.hReady(); } });
		
		hydrogen5.setTarget(new Runnable() { public void run() { reactWaterTest.hReady(); } });
		
		hydrogen6.setTarget(new Runnable() { public void run() { reactWaterTest.hReady(); } });
		
		oxygen0.setTarget(new Runnable() { public void run() { reactWaterTest.oReady(); } });
		
		oxygen1.setTarget(new Runnable() { public void run() { reactWaterTest.oReady(); } });
		
		oxygen2.setTarget(new Runnable() { public void run() { reactWaterTest.oReady(); } });
		
		oxygen3.setTarget(new Runnable() { public void run() { reactWaterTest.oReady(); } });
		
		hydrogen0.fork();
		hydrogen1.fork();
		hydrogen2.fork();
		hydrogen3.fork();
		hydrogen4.fork();
		hydrogen5.fork();
		hydrogen6.fork();
		oxygen0.fork();
		oxygen1.fork();
		oxygen2.fork();
		oxygen3.fork();
		oxygen0.join();
		oxygen1.join();
		oxygen2.join();
		oxygen3.join();
	} // end of test4()

} // end of class ReactWater