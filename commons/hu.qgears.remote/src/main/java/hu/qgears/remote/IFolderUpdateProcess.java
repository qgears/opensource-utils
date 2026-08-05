package hu.qgears.remote;

import java.util.concurrent.ExecutionException;

public interface IFolderUpdateProcess {
	boolean isFinished() throws ExecutionException;
}
