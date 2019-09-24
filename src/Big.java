import java.util.Random;

/**
 * Big company model
 */
public class Big extends Thread {
    private PrintManager printManager;
    private String name;
    private int nrPrintersUsed;

    public Big(String name, PrintManager printManager) {
        this.name = name;
        this.printManager = printManager;
        this.nrPrintersUsed = 30;
    }

    /**
     * Big company will wake up and try to print
     */
    @Override
    public void run() {
        while (true){
            try {
                justLive();
                printManager.bigPrint(this);
            } catch (InterruptedException e) {
            }
        }
    }

    private void justLive() {
        try {
            Thread.currentThread().sleep(500 +
                    ((new Random().nextInt(5)) * 200));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "Big" + name;
    }
}