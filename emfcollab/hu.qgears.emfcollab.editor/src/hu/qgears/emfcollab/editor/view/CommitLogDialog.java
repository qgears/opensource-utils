package hu.qgears.emfcollab.editor.view;

/*
 SWT/JFace in Action
 GUI Design with Eclipse 3.0
 Matthew Scarpino, Stephen Holder, Stanford Ng, and Laurent Mihalkovic

 ISBN: 1932394273

 Publisher: Manning
 */

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class CommitLogDialog extends Dialog {
	private static final int RESET_ID = IDialogConstants.NO_TO_ALL_ID + 1;

	private Text commitlogField;

	String title;
	public CommitLogDialog(Shell parentShell, String title) {
		super(parentShell);
		this.title=title;
	}
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(title);
		setShellStyle(SWT.RESIZE|SWT.SHELL_TRIM);
	}
	protected Control createDialogArea(Composite parent) {
		Composite comp = (Composite) super.createDialogArea(parent);

		GridLayout layout = (GridLayout) comp.getLayout();
		layout.numColumns = 2;

		Label usernameLabel = new Label(comp, SWT.RIGHT);
		usernameLabel.setText("Commit log: ");

		commitlogField = new Text(comp, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
//		commitlogField.set
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.heightHint=400;
		commitlogField.setLayoutData(data);
		commitlogField.setText("");
		return comp;
	}

	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		createButton(parent, RESET_ID, "Reset All", false);
	}

	protected void buttonPressed(int buttonId) {
		if (buttonId == RESET_ID) {
			commitlogField.setText("");
		} else {
			super.buttonPressed(buttonId);
		}
	}

	String commitlog = "";

	public String getCommitlog() {
		return commitlog;
	}
	@Override
	public boolean close() {
		commitlog = commitlogField.getText();
		return super.close();
	}

}