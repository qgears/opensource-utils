package hu.qgears.shm.test;

import java.io.PrintStream;

import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

public class MyResultPrinter {
	PrintStream writer;
	public MyResultPrinter(PrintStream writer) {
		this.writer=writer;
	}
	public void print(Result result)
	{
		for(Failure f:result.getFailures())
		{
			System.out.println(f.getMessage());
			f.getException().printStackTrace(writer);
		}
	}
}
