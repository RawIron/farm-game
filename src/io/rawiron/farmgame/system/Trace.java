package io.rawiron.farmgame.system;

import java.util.Stack;
import java.lang.System;


public class Trace {

    public static final boolean VERBOSE = true;
    public static final byte VERBOSE_LEVEL = 6;
    public static final boolean TRACE_TIMERS = true;
    public Stack<Long> timer = new Stack<Long>();


    public Trace() {
    }

    public static void trace(String msg) {
        System.out.println(msg);
    }

    public static long getTimer() {
        return java.lang.System.nanoTime();
    }
}
