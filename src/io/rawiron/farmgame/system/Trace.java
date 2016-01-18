package io.rawiron.farmgame.system;

import java.util.Stack;
import java.lang.System;


public class Trace {

    public static final boolean verbose = true;
    public static final byte verbose_level = 6;
    public static final boolean trace_timers = true;
    public Stack<Long> timer = new Stack<Long>();


    public Trace() {
    }

    public void trace(String msg) {
        System.out.println(msg);
    }

    public long getTimer() {
        return java.lang.System.nanoTime();
    }
}
