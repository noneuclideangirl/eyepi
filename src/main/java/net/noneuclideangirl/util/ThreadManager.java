package net.noneuclideangirl.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadManager {
    private static final ExecutorService threadPool = Executors.newCachedThreadPool();

    public static void execute(Runnable comm) {
        threadPool.execute(comm);
    }
}
