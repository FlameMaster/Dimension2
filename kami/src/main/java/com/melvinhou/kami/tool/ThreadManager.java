package com.melvinhou.kami.tool;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy;
import java.util.concurrent.TimeUnit;

/**
 * 线程池管理器
 */
public class ThreadManager {

    /**
     * 线程池
     */
    private static ThreadPool mThreadPool;

    /**
     * 获取单例的线程池对象
     */
    public static ThreadPool getThreadPool() {
        if (mThreadPool == null) {
            synchronized (ThreadManager.class) {
                if (mThreadPool == null) {

                    // 获取处理器数量
                    int cpuNum = Runtime.getRuntime().availableProcessors();

                    // 根据cpu数量,计算出合理的线程并发数
                    int threadNum = cpuNum * 2 + 1;
                    // System.out.println("cpu num:" + cpuNum);
                    mThreadPool = new ThreadPool(threadNum, threadNum, 0L);
                }
            }
        }

        return mThreadPool;
    }

    /**
     * 线程池
     */
    public static class ThreadPool {

        /**
         * 线程池执行者
         */
        private ThreadPoolExecutor mExecutor;
        /**
         * 核心线程数
         */
        private int corePoolSize;
        /**
         * 最大线程数
         */
        private int maximumPoolSize;
        /**
         * 休息时间
         */
        private long keepAliveTime;

        private ThreadPool(int corePoolSize, int maximumPoolSize,
                           long keepAliveTime) {
            this.corePoolSize = corePoolSize;
            this.maximumPoolSize = maximumPoolSize;
            this.keepAliveTime = keepAliveTime;
        }

        /**
         * 线程池几个参数的理解: 比如去火车站买票, 有10个售票窗口, 但只有5个窗口对外开放. 那么对外开放的5个窗口称为核心线程数,
         * 而最大线程数是10个窗口.如果5个窗口都被占用, 那么后来的人就必须在后面排队, 但后来售票厅人越来越多, 已经人满为患,
         * 就类似于线程队列已满. 这时候火车站站长下令, 把剩下的5个窗口也打开, 也就是目前已经有10个窗口同时运行. 后来又来了一批人,
         * 10个窗口也处理不过来了, 而且售票厅人已经满了, 这时候站长就下令封锁入口,不允许其他人再进来, 这就是线程异常处理策略.
         * 而线程存活时间指的是, 允许售票员休息的最长时间, 以此限制售票员偷懒的行为.
         */
        public void execute(Runnable runnable) {

            // 执行内容为空则返回
            if (runnable == null) {
                return;
            }

            // 若执行者为空，则创建
            if (mExecutor == null) {

                // 重新建一个执行者
                mExecutor = new ThreadPoolExecutor(corePoolSize,// 核心线程数
                        maximumPoolSize, // 最大线程数
                        keepAliveTime, // 闲置线程存活时间
                        TimeUnit.MILLISECONDS,// 时间单位
                        new LinkedBlockingDeque<Runnable>(),// 线程队列
                        Executors.defaultThreadFactory(),// 线程工厂
                        new AbortPolicy()// 队列已满,而且当前线程数已经超过最大线程数时的异常处理策略
                );
            }
            cancel(runnable);
            // 执行
            mExecutor.execute(runnable);
        }

        /**
         * 从线程队列中移除对象
         */
        public void cancel(Runnable runnable) {
            if (mExecutor != null) {
                if (mExecutor.getQueue().contains(runnable)){
                    mExecutor.getQueue().remove(runnable);
                }
            }
        }
    }
}
