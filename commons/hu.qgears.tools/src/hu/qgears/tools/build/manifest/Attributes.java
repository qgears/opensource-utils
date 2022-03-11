package hu.qgears.tools.build.manifest;

import java.util.HashMap;
import java.util.Map;

public class Attributes {
	public Map<String, String> values=new HashMap<>();
	public String getValue(String string) {

		return values.get(string);
	}

}
