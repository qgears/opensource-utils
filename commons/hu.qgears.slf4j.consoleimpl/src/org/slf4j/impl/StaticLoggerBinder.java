package org.slf4j.impl;

import org.slf4j.ILoggerFactory;

public class StaticLoggerBinder implements org.slf4j.spi.LoggerFactoryBinder{

	private static StaticLoggerBinder singleton=new StaticLoggerBinder();
	public static StaticLoggerBinder getSingleton()
	{
		return singleton;
	}
	MyLoggerFactory fact=new MyLoggerFactory();
	@Override
	public ILoggerFactory getLoggerFactory() {
		return fact;
	}

	@Override
	public String getLoggerFactoryClassStr() {
		return "";
	}

}
