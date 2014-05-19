package org.kholupko.xoredtest.ui.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationTreeContentProvider;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationsMessages;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ComboBoxViewerCellEditor;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ContainerCheckedTreeViewer;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.model.WorkbenchViewerComparator;
import org.kholupko.xoredtest.ui.CompositeLaunchTypeFilter;
import org.kholupko.xoredtest.ui.EmptyLaunchTypeFilter;

/**
 * 
 * @author A. Kholupko
 *
 */
public class LaunchConfigurationsSelectionControl extends FilteredContainerCheckedTree {
	
	//table control, shows checked launch configurations and allows to select its mode to launch
	protected TableViewer tableViewer;
	protected Composite tableComposite;

	public static final String SELECTED_COLUMN_LABEL = "Selected launch configuration";
	public static final String MODE_COLUMN_LABEL = "Mode";	
	
	/**
	 * Constructor
	 * @param parent
	 * @param treeStyle
	 * @param filter
	 * @param useNewLook
	 */
	public LaunchConfigurationsSelectionControl(Composite parent,
			int treeStyle, PatternFilter filter, boolean useNewLook) {
		super(parent, treeStyle, filter, useNewLook);
		
		initTableViewer();
	}


	protected void initTableViewer() {

		//to attach table right beneath tree 
		//treeViewer.getTree().setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		tableComposite = SWTFactory.createComposite(parent, parent.getFont(), 1, 1, GridData.FILL_VERTICAL, 0, 0); 
		
		tableViewer = new TableViewer(tableComposite, SWT.BORDER| SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
		tableViewer.setContentProvider(ArrayContentProvider.getInstance());
				
		Table table = tableViewer.getTable();
		table.setLayout(new GridLayout());
		table.setLayoutData(new GridData(GridData.FILL_BOTH));
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		TableViewerColumn colConfigName = new TableViewerColumn(tableViewer, SWT.NONE);
		colConfigName.getColumn().setWidth(200);
		colConfigName.getColumn().setText(SELECTED_COLUMN_LABEL);
		TableViewerColumn colConfigMode = new TableViewerColumn(tableViewer, SWT.NONE);
		colConfigMode.getColumn().setWidth(200);
		colConfigMode.getColumn().setText(MODE_COLUMN_LABEL);
		
		tableViewer.setLabelProvider(new SelectedConfigsTableLabelProvider());		
		
		colConfigMode.setEditingSupport(new SelectedConfigsTableEditingSupport(tableViewer));	
		
		// add check listener to tree to add & remove data from table
		((ContainerCheckedTreeViewer)treeViewer).addCheckStateListener(new LaunchConfigurationTreeCheckListener());
		
	}


	/**
	 * Populates launch configuration tree
	 * @param mode. Launch mode for content provider. <code>null</code> for all modes
	 * @param viewerfilters
	 */
	public void populateLaunchConfigurationTreeViewer(String mode, ViewerFilter[] viewerfilters) {
		
		treeViewer.setContentProvider(new LaunchConfigurationTreeContentProvider(mode, parent.getShell()));
		treeViewer.setLabelProvider(new DecoratingLabelProvider(DebugUITools.newDebugModelPresentation(), PlatformUI.getWorkbench().getDecoratorManager().getLabelDecorator()));
		treeViewer.setComparator(new WorkbenchViewerComparator());
		//expand all
		treeViewer.setAutoExpandLevel(TreeViewer.ALL_LEVELS);
		treeViewer.setInput(ResourcesPlugin.getWorkspace().getRoot());
		
		EmptyLaunchTypeFilter emptyLaunchTypeFilter = new EmptyLaunchTypeFilter();
		treeViewer.addFilter(emptyLaunchTypeFilter);
		
		CompositeLaunchTypeFilter compositeLaunchTypeFilter = new CompositeLaunchTypeFilter();
		treeViewer.addFilter(compositeLaunchTypeFilter);
		
		if(viewerfilters != null) {
			for (int i = 0; i < viewerfilters.length; i++) {
				treeViewer.addFilter(viewerfilters[i]);
			}
		}
		
	}
	
	/**
	 * 
	 * @return List of selected launch configurations wrappers
	 */
	public List<LaunchConfigurationWrapper> getSelectedLaunches(){
		ArrayList<LaunchConfigurationWrapper> result = new ArrayList<LaunchConfigurationWrapper>(); //m.b. set default capacity, cause 10 is much
		
		TableItem[] tableItems = tableViewer.getTable().getItems();
		for(int i = 0; i < tableItems.length; i++){
			Object element = tableItems[i].getData(); 
			if(element instanceof LaunchConfigurationWrapper)
				result.add((LaunchConfigurationWrapper) element);
		}
	
		return result;
	}
	
	/**
	 * Checks all selected launches in tree and add them to table
	 * @param launchConfigurationWrappers
	 */
	public void setSelectedLaunches(List<LaunchConfigurationWrapper> launchConfigurationWrappers){
		for(LaunchConfigurationWrapper lcWrapper : launchConfigurationWrappers){
		
			Widget treeItem = treeViewer.testFindItem(lcWrapper.getLaunchConfiguration());
			if(treeItem != null){
				Object treeElement = treeItem.getData();
				((ContainerCheckedTreeViewer)treeViewer).setChecked(treeElement, true);
	
				tableViewer.add(lcWrapper);
			}
		}
	}
	
	public TableViewer getTableViewer(){
		return tableViewer;
	}

	/**
	 * 
	 * @param launchConfiguration
	 */
	public void removeSelectedLaunchConfigFromTable(ILaunchConfiguration launchConfiguration){
		TableItem[] tableItems = tableViewer.getTable().getItems();
		for(int i = 0; i < tableItems.length; i++){
			if(tableItems[i].getData() instanceof LaunchConfigurationWrapper){
				LaunchConfigurationWrapper tableElement = (LaunchConfigurationWrapper)tableItems[i].getData();
				if(tableElement.getLaunchConfiguration().equals(launchConfiguration)){
					tableViewer.remove(tableElement);
					break;
				}
			}
		}

	}			
	
	protected class SelectedConfigsTableLabelProvider extends LabelProvider implements ITableLabelProvider {

		public String getColumnText(Object element, int columnIndex) {
			String result = "";
			LaunchConfigurationWrapper configWrapper = (LaunchConfigurationWrapper) element;
			switch (columnIndex) {
			case 0:
				result = configWrapper.getLaunchConfiguration().getName();
				break;
			case 1:
				result = configWrapper.getMode();
			}
			return result;
		}

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
	}
	

	/**
	 * provides mode selection combobox	       		
	 */
	protected class SelectedConfigsTableEditingSupport extends EditingSupport {
	     
	    private ComboBoxViewerCellEditor cellEditor = null;
	     
	    private SelectedConfigsTableEditingSupport(ColumnViewer viewer) {
	        super(viewer);
	        cellEditor = new ComboBoxViewerCellEditor((Composite) getViewer().getControl(), SWT.READ_ONLY);
	        cellEditor.setLabelProvider(new LabelProvider());
	        cellEditor.setContentProvider(new ArrayContentProvider());
	    }
	     
	    @Override
	    protected CellEditor getCellEditor(Object element) {
	    	
	    	List<String> input = new ArrayList<String>();
	    	
	    	try {
	    		input = ((LaunchConfigurationWrapper)element).getSupportedModes();
			} catch (CoreException ce) {
				DebugUIPlugin.log(ce);
				MessageDialog.openError(getShell(), LaunchConfigurationsMessages.LaunchConfigurationDialog_Error_19, ce.toString());
			} finally{
				cellEditor.setInput(input.toArray());
	    	}
	        return cellEditor;
	    }
	     
	    @Override
	    protected boolean canEdit(Object element) {
	        return true;
	    }
	     
	    @Override
	    protected Object getValue(Object element) {
	        if (element instanceof LaunchConfigurationWrapper) {
	        	LaunchConfigurationWrapper configWrapper = (LaunchConfigurationWrapper)element;
	            return configWrapper.getMode();
	        }
	        return null;
	    }
	     
	    @Override
	    protected void setValue(Object element, Object value) {
	        if (element instanceof LaunchConfigurationWrapper) {
	        	LaunchConfigurationWrapper configWrapper = (LaunchConfigurationWrapper) element;
                configWrapper.setMode((String) value);
                this.getViewer().update(element, null);
	        }
	    }
	     
	}	
	
	/**
	 * Adds/removes checked tree elements to/from selected launch configurations table
	 */
	private class LaunchConfigurationTreeCheckListener implements ICheckStateListener{
		public void checkStateChanged(CheckStateChangedEvent event) {
			Object element = event.getElement();
			
			try{
				if(element instanceof ILaunchConfiguration){
					if(event.getChecked())
						tableViewer.add(new LaunchConfigurationWrapper((ILaunchConfiguration)element));
					else
						removeSelectedLaunchConfigFromTable((ILaunchConfiguration)element);
				}
				else if(element instanceof ILaunchConfigurationType){
					TreeItem item = (TreeItem) treeViewer.testFindItem(element);
					TreeItem[] launchConfigItems = item.getItems();
					if(event.getChecked())
						for(int i = 0; i < launchConfigItems.length; i++)//getData can be null
							tableViewer.add(new LaunchConfigurationWrapper((ILaunchConfiguration) launchConfigItems[i].getData()));
					else
						for(int i = 0; i < launchConfigItems.length; i++)
							removeSelectedLaunchConfigFromTable((ILaunchConfiguration)launchConfigItems[i].getData());
						
				}
			}
			catch(CoreException ce){
				DebugUIPlugin.log(ce);
				MessageDialog.openError(getShell(), LaunchConfigurationsMessages.LaunchConfigurationDialog_Error_19, ce.toString());
				//cause can be thrown only when checked
				((ContainerCheckedTreeViewer)treeViewer).setChecked(event.getElement(), false);
			}
		}
		
	}

		
}
