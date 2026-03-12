package org.slf4j.impl;

import java.util.Date;
import java.util.logging.Level;

import org.slf4j.Marker;

public class LogOutput {

	public static void fmt(Level severe, String string) {
		System.err.println(new Date()+" "+severe+" "+string);
	}

	public static void fmt(Level severe, String string, Object[] arguments) {
		
		System.err.print(new Date()+" "+severe);
		if(arguments!=null)
		{
			for(Object o:arguments)
			{
				System.err.print(" "+o);
			}
		}
		System.err.println();
	}

	public static void fmt(Level severe, String string, Throwable t) {
		System.err.println(new Date()+" "+severe+" "+string);
		if(t!=null)
		{
			t.printStackTrace();
		}
	}

	public static void fmt(Level severe, Marker m, String string, Throwable t) {
		System.err.println(new Date()+" "+severe+" "+m+" "+string);
		if(t!=null)
		{
			t.printStackTrace();
		}
	}

	public static void fmt(Level severe, Marker marker, String string) {
		System.err.println(new Date()+" "+severe+" "+marker+" "+string);
	}
}
