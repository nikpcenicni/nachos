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

} // end of class ReactWater