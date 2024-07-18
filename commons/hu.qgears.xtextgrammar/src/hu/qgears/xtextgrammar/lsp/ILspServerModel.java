package hu.qgears.xtextgrammar.lsp;

import java.io.File;

public interface ILspServerModel {
	public default ITokenizer getTokenizer() {
		return null;
	}
	public default ILinkProvider getLinkProvider() {
		return null;
	}
	public File getLogsFolder();
}
