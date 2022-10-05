package hu.qgears.parser.contentassist;

public class CompletionProposal {
	public final String toInsert;
	public CompletionProposal(String toInsert, int offset, int i, int length, Object object, String key, Object object2,
			String string) {
		this.toInsert=toInsert;
	}
}
