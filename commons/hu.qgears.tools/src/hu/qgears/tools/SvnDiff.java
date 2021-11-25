package hu.qgears.tools;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Future;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import hu.qgears.commons.Pair;
import hu.qgears.commons.UtilFile;
import hu.qgears.commons.UtilProcess;
import joptsimple.annot.JOHelp;
import joptsimple.tool.AbstractTool;

public class SvnDiff extends AbstractTool {

	public class Args implements IArgs
	{
		public Args() throws IOException {
			workingCopy=new File(".").getCanonicalFile();
		}
		public File workingCopy;
		@JOHelp("Show unversioned files in the diff.")
		public boolean unversioned=true;

		@JOHelp("Command to start svn command line tool.")
		public String svnCommand="svn";
		
		@JOHelp("After the diff tool has exited delete the temporary folders.")
		public boolean deleteTempFilesOnExit=true;

		@JOHelp("Command to start the diff tool.")
		public String difftoolCommand="meld";
		@Override
		public void validate() {
			if(workingCopy==null||!workingCopy.exists())
			{
				throw new IllegalArgumentException("workingCopy must exist.");
			}
		}
	}
	/**
	 * The temporary folder as base for comparing.
	 */
	public class DiffOutput
	{
		public File folder;
	}
	@Override
	public String getId() {
		return "svndiff";
	}

	@Override
	public String getDescription() {
		return "Collect all differences of a current SVN working copy tree to the BASE revision and launch a difftool (meld) session. (Similar feature as git difftool -d) Both the base revision (left side of compare) and the working copy version (right side of compare) are collected into a temporary folder. BASE revision entries are copies from the svn. Working copy entries are symlinks into the working copy folder."
				+"\nDoes not handle all status (eg. conflict handling is missing). Use it on your own responsibility!";
	}

