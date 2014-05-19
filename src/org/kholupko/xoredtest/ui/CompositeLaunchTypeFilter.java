package org.kholupko.xoredtest.ui;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.kholupko.xoredtest.core.CompositeLaunchConfigurationDelegate;

public class CompositeLaunchTypeFilter extends ViewerFilter {

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if(element instanceof ILaunchConfiguration) {
			return true;
		}
		if(element instanceof ILaunchConfigurationType) {
			if(CompositeLaunchConfigurationDelegate.COMPOSITE_LAUNCH_CONFIGURATION_ID.equals(((ILaunchConfigurationType)element).getIdentifier()))
				return false;
		}
		return true;
	}

}
