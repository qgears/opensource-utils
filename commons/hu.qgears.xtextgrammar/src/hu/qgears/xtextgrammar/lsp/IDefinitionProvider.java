package hu.qgears.xtextgrammar.lsp;

public interface IDefinitionProvider {
	public class Location implements Comparable<Location> {
		@Override
		public int compareTo(Location other) {
			if (this == other) return 0;
			
			int result = 0;
			if ((result = this.uri.compareTo(other.uri)) != 0) {
				return result;
			}
			if ((result = Integer.compare(this.start_line, other.start_line)) != 0) {
				return result;
			}
			if ((result = Integer.compare(this.end_line, other.end_line)) != 0) {
				return result;
			}
			if ((result = Integer.compare(this.start_column, other.start_column)) != 0) {
				return result;
			}
			if ((result = Integer.compare(this.end_column, other.end_column)) != 0) {
				return result;
			}
			return 0;
		}
		public Location(String uri, int start_line, int start_column, int end_line, int end_column) {
			super();
			this.uri = uri;
			this.start_line = start_line;
			this.start_column = start_column;
			this.end_line = end_line;
			this.end_column = end_column;
		}
//		public String documentUri;
		public String uri;
		public int start_line;
		public int start_column;
		public int end_line;
		public int end_column;
	}
	public Location findDefinition(String textDocumentUri, int line, int column);
}
