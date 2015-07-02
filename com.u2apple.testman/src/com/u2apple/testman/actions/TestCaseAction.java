package com.u2apple.testman.actions;

import java.io.IOException;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import com.u2apple.testman.constant.Constants;
import com.u2apple.testman.core.UnitTestTool;

/**
 * Our sample action implements workbench action delegate. The action proxy will
 * be created by the workbench and shown in the UI. When the user tries to use
 * the action, this delegate will be created and execution will be delegated to
 * it.
 * 
 * @see IWorkbenchWindowActionDelegate
 */
public class TestCaseAction implements IWorkbenchWindowActionDelegate {
	private IWorkbenchWindow window;

	/**
	 * The constructor.
	 */
	public TestCaseAction() {
	}

	/**
	 * The action has been activated. The argument of the method represents the
	 * 'real' action sitting in the workbench UI.
	 * 
	 * @see IWorkbenchWindowActionDelegate#run
	 */
	@Override
	public void run(IAction action) {
		// IWorkbenchPage activePage = PlatformUI.getWorkbench()
		// .getActiveWorkbenchWindow().getActivePage();
		// IEditorPart activeEditor = activePage.getActiveEditor();
		// ICompilationUnit unit = null;
		// if (activeEditor != null && activeEditor.getEditorInput() != null) {
		// IWorkingCopyManager manager = JavaUI.getWorkingCopyManager();
		// unit = manager.getWorkingCopy(activeEditor.getEditorInput());
		// }
		//
		// ICompilationUnit workingCopy = null;
		// if (unit == null) {
		// ISelectionService service = window.getSelectionService();
		// ISelection selection = service.getSelection();
		// if (selection instanceof IStructuredSelection) {
		// Object element = ((IStructuredSelection) selection)
		// .getFirstElement();
		// if (element instanceof ICompilationUnit) {
		// ICompilationUnit icompilationUnit = (ICompilationUnit) element;
		// // boolean isWorkingCopy = icompilationUnit.isWorkingCopy();
		// try {
		// workingCopy = icompilationUnit.getWorkingCopy(null);
		// } catch (JavaModelException e) {
		// MessageDialog.openInformation(window.getShell(),
		// Constants.MESSAGE_DIALOG_TITLE, e.getMessage());
		// }
		//
		// } else {
		// MessageDialog
		// .openInformation(window.getShell(),
		// Constants.MESSAGE_DIALOG_TITLE,
		// "Select a Java Test case file and then generate test case.");
		//
		// }
		//
		// } else {
		// MessageDialog
		// .openInformation(window.getShell(),
		// Constants.MESSAGE_DIALOG_TITLE,
		// "Select a Java Test case file and then generate test case.");
		// }
		// } else {
		// try {
		// workingCopy = unit.getWorkingCopy(null);
		// } catch (JavaModelException e) {
		// MessageDialog.openInformation(window.getShell(),
		// Constants.MESSAGE_DIALOG_TITLE, e.getMessage());
		// }
		// }
		//
		// if (workingCopy != null) {
		// UnitTestTool tool = new UnitTestTool(workingCopy);
		// try {
		// tool.generateTestCase();
		// } catch (IOException | JavaModelException | MalformedTreeException |
		// BadLocationException e) {
		// MessageDialog.openInformation(window.getShell(),
		// Constants.MESSAGE_DIALOG_TITLE, e.getMessage());
		// }
		// }

		UnitTestTool tool = new UnitTestTool();
		try {
			boolean isSuccessful=tool.generateTestCases();
			if(isSuccessful){
				MessageDialog.openInformation(window.getShell(),
						Constants.MESSAGE_DIALOG_TITLE, Constants.SUCCESS_MESSAGE);
			}else{
				MessageDialog.openInformation(window.getShell(),
						Constants.MESSAGE_DIALOG_TITLE, Constants.FAIL_MESSAGE);
			}
			
		} catch (IOException e) {
			MessageDialog.openInformation(window.getShell(),
					Constants.MESSAGE_DIALOG_TITLE, e.getMessage());
		}
	}

	/**
	 * Selection in the workbench has been changed. We can change the state of
	 * the 'real' action here if we want, but this can only happen after the
	 * delegate has been created.
	 * 
	 * @see IWorkbenchWindowActionDelegate#selectionChanged
	 */
	@Override
	public void selectionChanged(IAction action, ISelection selection) {
	}

	/**
	 * We can use this method to dispose of any system resources we previously
	 * allocated.
	 * 
	 * @see IWorkbenchWindowActionDelegate#dispose
	 */
	@Override
	public void dispose() {
	}

	/**
	 * We will cache window object in order to be able to provide parent shell
	 * for the message dialog.
	 * 
	 * @see IWorkbenchWindowActionDelegate#init
	 */
	@Override
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}
}
