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

import java.util.concurrent.TimeUnit;

/**
 * TODO Time
 * 
 * @author zhangrui
 * @since   1.0
 */
public final class Time {
    private long value;
    private TimeUnit unit;

    public Time() {

    }

    public Time(long val, TimeUnit unit) {
	this.value = val;
	this.unit = unit;
    }

    public Time(long val) {
	this.value = val;
	this.unit = TimeUnit.MILLISECONDS;
    }

    public long getLong() {
	return this.value;
    }

    public TimeUnit getUnit() {
	return this.unit;
    }

    public long getMillSeconds() {
	return TimeUnit.MILLISECONDS.convert(value, unit);
    }

    @Override
    public String toString() {
	return value + " " + unit;
    }

    // @return the biggest value of Time
    public static Time Max() {
	return new Time(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    }

    public static Time Now() {
	return new Time(System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

    // equals to '+='
    public Time add(Time t) {
	if (this.unit == t.unit) {
	    this.value += t.value;
	    return this;
	}
	this.value += this.unit.convert(t.value, t.unit);
	return this;
    }

    // equals to '-='
    public Time sub(Time t) {
	if (this.unit == t.unit) {
	    this.value -= t.value;
	    return this;
	}
	this.value -= this.unit.convert(t.value, t.unit);
	return this;
    }

    // return t1+t2
    public static Time add(Time t1, Time t2) {
	if (t1.unit == t2.unit) {
	    return new Time(t1.value += t2.value, t1.unit);
	}
	long val1 = TimeUnit.MILLISECONDS.convert(t1.value, t1.unit);
	long val2 = TimeUnit.MILLISECONDS.convert(t2.value, t2.unit);
	return new Time(val1 + val2);
    }

    // return t1-t2
    public static Time sub(Time t1, Time t2) {
	if (t1.unit == t2.unit) {
	    return new Time(t1.value -= t2.value, t1.unit);
	}
	long val1 = TimeUnit.MILLISECONDS.convert(t1.value, t1.unit);
	long val2 = TimeUnit.MILLISECONDS.convert(t2.value, t2.unit);
	return new Time(val1 - val2);
    }

    public static Time multiply(int a, Time b) {
	return new Time(b.value * a, b.unit);
    }

    /*
     * @return the bigger one between t1 and t2
     */
    public static Time MAX(Time t1, Time t2) {
	if (t1.unit == t2.unit) {
	    return (t1.value > t2.value) ? t1 : t2;
	}
	long val1 = TimeUnit.MILLISECONDS.convert(t1.value, t1.unit);
	long val2 = TimeUnit.MILLISECONDS.convert(t2.value, t2.unit);
	return (val1 > val2) ? t1 : t2;
    }

    /*
     * @return the smaller one between t1 and t2
     */
    public static Time MIN(Time t1, Time t2) {
	if (t1.unit == t2.unit) {
	    return (t1.value < t2.value) ? t1 : t2;
	}
	long val1 = TimeUnit.MILLISECONDS.convert(t1.value, t1.unit);
	long val2 = TimeUnit.MILLISECONDS.convert(t2.value, t2.unit);
	return (val1 < val2) ? t1 : t2;
    }
}
