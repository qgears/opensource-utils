package hu.qgears.tools.build.manifest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Simple Manifest reader re-implementation
 *
 */
public class SimpleManifest {
	class PeekReader
	{
		String nextLine;
		BufferedReader br;
		public PeekReader(InputStream input) throws IOException {
			InputStreamReader isr=new InputStreamReader(input);
			br=new BufferedReader(isr);
			nextLine=br.readLine();
		}
		public String readLine() throws IOException {
			String ret=nextLine;
			nextLine=br.readLine();
			return ret;
		}
		public String peekLine() {
			return nextLine;
		}
	}
	Attributes atts=new Attributes();
	public SimpleManifest(InputStream input) throws IOException {
		try
		{
			PeekReader br=new PeekReader(input);
			String l=br.readLine();
			while(l!=null)
			{
				StringBuilder value=new StringBuilder();
				int idx=l.indexOf(": ");
				if(idx>0)
				{
					String key=l.substring(0, idx);
					value.append(l.substring(idx+2));
					String nextLine=br.peekLine();
					while(nextLine!=null && nextLine.startsWith(" "))
					{
						br.readLine();
						value.append(nextLine.substring(1));
						nextLine=br.peekLine();
					}
					atts.values.put(key, value.toString());
				}
				l=br.readLine();
			}
		}finally
		{
			input.close();
		}
	}

	public Attributes getMainAttributes() {
		return atts;
	}

}
