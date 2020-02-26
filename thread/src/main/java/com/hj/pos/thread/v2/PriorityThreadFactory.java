package com.hj.pos.thread.v2;


import android.util.Log;

import com.hj.pos.thread.v2.executor.PriorityPoolExecutor;
import com.hj.pos.thread.v2.runnable.PriorityThread;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


/**
 * PriorityThread
 */
public class PriorityThreadFactory {
    private static final String TAG = "PriorityThreadFactory";
    private final PriorityPoolExecutor executor;        // 固定大小的线程池
    private final ExecutorService singleThreadExecutor;
    private final ScheduledExecutorService scheduledThreadPool;

    private Map<Long, ScheduledFuture> futureMap = new HashMap<>();// 用于缓存Scheduled事件

    private static PriorityThreadFactory factory = null;

    private PriorityThreadFactory() {
        //		NCPU = CPU的数量
        //		UCPU = 期望对CPU的使用率 0 ≤ UCPU ≤ 1
        //		W/C = 等待时间与计算时间的比率
        //		如果希望处理器达到理想的使用率，那么线程池的最优大小为：
        //		线程池大小=NCPU *UCPU(1+W/C)
        //		int ncpus = Runtime.getRuntime().availableProcessors();
        //		corePoolSize = (int) (ncpus * 0.5);

        int cpuCount = Runtime.getRuntime().availableProcessors();
        int corePoolSize = cpuCount * 2 + 1;
        int maximumPoolSize = cpuCount * 4 + 1;
        long keepAlive = 10L;
        executor = new PriorityPoolExecutor(corePoolSize, maximumPoolSize,
                keepAlive, TimeUnit.MILLISECONDS);

        singleThreadExecutor = Executors.newSingleThreadExecutor();

        scheduledThreadPool = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors() + 1);
    }

    /**
     * 单例模式,获取线程池
     *
     * @return 获取当前实例
     */
    public static PriorityThreadFactory getPool() {
        if (factory == null) {
            factory = new PriorityThreadFactory();
        }
        return factory;
    }

    public String getPoolInfo() {
        return "当前排队数: " + this.executor.getQueue().size() +
                " 当前活动数: " + this.executor.getActiveCount() +
                " 执行完成数: " + this.executor.getCompletedTaskCount() +
                " 总数: " + this.executor.getTaskCount() +
                " CorePoolSize: " + this.executor.getCorePoolSize() +
                " PoolSize: " + this.executor.getPoolSize() +
                " LargestPoolSize: " + this.executor.getLargestPoolSize() +
                " MaximumPoolSize: " + this.executor.getMaximumPoolSize();
    }

    /**
     * 固定大小的线程池执行线程
     *
     * @param command 新加线程
     */
    public void execute(PriorityThread command) {
        this.executor.execute(command);
    }

    /**
     * 定时线程池执行线程
     * 以固定的频率去执行任务，周期是指每次执行任务成功执行之间的间隔。
     *
     * @param command 新加线程
     * @param delay   等待时间
     * @param period  循环周期
     * @return 线程id
     */
    public long scheduleAtFixedRate(PriorityThread command, long delay, long period) {
        ScheduledFuture future = this.scheduledThreadPool.scheduleAtFixedRate(command, delay, period, TimeUnit.MILLISECONDS);
        long id = System.currentTimeMillis();
        this.futureMap.put(id, future);
        Log.i(TAG, "thread " + command.getThreadName() + " id " + id + " start");
        return id;
    }

    /**
     * 定时线程池执行线程,不循环
     *
     * @param command 新加线程
     * @param delay   等待时间
     * @return ScheduledFuture对象
     */
    public ScheduledFuture schedule(PriorityThread command, long delay) {
        return this.scheduledThreadPool.schedule(command, delay, TimeUnit.MILLISECONDS);
    }

    public void cancelSchedule(long schedule_id) {
        ScheduledFuture future = this.futureMap.get(schedule_id);
        if (future != null && !future.isCancelled()) {
            future.cancel(true);
            Log.i(TAG, "thread " + future.toString() + " id " + schedule_id + " canceled");
        }
        this.futureMap.remove(schedule_id);
    }

    public void cancelSchedule(long schedule_id, String threadName) {
        ScheduledFuture future = this.futureMap.get(schedule_id);
        if (future != null && !future.isCancelled()) {
            future.cancel(true);
            Log.i(TAG, "thread " + threadName + " id " + schedule_id + " canceled");
        }
        this.futureMap.remove(schedule_id);
    }


    /**
     * 单个线程线程池执行线程
     *
     * @param command 新加线程
     */
    public void singleExecute(PriorityThread command) {
        this.singleThreadExecutor.execute(command);
    }
}
