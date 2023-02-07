package hu.qgears.parser.contentassist;

/**
 * DTO class to store all information about a proposal given at a specific offset of the source code.
 */
public class QCompletionProposal {
	public final String toInsert;
	public final int overWriteNchars;
	/**
	 * 
	 * @param toInsert string to be inserted into the code
	 * @param offset
	 * @param overWriteNchars overwrite this number of characters left of the current position (this many characters should be similar in the proposal)
	 * @param length
	 * @param object
	 * @param key
	 * @param object2
	 * @param string
	 */
	public QCompletionProposal(String toInsert, int offset, int overWriteNchars, int length, Object object, String key, Object object2,
			String string) {
		this.toInsert=toInsert;
		this.overWriteNchars=overWriteNchars;
	}
}
