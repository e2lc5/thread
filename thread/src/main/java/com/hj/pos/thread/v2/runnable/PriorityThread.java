package com.hj.pos.thread.v2.runnable;

import android.util.Log;

/**
 * 优先级线程
 *
 * @author fish
 */
public abstract class PriorityThread extends Thread {

    public PriorityThread() {
        super();
        setName(getThreadName());
    }

    public PriorityThread(int priority) {
        super();
        setName(getThreadName());
        setPriority(priority);
        Log.i(getClass().getSimpleName(), ("线程启动 " + this.toString()));
    }

    @Override
    public abstract void run();

    public abstract String getThreadName();

}
