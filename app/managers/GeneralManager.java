package managers;

public class GeneralManager {
    public static void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            System.out.println("Thread interrupted.");
        }
    }
}
