package hu.qgears.crossref;

public interface ICrossRefObjectListener {
	default void crObjectClosed(CrossRefObject cro) {}
}
