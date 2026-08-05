package hu.qgears.emfcollab.srv;

import java.util.HashMap;
import java.util.Map;

public class EmfFileSet implements EmfSerializable {
	private static final long serialVersionUID = 1L;
	private Map<String, byte[]> files=new HashMap<String, byte[]>();
	public Map<String, byte[]> getFiles() {
		return files;
	}
	public void setFiles(Map<String, byte[]> files) {
		this.files = files;
	}
}
