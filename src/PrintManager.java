import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class PrintManager {
    private Queue<Little> littlePrintingQueue;
    private Queue<Big> bigPrintingQueue;
    private boolean waitForBig = false;
    ReentrantLock lock;
    Condition bigBossAllow, threeConsecutiveBig, toPrintingStage, bigToPrintingStage;
    int consecutive = 0;

    private final int nrPrinters = 30;
    private boolean[] printerUsed;
    private int printersInUse = 0;


    public PrintManager() {
        printerUsed = new boolean[nrPrinters];
        littlePrintingQueue = new LinkedList<>();
        bigPrintingQueue = new LinkedList<>();
        lock = new ReentrantLock();
        bigBossAllow = lock.newCondition();
        toPrintingStage = lock.newCondition();
        bigToPrintingStage = lock.newCondition();
        threeConsecutiveBig = lock.newCondition();
    }

    /**
     * add a little object to a print queue and do printing based on the conditions
     * @param little
     * @throws InterruptedException
     */
    public void littlePrint(Little little) throws InterruptedException {
        lock.lock();

        try {
            //After 3 consecutive big company, the printing queue is closed
            while (consecutive == 3) {
                System.out.println("3 big continuous, no more little adding");
                threeConsecutiveBig.await();
            }

            // Adding till 3 consecutive big events happened
            System.out.println("add little print queue");
            littlePrintingQueue.add(little);
            System.out.println(littlePrintingQueue);

            // wait for big boss
            while (waitForBig) {
                System.out.println("wait for big");
                bigBossAllow.await();
            }

            // do print
            littleDoPrint();

            System.out.println("print small");
            littlePrintingQueue.poll();

            // start new queue
            if (littlePrintingQueue.isEmpty()) {
                consecutive = 0;
                System.out.println("start new queue");
                threeConsecutiveBig.signalAll();
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * add a big object to a print queue and do printing based on the conditions
     * @param big
     * @throws InterruptedException
     */
    public void bigPrint(Big big) throws InterruptedException {
        lock.lock();

        try {
            //After 3 consecutive big company, the printing queue is closed
            while (consecutive == 3) {
                System.out.println("3 big, little print time");
                waitForBig = false;
                if (littlePrintingQueue.isEmpty()           // no little and big in the printing queue
                        && bigPrintingQueue.isEmpty()){     // => reset to a new queue
                    consecutive = 0;
                    System.out.println("new queue");
                    threeConsecutiveBig.signalAll();
                }

                // little printing queue is not empty => let them finish
                bigBossAllow.signalAll();
                threeConsecutiveBig.await();

            }

            // big boss are using printer => little should wait
            waitForBig = true;
            System.out.println("add big print queue");
            bigPrintingQueue.add(big);
            System.out.println(bigPrintingQueue);

            // increase number of big consecutive tasks
            consecutive++;

            //Printing
            bigDoPrint();

            System.out.println("print big");
            bigPrintingQueue.poll();
        } finally {
            lock.unlock();
        }
    }

    private void littleDoPrint() throws InterruptedException {
        int nrPrinter = getFreePrinter();
        try {
            Thread.currentThread().sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        releasePrinter(nrPrinter);
    }

    private void bigDoPrint() throws InterruptedException{
        getAllPrinter();
        try {
            Thread.currentThread().sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        releaseAllPrinter();
    }

    private int assignFreePrinter() {
        for (int i = 0; i < nrPrinters; i++) {
            if (!printerUsed[i]) {
                printerUsed[i] = true;
                printersInUse++;
                return i;
            }
        }
        return -1;
    }

    /**
     * Get 1 free printer in the printers list
     */
    public int getFreePrinter() throws InterruptedException {
        lock.lock();
        try {
            while (printersInUse == nrPrinters) {
                toPrintingStage.await();
            }
            return assignFreePrinter();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Release 1 used printer
     * @param printerNr id of released printer
     * @throws InterruptedException
     */
    public void releasePrinter(int printerNr) throws InterruptedException {
        lock.lock();
        try {
            assert printerNr > 0 && printerNr < nrPrinters : "Invalid printer";
            assert printerUsed[printerNr] : "Printer is not in use";
            printerUsed[printerNr] = false;
            printersInUse--;
            Thread.currentThread().sleep(1000);
            toPrintingStage.signal();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Get all free printers in the printers list
     */
    public void getAllPrinter() throws InterruptedException {
        lock.lock();
        try {
            while (printersInUse != 0) {
                bigToPrintingStage.await();
            }
            System.out.println("Take all the resources");
            Arrays.fill(printerUsed, true);
            printersInUse = 30;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Release all free printers in the printers list
     */
    public void releaseAllPrinter() throws InterruptedException {
        lock.lock();
        try {
            System.out.println("Release all printers");
            Arrays.fill(printerUsed, false);
            printersInUse = 0;
            Thread.currentThread().sleep(1000);
            bigToPrintingStage.signal();
        } finally {
            lock.unlock();
        }
    }
}
