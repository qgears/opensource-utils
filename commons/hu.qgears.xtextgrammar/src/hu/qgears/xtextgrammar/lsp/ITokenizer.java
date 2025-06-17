package hu.qgears.xtextgrammar.lsp;

import java.util.List;

import org.eclipse.emf.common.util.URI;

public abstract class ITokenizer {
	public class Token5 {
		public int line, column, length, type, modifiers;
		public Token5(int line, int column, int length, int type, int modifiers) {
			super();
			this.line = line;
			this.column = column;
			this.length = length;
			this.type = type;
			this.modifiers = modifiers;
		}
		
		public Token5() {}
	}
	public abstract List<Token5> tokenize(URI uri);
	public abstract List<String> getTokenTypes();
	public abstract List<String> getTokenModifiers();
}
