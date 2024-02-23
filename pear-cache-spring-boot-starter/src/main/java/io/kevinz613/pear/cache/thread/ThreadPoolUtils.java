package io.kevinz613.pear.cache.thread;

import java.util.concurrent.*;

/**
 * 线程池工具类
 *
 * @author kevinz613
 */
public class ThreadPoolUtils {

    private static ThreadPoolExecutor executor = new ThreadPoolExecutor(16, 16, 30, TimeUnit.SECONDS, new ArrayBlockingQueue<>(4096), new ThreadPoolExecutor.CallerRunsPolicy());

    /**
     * execute task in thread pool
     *
     * @param command 命令
     */
    public static void execute(Runnable command) {
        executor.execute(command);
    }

    /**
     * 提交
     *
     * @param task 任务
     */
    public static <T> Future<T> submit(Callable<T> task) {
        return executor.submit(task);
    }

    /**
     * 关闭
     */
    public static void shutdown() {
        if (executor != null) {
            executor.shutdown();
        }
    }
}
