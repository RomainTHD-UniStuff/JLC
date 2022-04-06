package fr.rthd.jlc;

/**
 * Timer, used for debug purpose
 * @author RomainTHD
 */
public class Timer {
    private long start;
    private long end;

    public Timer start() {
        start = System.nanoTime();
        return this;
    }

    public long stop() {
        end = System.nanoTime();
        return getTime();
    }

    public long getTime() {
        return (end - start) / 1_000;
    }

    public long clock() {
        return (System.nanoTime() - start) / 1_000;
    }

    public long reset() {
        long time = clock();
        start();
        return time;
    }
}