	@Override
	protected IArgs createArgsObject() {
		try {
			return new Args();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	@Override
	protected int doExec(IArgs ia) throws Exception {
		Args a=(Args) ia;
		String s = UtilProcess
				.execute(new ProcessBuilder(a.svnCommand, "status", "-v", "--xml").directory(a.workingCopy).start());
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		InputSource is = new InputSource(new StringReader(s));
		// Debugger hangs when string is too long :-)
		s=null;
		Document doc = db.parse(is);

		XPath xPath = XPathFactory.newInstance().newXPath();
		{
			NodeList nodes = (NodeList) xPath.evaluate("target/entry[@path='.']/wc-status", doc.getDocumentElement(),
					XPathConstants.NODESET);
			if(nodes.getLength()>0) {
				Element e = (Element) nodes.item(0);
				String rev = e.getAttribute("revision");
				System.out.println("BASE Revision: " + rev);
			}
		}
		{
			boolean unversionedOmitted=false;
			DiffOutput left=new DiffOutput();
			File folder=File.createTempFile("svndiff", "temp");
			folder.delete();
			folder.mkdirs();
			left.folder=new File(folder, "left");
			DiffOutput right=new DiffOutput();
			right.folder=new File(folder, "right");
			left.folder.mkdirs();
			right.folder.mkdirs();
			NodeList entries = (NodeList) xPath.evaluate("target/entry", doc.getDocumentElement(),
					XPathConstants.NODESET);
			XPathExpression selectMode = xPath.compile("wc-status/@item");
			XPathExpression selectPath = xPath.compile("@path");
			for (int i = 0; i < entries.getLength(); ++i) {
				Element e = (Element) entries.item(i);
				// WORKAROUND: Required for XPath to perform reasonably - see: http://stackoverflow.com/questions/3782618/xpath-evaluate-performance-slows-down-absurdly-over-multiple-calls
				e.getParentNode().removeChild(e);
				// printXmlNode(e);

				String mod = (String) selectMode.evaluate(e, XPathConstants.STRING);
				String path = (String) selectPath.evaluate(e, XPathConstants.STRING);
				switch (mod) {
				case "normal":
					// Normals are skipped - nothing to do
					break;
				case "modified":
				{
					File f=new File(left.folder, path);
					f.getParentFile().mkdirs();
					byte[] leftContent=svnCat(a, a.workingCopy, path);
					if(leftContent!=null)
					{
						UtilFile.saveAsFile(f, leftContent);
					}
					File g=new File(right.folder, path);
					g.getParentFile().mkdirs();
					myCreateSymbolicLink(mod, g.toPath(), new File(a.workingCopy, path).toPath());
					break;
				}
				case "unversioned":
				if(a.unversioned)
				{
					File g=new File(right.folder, path);
					g.getParentFile().mkdirs();
					myCreateSymbolicLink(mod, g.toPath(), new File(a.workingCopy, path).toPath());
				}else
				{
					unversionedOmitted=true;
				}
				break;
				case "missing":
					// System.err.println("Missing: "+path);
					if(a.unversioned)
					{
						File f=new File(left.folder, path);
						f.getParentFile().mkdirs();
						byte[] leftContent=svnCat(a, a.workingCopy, path);
						if(leftContent!=null)
						{
							UtilFile.saveAsFile(f, leftContent);
						}
						// TODO create link for the highest level existing folder!
					}else
					{
						unversionedOmitted=true;
					}
					break;
				case "deleted":
					File f=new File(left.folder, path);
					f.getParentFile().mkdirs();
					byte[] leftContent=svnCat(a, a.workingCopy, path);
					if(leftContent!=null)
					{
						UtilFile.saveAsFile(f, leftContent);
					}
					break;
				case "added":
				{
					// System.err.println("Added: '"+path+"'");
					File g=new File(right.folder, path);
					g.getParentFile().mkdirs();
					myCreateSymbolicLink(mod, g.toPath(), new File(a.workingCopy, path).toPath());
				}
				break;
				case "external":
					// Nothing to do with externals
					break;
				default:
					if (!path.equals(".")) {
						System.out.println("difference: " + mod + " path: " + path);
					}
					break;
				}
			}
			if(unversionedOmitted)
			{
				System.err.println("There are unversioned changes (missing or added files) that are omitted. Use --unversioned true to make them visible.");
			}
			Process difftool=new ProcessBuilder().command("meld", left.folder.getAbsolutePath(), right.folder.getAbsolutePath()).start();
			UtilProcess.getProcessReturnValueFuture(difftool).get();
			deleteRecursiveDontFollowSymlinks(folder);
		}
		return 0;
	}
	/**
	 * Create symbolic link to a file.
	 * (Or a folder with similar name in case the target is a folder.)
	 * 
	 * Handle exceptions inside: in case of exception that will be logged to stderr and ignored.
	 * @param why
	 * @param path
	 * @param target
	 * @throws IOException
	 */
	private void myCreateSymbolicLink(String why, Path path, Path target) {
		try {
			if(target.toFile().isDirectory())
			{
				Files.createDirectory(path);
			}else
			{
				Files.createSymbolicLink(path, target);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private byte[] svnCat(Args a, File workingCopy, String path) throws Exception
	{
		Process p=new ProcessBuilder().command("/usr/bin/svn", "cat", path).directory(workingCopy).start();
		Future<Pair<byte[], byte[]>> fut=UtilProcess.saveOutputsOfProcess(p);
		Pair<byte[], byte[]> pa=fut.get();
		byte[] retBytes=pa.getA();
		if(retBytes.length==0)
		{
			int ret=UtilProcess.getProcessReturnValueFuture(p).get();
			if(ret!=0)
			{
				// svn cat returns error in case of folder.
				return null;
			}
		}
		return retBytes;
	}

	@SuppressWarnings("unused")
	private void printXmlNode(Node n) {
		if (n == null) {
			System.out.println("NULL");
			return;
		}
		try {
			System.out.println("NODE: " + n.getNodeName() + " " + toString(n, true, true));
		} catch (Exception e) {
			new RuntimeException(e);
		}
	}

	public static String toString(Node node, boolean omitXmlDeclaration, boolean prettyPrint) {
		if (node == null) {
			throw new IllegalArgumentException("node is null.");
		}

		try {
			// Remove unwanted whitespaces
			node.normalize();
			XPath xpath = XPathFactory.newInstance().newXPath();
			XPathExpression expr = xpath.compile("//text()[normalize-space()='']");
			NodeList nodeList = (NodeList) expr.evaluate(node, XPathConstants.NODESET);

			for (int i = 0; i < nodeList.getLength(); ++i) {
				Node nd = nodeList.item(i);
				nd.getParentNode().removeChild(nd);
			}

			// Create and setup transformer
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

			if (omitXmlDeclaration == true) {
				transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			}

			if (prettyPrint == true) {
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
			}

			// Turn the node into a string
			StringWriter writer = new StringWriter();
			transformer.transform(new DOMSource(node), new StreamResult(writer));
			return writer.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	/**
	 * Delete the given file or directory. If it is a directory then delete all
	 * its content recursively. (Works like "rm -rf" with some differences)
	 * 
	 * Folder symbolic links are not followed so the contents of the original folder (which is linked from any subtree
	 * of the dir folder) is not touched.
	 * 
	 * @param dir
	 */
	public static void deleteRecursiveDontFollowSymlinks(File dir) {
		if (dir.exists()) {
			if (!Files.isSymbolicLink(dir.toPath()) && dir.isDirectory()) {
				for (File f : dir.listFiles()) {
					deleteRecursiveDontFollowSymlinks(f);
				}
			}
			if (!dir.delete()){
				System.err.println("Cannot delete file: "+dir);
			}
		}
	}
}
