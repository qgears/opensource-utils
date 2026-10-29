package hu.qgears.textrender.libschrift;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import hu.qgears.nativeloader.NativeBinary;
import hu.qgears.nativeloader.NativeLoadException;
import hu.qgears.nativeloader.NativesToLoad;
import hu.qgears.nativeloader.UtilNativeLoader;
import hu.qgears.nativeloader.XmlNativeLoader;

public class LibschriftAccessor extends XmlNativeLoader {
	
	private static final Logger LOG = Logger.getLogger(LibschriftAccessor.class);
	private static LibschriftNative instance;
	
	private LibschriftAccessor() {
	}

	@Override
	public void load(File nativeLibFile) throws Throwable {
		Runtime.getRuntime().load(nativeLibFile.getAbsolutePath());
	}

	public static synchronized LibschriftNative getInstance() {
		if (instance == null) {
			instance = new LibschriftNative();
			UtilNativeLoader.loadNatives(new LibschriftAccessor());
		}
		return instance;
	}
	
	@Override
	public NativesToLoad getNatives(String arch, String os) throws NativeLoadException {
		NativesToLoad orig = super.getNatives(arch, os);
		List<NativeBinary> nats = orig.getBinaries();
		if (Boolean.getBoolean("libschrift.softfloat")) {
			LOG.warn("Using softfloat version of libschrift");
			nats = new ArrayList<>(orig.getBinaries().size());
			for (NativeBinary n : orig.getBinaries()) {
				nats.add(new NativeBinary(n.getLibId(), patch(n.getLibPath()), patch(n.getInstallPath())));
			}
		} else {
			LOG.warn("Using normal version of libschrift");
		}
		return new NativesToLoad(orig.getPreloads(), nats);
	}

	private String patch(String path) {
		return path.replace("libqlibschrift", "libqlibschrift_sf");
	}
}
