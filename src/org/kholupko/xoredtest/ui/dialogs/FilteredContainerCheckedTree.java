package org.kholupko.xoredtest.ui.dialogs;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.debug.internal.core.IInternalDebugCoreConstants;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.dialogs.ContainerCheckedTreeViewer;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;

/**
 * Allows to filter ContainerCheckedTree correctly, without loosing checked elements
 * @author A. Kholupko
 *
 */
public class FilteredContainerCheckedTree extends FilteredTree {

	
	private Set<Object> checkedItemsSet = new HashSet<Object>();

	public FilteredContainerCheckedTree(Composite parent, int treeStyle,
			PatternFilter filter, boolean useNewLook) {
		super(parent, treeStyle, filter, useNewLook);

	}

	protected class ImprovedContainerCheckedTreeViewer extends ContainerCheckedTreeViewer{

		public ImprovedContainerCheckedTreeViewer(Composite parent, int style) {
			super(parent, style);
			
			addDoubleClickListener(new IDoubleClickListener() {
				@Override
				public void doubleClick(DoubleClickEvent event) {
					ContainerCheckedTreeViewer viewer = (ContainerCheckedTreeViewer) event.getViewer();
					IStructuredSelection thisSelection = (IStructuredSelection) event.getSelection(); 
				    Object selectedNode = thisSelection.getFirstElement();
				    boolean checked = viewer.getChecked(selectedNode);
					
				    viewer.setChecked(selectedNode, !checked);
					
					fireCheckStateChanged(selectedNode, !checked);
				}
			});
		}
	
		public void fireCheckStateChanged(Object element, boolean state) {
			fireCheckStateChanged(new CheckStateChangedEvent(this, element, state));
		}		
		
	}
	
	@Override
	protected TreeViewer doCreateTreeViewer(Composite parent, int style) {
		
		ImprovedContainerCheckedTreeViewer checkedTreeViewer = new ImprovedContainerCheckedTreeViewer(treeComposite, style);
		
		Tree tree = checkedTreeViewer.getTree();
		tree.setLayout(new GridLayout());
		tree.setLayoutData(new GridData(GridData.FILL_BOTH));
		tree.setFont(parent.getFont());		
		
		tree.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		
		
		checkedTreeViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				if (!event.getChecked() && checkedItemsSet != null) {

					Object element = event.getElement();
					
					//if(checkState.contains(element))
						checkedItemsSet.remove(element);
					
					TreeItem item = (TreeItem) treeViewer.testFindItem(element);
					
					int childrenNumber = item.getItems().length;
					//save only leafs
					if(item != null && childrenNumber != 0){
						TreeItem[] children = item.getItems();
						for(int i = 0; i < childrenNumber; i++){
							TreeItem treeElement = children[i];
							checkedItemsSet.remove(treeElement.getData());
							
							//continue recursively for deeper trees 
						}
					}							
					
				} else if (event.getChecked()) {
					rememberLeafCheckState();
				}				
			}
		});
		return checkedTreeViewer;
	}
	
	
	/**
	 * saves checked tree state, intended to be invoked before filtering
	 */
	protected void rememberLeafCheckState() {
		
		Object[] currentCheckedElements = ((ContainerCheckedTreeViewer) treeViewer).getCheckedElements();

		for (int i = 0; i < currentCheckedElements.length; i++)
			if (!((ContainerCheckedTreeViewer) treeViewer).getGrayed(currentCheckedElements[i])){
				TreeItem item = (TreeItem) treeViewer.testFindItem(currentCheckedElements[i]);

				if (item != null && item.getItems().length == 0)
					if (!checkedItemsSet.contains(currentCheckedElements[i]))
						checkedItemsSet.add(currentCheckedElements[i]);
				}
	}

	/**
	 * restores checked tree state, intended to be invoked after filtering
	 */
	protected void restoreLeafCheckState() {
		if (treeViewer == null || treeViewer.getTree().isDisposed())
			return;
		if (checkedItemsSet == null || checkedItemsSet.isEmpty())
			return;

		// clearing
		((ContainerCheckedTreeViewer) treeViewer).setCheckedElements(new Object[0]);
		((ContainerCheckedTreeViewer) treeViewer).setGrayedElements(new Object[0]);

		// setting leafs checked => parents auto

		((ContainerCheckedTreeViewer) treeViewer).expandAll();
		
		for(Object element : checkedItemsSet){
			TreeItem item = (TreeItem) treeViewer.testFindItem(element);
				
			if(item != null && item.getItems().length == 0)
				((ContainerCheckedTreeViewer) treeViewer).setChecked(element, true);
		}

	}

	
	@Override
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.FilteredTree#textChanged()
	 * have to save checked elements before filtering
	 */
	protected void textChanged() {
		rememberLeafCheckState();
		String text = getFilterString();
		if(text.equals(IInternalDebugCoreConstants.EMPTY_STRING)) {
			//fix strange reaction - unexpanded items with null data
			getPatternFilter().setPattern(null);
			treeViewer.refresh();
			treeViewer.expandAll();
			restoreLeafCheckState();
			return;
		}
		super.textChanged();
	}
	
	@Override
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.FilteredTree#updateToolbar(boolean)
	 * this method is guaranteed to be invoked after filtering, so restore checked elements
	 */
	protected void updateToolbar(boolean visible) {
		super.updateToolbar(visible);
		restoreLeafCheckState();
	}
	
	
	

	

}
