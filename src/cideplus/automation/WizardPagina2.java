package cideplus.automation;

import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import cideplus.FeaturerPlugin;
import cideplus.ui.configuration.FeaturesConfigurationUtil;
import cideplus.ui.configuration.ICompilationUnitFeaturesManager;

public class WizardPagina2 extends WizardPage {
	private final IProject project;
	private IJavaProject javaProject;
	public Tree tree, lstSeeds;

	public WizardPagina2(String pageName, IProject p) {
		super(pageName);
		this.setTitle("Semi-automatic Feature Extraction - Step 2");
		this.project = p;
	}

	@Override
	public boolean canFlipToNextPage() {
		return (false);
	}

	/*MÉTODOS UTILITARIOS*/
	private ICompilationUnitFeaturesManager getSafeCompilationUnit(ICompilationUnit cu) {
		try {
			return FeaturesConfigurationUtil.getFeaturesManager(project).getManagerForFile(cu);
		} catch (Exception e) {
			MessageDialog.openError(getShell(), "Erro", "Não foi possível criar o compilation unit para o arquivo. "+e.getMessage());
			throw new RuntimeException(e);
		}
	}

	private CompilationUnit getAst(ICompilationUnit cu) {
		return Util.getAst(cu);
	}

	public void createControl(Composite parent) {

		final Image img_comp_unit = getImage("/jcu_obj.gif");
		final Image img_src = getImage("/images/source_obj.gif");
		final Image img_lib = getImage("/images/library_obj.gif");
		final Image img_pkg = getImage("/images/package_obj.gif");
		final Image img_inter = getImage("/images/int_obj.gif");
		final Image img_classe = getImage("/images/class_obj.gif");
		final Image img_mathpub = getImage("/images/methpub_obj.gif");
		final Image img_mathpro = getImage("/images/methpro_obj.gif");
		final Image img_mathpri = getImage("/images/methpri_obj.gif");
		final Image img_field_pub = getImage("/images/field_public_obj.gif");
		final Image img_field_pri = getImage("/images/field_private_obj.gif");

		final Composite composite = new Composite(parent, SWT.NONE);

		javaProject = JavaCore.create(project);

		//		javaProject = null;
		//
		//		try {
		//			javaProject = (IJavaProject) project.getNature(JavaCore.NATURE_ID);
		//		} catch (CoreException e2) {
		//			e2.printStackTrace();
		//			return;
		//		}


		composite.setLayout(null);
		Label label = new Label(composite, SWT.NONE);
		label.setText("Select the seeds for feature extraction:");
		label.setBounds(30, 30, 230, 50);

		tree = new Tree(composite, SWT.MULTI | SWT.BORDER);

		// Tree root. The project.
		final TreeItem root = new TreeItem(tree, SWT.NULL);
		root.setText(project.getName());
		root.setGrayed(true);
		root.setImage(new Image(null, FeaturerPlugin.getFile("/images/prj_obj.gif")));


		try {
			for (IPackageFragmentRoot packageRoot : javaProject.getAllPackageFragmentRoots()) {
				if (!packageRoot.isArchive()) {
					TreeItem pkgRootItem = new TreeItem(root, SWT.NONE);
					pkgRootItem.setText(packageRoot.getElementName());
					pkgRootItem.setImage(img_src);

					for (IJavaElement packageFragment : packageRoot.getChildren()) {
						if (packageFragment.getElementType() == IJavaElement.PACKAGE_FRAGMENT) {
							IPackageFragment pkg = (IPackageFragment) packageFragment;
							List<ICompilationUnit> compUnits = Arrays.asList(pkg.getCompilationUnits());
							if (!compUnits.isEmpty()) {
								TreeItem pkgItem = new TreeItem(pkgRootItem, SWT.NONE);
								pkgItem.setText(pkg.getElementName());
								pkgItem.setImage(img_pkg);

								for(ICompilationUnit compUnit : compUnits) {
									TreeItem compUnitItem = new TreeItem(pkgItem, SWT.NONE);
									compUnitItem.setText(compUnit.getElementName());
									compUnitItem.setImage(img_comp_unit);

									//									CompilationUnit ast = getAst(compUnit);


								}
							}
						}
					}
				}
			}
		} catch (JavaModelException e2) {
			e2.printStackTrace();
		}


		//		tree.addListener(SWT.Expand, new Listener() {
		//
		//			public void handleEvent(Event event) {
		//				final TreeItem treeItem = (TreeItem) event.item;
		//				if(treeItem == null || treeItem.getItems().length > 1) {
		//					System.out.println("item == null || item.getItems().length > 1 is true");
		//					return;
		//				}
		//
		//				composite.getDisplay().asyncExec(new Runnable() {
		//					public void run() {
		//						System.out.println("Running asyncExec!");
		//						try {
		//
		//							// Item é a pasta que contem packages. O default do eclipse é src.
		//							if (treeItem.getData() instanceof IPackageFragmentRoot) {
		//								System.out.println("item.getData() instanceof IPackageFragmentRoot");
		//								IPackageFragmentRoot ipfr = (IPackageFragmentRoot)treeItem.getData();
		//								System.out.println("IPackageFragmentRoot: '" + ipfr + "'");
		//								TreeSet<Item> aux = new TreeSet<Item>();
		//
		//								tree.setCursor(new Cursor(null, SWT.CURSOR_WAIT));
		//								for(IJavaElement e : ipfr.getChildren()) {
		//									if (e.getElementType() == IJavaElement.PACKAGE_FRAGMENT) {
		//										IPackageFragment pkg = (IPackageFragment) e;
		//										System.out.println("package: '" + pkg + "'");
		//										if (pkg.getCompilationUnits().length == 0 && pkg.getClassFiles().length == 0) {
		//											System.out.println("pkg.getCompilationUnits().length == 0 && pkg.getClassFiles().length == 0");
		//											continue;
		//										}
		//										Item ti = new Item();
		//										if(pkg.isDefaultPackage())
		//											ti.setText("(default package)");
		//										else
		//											ti.setText(pkg.getElementName());
		//
		//										System.out.println("item text: " + ti.getText());
		//
		//										ti.setData(pkg);
		//										ti.setImage(img_pkg);
		//										aux.add(ti);
		//									}
		//								}
		//								//								treeItem.removeAll();
		//								addItems(aux, treeItem, true);
		//								tree.setCursor(new Cursor(null, SWT.CURSOR_ARROW));
		//							}
		//							else if (treeItem.getData() instanceof IPackageFragment) {
		//								System.out.println("item.getData() instanceof IPackageFragment");
		//								IPackageFragment pkg = (IPackageFragment)treeItem.getData();
		//								TreeSet<Item> aux = new TreeSet<Item>();
		//
		//								tree.setCursor(new Cursor(null, SWT.CURSOR_WAIT));
		//
		//								TIPO SOURCE
		//								if(pkg.getKind() ==	IPackageFragmentRoot.K_SOURCE) {
		//									for(ICompilationUnit cu : pkg.getCompilationUnits()) {
		//										ICompilationUnitFeaturesManager cuManager = getSafeCompilationUnit(cu);
		//										//IColoredJavaSourceFile source = ColoredJavaSourceFile.getColoredJavaSourceFile(cu);
		//										CompilationUnit ast = getAst(cu);
		//
		//										//FIXME Não necessariamente é um TypeDeclaration, pode ser um EnumDeclaration
		//										for(TypeDeclaration t : (java.util.List<TypeDeclaration>)ast.types()) {
		//											Item i = new Item();
		//											if(t.getSuperclassType() != null)
		//												i.setText(t.getName().getIdentifier() + ": " + t.getSuperclassType().toString());
		//											else
		//												i.setText(t.getName().getIdentifier() + ": Object");
		//											if(t.isInterface()) {
		//												i.setData2("3" + t.resolveBinding().getKey());
		//												i.setImage(img_inter);
		//											}
		//											else {
		//												i.setData2("2" + t.resolveBinding().getKey());
		//												i.setImage(img_classe);
		//											}
		//
		//											TreeSet<Item> aux2 = new TreeSet<Item>();
		//											for(BodyDeclaration bd : (java.util.List<BodyDeclaration>)t.bodyDeclarations()) {
		//												switch(bd.getNodeType()) {
		//												case ASTNode.METHOD_DECLARATION: {
		//													Item it = new Item();
		//													MethodDeclaration md = (MethodDeclaration)bd;
		//													String temp = "";
		//													for(SingleVariableDeclaration svd : (java.util.List<SingleVariableDeclaration>) md.parameters()) {
		//														temp += svd.getType().toString() + ", ";
		//													}
		//													if(!temp.equals(""))
		//														temp =  temp.substring(0, temp.length() - 2);
		//													if(md.getReturnType2() != null)
		//														it.setText(md.getName().getFullyQualifiedName() + "(" + temp + "): " + md.getReturnType2().toString());
		//													else
		//														it.setText(md.getName().getFullyQualifiedName() + "(" + temp+ ")");
		//													if((md.getModifiers() & Flags.AccPublic) == Flags.AccPublic)
		//														it.setImage(img_mathpub);
		//													else if((md.getModifiers() & Flags.AccProtected) == Flags.AccProtected)
		//														it.setImage(img_mathpro);
		//													else
		//														it.setImage(img_mathpri);
		//													it.setData("4" + md.resolveBinding().getKey());
		//													aux2.add(it);
		//													break;
		//												}
		//
		//												case ASTNode.FIELD_DECLARATION: {
		//													FieldDeclaration fd = (FieldDeclaration)bd;
		//													java.util.List<VariableDeclarationFragment> frag = fd.fragments();
		//
		//													for(VariableDeclarationFragment vd : frag) {
		//														Item it = new Item();
		//														it.setText(vd.getName().getFullyQualifiedName() + ": " + fd.getType().toString());
		//														it.setData("5" + vd.resolveBinding().getKey());
		//														if((fd.getModifiers() & Flags.AccPublic) == Flags.AccPublic)
		//															it.setImage(img_field_pub);
		//														else
		//															it.setImage(img_field_pri);
		//														aux2.add(it);
		//													}
		//													break;
		//												}
		//
		//												case ASTNode.INITIALIZER: {
		//													break;
		//												}
		//												}
		//											}
		//											i.setData(aux2);
		//											aux.add(i);
		//										}
		//									}
		//									treeItem.removeAll();
		//									for(Item i : aux) {
		//										TreeItem ti = new TreeItem(treeItem, SWT.NULL);
		//										ti.setText(i.getText());
		//										ti.setData(i.getData2());
		//										ti.setImage(i.getImage());
		//										addItems((TreeSet<Item>)i.getData(), ti, false);
		//									}
		//								}
		//								TIPO BINARY
		//								else {
		//									for(IClassFile cu : pkg.getClassFiles()) {
		//										IType t = cu.findPrimaryType();
		//										if(t == null)
		//											continue;
		//										Item i = new Item();
		//										String name = "(Anonymous)";
		//										if(!t.getElementName().equals(""))
		//											name = t.getElementName();
		//
		//										if(t.getSuperclassName() != null)
		//											i.setText(name + ": " + t.getSuperclassName());
		//										else
		//											i.setText(name + ": (Unknow)");
		//										if(t.isInterface()) {
		//											i.setImage(img_inter);
		//											i.setData2("3" + t.getKey());
		//										}
		//										else {
		//											i.setImage(img_classe);
		//											i.setData2("2" + t.getKey());
		//										}
		//										TreeSet<Item> aux2 = new TreeSet<Item>();
		//										for(IField fd : t.getFields()) {
		//											Item it = new Item();
		//
		//											it.setText(fd.getElementName() + ": " + fd.getTypeSignature());
		//
		//											it.setData("5" + fd.getKey());
		//											if((fd.getFlags() & Flags.AccPublic) == Flags.AccPublic)
		//												it.setImage(img_field_pub);
		//											else
		//												it.setImage(img_field_pri);
		//											aux2.add(it);
		//										}
		//
		//										for(IMethod md : t.getMethods()) {
		//											Item it = new Item();
		//											String temp = "";
		//
		//											for(String svd : md.getParameterTypes()) {
		//												temp += svd + ", ";
		//											}
		//											if(!temp.equals(""))
		//												temp =  temp.substring(0, temp.length() - 2);
		//											it.setText(md.getElementName() + "(" + temp + "): " + md.getReturnType());
		//
		//											if((md.getFlags() & Flags.AccPublic) == Flags.AccPublic)
		//												it.setImage(img_mathpub);
		//											else if((md.getFlags() & Flags.AccProtected) == Flags.AccProtected)
		//												it.setImage(img_mathpro);
		//											else
		//												it.setImage(img_mathpri);
		//											it.setData("4" + md.getKey());
		//											aux2.add(it);
		//										}
		//										i.setData(aux2);
		//										aux.add(i);
		//									}
		//									treeItem.removeAll();
		//									for(Item i : aux) {
		//										TreeItem ti = new TreeItem(treeItem, SWT.NULL);
		//										ti.setText(i.getText());
		//										ti.setData(i.getData2());
		//										ti.setImage(i.getImage());
		//										addItems((TreeSet<Item>)i.getData(), ti, false);
		//									}
		//								}
		//								tree.setCursor(new Cursor(null, SWT.CURSOR_ARROW));
		//							}
		//						} catch(CoreException e1) {
		//							tree.setCursor(new Cursor(null, SWT.CURSOR_ARROW));
		//							e1.printStackTrace();;
		//						}
		//					}
		//
		//
		//				});
		//			}});

		/*		parent.getDisplay().asyncExec(new Runnable() {
			public void run() {
				try {
					for(IPackageFragmentRoot r: javaProject.getAllPackageFragmentRoots()) {
						TreeItem nivel0 = new TreeItem(root, SWT.NULL);
						if(r.getElementName().trim().equals(""))
							nivel0.setText("(src)");
						else
							nivel0.setText(r.getElementName());
						nivel0.setData(r);
						//nivel0.setData("0" + r.getElementName().replaceAll("\\.", "/"));
						nivel0.setImage(img_lib);
						if(r.getKind() == IPackageFragmentRoot.K_SOURCE){
							nivel0.setImage(img_src);
						}
						new TreeItem(nivel0, SWT.NULL);
					}*/
		//					for(IPackageFragment pkg : javaProject.getPackageFragments()) {
		//						if(pkg.getCompilationUnits().length == 0)
		//							continue;
		//						TreeItem nivel1 = new TreeItem(root, SWT.NULL);
		//						if(pkg.isDefaultPackage())
		//							nivel1.setText("(default package)");
		//						else
		//							nivel1.setText(pkg.getElementName());
		//						nivel1.setData("1" + pkg.getElementName().replaceAll("\\.", "/"));
		//						nivel1.setImage(img_pkg);
		//
		//						for(ICompilationUnit cu : pkg.getCompilationUnits()) {
		//							IColoredJavaSourceFile source = ColoredJavaSourceFile.getColoredJavaSourceFile(cu);
		//
		//							for(TypeDeclaration t : (java.util.List<TypeDeclaration>)source.getAST().types()) {
		//								TreeItem nivel2 = new TreeItem(nivel1, SWT.NULL);
		//								if(t.getSuperclassType() != null)
		//									nivel2.setText(t.getName().getIdentifier() + ": " + t.getSuperclassType().toString());
		//								else
		//									nivel2.setText(t.getName().getIdentifier() + ": Object");
		//								if(t.isInterface()) {
		//									nivel2.setData("3" + t.resolveBinding().getKey());
		//									nivel2.setImage(img_inter);
		//								}
		//								else {
		//									nivel2.setData("2" + t.resolveBinding().getKey());
		//									nivel2.setImage(img_classe);
		//								}
		//
		//								for(BodyDeclaration bd : (java.util.List<BodyDeclaration>)t.bodyDeclarations()) {
		//									switch(bd.getNodeType()) {
		//									case ASTNode.METHOD_DECLARATION: {
		//										TreeItem nivel3 = new TreeItem(nivel2, SWT.NULL);
		//										MethodDeclaration md = (MethodDeclaration)bd;
		//										String aux = "";
		//										for(SingleVariableDeclaration svd : (java.util.List<SingleVariableDeclaration>) md.parameters()) {
		//										   aux += svd.getType().toString() + ", ";
		//										}
		//										if(!aux.equals(""))
		//											aux =  aux.substring(0, aux.length() - 2);
		//										if(md.getReturnType2() != null)
		//											nivel3.setText(md.getName().getFullyQualifiedName() + "(" + aux + "): " + md.getReturnType2().toString());
		//										else
		//											nivel3.setText(md.getName().getFullyQualifiedName() + "(" + aux + ")");
		//										if((md.getModifiers() & Flags.AccPublic) == Flags.AccPublic)
		//											nivel3.setImage(img_mathpub);
		//										else if((md.getModifiers() & Flags.AccProtected) == Flags.AccProtected)
		//											nivel3.setImage(img_mathpro);
		//										else
		//											nivel3.setImage(img_mathpri);
		//										nivel3.setData("4" + md.resolveBinding().getKey());
		//										break;
		//									}
		//
		//									case ASTNode.FIELD_DECLARATION: {
		//										FieldDeclaration fd = (FieldDeclaration)bd;
		//										java.util.List<VariableDeclarationFragment> frag = fd.fragments();
		//
		//										for(VariableDeclarationFragment vd : frag) {
		//											TreeItem nivel3 = new TreeItem(nivel2, SWT.NULL);
		//											nivel3.setText(vd.getName().getFullyQualifiedName() + ": " + fd.getType().toString());
		//											nivel3.setData("5" + vd.resolveBinding().getKey());
		//											if((fd.getModifiers() & Flags.AccPublic) == Flags.AccPublic)
		//												nivel3.setImage(img_field_pub);
		//											else
		//												nivel3.setImage(img_field_pri);
		//										}
		//										break;
		//									}
		//
		//									case ASTNode.INITIALIZER: {
		//										break;
		//									}
		//									}
		//								}
		//							}
		//						}
		//					}
		/*				} catch (CoreException e1) {
					tree.setCursor(new Cursor(null, SWT.CURSOR_ARROW));
					e1.printStackTrace();
				}
			}});*/

		tree.setBounds(30, 80, 450, 300);

		/*
		final Text txtRegExp = new Text(composite, SWT.SINGLE | SWT.BORDER);
		txtRegExp.setBounds(30, 400, 240, 22);
		txtRegExp.setText(".*");
		txtRegExp.setTextLimit(80);

		lstSeeds = new Tree(composite, SWT.MULTI | SWT.BORDER);
		lstSeeds.setBounds(330, 80, 240, 300);

		Button btnAdd = new Button(composite, SWT.NONE);
		btnAdd.setText("=>");
		btnAdd.addMouseListener(new MouseListener() {

			public void search(TreeItem t, Tree lst, String pattern) {
				for(TreeItem i : t.getItems()) {
					int aux;
					aux = i.getText().indexOf('(');
					if(aux == -1) {
						aux = i.getText().indexOf(':');
						if(aux == -1)
							aux = i.getText().length();
					}

					if(i.getText().substring(0, aux).matches(pattern)) {
						TreeItem item = new TreeItem(lst, SWT.NULL);
						item.setText(i.getText());
						item.setData(i.getData());
						item.setImage(i.getImage());
					}
					else
						search(i, lst, pattern);
				}
			}
			public void mouseDoubleClick(MouseEvent e) {}
			public void mouseDown(MouseEvent e) {}
			public void mouseUp(MouseEvent e) {
				lstSeeds.removeAll();

				Arrays.sort(lstSeeds.getItems());
				for(TreeItem i : tree.getItems()) {
					search(i, lstSeeds, txtRegExp.getText());
				}
//				for(TreeItem i : tree.getSelection()) {
//					System.out.println(i.getData().toString());
//				}
			}
		});
		btnAdd.setBounds(280, 210, 40, 20);
		 */
		setControl(composite);
	}

	public Image getImage(String path) {
		return new Image(getShell().getDisplay(), FeaturerPlugin.getFile(path));
	}


	public void addItems(TreeSet<Item> aux, TreeItem item, boolean add) {
		for(Item i : aux) {
			TreeItem ti = new TreeItem(item, SWT.NULL);
			ti.setText(i.getText());
			ti.setData(i.getData());
			ti.setImage(i.getImage());
			if(add)
				new TreeItem(ti, SWT.NULL);
		}
		aux.clear();
	}
}

class Item implements Comparable<Item> {
	private String text;
	private Object data;
	private Object data2;

	public Object getData2() {
		return data2;
	}

	public void setData2(Object data2) {
		this.data2 = data2;
	}

	private Image image;

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public Image getImage() {
		return image;
	}

	public void setImage(Image image) {
		this.image = image;
	}

	public int compareTo(Item o) {
		return text.compareTo(o.text);
	}
}