import java.util.Random;

/**
 * Little company model
 */
public class Little extends Thread {
    private PrintManager printManager;
    private String name;
    private int nrPrintersUsed;

    public Little(String name, PrintManager printManager) {
        this.name = name;
        this.printManager = printManager;
        this.nrPrintersUsed = 1;
    }

    /**
     * They will try to print as soon as they are created
     */
    @Override
    public void run() {
        while (true){
            try {
                justLive();
                printManager.littlePrint(this);
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
        return "Little" + name;
    }
}

