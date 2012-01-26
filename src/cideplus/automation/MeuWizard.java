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
	private IProject p;
	private WizardPagina1 p1;
	private WizardPagina2 p2;
	private TreeSet<String> seeds;
	private ArrayList<IPackageFragment> pacotes;
	public int feature = -1;
	
	public MeuWizard(IProject project, ArrayList<IPackageFragment> pacotes, TreeSet<String> seeds) {
		this.p = project;
		this.p1 = new WizardPagina1("SelectFeatures", this.p);
		this.p2 = new WizardPagina2("SelectFeatures2", this.p);
		this.seeds = seeds;		
		this.pacotes = pacotes;
		addPage(this.p1);		
		addPage(this.p2);	
	}

	public boolean performFinish() {
		if(this.p1.combo.getSelectionIndex() > -1 && this.p2.tree.getSelectionCount() > 0) {
			this.feature = this.p1.combo.getSelectionIndex();
			this.seeds.clear();
			for(TreeItem i : this.p2.tree.getSelection()) {
				if(i.getData() instanceof String)
					this.seeds.add((String)i.getData());
				else if(i.getData() instanceof IPackageFragmentRoot) {
					MessageDialog.openInformation(this.getShell(), "Semi-automatic Feature Extraction", "PackageRoot not supported yet!");
				}
				else if(i.getData() instanceof IPackageFragment) {
					pacotes.add((IPackageFragment)i.getData());
				}
			}			
		}
		return true;
	}
}
