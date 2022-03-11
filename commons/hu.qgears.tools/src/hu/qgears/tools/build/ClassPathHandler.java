package hu.qgears.tools.build;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * SAX handler for .classpath file of Eclipse project.
 */
public class ClassPathHandler extends DefaultHandler
{
	public List<String> sourceFolders=new ArrayList<>();
	public List<String> libFolders=new ArrayList<>();
	public List<String> userLibraries=new ArrayList<>();
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if(qName.equals("classpathentry"))
		{
			String kind=attributes.getValue("kind");
			if("src".equals(kind))
			{
				String path=attributes.getValue("path");
				sourceFolders.add(path);
			}
			if("lib".equals(kind))
			{
				String path=attributes.getValue("path");
				libFolders.add(path);
			}
			if("con".equals(kind))
			{
				String path=attributes.getValue("path");
				String prefix="org.eclipse.jdt.USER_LIBRARY/";
				if(path.startsWith(prefix))
				{
					
					String id=path.substring(prefix.length());
					userLibraries.add(id);
				}
			}
		}
		super.startElement(uri, localName, qName, attributes);
	}
}
