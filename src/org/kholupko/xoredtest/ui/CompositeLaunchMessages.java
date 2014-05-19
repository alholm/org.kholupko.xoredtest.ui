package org.kholupko.xoredtest.ui;

import org.eclipse.osgi.util.NLS;

public class CompositeLaunchMessages extends NLS {
	private static final String BUNDLE_NAME = "org.kholupko.xoredtest.ui.CompositeLaunchMessages";

		
	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, CompositeLaunchMessages.class);
	}
	
	public static String SelectLaunchConfigurationsTabLabel;
	public static String NoConfigurationsSelectedMessage;
	public static String SelectConfigurationsLabelText;
	public static String ExceptionWhileLaunchingCompositeConfigurationTitle;
	public static String TerminateOtherLaunchesQuestion;
	
}
