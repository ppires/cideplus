package cideplus.ui.configuration.dialogs;

import static org.eclipse.jface.dialogs.MessageDialog.openConfirm;

import java.io.IOException;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.action.ControlContribution;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import cideplus.model.Feature;
import cideplus.ui.configuration.FeaturesConfigurationUtil;
import cideplus.ui.configuration.FeaturesManager;

/**
 * Caixa de diálogo para configuração das features do projeto.
 * @see ConfigureFeaturesDialog.configure
 * @author rogel
 *
 */
public class ConfigureFeaturesDialog extends Dialog {

	private IJavaProject javaProject;
	protected Set<Feature> features;

	private FeaturesManager featuresManager;
	protected TableViewer tableViewer;
	protected Table table;


	public ConfigureFeaturesDialog(Shell parentShell, IJavaProject project) {
		super(parentShell);
		javaProject = project;
		featuresManager = FeaturesConfigurationUtil.getFeaturesManager(javaProject.getProject());
	}

	public FeaturesManager getFeaturesManager() {
		return featuresManager;
	}
	
	
	/**
	 * Mostra a caixa de diálogo para configuração das features do projeto
	 * @throws CoreException 
	 * @throws IOException 
	 */
	public void configure() throws CoreException, IOException {
		features = featuresManager.getFeatures();
		if(features.size() == 0){
			features.add(new Feature(1));
			features.add(new Feature(2));
			features.add(new Feature(3));
			features.add(new Feature(4));
			features.add(new Feature(5));
		}
		setBlockOnOpen(true);
		if(open() == Window.OK){
			featuresManager.saveFeatures(features);
			//MessageDialog.openInformation(getShell(), "Featurer", "Features configuration saved successfully.");
		}
	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,	true);		
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL,	true);		
	}
	
	@Override
	protected boolean isResizable() {
		return true;
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Configure Features");
	}
	
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite comp = (Composite) super.createDialogArea(parent);
		createToolBar(comp);
		createTable(comp);
				
		return comp;
	}

	/* 
	 * CÓDIGO DE CRIAÇÃO DOS COMPONENTES DA VIEW
	 */
	
	protected void createToolBar(Composite comp) {
		ToolBarManager toolBarManager = new ToolBarManager();
		toolBarManager.add(new ControlContribution("Custom") {
			protected Control createControl(Composite parent) {
				SashForm sf = new SashForm(parent, SWT.NONE);
				Button b1 = new Button(sf, SWT.PUSH);
				b1.setText("Add");
				b1.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						int i = 0;
						for (Feature f : features) {
							i = f.getId();
						}
						features.add(new Feature(i + 1));
						tableViewer.refresh();
					}
				});
				Button b2 = new Button(sf, SWT.PUSH);
				b2.setText("Remove");
				b2.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						if(tableViewer.getSelection() instanceof IStructuredSelection){
							Object firstElement = ((IStructuredSelection)tableViewer.getSelection()).getFirstElement();
							if(firstElement instanceof Feature){
								Feature feature = (Feature) firstElement;
								if(openConfirm(getShell(), "Remove Feature", "Do you really want to remove "+feature.getName()+"?")){
									features.remove(feature);
									tableViewer.refresh();
								}
							}
						}
					}
				});
				return sf;
			}
		});
		toolBarManager.createControl(comp);
	}
	
	protected void createTable(Composite comp) {
		int style = SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.HIDE_SELECTION;
		this.table = new Table(comp, style);
		this.tableViewer = new TableViewer(table);
		GridData gd = getGridData();
		table.setLayoutData(gd);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		configureColumns();
		//createTableViewerColumn(tableViewer, "Id", 50, "id");
		//createTableViewerColumn(tableViewer, "Feature", 200, "name");
		//createTableViewerColumn(tableViewer, "Color", 120, "rgb");
		
		configureTableViewer();
		tableViewer.refresh();
	}

	protected void configureColumns() {
		createColumn(table, "Id", 50);
		createColumn(table, "Feature", 200);
		createColumn(table, "Color", 120);
	}

	protected void configureTableViewer() {
		tableViewer.setContentProvider(new ConfigureFeaturesTableViewer());
		tableViewer.setLabelProvider(getLabelProvider());
		tableViewer.setCellModifier(getCellModifier());
		tableViewer.setCellEditors(getCellEditors(table));
		tableViewer.setColumnProperties(new String[]{"id", "name", "rgb"});
		tableViewer.setInput(features);
	}

	protected IBaseLabelProvider getLabelProvider() {
		return new ConfigureFeaturesLabelProvider(getShell().getDisplay());
	}

	protected ICellModifier getCellModifier() {
		return new ConfigureFeaturesCellModifier(tableViewer);
	}

	protected CellEditor[] getCellEditors(Table table) {
		return FeaturesDialogCellEditorProvider.getConfigurationCellEditors(table);
	}


	/* AUX */
	private GridData getGridData() {
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 400;
		return gd;
	}

	protected TableColumn createColumn(Table table, String title, int width) {
		TableColumn column = new TableColumn(table, SWT.LEFT);
		column.setText(title);
		column.setWidth(width);
		return column;
	}
	
	/*
	private void createTableViewerColumn(TableViewer tableViewer, String title, int width, final String property) {
		TableViewerColumn tableViewerColumn = new TableViewerColumn(tableViewer, SWT.LEFT);
		tableViewerColumn.setLabelProvider(new CellLabelProvider() {
			
			@Override
			public void update(ViewerCell cell) {
				Feature feature = (Feature) cell.getElement();
				if(property.equals("id")){
					cell.setText(feature.getId().toString());
				} else if(property.equals("name")){
					cell.setText(feature.getName());
				} else if(property.equals("rgb")){
					cell.setText("...");
				}
			}
		});
		tableViewerColumn.getColumn().setText(title);
		tableViewerColumn.getColumn().setWidth(width);
	}	*/
	/* AUX */

}



