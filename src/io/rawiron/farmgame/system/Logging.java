package io.rawiron.farmgame.system;

import java.lang.System;


public class Logging {
	public boolean log;
	public boolean log_data_read;

	public static void log(String msg) {
        System.out.println(msg);
    }
}
