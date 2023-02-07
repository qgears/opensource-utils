package hu.qgears.xtextgrammar.contentassist;

import java.util.Set;

public class CrossReference {
	public final String claName;
	public final String featureName;
	public Set<String> acceptedTypes;
	public CrossReference(String claName, String featureName) {
		this.claName=claName;
		this.featureName=featureName;
	}
}
