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
        if (hydrogenCount >= 2 && oxygenCount >= 1){
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
        if (hydrogenCount >= 2){
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
		System.out.println("React Water Test 1: Starting");
		/* Variable Initialization */
		ReactWater reactWaterTest    = new ReactWater(); // ReactWater object for testing
		KThread hydrogen0            = new KThread();    // 2 Hydrogen kthreads for testing
		hydrogen0.setName("Hydrogen 0");
		KThread hydrogen1            = new KThread();
		hydrogen1.setName("Hydrogen 1");
		KThread oxygen0              = new KThread();    // 1 Oxygen kthread for testing
		oxygen0.setName("Oxygen 0");
		
		/* Test Case 1: One Water Molecule: 2 H and 1 0 */
		hydrogen0.setTarget(new Runnable() {
			public void run() {
				System.out.println("React Water Test 1: " + hydrogen0.getName() + " made hydrogen");
				reactWaterTest.hReady();
			}
		});
		
		hydrogen1.setTarget(new Runnable() {
			public void run() {
				System.out.println("React Water Test 1: " + hydrogen1.getName() + " made hydrogen");
				reactWaterTest.hReady();
			}
		});
		
		oxygen0.setTarget(new Runnable() {
			public void run() {
				System.out.println("React Water Test 1: " + oxygen0.getName() + " made oxygen");
				reactWaterTest.oReady();
			}
		});
		
		hydrogen0.fork();
		hydrogen1.fork();
		oxygen0.fork();
		oxygen0.join();
		System.out.println("React Water Test 1: Completed");
	} // end of test1()
	
	/**
	 * test2()
	 * A method to test the ReactWater() class
	 */
	public static void test2() {
		System.out.println("React Water Test 2: Starting");
		/* Variable Initialization */
		ReactWater reactWaterTest = new ReactWater(); // ReactWater object for testing
		KThread hydrogen0         = new KThread();    // 2 Hydrogen kthreads for testing
		KThread hydrogen1         = new KThread();
		KThread oxygen0           = new KThread();    // 4 Oxygen kthreads for testing
		KThread oxygen1           = new KThread();
		KThread oxygen2           = new KThread();
		KThread oxygen3           = new KThread();
		
		/* Test case 2: One Water Molecule: 2 H and 4 O */
		hydrogen0.setTarget(new Runnable() {
			public void run() {
				System.out.println("React Water Test 2: " + hydrogen0.getName() + " made hydrogen");
				reactWaterTest.hReady();
			}
		});
		
		hydrogen1.setTarget(new Runnable() {
			public void run() {
				System.out.println("React Water Test 2: " + hydrogen1.getName() + " made hydrogen");
				reactWaterTest.hReady();
			}
		});
		
		oxygen0.setTarget(new Runnable() {
			public void run() {
				System.out.println("React Water Test 2: " + oxygen0.getName() + " made oxygen");
				reactWaterTest.oReady();
			}
		});
		
		oxygen1.setTarget(new Runnable() {
			public void run() {
				System.out.println("React Water Test 2: " + oxygen1.getName() + " made oxygen");
				reactWaterTest.oReady();
			}
		});
		
		oxygen2.setTarget(new Runnable() {
			public void run() {
				System.out.println("React Water Test 2: " + oxygen2.getName() + " made oxygen");
				reactWaterTest.oReady();
			}
		});
		
		oxygen3.setTarget(new Runnable() {
			public void run() {
				System.out.println("React Water Test 2: " + oxygen3.getName() + " made oxygen");
				reactWaterTest.oReady();
			}
		});
		
		hydrogen0.fork();
		hydrogen1.fork();
		oxygen0.fork();
		oxygen1.fork();
		oxygen2.fork();
		oxygen3.fork();
		oxygen0.join();

		System.out.println("React Water Test 2: Completed");
	} // end of test2()
	
	
	/**
	 * test3()
	 * A method to test the ReactWater() class
	 */
	public static void test3() {
		System.out.println("React Water Test 3: Starting");
		/* Variable Initialization */
		ReactWater reactWaterTest = new ReactWater(); // ReactWater object for testing
		KThread hydrogen0         = new KThread();    // 4 Hydrogen kthreads for testing
		hydrogen0.setName("Hydrogen 0");
		KThread hydrogen1         = new KThread();
		hydrogen1.setName("Hydrogen 1");
		KThread hydrogen2         = new KThread();
		hydrogen2.setName("Hydrogen 2");
		KThread hydrogen3         = new KThread();
		hydrogen3.setName("Hydrogen 3");
		KThread oxygen0           = new KThread();    // 1 Oxygen kthreads for testing
		oxygen0.setName("Hydrogen 0");
		
		/* Test case 3: One Water Molecule: 4 H and 1 O */
		hydrogen0.setTarget(new Runnable() {
			public void run() {
				System.out.println("React Water Test 1: " + hydrogen0.getName() + " made hydrogen");
				reactWaterTest.hReady();
			}
		});
		
		hydrogen1.setTarget(new Runnable() {
			public void run() {
				System.out.println("React Water Test 1: " + hydrogen0.getName() + " made hydrogen");
				reactWaterTest.hReady();
			}
		});
		
		hydrogen2.setTarget(new Runnable() {
			public void run() {
				System.out.println("React Water Test 1: " + hydrogen0.getName() + " made hydrogen");
				reactWaterTest.hReady();
			}
		});
		
		hydrogen3.setTarget(new Runnable() {
			public void run() {
				System.out.println("React Water Test 1: " + hydrogen0.getName() + " made hydrogen");
				reactWaterTest.hReady();
			}
		});
		
		oxygen0.setTarget(new Runnable() {
			public void run() {
				System.out.println("React Water Test 1: " + hydrogen0.getName() + " made hydrogen");
				reactWaterTest.oReady();
			}
		});
		
		hydrogen0.fork();
		hydrogen1.fork();
		hydrogen2.fork();
		hydrogen3.fork();
		oxygen0.fork();
		oxygen0.join();
		System.out.println("React Water Test 3: Complete");
	} // end of test3()
	
	
	/**
	 * test4()
	 * A method to test the ReactWater() class
	 */
	public static void test4() {
		System.out.println("React Water Test 4: Starting");
		/* Variable Initialization */
		ReactWater reactWaterTest = new ReactWater(); // ReactWater object for testing
		KThread hydrogen0         = new KThread();    // 7 Hydrogen kthreads for testing
		hydrogen0.setName("Hydrogen0 ");
		KThread hydrogen1         = new KThread();
		hydrogen1.setName("Hydrogen 1");
		KThread hydrogen2         = new KThread();
		hydrogen2.setName("Hydrogen 2");
		KThread hydrogen3         = new KThread();
		hydrogen3.setName("Hydrogen 3");
		KThread hydrogen4         = new KThread();
		hydrogen4.setName("Hydrogen 4");
		KThread hydrogen5         = new KThread();
		hydrogen5.setName("Hydrogen 5");
		KThread hydrogen6         = new KThread();
		hydrogen6.setName("Hydrogen 6");
		KThread oxygen0           = new KThread();    // 4 Oxygen kthreads for testing
		oxygen0.setName("Oxygen 0");
		KThread oxygen1           = new KThread();
		oxygen1.setName("Oxygen 1");
		KThread oxygen2           = new KThread();
		oxygen2.setName("Oxygen 2");
		KThread oxygen3           = new KThread();
		oxygen3.setName("Oxygen 3");
		
		/* Test case 4: Three Water Molecules: 7 H and 4 O */
		hydrogen0.setTarget(new Runnable() {
			public void run() {
				System.out.println("React Water Test 1: " + hydrogen0.getName() + " made hydrogen");
				reactWaterTest.hReady();
			}
		});
		
		hydrogen1.setTarget(new Runnable() {
			public void run() {
				System.out.println("React Water Test 1: " + hydrogen0.getName() + " made hydrogen");
				reactWaterTest.hReady();
			}
		});
		
		hydrogen2.setTarget(new Runnable() {
			public void run() {
				System.out.println("React Water Test 1: " + hydrogen0.getName() + " made hydrogen");
				reactWaterTest.hReady();
			}
		});
		
		hydrogen3.setTarget(new Runnable() {
			public void run() {
				System.out.println("React Water Test 1: " + hydrogen0.getName() + " made hydrogen");
				reactWaterTest.hReady();
			}
		});
		
		hydrogen4.setTarget(new Runnable() {
			public void run() {
				System.out.println("React Water Test 1: " + hydrogen0.getName() + " made hydrogen");
				reactWaterTest.hReady();
			}
		});
		
		hydrogen5.setTarget(new Runnable() {
			public void run() {
				System.out.println("React Water Test 1: " + hydrogen0.getName() + " made hydrogen");
				reactWaterTest.hReady();
			}
		});
		
		hydrogen6.setTarget(new Runnable() {
			public void run() {
				reactWaterTest.hReady();
			}
		});
		
		oxygen0.setTarget(new Runnable() {
			public void run() {
				reactWaterTest.oReady();
			}
		});
		
		oxygen1.setTarget(new Runnable() {
			public void run() {
				reactWaterTest.oReady();
			}
		});
		
		oxygen2.setTarget(new Runnable() {
			public void run() {
				reactWaterTest.oReady();
			}
		});
		
		oxygen3.setTarget(new Runnable() {
			public void run() {
				reactWaterTest.oReady();
			}
		});
		
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
		
		System.out.println("React Water Test 4: Complted successfully");
	} // end of test4()

} // end of class ReactWater