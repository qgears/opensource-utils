package hu.qgears.parser;

import hu.qgears.parser.impl.TreeElem;

public class TreeRenderer {
	public String render(ITreeElem _root, String prefix) {
		TreeElem root = (TreeElem) _root;
		StringBuilder ret = new StringBuilder();
		ret.append(prefix + root + "\n");
		for (TreeElem ch : root.getSubs()) {
			ret.append(render(ch, prefix + "\t") + "\n");
		}
		return ret.toString();
	}

	public String render2(ITreeElem _root, String prefix) {
		StringBuilder ret = new StringBuilder();
		render2(ret, _root, prefix);
		return ret.toString();
	}
	
	public void render2(StringBuilder ret, ITreeElem _root, String prefix)
	{
		TreeElem root = (TreeElem) _root;
		ret.append(prefix
				+ root.getType().getName()
				+ (root.getToken() != null ? " '" + root.getToken().getText()
						+ "'" : "") + "\n");
		for (TreeElem ch : root.getSubs()) {
			render2(ret, ch, prefix + "\t");
		}
	}

	public static String render2(ITreeElem tree) {
		return new TreeRenderer().render2(tree, "");
	}
}
