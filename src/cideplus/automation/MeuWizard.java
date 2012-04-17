package cideplus.automation;

import java.util.ArrayList;
import java.util.TreeSet;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.TreeItem;

public class MeuWizard extends Wizard {
	private final IProject project;
	private final WizardPagina1 page1;
	private final WizardPagina2 page2;
	private final TreeSet<String> seeds;
	private final ArrayList<IPackageFragment> pacotes;
	public int feature = -1;

	public MeuWizard(IProject project, ArrayList<IPackageFragment> pacotes, TreeSet<String> seeds) {
		this.project = project;
		this.page1 = new WizardPagina1("SelectFeatures", this.project);
		this.page2 = new WizardPagina2("SelectFeatures2", this.project);
		this.seeds = seeds;
		this.pacotes = pacotes;
		addPage(this.page1);
		addPage(this.page2);
	}

	@Override
	public boolean performFinish() {
		if(this.page1.featuresCombo.getSelectionIndex() > -1 && this.page2.tree.getSelectionCount() > 0) {
			this.feature = this.page1.featuresCombo.getSelectionIndex();
			this.seeds.clear();
			for(TreeItem i : this.page2.tree.getSelection()) {
				if(i.getData() instanceof String) {
					this.seeds.add((String)i.getData());
					return true;
				}
				else if(i.getData() instanceof IPackageFragment) {
					pacotes.add((IPackageFragment)i.getData());
					return true;
				}
				else if(i.getData() instanceof IPackageFragmentRoot) {
					MessageDialog.openInformation(this.getShell(), "Semi-automatic Feature Extraction", "PackageRoot not supported yet!");
					return false;
				}
				else if(i.getData() instanceof IProject) {
					MessageDialog.openInformation(this.getShell(), "Semi-automatic Feature Extraction", "Project not supported.\n\nYou can only select packages, classes, methods, or fields.");
					return false;
				}
			}
		}
		else {
			MessageDialog.openInformation(this.getShell(), "Semi-automatic Feature Extraction", "Please, select one seed at least!");
			return false;
		}
		return true;
	}
}
