/*
 * Copyright 2024 OSPLAB (Optical Signal Processing Lab Of UESTC)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package osak.ext.ns3.core;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * TODO Timer
 * 
 * @author zhangrui
 * @since   1.0
 */
public final class Timer {
    private static final ScheduledThreadPoolExecutor executor = TimerPool.getInstance();
    private Runnable task = null;
    private Time delay = new Time(0, TimeUnit.SECONDS);
    private ScheduledFuture<?> res = null;

    public Timer() {
    }

    public void SetFunction(Runnable func) {
	task = func;
    }

    public void SetDelay(Time delay) {
	this.delay = delay;
    }

    public void Cancel() {
	if (res.isCancelled()) {
	    return;
	} else {
	    res.cancel(true);
	}
    }

    public void Schedule() {
	res = executor.schedule(task, delay.getLong(), delay.getUnit());
    }

    public boolean IsExpired() {
	return GetDelayLeft().getLong() < 0;
    }

    public void Schedule(Time delay) {
	this.delay = delay;
	Schedule();
    }

    public void Schedule(Time delay, Runnable func) {
	this.task = func;
	Schedule(delay);
    }

    public static void Schedules(Time delay, Runnable func) {
	executor.schedule(func, delay.getLong(), delay.getUnit());
    }

    /**
     * @returns The currently-configured delay for the next Schedule.
     */
    public Time GetDelay() {
	return this.delay;
    }

    public boolean IsRunning() {
	if (res.isCancelled()) {
	    return false;
	}
	if (GetDelayLeft().getLong() <= 0L && !res.isDone()) {
	    return true;
	}
	return false;
    }

    /**
     * @returns The amount of time left until this timer expires.
     *
     *          This method returns zero if the timer is in EXPIRED state.
     */
    public Time GetDelayLeft() {
	return new Time(res.getDelay(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS);
    }

    public BlockingQueue<Runnable> getQueue() {
	return executor.getQueue();
    }


}
