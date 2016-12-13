package hu.qgears.tools;

import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;

import hu.qgears.commons.UtilString;

public class UtilFile2 {
	private static int mapValue(PosixFilePermission p)
	{
		switch (p) {
		case GROUP_EXECUTE: return 1*8;
		case GROUP_READ: return 4*8;
		case GROUP_WRITE: return 2*8;
		case OWNER_EXECUTE: return 1*8*8;
		case OWNER_READ: return 4*8*8;
		case OWNER_WRITE: return 2*8*8;
		case OTHERS_EXECUTE: return 1;
		case OTHERS_READ: return 4;
		case OTHERS_WRITE: return 2;
		default:
			throw new RuntimeException("Unknown permission");
		}
	}

	public static String serializePerm(Set<PosixFilePermission> perm) {
		int ret=0;
		for(PosixFilePermission p: perm)
		{
			ret+=mapValue(p);
		}
		return UtilString.fillLeft(Integer.toString(ret, 8), 4, '0');
	}

}
