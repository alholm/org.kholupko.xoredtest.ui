package org.kholupko.xoredtest.ui.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;

/**
 * Class represents selected launch configuration table row for {@link LaunchConfigurationsSelectionControl}
 * @author A. Kholupko
 *
 */
public class LaunchConfigurationWrapper {

	private ILaunchConfiguration launchConfiguration;
	private String mode;
	
	public LaunchConfigurationWrapper(ILaunchConfiguration launchConfiguration) throws CoreException {
		this(launchConfiguration, launchConfiguration.supportsMode(ILaunchManager.RUN_MODE) ? ILaunchManager.RUN_MODE : ILaunchManager.DEBUG_MODE);
	}	
	
	public LaunchConfigurationWrapper(ILaunchConfiguration launchConfiguration,
			String mode) {
		this.launchConfiguration = launchConfiguration;
		this.mode = mode;

	}

	/**
	 * Considers only RUN and DEBUG modes. 
	 * To add some modes override this
	 * @return the list modes supported by this configuration.
	 * @throws CoreException
	 */
	public List<String> getSupportedModes() throws CoreException{
		
		ArrayList<String> result = new ArrayList<String>();
	
		if(launchConfiguration.supportsMode(ILaunchManager.RUN_MODE))
			result.add(ILaunchManager.RUN_MODE);
		if(launchConfiguration.supportsMode(ILaunchManager.DEBUG_MODE))
			result.add(ILaunchManager.DEBUG_MODE);				

		return result;
	}
	
	
	public ILaunchConfiguration getLaunchConfiguration() {
		return launchConfiguration;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}
	
	
}
