package hu.qgears.commons;

import java.io.File;

/**
 * Filter files based on arbitrary decision.
 * @author rizsi
 *
 */
public interface IFileFilter {
	/**
	 * Run filter on this file.
	 * @param f file to be tested against the filter
	 * @return true when this filter allows this file to be processed.
	 */
	boolean fileAllowed(File f);
}
