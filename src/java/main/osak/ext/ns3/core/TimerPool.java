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

import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * TimerPool: use to construct a global ScheduledThreadPoolExecutor
 * 
 * @author zhangrui
 * @since 1.0
 */
public final class TimerPool {
    private ScheduledThreadPoolExecutor timerPoolExecutor = null;

    private TimerPool() {
	timerPoolExecutor = new ScheduledThreadPoolExecutor(4);
	timerPoolExecutor.setRemoveOnCancelPolicy(true);
    }

    public ScheduledThreadPoolExecutor getExecutor() {
	return timerPoolExecutor;
    }

    public static class TimerPoolInstance {
	private static final TimerPool timerPool = new TimerPool();
    }

    public static ScheduledThreadPoolExecutor getInstance() {
	return TimerPoolInstance.timerPool.getExecutor();
    }
}
