package org.kholupko.xoredtest.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.debug.internal.ui.launchConfigurations.ClosedProjectFilter;
import org.eclipse.debug.internal.ui.launchConfigurations.DeletedProjectFilter;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationsMessages;
import org.eclipse.debug.internal.ui.launchConfigurations.WorkingSetsFilter;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationListener;
import org.eclipse.jface.viewers.ColumnViewerEditorDeactivationEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ContainerCheckedTreeViewer;
import org.eclipse.ui.dialogs.PatternFilter;
import org.kholupko.xoredtest.core.CompositeLaunchConfigurationDelegate;
import org.kholupko.xoredtest.ui.dialogs.LaunchConfigurationWrapper;
import org.kholupko.xoredtest.ui.dialogs.LaunchConfigurationsSelectionControl;

/**
 * 
 * @author A. Kholupko
 *
 */
public class SelectLaunchConfigurationsTab extends AbstractLaunchConfigurationTab {

	
	private LaunchConfigurationsSelectionControl launchSelectControl;

	@Override
	public void createControl(Composite parent) {
		
		Composite mainComposite = SWTFactory.createComposite(parent, 2, 1, GridData.FILL_BOTH);
		setControl(mainComposite);
	
		SWTFactory.createLabel(mainComposite, CompositeLaunchMessages.SelectConfigurationsLabelText, 2);
		
		//TODO create help content and corresponding contextId
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), getHelpContextId());

		
		ArrayList<ViewerFilter> filters = new ArrayList<ViewerFilter>();
		ClosedProjectFilter fClosedProjectFilter = new ClosedProjectFilter();
		if(DebugUIPlugin.getDefault().getPreferenceStore().getBoolean(IInternalDebugUIConstants.PREF_FILTER_LAUNCH_CLOSED)) {
			filters.add(fClosedProjectFilter);
		}
		DeletedProjectFilter fDeletedProjectFilter = new DeletedProjectFilter();
		if(DebugUIPlugin.getDefault().getPreferenceStore().getBoolean(IInternalDebugUIConstants.PREF_FILTER_LAUNCH_DELETED)) {
			filters.add(fDeletedProjectFilter);
		}
		WorkingSetsFilter fWorkingSetsFilter = new WorkingSetsFilter();
		if(DebugUIPlugin.getDefault().getPreferenceStore().getBoolean(IInternalDebugUIConstants.PREF_FILTER_WORKING_SETS)) {
			filters.add(fWorkingSetsFilter);
		}
		
		//Special pattern filter to hide empty configuration type
		PatternFilter launchConfigutationsPatternFilter = new PatternFilter(){
			@Override
			public boolean isElementVisible(Viewer viewer, Object element) {
				return isParentMatch(viewer, element) || ((element instanceof ILaunchConfiguration) && isLeafMatch(viewer, element));
			}			
		};
		
		launchSelectControl = new LaunchConfigurationsSelectionControl(mainComposite, 
				SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION, 
				launchConfigutationsPatternFilter, true);
		
		launchSelectControl.populateLaunchConfigurationTreeViewer(null, 
				(ViewerFilter[]) filters.toArray(new ViewerFilter[filters.size()]));
		
		
		//set listeners to update Launch Configuration Dialog		
		((ContainerCheckedTreeViewer) launchSelectControl.getViewer()).addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				updateLaunchConfigurationDialog();
			}
		}); 
		
		launchSelectControl.getTableViewer().getColumnViewerEditor().addEditorActivationListener(new ColumnViewerEditorActivationListener() {
			@Override
			public void beforeEditorDeactivated(
					ColumnViewerEditorDeactivationEvent event) {
			}
			@Override
			public void beforeEditorActivated(ColumnViewerEditorActivationEvent event) {
			}
			@Override
			public void afterEditorDeactivated(ColumnViewerEditorDeactivationEvent event) {
				updateLaunchConfigurationDialog();
			}
			@Override
			public void afterEditorActivated(ColumnViewerEditorActivationEvent event) {
			}
		});

		Dialog.applyDialogFont(mainComposite); 

	}
	
	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		
		//clear controls
		((ContainerCheckedTreeViewer)launchSelectControl.getViewer()).setCheckedElements(new Object[0]);
		launchSelectControl.getTableViewer().refresh();
		
		try {
			List<String> launchConfigsMementoModeList = configuration.getAttribute(CompositeLaunchConfigurationDelegate.ATTR_COMPOSITE_LAUNCHES_LIST, (List)null);
			
			if(launchConfigsMementoModeList != null && !launchConfigsMementoModeList.isEmpty()){
			
				List<LaunchConfigurationWrapper> launchConfigurationWrappers = new ArrayList<LaunchConfigurationWrapper>();
							
				for (String launchConfigMementoModeItem : launchConfigsMementoModeList) {
					String[] mementoModeArray = launchConfigMementoModeItem.split("\\" + CompositeLaunchConfigurationDelegate.MEMENTO_MODE_ITEM_DELIMETER);

					String subLaunchConfigMemento = mementoModeArray[0];
					String subLaunchConfigMode = mementoModeArray[1];
				
					ILaunchConfiguration subLaunchConfig = DebugPlugin.getDefault().getLaunchManager().getLaunchConfiguration(subLaunchConfigMemento);
				
					launchConfigurationWrappers.add(new LaunchConfigurationWrapper(subLaunchConfig, subLaunchConfigMode));
				
				}
				
				launchSelectControl.setSelectedLaunches(launchConfigurationWrappers);
				
			}
			
		} catch (CoreException ce) {
			DebugUIPlugin.log(ce);
			MessageDialog.openError(getShell(), LaunchConfigurationsMessages.LaunchConfigurationDialog_Error_19, ce.toString());			
		}
		

	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {

		List<LaunchConfigurationWrapper> selectedLaunches = launchSelectControl.getSelectedLaunches();
		
		if(selectedLaunches.isEmpty())
			configuration.setAttribute(CompositeLaunchConfigurationDelegate.ATTR_COMPOSITE_LAUNCHES_LIST, (List)null);
		else{
			
			// cant use Map here, order is lost (even LinkedHashMap tested)
			List<String> launchConfigsMementoModeList = new ArrayList<String>();
			
			for(LaunchConfigurationWrapper configWrapper : selectedLaunches){
				try {
					
					launchConfigsMementoModeList.add(configWrapper.getLaunchConfiguration().getMemento() 
							+ CompositeLaunchConfigurationDelegate.MEMENTO_MODE_ITEM_DELIMETER
							+ configWrapper.getMode());
						
				} catch (CoreException ce) {
					DebugUIPlugin.log(ce);
					MessageDialog.openError(getShell(), LaunchConfigurationsMessages.LaunchConfigurationDialog_Error_19, ce.toString());			
				}
				
			}
			configuration.setAttribute(CompositeLaunchConfigurationDelegate.ATTR_COMPOSITE_LAUNCHES_LIST, launchConfigsMementoModeList);
		}
	}
	
	@Override
	public boolean isValid(ILaunchConfiguration launchConfig) {
		setErrorMessage(null);
		
		if(launchSelectControl.getSelectedLaunches().isEmpty()){
			setErrorMessage(CompositeLaunchMessages.NoConfigurationsSelectedMessage);
			return false;
		}

		return true;
	}

	@Override
	public String getName() {
		return CompositeLaunchMessages.SelectLaunchConfigurationsTabLabel;
	}

}
