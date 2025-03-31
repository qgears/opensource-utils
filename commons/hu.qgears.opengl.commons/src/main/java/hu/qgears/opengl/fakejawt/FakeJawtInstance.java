package hu.qgears.opengl.fakejawt;

import java.io.File;

import hu.qgears.nativeloader.XmlNativeLoader3;

public class FakeJawtInstance extends XmlNativeLoader3 {
	private static FakeJawtInstance instance=new FakeJawtInstance();

	public static FakeJawtInstance getInstance() {
		return instance;
	}
	private FakeJawtInstance()
	{
		load();
	}
	@Override
	public void load(File nativeLibFile) throws Throwable {
		Runtime.getRuntime().load(nativeLibFile.getAbsolutePath());
	}
}
