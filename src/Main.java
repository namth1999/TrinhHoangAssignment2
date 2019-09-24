public class Main {
    public static void main(String[] args) {
        PrintManager printManager = new PrintManager();
        Thread1 t1 = new Thread1(printManager);
        Thread2 t2 = new Thread2(printManager);

        t1.start();
        t2.start();
    }

    /**
     * Inner thread uses for create little fish asynchronously with big fish
     */
    static class Thread1 extends Thread {
        PrintManager printManager;

        public Thread1(PrintManager printManager) {
            this.printManager = printManager;
        }

        @Override
        public void run() {
            for (int i=0;i<25;i++){
                Little l = new Little("l"+(i+1),printManager);
                l.start();
            }
        }
    }

    /**
     * Thread uses for create big fish asynchronously with little
     */
    static class Thread2 extends Thread {
        PrintManager printManager;

        public Thread2(PrintManager printManager) {
            this.printManager = printManager;
        }

        @Override
        public void run() {
            for (int i=0;i<15;i++){
                Big b = new Big("b"+(i+1),printManager);
                b.start();
            }
        }
    }
}
