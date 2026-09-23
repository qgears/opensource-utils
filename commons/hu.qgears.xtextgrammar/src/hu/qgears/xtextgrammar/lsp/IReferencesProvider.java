package hu.qgears.xtextgrammar.lsp;

import java.util.List;

import hu.qgears.xtextgrammar.lsp.IDefinitionProvider.Location;

public interface IReferencesProvider {
	public List<Location> findReferences(String textDocumentUri, int line, int column);
}
