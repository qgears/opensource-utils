package hu.qgears.xtextgrammar.lsp;

import java.io.File;

import hu.qgears.xtextgrammar.lsp.IDefinitionProvider.Location;

public interface ILspServerModel {
	public  ITokenizer getTokenizer();
	public IDefinitionProvider getLinkProvider();
	public File getLogsFolder();
}
