package hu.qgears.opengl.commons;

/*
 * Suppressing warnings, as fields are public and static by design. Adding
 * getters and setters does not increase security.
 */
@SuppressWarnings({ "squid:ClassVariableVisibilityCheck", "squid:S1444" })
public class OGlGlobalParameters {
	public static boolean logMouseMessages;
	public static boolean logKeyMessages;
}
