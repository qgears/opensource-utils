package hu.qgears.images.text;

public enum EWrapMode {

	/**
	 * Wrap lines at character boundaries.
	 */
	CHAR,

	/**
	 * Wrap lines at word boundaries.
	 */
	WORD,
	/**
	 * Wrap lines at word boundaries, but fall back to character boundaries if there
	 * is not enough space for a full word.
	 */
	WORD_CHAR
}
