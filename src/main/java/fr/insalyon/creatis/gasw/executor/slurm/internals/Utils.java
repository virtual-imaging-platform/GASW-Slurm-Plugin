package fr.insalyon.creatis.gasw.executor.slurm.internals;

public class Utils {

    public static void sleepNException(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {}
    }
}
