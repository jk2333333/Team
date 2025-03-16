package managers;

public class GeneralManager {

    /**
     * Introduces a delay to allow UI commands to execute in the correct order.
     * Prevents UI desynchronization.
     *
     * @param ms The sleep duration in milliseconds
     */
    public static void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            System.out.println("Thread interrupted.");
        }
    }
}
