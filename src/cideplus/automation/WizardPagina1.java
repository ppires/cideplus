package cideplus.automation;

import java.util.Set;

import javax.swing.JOptionPane;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import cideplus.model.Feature;
import cideplus.ui.configuration.FeaturesConfigurationUtil;
import cideplus.ui.configuration.FeaturesManager;

public class WizardPagina1 extends WizardPage {
	private IProject project;
	public Combo combo;
	public Label colorArea;

	public WizardPagina1(String pageName, IProject p) {
		super(pageName);
		this.setTitle("Semi-automatic Feature Extraction - Step 1");
		this.project = p;
	}
	
	public boolean canFlipToNextPage() {
		return (combo.getSelectionIndex() > -1);		
	}
    
	public void createControl(Composite parent) {
		IJavaProject jproject = null;
		final Composite composite = new Composite(parent, SWT.NONE);		

		try {
			jproject = (IJavaProject)project.getNature(JavaCore.NATURE_ID);
		} catch (CoreException e2) {
			e2.printStackTrace();
			return;
		}		

		composite.setLayout(null);
		final Button rename = new Button(composite, SWT.NONE);
		Label label = new Label(composite, SWT.NONE);
		label.setText("Select a feature to extract:");
		combo = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
		final FeaturesManager featuresManager = FeaturesConfigurationUtil.getFeaturesManager(project);
		combo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				getContainer().updateButtons();				
				int i = 0;
				for (cideplus.model.Feature feat : getSafeFeatures(featuresManager)) {
					if(i == combo.getSelectionIndex()){
						colorArea.setBackground(new Color(getShell().getDisplay(), FeaturesConfigurationUtil.getRGB(feat)));
					}
					i++;
				}
				//colorArea.setBackground(new Color(null, FeatureManager.getFeatures().get(combo.getSelectionIndex()).getRGB()));
				rename.setEnabled(combo.getSelectionIndex() > -1);
			}});
		for (cideplus.model.Feature feature : getSafeFeatures(featuresManager)) {
			combo.add(feature.getName());			
		}
		label.setBounds(30, 50, 150, 50);
		combo.setBounds(180, 47, 180, 50);
		colorArea = new Label(composite, SWT.BORDER);
		colorArea.setBounds(30, 150, 330, 200);
		Label label2 = new Label(composite, SWT.NONE);
		label2.setText("Associated Color:");
		label2.setBounds(30, 120, 100, 50);
		rename.setText("Rename");
		rename.setEnabled(false);
		rename.setBounds(365, 46, 60, 25);
		rename.addMouseListener(new MouseListener() {
			public void mouseDoubleClick(MouseEvent e) {}
			public void mouseDown(MouseEvent e) {}
			public void mouseUp(MouseEvent e) {
				String name = JOptionPane.showInputDialog("Enter the feature name:");
				if(name != null && !name.trim().equals("")) {
					//FeatureNameManager.getFeatureNameManager(project).
					//	setFeatureName(FeatureManager.getFeatures().get(combo.getSelectionIndex()), name.trim());
					//combo.setItem(combo.getSelectionIndex(), name.trim());
				}
			}});
		/*
		table = new Table(composite, SWT.CHECK | SWT.BORDER);
		FormData formData = new FormData();
		formData.top = new FormAttachment(label, 5);
		formData.bottom = new FormAttachment(100, 0);
		formData.right = new FormAttachment(100, 0);
		formData.left = new FormAttachment(0, 0);
		table.setLayoutData(formData);

		for (Feature feature : FeatureManager.getVisibleFeatures(project)) {
			TableItem item = new TableItem(table, SWT.NONE);
			item.setText(feature.getName(project));
			item.setData(feature);
			item.setChecked(selectAll || initialSelection.contains(feature));
		}
		*/
		setControl(composite);
	}

	private Set<Feature> getSafeFeatures(final FeaturesManager featuresManager){
		try {
			return featuresManager.getFeatures();
		} catch (Exception e) {
			MessageDialog.openError(getShell(), "Erro", "Erro ao ler as features do projeto");
			throw new RuntimeException(e);
		}
	}
	
/*
	public Set<Feature> getSelectedFeatures() {
		Set<Feature> result = new HashSet<Feature>();
		for (TableItem item : table.getItems()) {
			if (item.getChecked())
				result.add((Feature) item.getData());
		}
		return result;
	}

	public void setInitialSelection(Set<Feature> initialSelection) {
		if (initialSelection != null)
			this.initialSelection = initialSelection;
		else
			this.initialSelection = Collections.EMPTY_SET;
	}

	public void selectAll(boolean allSelected) {
		this.selectAll = allSelected;
	}
	*/
	}
