package com.u2apple.testman.actions;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;

import com.u2apple.testman.core.TestCaseRefactor;

public class ExtractTestCaseAction implements IWorkbenchWindowActionDelegate {
	private IWorkbenchWindow window;

	@Override
	public void run(IAction action) {
		TestCaseRefactor testCaseRefactor = new TestCaseRefactor();
		try {
			testCaseRefactor.extractMethodByBrand();
			MessageDialog.openInformation(window.getShell(), "Extract test case",
					"Extract test case is successful.");
		} catch (JavaModelException | MalformedTreeException
				| BadLocationException | PartInitException e) {
			MessageDialog.openInformation(window.getShell(), "Extract test case",
					"Extract test case is failed.");
		}

	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

}
