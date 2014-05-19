package org.kholupko.xoredtest.ui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.jface.dialogs.MessageDialog;

public class CompositeLaunchExceptionStatusHandler implements IStatusHandler {

	@Override
	public Object handleStatus(IStatus status, Object source) throws CoreException {
		final boolean[] result = new boolean[1];
		final String statusMessage = status.getMessage();
		final String exceptionMessage = status.getException().getMessage();
		DebugUIPlugin.getStandardDisplay().syncExec(new Runnable() {
			public void run() {
				String title = CompositeLaunchMessages.ExceptionWhileLaunchingCompositeConfigurationTitle; 
				String message = statusMessage + " Reason:\n" + exceptionMessage + "\n" + CompositeLaunchMessages.TerminateOtherLaunchesQuestion;
				result[0] = (MessageDialog.openQuestion(DebugUIPlugin.getShell(), title, message));
			}
		});
		return Boolean.valueOf(result[0]);
	}

}
