package hu.qgears.parser.coloring;

import java.util.HashMap;
import java.util.Map;

public class StyleBasedColoringConfiguration {
	/**
	 * Token identifier to style map.
	 */
	public final Map<String,String> tokenToStyle=new HashMap<>();
	/**
	 * Non terminal identifier to style map.
	 */
	public final Map<String,String> typeToStyle=new HashMap<>();

}
