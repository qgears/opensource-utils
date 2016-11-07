package hu.qgears.coolrmi.test;

public class TestService implements ITestService{

	@Override
	public String call1(String message) {
		return "test "+message;
	}

}
