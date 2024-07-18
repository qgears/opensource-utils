package hu.qgears.xtextgrammar.lsp;

import java.util.List;

public interface ILinkProvider {
	public class Location {
		public String uri;
		public int start_line;
		public int start_column;
		public int end_line;
		public int end_column;
	}
	public default List<Location> provideLinks(String textDocumentUri, int line, int column) {
		return null;
	}
}
