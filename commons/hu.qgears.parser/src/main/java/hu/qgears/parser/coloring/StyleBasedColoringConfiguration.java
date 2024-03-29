package hu.qgears.parser.coloring;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

public class StyleBasedColoringConfiguration {
	/**
	 * Token identifier to style map.
	 */
	public final Map<String,String> tokenToStyle=new HashMap<>();
	/**
	 * Non terminal identifier to style map.
	 */
	public final Map<String,String> typeToStyle=new HashMap<>();
	
	/**
	 * Render to text for debug purpose
	 * @return
	 */
	public String renderToString()
	{
		StringBuilder sb=new StringBuilder();
		for(String key: new TreeSet<>(tokenToStyle.keySet()))
		{
			sb.append(key);
			sb.append("->");
			sb.append(tokenToStyle.get(key));
			sb.append("\n");
		}
		for(String key: new TreeSet<>(typeToStyle.keySet()))
		{
			sb.append(key);
			sb.append("->");
			sb.append(typeToStyle.get(key));
			sb.append("\n");
		}
		return sb.toString();
	}
}
