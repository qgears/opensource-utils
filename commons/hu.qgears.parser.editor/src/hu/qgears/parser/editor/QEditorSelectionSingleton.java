package hu.qgears.parser.editor;

import org.eclipse.core.resources.IProject;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.viewers.SelectionChangedEvent;

import hu.qgears.commons.Pair;
import hu.qgears.commons.UtilEvent;

/**
 * Singleton object to communicate selection.
 */
public class QEditorSelectionSingleton {
	private static final QEditorSelectionSingleton instance=new QEditorSelectionSingleton();
	public static QEditorSelectionSingleton getInstance() {
		return instance;
	}
	public final UtilEvent<Pair<IProject, EObject>> selectionEvent=new UtilEvent<>();
	public final UtilEvent<SelectionChangedEvent> outlineSelectionEvent=new UtilEvent<>();
}
