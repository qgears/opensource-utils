package hu.qgears.parser.editor.coloring;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.custom.StyleRange;

/**
 * Print coloring info for debug purpose
 * TODO remove usage from code after debugged
 */
public class ColoringLogger {
	public void print() {
		// TODO Auto-generated method stub
		
	}
	public void addStyleRange(StyleRange se, IDocument iDocument) {
		int start= se.start;
		int length=se.length;
		StringBuilder s=new StringBuilder();
		for(int i=start;i<start+length;++i)
		{
			try {
				s.append(iDocument.getChar(i));
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		s.append(": ");
		s.append(""+se.foreground);
		System.out.println(s.toString());
	}

}
