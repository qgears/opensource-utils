package hu.qgears.tools;

import java.util.List;

public interface ITool {

	String getId();

	String getDescription();

	int exec(List<String> subList) throws Exception;

}
