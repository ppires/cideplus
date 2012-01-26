package cideplus.automation;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression.Operator;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import cideplus.FeaturerPlugin;
import cideplus.model.Feature;
import cideplus.ui.configuration.CompilationUnitFeaturesManager;
import cideplus.ui.configuration.FeaturesConfigurationUtil;
import cideplus.ui.configuration.FeaturesManager;

public class ColorDetectionAction implements IObjectActionDelegate {

	private Shell shell;
	private ISelection selection;
	private TreeSet<String> seeds = new TreeSet<String>();
	private ArrayList<IPackageFragment> pacotes = new ArrayList<IPackageFragment>();
	private int contSE1, contSE2, contSE3, contSE4, contSE5, contSE6;
	private HashSet<String> se3;
	private PrintStream arqLog;
	private FeaturesManager manager;

	public ColorDetectionAction() {
		super();
	}
	/*M�TODOS UTILITARIOS*/
	private Set<Feature> getSafeFeatures(final FeaturesManager featuresManager){
		return Util.getSafeFeatures(featuresManager);
	}

	private CompilationUnitFeaturesManager getSafeManager(IJavaProject jproject, ICompilationUnit compUnit){
		try {
			if(manager == null){
				manager = FeaturesConfigurationUtil.getFeaturesManager(jproject.getProject());
			}
			return manager.getManagerForFile(compUnit);
		} catch (Exception e) {
			MessageDialog.openError(null, "Erro", "Erro ao ler as features do projeto. "+e.getMessage());
			throw new RuntimeException(e);
		}
	}

	boolean stopGrowingCache;
	int cacheUtilization = 0;
	private Map<ICompilationUnit, CompilationUnit> cacheAst;
	private CompilationUnit getAst(ICompilationUnit compUnit) {
		CompilationUnit ast;
		if(!stopGrowingCache){
			long totalMemory = Runtime.getRuntime().totalMemory();
			long maxMemory = Runtime.getRuntime().maxMemory();
			long freeMemory = Runtime.getRuntime().freeMemory();

			if(maxMemory - totalMemory < 350000000){//se a mem�ria ficar em menos de 350MB checar se precisa liberar o cache
				if(freeMemory < 450000000){//se tiver menos de 450mb liberar cache
					//parar de adicionar objetos ao cache.. est� com pouca mem�ria
					//usar apenas o que j� foi feito cache
					stopGrowingCache = true;
				}
			}
		}
		if((ast = cacheAst.get(compUnit)) == null){
			ast = Util.getAst(compUnit);
			if(!stopGrowingCache){
				cacheAst.put(compUnit, ast);
			}
		} else {
			cacheUtilization++;
		}
		return ast;
	}
	/*FIM M�TODOS UTILITARIOS*/

	public void run(IAction action) {
		try {
			System.gc();
			manager = null;
			cacheAst = new HashMap<ICompilationUnit, CompilationUnit>(50);
			cacheUtilization = 0;
			stopGrowingCache = false;
			executeColorDetection();
		} catch (Exception e) {
			MessageDialog.openError(null, "Erro", e.getMessage());
			throw new RuntimeException(e);
		}
	}

	private void executeColorDetection() {
		final IJavaProject jproject = getSelectedJavaProject();
		if (jproject == null) {
			MessageDialog.openInformation(shell, "Semi-automatic Feature Extraction", "No Java project selected.");
			return;
		}
		contSE1 = contSE2 = contSE3 = contSE4 = contSE5 = contSE6 = 0;
		se3 = new HashSet<String>();

		/* WIZARD  (preenchimento do combo com as features) */
		try {
			final IProject project = getSelectedJavaProject().getCorrespondingResource().getProject();
			seeds.clear();
			pacotes.clear();

			final MeuWizard wzd = new MeuWizard(project, pacotes, seeds);
			WizardDialog dlg = new WizardDialog(shell, wzd);
			dlg.setPageSize(400, 350);
			dlg.open();

			if(dlg.getReturnCode() == dlg.CANCEL)
				return;

			if((pacotes != null && pacotes.size() > 0) || (seeds != null && seeds.size() > 0)) {
				Job job = new WorkspaceJob("Semi-automatic Feature Extraction") {

					@Override
					public IStatus runInWorkspace(IProgressMonitor monitor)
							throws CoreException {
						try {
							monitor.beginTask("Detecting features...", IProgressMonitor.UNKNOWN);
							execute(jproject, project, wzd, monitor);
							monitor.done();
							//atualiza todos os editores abertos
							FeaturesConfigurationUtil.updateEditors(shell.getDisplay(), null);
						} catch(OperationCanceledException e){
							return new Status(IStatus.CANCEL, FeaturerPlugin.PLUGIN_ID, e.getMessage());
						} catch (Exception e) {
							e.printStackTrace();
							return new Status(IStatus.ERROR, FeaturerPlugin.PLUGIN_ID, e.getMessage());
						} finally {
							System.out.println(cacheUtilization);
							cacheAst = null;//zera o cache para liberar a mem�ria
							ColorDetectionAction.this.manager = null;
						}
						return new Status(IStatus.OK, FeaturerPlugin.PLUGIN_ID, "Finish!");
					}

				};
				job.setUser(true);
				job.schedule();
				shell.setCursor(new Cursor(null, SWT.CURSOR_ARROW));
			}
			else {
				shell.setCursor(new Cursor(null, SWT.CURSOR_ARROW));
				MessageDialog.openInformation(shell, "Semi-automatic Feature Extraction", "Please, select one seed at least!");
				return;
			}
		} catch (JavaModelException e) {
			shell.setCursor(new Cursor(null, SWT.CURSOR_ARROW));
			e.printStackTrace();
			return;
		}
		//MessageDialog.openInformation(shell, "Semi-automatic Feature Extraction", "Finish!");

	}

	private void execute(IJavaProject jproject, IProject project, MeuWizard wzd, IProgressMonitor monitor) {
		Calendar start = Calendar.getInstance();
		try {
			NumberFormat f = new DecimalFormat("00");
			arqLog = new PrintStream("log_" + start.get(Calendar.YEAR) + "_" + f.format((start.get(Calendar.MONTH) + 1)) + "_" +
					f.format(start.get(Calendar.DAY_OF_MONTH)) + "_" + f.format(start.get(Calendar.HOUR)) + "_" +
					f.format(start.get(Calendar.MINUTE)) + "_" + f.format(start.get(Calendar.SECOND)) +".log");
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			return;
		}

		FeaturesManager featuresManager = FeaturesConfigurationUtil.getFeaturesManager(project);
		Feature selectedFeature = getSelectedFeature(wzd, featuresManager);

		//		searchIFDEF(jproject, selectedFeature);

		System.out.println("----------------------------------------");
		System.out.println("Seeds:");
		arqLog.println("Seeds:");
		//    	for(IPackageFragment pacote : pacotes) {
		//    		System.out.println(pacote.getElementName());
		//    		arqLog.println(pacote.getElementName());
		//    	}
		//    	arqLog.println();
		//    	System.out.println("----------------------------------------");

		for(IPackageFragment pacote : pacotes) {
			searchPKG(jproject, pacote, selectedFeature, monitor);
		}

		for(String seed : seeds) {
			System.out.println(seed);
			arqLog.println(seed);
		}
		System.out.println("----------------------------------------");
		arqLog.println();
		//shell.setCursor(new Cursor(null, SWT.CURSOR_WAIT));

		monitor.setTaskName("Detecting features... Seeds: " + seeds);

		for(String seed : seeds) {
			if(seed.substring(0, 1).equals("2") || seed.substring(0, 1).equals("3"))
				searchIMP(jproject, seed.substring(1), selectedFeature, monitor);

			searchPROJ(jproject, seed.substring(1), selectedFeature, monitor);
		}

		Calendar end = Calendar.getInstance();
		end.add(Calendar.HOUR, -start.get(Calendar.HOUR));
		end.add(Calendar.MINUTE, -start.get(Calendar.MINUTE));
		end.add(Calendar.SECOND, -start.get(Calendar.SECOND));
		arqLog.println();
		System.out.println("Tempo de execu��o: " + end.get(Calendar.HOUR) + ":" + end.get(Calendar.MINUTE) + ":" + end.get(Calendar.SECOND));
		System.out.println("\nSE1: " + contSE1);
		System.out.println("SE2: " + contSE2);
		System.out.println("SE3: " + contSE3);
		System.out.println("SE3 diferentes: " + se3.size());
		System.out.println("SE4: " + contSE4);
		System.out.println("SE5: " + contSE5);
		System.out.println("SE6: " + contSE6);
		arqLog.println("Tempo de execu��o: " + end.get(Calendar.HOUR) + ":" + end.get(Calendar.MINUTE) + ":" + end.get(Calendar.SECOND));
		arqLog.println("SE1: " + contSE1);
		arqLog.println("SE2: " + contSE2);
		arqLog.println("SE3: " + contSE3);
		arqLog.println("SE3 diferentes: " + se3.size());
		arqLog.println("SE4: " + contSE4);
		arqLog.println("SE5: " + contSE5);
		arqLog.println("SE6: " + contSE6);
		//shell.setCursor(new Cursor(null, SWT.CURSOR_ARROW));
		arqLog.close();
	}

	private Feature getSelectedFeature(MeuWizard wzd,
			FeaturesManager featuresManager) {
		Feature selectedFeature = null;
		int i = 0;
		for (cideplus.model.Feature feat : getSafeFeatures(featuresManager)) {
			if(i == wzd.feature){
				selectedFeature = feat;
			}
			i++;
		}
		return selectedFeature;
	}

	public void searchPKG(IJavaProject jproject, IPackageFragment pkg, Feature feature, IProgressMonitor monitor) {
		try {
			System.out.println("Class: " + pkg.getClassFiles().length);
			System.out.println("CU: " + pkg.getCompilationUnits().length);
			monitor.setTaskName(""+pkg.getClassFiles().length+" classes, " +pkg.getCompilationUnits().length+" compilation units");
			if(pkg.getKind()==IPackageFragmentRoot.K_SOURCE) {
				/* Marca todas as CompilationUnits do pacote */
				System.out.print("Colorindo todas as CU do pacote " + pkg.getElementName() + "... ");
				for(ICompilationUnit compUnit : pkg.getCompilationUnits()) {
					CompilationUnitFeaturesManager managerForFile = getSafeManager(jproject, compUnit);
					CompilationUnit ast = getAst(compUnit);
					//IColoredJavaSourceFile source = ColoredJavaSourceFile.getColoredJavaSourceFile(compUnit);
					//IColorManager nodeColors = source.getColorManager();
					//nodeColors.beginBatch();
					managerForFile.setFeature(ast, feature);
					managerForFile.commitChanges();
					//ColoredIDEPlugin.getDefault().notifyListeners(new ColorChangedEvent(this, source.getAST(), source));
					//nodeColors.endBatch();
				}
				System.out.println("conclu�do!");
			}


			/* Marca os imports deste pacote <pacote.*> em todo o projeto */
			monitor.setTaskName("Marking imports of package "+pkg.getElementName());
			System.out.print("Colorindo todos os <import " + pkg.getElementName() + ".*>... ");
			searchIMP(jproject, pkg.getElementName().replaceAll("\\.", "/"), feature, monitor);
			System.out.println("conclu�do!");


			if (pkg.getKind() == IPackageFragmentRoot.K_SOURCE) {
				/* Procura usos das classes das compUnits em todo o projeto */
				System.out.println("Adicionando as classes do pacote " + pkg.getElementName() + " na lista de sementes: ");
				for (ICompilationUnit compUnit : pkg.getCompilationUnits()) {
					//CompilationUnitFeaturesManager managerForFile = getSafeManager(jproject, compUnit);
					CompilationUnit ast = getAst(compUnit);
					//IColoredJavaSourceFile source = ColoredJavaSourceFile.getColoredJavaSourceFile(compUnit);
					for (Object type : ast.types()) {
						TypeDeclaration td = (TypeDeclaration) type;
						ITypeBinding binding = td.resolveBinding();
						//searchPROJ(jproject, binding.getKey(), feature, monitor);
						if(td.isInterface())
							seeds.add("3" + binding.getKey());
						else
							seeds.add("2" + binding.getKey());
					}
				}
			} else {
				/* Procura usos das classes das compUnits em todo o projeto */
				System.out.println("Adicionando as classes do pacote " + pkg.getElementName() + " na lista de sementes: ");
				for (IClassFile cu : pkg.getClassFiles()) {
					IType t = cu.findPrimaryType();
					if(t.isInterface())
						seeds.add("3" + t.getKey());
					else
						seeds.add("2" + t.getKey());
					//searchIMP(jproject, t.getKey(), feature, monitor);
					//searchPROJ(jproject, t.getKey(), feature, monitor);
				}

			}
			System.out.println("Conclu�do pacote: " + pkg.getElementName());
		} catch(CoreException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}


	@SuppressWarnings("unchecked")
	public void searchPROJ(IJavaProject jproject, String name, Feature feature, IProgressMonitor monitor) {
		System.out.println(">> ENTROU searchPROJ: " + name);
		try {
			/* Para todo PACOTE do PROJETO fa�a: */
			IPackageFragment[] pkgs = jproject.getPackageFragments();
			for (IPackageFragment pkg : pkgs) {
				ICompilationUnit[] compUnits = pkg.getCompilationUnits();

				for (ICompilationUnit compUnit : compUnits) {
					CompilationUnitFeaturesManager managerForFile = getSafeManager(jproject, compUnit);
					CompilationUnit ast = getAst(compUnit);
					//IColoredJavaSourceFile source = ColoredJavaSourceFile.getColoredJavaSourceFile(compUnit);
					//IColorManager nodeColors = source.getColorManager();
					//nodeColors.beginBatch();

					for(AbstractTypeDeclaration type : (List<AbstractTypeDeclaration>)ast.types()) {
						monitor.setTaskName("Checking type ... PROJ "+type.getName());
						if(monitor.isCanceled()){
							throw new OperationCanceledException();
						}
						if(type instanceof TypeDeclaration) {
							TypeDeclaration td = (TypeDeclaration)type;

							/* se � a PR�PRIA CLASSE */
							if(td.resolveBinding().getKey().equals(name)) {
								/* marca, se o PAI j� nao estiver marcado */
								if(ast.types().size() == 1) {
									marca(managerForFile, ast, feature);
								}
								else if(!managerForFile.hasFeature(td.getParent(), feature)) {
									marca(managerForFile, td, feature);
								}
								/* marca os IMPORTS da classe em todo o projeto */
								searchIMP(jproject, td.resolveBinding().getKey(), feature, monitor);
							}
							/* se � um FILHO da classe  */
							else if(td.getSuperclassType() != null && checkType(td.getSuperclassType(), name)) {
								/* procura por esta classe em todo projeto */
								searchPROJ(jproject, td.resolveBinding().getKey(), feature, monitor);
							}
							else {
								for(Type t : (List<Type>)td.superInterfaceTypes()) {
									if(checkType(t, name)) {
										// System.out.println("Interface: " + name);
										/* marca o nome da interface na cl�usula implements */
										marca(managerForFile, t, feature);
										IJavaElement ije = jproject.findElement(t.resolveBinding().getKey(), null);
										if(ije instanceof IType) {
											/* marca os m�todos da interface */
											TreeSet<String> aux = new TreeSet<String>();

											IType inter = (IType) ije;
											for(IMethod m : inter.getMethods()) {
												aux.add(m.getElementName() + m.getSignature());
											}

											for(BodyDeclaration bd : (List<BodyDeclaration>)td.bodyDeclarations()) {
												if(bd.getNodeType() == ASTNode.METHOD_DECLARATION) {
													MethodDeclaration md = (MethodDeclaration)bd;
													IMethod im = (IMethod)md.resolveBinding().getJavaElement();
													if(aux.contains(im.getElementName() + im.getSignature()))
														searchPROJ(jproject, md.resolveBinding().getKey(), feature, monitor);
												}
											}
										}
									}
								}
								if(!managerForFile.hasFeature(td.getParent(), feature) &&
										!managerForFile.hasFeature(td, feature))
									searchBODY(td.bodyDeclarations(), name, managerForFile, feature, monitor);
							}
						}
						else {
							System.out.println("Tipo n�o tratado: " + type.nodeClassForType(type.getNodeType()) + " - " + type.getName().getFullyQualifiedName());
						}
					}

					//ColoredIDEPlugin.getDefault().notifyListeners(new ColorChangedEvent(this, compUnit, source));
					managerForFile.commitChanges();
				}
			}
		}
		catch(CoreException e){
			throw new RuntimeException(e.getMessage(), e);
		}
		System.out.println(">> SAIU searchPROJ: "+ name);
	}

	public void searchBODY(List<BodyDeclaration> bds, String name, CompilationUnitFeaturesManager managerForFile, Feature feature, IProgressMonitor monitor) {
		for(BodyDeclaration bd : bds) {
			//System.out.println(bd.nodeClassForType(bd.getNodeType()));
			switch(bd.getNodeType()) {
			case ASTNode.METHOD_DECLARATION: {
				MethodDeclaration md = (MethodDeclaration)bd;

				//				System.out.println("=========================================");
				//				System.out.println(">> METODO: " + md.getName().getFullyQualifiedName() + ":" + md.resolveBinding().getKey());

				//if(Modifier.isPublic(md.getModifiers()));

				String aux[] = name.split("&");
				if(aux.length > 1) {
					assert aux.length > 2;

					if(md.resolveBinding().getKey().equals(aux[0])) {
						SingleVariableDeclaration arg = (SingleVariableDeclaration)md.parameters().get(Integer.parseInt(aux[1]));
						marca(managerForFile, arg, feature);
						searchSTM(md.getBody(), arg.resolveBinding().getKey(), managerForFile, feature, monitor);
					}
					searchSTM(md.getBody(), name, managerForFile, feature, monitor);
					break;
				}

				if(md.getReturnType2() != null && checkType(md.getReturnType2(), name)) {
					/* colore a declara��o toda */
					searchPROJ(managerForFile.getCompilationUnit().getJavaProject(), md.resolveBinding().getKey(), feature, monitor);
				}
				else if(md.resolveBinding().getKey().equals(name)) {
					/* colore a declara��o toda */
					marca(managerForFile, md, feature);
				}
				else {
					/* procura nos par�metros */
					int cont = 0;
					for(SingleVariableDeclaration svd : (List<SingleVariableDeclaration>) md.parameters()) {
						if(svd.resolveBinding().getKey().equals(name) || checkType(svd.getType(), name)) {
							/* adiciona "&999" ao BindingKey para referenciar o n�mero do par�metro */
							searchPROJ(managerForFile.getCompilationUnit().getJavaProject(), md.resolveBinding().getKey() + "&" + cont, feature, monitor);
						}
						cont++;
					}
					searchSTM(md.getBody(), name, managerForFile, feature, monitor);
				}
				break;
			}

			case ASTNode.FIELD_DECLARATION: {
				FieldDeclaration fd = (FieldDeclaration)bd;
				List<VariableDeclarationFragment> frag = fd.fragments();

				if(checkType(fd.getType(), name)) {
					for(VariableDeclarationFragment vd : frag) {
						searchBODY(bds, vd.resolveBinding().getKey(), managerForFile, feature, monitor);
					}
					break;
				}

				boolean todos = true, achou = false;

				for(VariableDeclarationFragment vd : frag) {
					//					System.out.println(">> CAMPO: " + vd.getName().getFullyQualifiedName() + ":" + vd.resolveBinding().getKey());
					if(vd.resolveBinding().getKey().equals(name)) {
						marca(managerForFile, vd, feature);
						achou = true;
					}
					else
						if(searchEXP(vd.getInitializer(), name, managerForFile, feature, monitor)) {
							//							System.out.println(">> SE4 (FD):" + vd.getName() + ": " + fd.getType().resolveBinding().getQualifiedName());
							if(podeMarcar(vd, managerForFile, feature))
								log(4, vd);
							searchBODY(bds, vd.resolveBinding().getKey(), managerForFile, feature, monitor);
						}
					todos &= managerForFile.hasFeature(vd, feature);
				}

				if(achou && todos) {
					/* Se todos os fields da declara��o s�o da feature, colore a declaracao */
					marca(managerForFile, fd, feature);
					for(VariableDeclarationFragment vd : frag) {
						managerForFile.removeFeature(vd, feature);
					}
				}
				break;
			}

			case ASTNode.TYPE_DECLARATION: {
				TypeDeclaration td = (TypeDeclaration)bd;
				searchBODY(td.bodyDeclarations(), name, managerForFile, feature, monitor);
				break;
			}

			case ASTNode.INITIALIZER: {
				Initializer init = (Initializer)bd;
				searchSTM(init.getBody(), name, managerForFile, feature, monitor);
				break;
			}

			default: {
				System.out.println("BodyDeclaration n�o tratada <" + bd.nodeClassForType(bd.getNodeType()) + ">");
			}
			}
		}
	}

	public void searchSTM(Statement stm, String name, CompilationUnitFeaturesManager managerForFile, Feature feature, IProgressMonitor monitor) {
		if(stm == null) return;
		//System.out.println(ASTNode.nodeClassForType(z.getNodeType()).getName());
		switch(stm.getNodeType()) {
		case ASTNode.BLOCK: {
			Block b = (Block) stm;
			List<Statement> stms = b.statements();
			Boolean todos  = true;
			for(Statement stmaux : stms) {
				searchSTM(stmaux, name, managerForFile, feature, monitor);
				todos &= (managerForFile.hasFeature(stmaux, feature));
			}
			if(todos && !stms.isEmpty()) {
				if(b.getParent() != null && b.getParent().getNodeType() == ASTNode.CATCH_CLAUSE) {
					break;
				}
				for(Statement stmaux : stms) {
					managerForFile.removeFeature(stmaux, feature);
				}
				if(b.getParent() != null) {
					System.out.println("TIPO DO PAI: "+b.getParent().nodeClassForType(b.getParent().getNodeType()));
					if(b.getParent().getNodeType() == ASTNode.METHOD_DECLARATION) {
						MethodDeclaration md = (MethodDeclaration)b.getParent();
						searchPROJ(managerForFile.getCompilationUnit().getJavaProject(), md.resolveBinding().getKey(), feature, monitor);
					}
					else
						marca(managerForFile, b, feature);
				}
				else
					marca(managerForFile, b, feature);
			}
			break;
		}
		case ASTNode.VARIABLE_DECLARATION_STATEMENT: {
			VariableDeclarationStatement vdstm = (VariableDeclarationStatement) stm;

			if(checkType(vdstm.getType(), name)) {
				marca(managerForFile, vdstm, feature);
				List<VariableDeclarationFragment> frag = vdstm.fragments();
				for(VariableDeclarationFragment vd : frag) {
					searchSTM((Statement)vdstm.getParent(), vd.resolveBinding().getKey(), managerForFile, feature, monitor);
				}
				break;
			}

			List<VariableDeclarationFragment> frag = vdstm.fragments();
			boolean todos = true, achou = false;
			for(VariableDeclarationFragment vd : frag) {
				//				System.out.println(">> LOCAL: " + vd.getName().getFullyQualifiedName() + ":" + vd.resolveBinding().getKey());

				if(searchEXP(vd.getInitializer(), name, managerForFile, feature, monitor)) {
					if(podeMarcar(vd, managerForFile, feature)) {
						//						System.out.println(">> SE4 (VD):" + vd.getName() + ": " + vdstm.getType().resolveBinding().getQualifiedName());
						marca(managerForFile, vd, feature);
						log(4, vd);
					}
					achou = true;
					/* Todos os pr�ximos acessos a vari�vel colorida devem ser coloridos.(?) */
					searchSTM((Statement)vdstm.getParent(), vd.resolveBinding().getKey(), managerForFile, feature, monitor);
				}
				todos &= managerForFile.hasFeature(vd, feature);
			}

			if(achou && todos) {
				marca(managerForFile, vdstm, feature);
				for(VariableDeclarationFragment vd : frag) {
					managerForFile.removeFeature(vd, feature);
				}
			}

			break;
		}
		case ASTNode.EXPRESSION_STATEMENT: {
			ExpressionStatement estm = (ExpressionStatement)stm;
			if(searchEXP(estm.getExpression(), name, managerForFile, feature, monitor)) {
				marca(managerForFile, stm, feature);
			}
			break;
		}
		case ASTNode.RETURN_STATEMENT: {
			ReturnStatement rstm = (ReturnStatement)stm;
			if(searchEXP(rstm.getExpression(), name, managerForFile, feature, monitor)) {
				/* SE a EXP do RETURN for colorida, colorir o m�todo todo(?) */
				/*
				ASTNode aux = rstm.getParent();

				while(aux != null && aux.getNodeType() != ASTNode.METHOD_DECLARATION)
					aux = aux.getParent();
				if(aux != null) {
					MethodDeclaration md = (MethodDeclaration)aux;

					if(!seeds.contains("4" + md.resolveBinding().getKey()) &&
						!(md.getReturnType2() != null &&
						 (seeds.contains("2" + md.getReturnType2().resolveBinding().getKey()) ||
						  seeds.contains("3" + md.getReturnType2().resolveBinding().getKey()))) &&
						!seeds.contains("2" + ((TypeDeclaration)md.getParent()).resolveBinding().getKey())) {
						System.out.println(">> SE2: " + ((MethodDeclaration)aux).resolveBinding().getName());
						contE2++;
					}
					searchPROJ(nodeColors.getSource().getCompilationUnit().getJavaProject(), ((MethodDeclaration)aux).resolveBinding().getKey(), feature);
				}
				else
				 */
				marca(managerForFile, rstm, feature); // surreal!!! colore s� o return???
			}
			break;
		}
		case ASTNode.IF_STATEMENT: {
			IfStatement ifstm = (IfStatement)stm;

			searchSTM(ifstm.getThenStatement(), name, managerForFile, feature, monitor);
			searchSTM(ifstm.getElseStatement(), name, managerForFile, feature, monitor);

			if(searchEXP(ifstm.getExpression(), name , managerForFile, feature, monitor)) { // E1
				if(managerForFile.hasFeature(ifstm.getThenStatement(), feature) &&
						(ifstm.getElseStatement() == null || managerForFile.hasFeature(ifstm.getElseStatement(), feature))) {
					marca(managerForFile, ifstm, feature);
				}
				else if(ifstm.getThenStatement() != null && (ifstm.getElseStatement() == null || managerForFile.hasFeature(ifstm.getElseStatement(), feature))) {
					marca(managerForFile, ifstm.getThenStatement(), feature);
					marca(managerForFile, ifstm, feature);
				}
				else {
					marca(managerForFile, ifstm.getExpression(), feature);
					if(podeMarcar(ifstm, managerForFile, feature)) log(5, ifstm);
				}
			}
			else {
				if(managerForFile.hasFeature(ifstm.getThenStatement(), feature) &&
						(ifstm.getElseStatement() == null || managerForFile.hasFeature(ifstm.getElseStatement(), feature))) { // E2
					if(hasSideEffect(ifstm.getExpression())) { // SE6
						if(podeMarcar(ifstm, managerForFile, feature)) log(6, ifstm);
					}
					marca(managerForFile, ifstm, feature);
				}
			}

			break;
		}
		case ASTNode.SWITCH_STATEMENT: {
			SwitchStatement swstm = (SwitchStatement)stm;
			if(searchEXP(swstm.getExpression(), name , managerForFile, feature, monitor)){
				marca(managerForFile, swstm, feature);
			}
			else {
				List<Statement> stms = swstm.statements();
				Boolean todos  = true;
				for(Statement stmaux : stms) {
					searchSTM(stmaux, name, managerForFile, feature, monitor);
					todos &= (managerForFile.hasFeature(stmaux, feature));
				}
				if(todos && !stms.isEmpty()) {
					for(Statement stmaux : stms) {
						managerForFile.removeFeature(stmaux, feature);
					}
					if(hasSideEffect(swstm.getExpression())) { // SE6
						if(podeMarcar(swstm, managerForFile, feature)) log(6, swstm);
					}
					marca(managerForFile, swstm, feature);
				}
			}
			break;
		}
		case ASTNode.SWITCH_CASE: {
			//			SwitchCase swcstm = (SwitchCase)stm;
			//			swcstm.
			//			if(searchEXP(swcstm.getExpression(), name , nodeColors, feature)){
			//				marca(nodeColors, swcstm, feature);
			//			}
			break;
		}
		case ASTNode.SYNCHRONIZED_STATEMENT: {
			SynchronizedStatement sstm = (SynchronizedStatement)stm;
			if(searchEXP(sstm.getExpression(), name, managerForFile, feature, monitor))
				marca(managerForFile, sstm, feature);
			else {
				searchSTM(sstm.getBody(), name, managerForFile, feature, monitor);
				if(managerForFile.hasFeature(sstm.getBody(), feature)) {
					if(hasSideEffect(sstm.getExpression())) { // SE6
						if(podeMarcar(sstm, managerForFile, feature)) log(6, sstm);
					}
					marca(managerForFile, sstm, feature);
				}
			}
			break;
		}
		case ASTNode.FOR_STATEMENT: {
			ForStatement forstm = (ForStatement)stm;

			/*
			for(Expression e : (List<Expression>)forstm.initializers()) {
				searchEXP(e, name, nodeColors, feature);
			}
			for(Expression e : (List<Expression>)forstm.updaters()) {
				searchEXP(e, name, nodeColors, feature);
			}
			 */
			if(searchEXP(forstm.getExpression(), name, managerForFile, feature, monitor)) {
				marca(managerForFile, forstm.getBody(), feature);
				marca(managerForFile, forstm, feature);
			}
			else {
				searchSTM(forstm.getBody(), name, managerForFile, feature, monitor);
				if(managerForFile.hasFeature(forstm.getBody(), feature)) {
					if(podeMarcar(forstm, managerForFile, feature)) log(6, forstm);
					marca(managerForFile, forstm, feature);
				}
			}
			break;
		}
		case ASTNode.ENHANCED_FOR_STATEMENT: {
			EnhancedForStatement forstm = (EnhancedForStatement)stm;
			SingleVariableDeclaration formal = forstm.getParameter();
			if(formal.resolveBinding().getKey().equals(name) ||
					checkType(formal.getType(), name) ||
					searchEXP(forstm.getExpression(), name, managerForFile, feature, monitor)) {
				marca(managerForFile, forstm.getBody(), feature);
				marca(managerForFile, forstm, feature);
			}
			else {
				searchSTM(forstm.getBody(), name, managerForFile, feature, monitor);
				if(managerForFile.hasFeature(forstm.getBody(), feature)) {
					if(podeMarcar(forstm, managerForFile, feature)) log(6, forstm);
					marca(managerForFile, forstm, feature);
				}
			}
			break;

		}
		case ASTNode.DO_STATEMENT: {
			DoStatement dostm = (DoStatement)stm;
			if(searchEXP(dostm.getExpression(), name, managerForFile, feature, monitor)) {
				marca(managerForFile, dostm.getBody(), feature);
				marca(managerForFile, dostm, feature);
			}
			else {
				searchSTM(dostm.getBody(), name, managerForFile, feature, monitor);
				if(managerForFile.hasFeature(dostm.getBody(), feature)) {
					if(podeMarcar(dostm, managerForFile, feature) && hasSideEffect(dostm.getExpression())) log(6, dostm);
					marca(managerForFile, dostm, feature);
				}
			}
			break;
		}
		case ASTNode.WHILE_STATEMENT: {
			WhileStatement wstm = (WhileStatement)stm;
			if(searchEXP(wstm.getExpression(), name, managerForFile, feature, monitor)) {
				marca(managerForFile, wstm.getBody(), feature);
				marca(managerForFile, wstm, feature);
			}
			else {
				searchSTM(wstm.getBody(), name, managerForFile, feature, monitor);
				if(managerForFile.hasFeature(wstm.getBody(), feature)) {
					if(podeMarcar(wstm, managerForFile, feature) && hasSideEffect(wstm.getExpression())) log(6, wstm);
					marca(managerForFile, wstm, feature);
				}
			}
			break;
		}
		case ASTNode.TRY_STATEMENT: {
			TryStatement tstm = (TryStatement)stm;
			//try
			searchSTM(tstm.getBody(), name, managerForFile, feature, monitor);
			// catch
			for(CatchClause cc : (List<CatchClause>) tstm.catchClauses()) {
				/* Exce��o capturada na cl�usula Catch */
				if(checkType(cc.getException().getType(), name))
					marca(managerForFile, cc.getException(), feature);
				else {
					searchSTM(cc.getBody(), name, managerForFile, feature, monitor);
				}
				//if(nodeColors.hasColor(cc.getBody(), feature))
				//marca(nodeColors, cc, feature);
			}
			// finally
			searchSTM(tstm.getFinally(), name, managerForFile, feature, monitor);

			// bloco try todo colorido e n�o tem finally, marcar todo o try/catch
			if(tstm.getFinally() == null && managerForFile.hasFeature(tstm.getBody(), feature))
				marca(managerForFile, tstm, feature);
			break;
		}
		case ASTNode.THROW_STATEMENT: {
			ThrowStatement trstm = (ThrowStatement)stm;
			if(searchEXP(trstm.getExpression(), name, managerForFile, feature, monitor))
				marca(managerForFile, trstm, feature);
			break;
		}
		case ASTNode.CONSTRUCTOR_INVOCATION: {
			ConstructorInvocation ci = (ConstructorInvocation)stm;

			String aux[] = name.split("&");
			if(aux.length > 1) {
				assert aux.length > 2;

				if(ci.resolveConstructorBinding().getKey().equals(aux[0])) {
					Expression arg = (Expression)ci.arguments().get(Integer.parseInt(aux[1]));
					marca(managerForFile, arg, feature);
				}
				break;
			}

			if(ci.resolveConstructorBinding().getKey().equals(name))
				marca(managerForFile, ci, feature);

			List<Expression> args = ci.arguments();
			for(Expression arg : args) {
				if(searchEXP(arg, name, managerForFile, feature, monitor)) {
					if(podeMarcar(ci, managerForFile, feature)) {
						if(ci.resolveConstructorBinding() != null)
							se3.add(ci.resolveConstructorBinding().getKey());
						log(3, ci);
						marca(managerForFile, ci, feature);
					}
					break; // novidade
				}
			}
			break;
		}
		case ASTNode.SUPER_CONSTRUCTOR_INVOCATION: {
			SuperConstructorInvocation ci = (SuperConstructorInvocation)stm;

			String aux[] = name.split("&");
			if(aux.length > 1) {
				assert aux.length > 2;

				if(ci.resolveConstructorBinding().getKey().equals(aux[0])) {
					Expression arg = (Expression)ci.arguments().get(Integer.parseInt(aux[1]));
					marca(managerForFile, arg, feature);
				}
				break;
			}

			if(searchEXP(ci.getExpression(), name, managerForFile, feature, monitor))
				marca(managerForFile, ci, feature);

			if(ci.resolveConstructorBinding().getKey().equals(name))
				marca(managerForFile, ci, feature);

			List<Expression> args = ci.arguments();
			for(Expression arg : args) {
				if(searchEXP(arg, name, managerForFile, feature, monitor)) {
					if(podeMarcar(ci, managerForFile, feature)) {
						if(ci.resolveConstructorBinding() != null)
							se3.add(ci.resolveConstructorBinding().getKey());
						log(3, ci);
						marca(managerForFile, ci, feature);
					}
					break; // novidade
				}
			}
			break;

		}
		}
	}

	public boolean searchEXP(Expression exp, String name, CompilationUnitFeaturesManager managerForFile, Feature feature, IProgressMonitor monitor) {
		if(exp == null) return false;
		switch(exp.getNodeType()) {
		case ASTNode.CONDITIONAL_EXPRESSION: {// tern�rio
			ConditionalExpression cond = (ConditionalExpression)exp;
			boolean a = searchEXP(cond.getExpression(), name, managerForFile, feature, monitor);
			boolean b = searchEXP(cond.getThenExpression(), name, managerForFile, feature, monitor);
			boolean c = searchEXP(cond.getElseExpression(), name, managerForFile, feature, monitor);

			if((a || b || c) && !(a && b && c)) {
				if(podeMarcar(cond, managerForFile, feature)) log(1, cond);
			}

			/* tem que ver isso aqui no futuro !!! */
			return a || b || c;
		}
		case ASTNode.INSTANCEOF_EXPRESSION: {
			InstanceofExpression inst = (InstanceofExpression)exp;
			boolean a = searchEXP(inst.getLeftOperand(), name, managerForFile, feature, monitor);
			boolean b = checkType(inst.getRightOperand(), name);
			if((a || b) && !(a && b)) {
				if(podeMarcar(inst, managerForFile, feature)) log(1, inst);
			}
			return a || b;
		}
		case ASTNode.PARENTHESIZED_EXPRESSION: {
			ParenthesizedExpression par = (ParenthesizedExpression)exp;
			return searchEXP(par.getExpression(), name, managerForFile, feature, monitor);
		}
		case ASTNode.CAST_EXPRESSION: {
			CastExpression cast = (CastExpression)exp;
			boolean a = searchEXP(cast.getExpression(), name, managerForFile, feature, monitor);
			boolean b = checkType(cast.getType(), name);
			if((a || b) && !(a && b)) {
				if(podeMarcar(cast, managerForFile, feature)) log(1, cast);
			}
			return a || b;
		}
		case ASTNode.PREFIX_EXPRESSION: {
			PrefixExpression pre = (PrefixExpression)exp;
			return searchEXP(pre.getOperand(), name, managerForFile, feature, monitor);
		}
		case ASTNode.POSTFIX_EXPRESSION: {
			PostfixExpression pos = (PostfixExpression)exp;
			return searchEXP(pos.getOperand(), name, managerForFile, feature, monitor);
		}
		case ASTNode.INFIX_EXPRESSION: {
			int contMarcada = 0, cont = 2;
			InfixExpression inf = (InfixExpression)exp;
			if(searchEXP(inf.getLeftOperand(), name, managerForFile, feature, monitor))
				contMarcada++;
			if(searchEXP(inf.getRightOperand(), name, managerForFile, feature, monitor))
				contMarcada++;
			for(Expression e : (List<Expression>)inf.extendedOperands()) {
				if(searchEXP(e, name, managerForFile, feature, monitor))
					contMarcada++;
				cont++;
			}
			if(contMarcada > 0 && contMarcada < cont) {
				if(podeMarcar(inf, managerForFile, feature)) log(1, inf);
			}
			return contMarcada > 0;
		}
		case ASTNode.ASSIGNMENT: {
			Assignment asg = (Assignment)exp;
			boolean esq = searchEXP(asg.getLeftHandSide(), name, managerForFile, feature, monitor);
			boolean dir = searchEXP(asg.getRightHandSide(), name, managerForFile, feature, monitor);
			if(!esq && dir) {
				//				System.out.println(">> SE4 (ASSIGN): " + asg.toString());
				if(podeMarcar(asg, managerForFile, feature)) log(4, asg);
				switch(asg.getLeftHandSide().getNodeType()) {
				case ASTNode.SIMPLE_NAME: {
					//System.out.println(exp.getParent().nodeClassForType(exp.getParent().getNodeType()));
					if(exp.getParent() != null && exp.getParent().getParent() != null)
						searchSTM((Statement)exp.getParent().getParent(), ((SimpleName)asg.getLeftHandSide()).resolveBinding().getKey(), managerForFile, feature, monitor);
					break;
				}
				case ASTNode.FIELD_ACCESS: {
					searchPROJ(managerForFile.getCompilationUnit().getJavaProject(), ((FieldAccess)asg.getLeftHandSide()).resolveFieldBinding().getKey(), feature, monitor);
					break;
				}
				default:
					System.out.println("ERRO: "+asg.getLeftHandSide().nodeClassForType(asg.getLeftHandSide().getNodeType()));
				}
				return false;
			}
			return esq;
		}
		case ASTNode.CLASS_INSTANCE_CREATION: {
			ClassInstanceCreation cic = (ClassInstanceCreation)exp;
			List<Expression> args = cic.arguments();

			String aux[] = name.split("&");
			if(aux.length > 1) {
				//System.out.println("� CIC: "+cic.toString()+"\nAux:" + name);
				assert aux.length > 2;

				if(cic.resolveConstructorBinding().getKey().equals(aux[0])) {
					Expression arg = (Expression)cic.arguments().get(Integer.parseInt(aux[1]));
					marca(managerForFile, arg, feature);
				}
				return false;
			}
			else if(checkType(cic.getType(), name)) {
				for(Expression arg : args)
					managerForFile.removeFeature(arg, feature);
				return true;
			}

			for(Expression arg : args) {
				if(searchEXP(arg, name, managerForFile, feature, monitor)) {
					if(podeMarcar(cic, managerForFile, feature)) {
						if(cic.resolveConstructorBinding() != null)
							se3.add(cic.resolveConstructorBinding().getKey());
						log(3, cic);
						//marca(nodeColors, cic, feature);
					}
					return true; // novidade
				}
			}

			if(cic.getAnonymousClassDeclaration() != null) {
				AnonymousClassDeclaration acd  = cic.getAnonymousClassDeclaration();
				searchBODY(acd.bodyDeclarations(), name, managerForFile, feature, monitor);
			}

			break;
		}
		case ASTNode.SUPER_METHOD_INVOCATION: {
			SuperMethodInvocation mi = (SuperMethodInvocation)exp;
			List<Expression> args = mi.arguments();

			String aux[] = name.split("&");
			if(aux.length > 1) {
				assert aux.length > 2;
				if(mi.resolveMethodBinding().getKey().equals(aux[0])) {
					Expression arg = (Expression)mi.arguments().get(Integer.parseInt(aux[1]));
					marca(managerForFile, arg, feature);
				}
				return false;
			}
			else if(mi.resolveMethodBinding().getKey().equals(name)) {
				for(Expression arg : args)
					managerForFile.removeFeature(arg, feature);
				return true;
			}

			for(Expression arg : args) {
				if(searchEXP(arg, name, managerForFile, feature, monitor)) {
					if(podeMarcar(mi, managerForFile, feature)) {
						if(mi.resolveMethodBinding() != null)
							se3.add(mi.resolveMethodBinding().getKey());
						log(3, mi);
						//marca(nodeColors, mi, feature);
					}
					return true; // novidade
				}
			}
			break;
		}
		case ASTNode.METHOD_INVOCATION: {
			MethodInvocation mi = (MethodInvocation)exp;
			List<Expression> args = mi.arguments();

			/* verificar isso */
			if(searchEXP(mi.getExpression(), name, managerForFile, feature, monitor))
				return true;
			/* fim verificar */

			String aux[] = name.split("&");
			if(aux.length > 1) {
				assert aux.length > 2;
				if(mi.resolveMethodBinding().getKey().equals(aux[0])) {
					Expression arg = (Expression)mi.arguments().get(Integer.parseInt(aux[1]));
					marca(managerForFile, arg, feature);
				}
				return false;
			}
			else if(mi.resolveMethodBinding().getKey().equals(name)) {
				for(Expression arg : args)
					managerForFile.removeFeature(arg, feature);
				return true;
			}

			//int cont = 0;
			for(Expression arg : args) {
				if(searchEXP(arg, name, managerForFile, feature, monitor)) {
					if(podeMarcar(mi, managerForFile, feature)) {
						if(mi.resolveMethodBinding() != null)
							se3.add(mi.resolveMethodBinding().getKey());
						log(3, mi);
						//marca(nodeColors, cic, feature);
					}
					return true; // novidade
					/* Localiza a decaracao do m�todo para marcar o argumento */
					//					searchPROJ(nodeColors.getSource().getCompilationUnit().getJavaProject(), mi.resolveMethodBinding().getKey() + "&" + cont, feature);
				}
				//cont++;
			}
			break;
		}
		case ASTNode.THIS_EXPRESSION: {
			ThisExpression te = (ThisExpression)exp;

			if(te.resolveTypeBinding().getKey().equals(name))
				return true;
			break;
		}
		case ASTNode.SUPER_FIELD_ACCESS: {
			SuperFieldAccess fa = (SuperFieldAccess)exp;


			//			if(searchEXP(fa.getExpression(), name, nodeColors, feature))
			//				return true;
			return searchEXP(fa.getQualifier(), name, managerForFile, feature, monitor);
		}
		case ASTNode.FIELD_ACCESS: {
			/*
			 * A().B, onde A � uma EXPRESSION e B � um SimpleName
			 */
			FieldAccess fa = (FieldAccess)exp;
			if(searchEXP(fa.getExpression(), name, managerForFile, feature, monitor))
				return true;

			return searchEXP(fa.getName(), name, managerForFile, feature, monitor);
		}
		case ASTNode.QUALIFIED_NAME: {
			/*
			 * A.B, onde A � um NAME e B � um SimpleName
			 */
			QualifiedName qn = (QualifiedName)exp;

			if(searchEXP(qn.getQualifier(), name, managerForFile, feature, monitor))
				return true;
			return searchEXP(qn.getName(), name, managerForFile, feature, monitor);
		}
		case ASTNode.SIMPLE_NAME: {
			SimpleName sn = (SimpleName)exp;
			//System.out.println(sn.resolveBinding().getKey());
			if(sn.resolveBinding().getKey().equals(name) || (sn.resolveTypeBinding() != null && sn.resolveTypeBinding().getKey().equals(name)))
				return true;
			break;
		}
		case ASTNode.TYPE_LITERAL: {
			TypeLiteral tl = (TypeLiteral)exp;
			if(tl.getType() == null || tl.getType().resolveBinding() == null)
				return false;
			else
				return checkType(tl.getType(), name);

		}
		//		default: {
		//			System.out.println("Expression n�o tratada: " + exp.nodeClassForType(exp.getNodeType()) + exp.toString());
		//		}
		}
		return false;
	}

	public void searchIMP(IJavaProject jproject, String name, Feature feature, IProgressMonitor monitor) {
		try {
			/* Para todo PACOTE do PROJETO fa�a: */
			if(name == null)
				return;
			IPackageFragment[] pkgs = jproject.getPackageFragments();
			if(pkgs == null)
				return;
			for (IPackageFragment pkg : pkgs) {
				ICompilationUnit[] compUnits = pkg.getCompilationUnits();
				if(compUnits == null)
					continue;
				for (ICompilationUnit compUnit : compUnits) {
					monitor.setTaskName("Checking compilation unit "+compUnit.getElementName());
					if(monitor.isCanceled()){
						throw new OperationCanceledException();
					}
					monitor.setTaskName("Checking type ... IMP "+compUnit.getElementName());
					//IColoredJavaSourceFile source = ColoredJavaSourceFile.getColoredJavaSourceFile(compUnit);
					//IColorManager nodeColors = source.getColorManager();
					CompilationUnitFeaturesManager manager = getSafeManager(jproject, compUnit);
					CompilationUnit ast = getAst(manager.getCompilationUnit());
					//nodeColors.beginBatch();
					for(ImportDeclaration imp : (List<ImportDeclaration>)ast.imports()) {
						//if(imp.getName().getFullyQualifiedName().equals(name) &&

						if(imp.resolveBinding() != null && imp.resolveBinding().getKey().equals(name) &&
								!manager.hasFeature(imp, feature) &&
								!manager.hasFeature(imp.getParent(), feature)) {
							marca(manager, imp, feature);
						}
					}
					manager.commitChanges();
				}
			}
		}
		catch(CoreException e){
			throw new RuntimeException(e);
		}
	}

	public boolean checkType(Type t, String name) {
		if(t.isParameterizedType()) {
			ParameterizedType pt = (ParameterizedType)t;
			boolean r = checkType(pt.getType(), name);

			for(Type aux : (List<Type>)pt.typeArguments())
				r |= checkType(aux, name);

			return r;
		}
		else {
			return t.resolveBinding().getKey().equals(name);
		}
	}

	private IJavaProject getSelectedJavaProject() {
		if (selection instanceof IStructuredSelection) {
			Object selected = ((IStructuredSelection) selection)
					.getFirstElement();
			if (selected instanceof IJavaProject)
				return (IJavaProject) selected;
		}
		return null;
	}

	public boolean hasSideEffect(Expression exp) {
		if(exp == null) return false;
		switch(exp.getNodeType()) {
		case ASTNode.CONDITIONAL_EXPRESSION: {// tern�rio
			ConditionalExpression cond = (ConditionalExpression)exp;
			if(hasSideEffect(cond.getExpression()))
				return true;
			if(hasSideEffect(cond.getThenExpression()))
				return true;
			if(hasSideEffect(cond.getElseExpression()))
				return true;
			return false;
		}
		case ASTNode.INSTANCEOF_EXPRESSION: {
			InstanceofExpression inst = (InstanceofExpression)exp;
			return hasSideEffect(inst.getLeftOperand());
		}
		case ASTNode.PARENTHESIZED_EXPRESSION: {
			ParenthesizedExpression par = (ParenthesizedExpression)exp;
			return hasSideEffect(par.getExpression());
		}
		case ASTNode.CAST_EXPRESSION: {
			CastExpression cast = (CastExpression)exp;
			return hasSideEffect(cast.getExpression());
		}
		case ASTNode.PREFIX_EXPRESSION: {
			PrefixExpression pre = (PrefixExpression)exp;
			if(pre.getOperator().equals(Operator.DECREMENT) || pre.getOperator().equals(Operator.INCREMENT))
				return true;
			else
				return hasSideEffect(pre.getOperand());
		}
		case ASTNode.POSTFIX_EXPRESSION: {
			PostfixExpression pos = (PostfixExpression)exp;
			if(pos.getOperator().equals(Operator.DECREMENT) || pos.getOperator().equals(Operator.INCREMENT))
				return true;
			else
				return hasSideEffect(pos.getOperand());
		}
		case ASTNode.INFIX_EXPRESSION: {
			InfixExpression inf = (InfixExpression)exp;
			if(hasSideEffect(inf.getLeftOperand()))
				return true;
			if(hasSideEffect(inf.getRightOperand()))
				return true;
			for(Expression e : (List<Expression>)inf.extendedOperands()) {
				if(hasSideEffect(e))
					return true;
			}
			return false;
		}
		case ASTNode.ASSIGNMENT: {
			Assignment asg = (Assignment)exp;
			return true;
		}
		case ASTNode.CLASS_INSTANCE_CREATION: {
			ClassInstanceCreation cic = (ClassInstanceCreation)exp;
			return true;
		}
		case ASTNode.SUPER_METHOD_INVOCATION: {
			SuperMethodInvocation mi = (SuperMethodInvocation)exp;
			return true;
		}
		case ASTNode.METHOD_INVOCATION: {
			MethodInvocation mi = (MethodInvocation)exp;
			return true;
		}
		case ASTNode.THIS_EXPRESSION: {
			ThisExpression te = (ThisExpression)exp;
			return false;
		}
		case ASTNode.SUPER_FIELD_ACCESS: {
			SuperFieldAccess fa = (SuperFieldAccess)exp;
			return hasSideEffect(fa.getQualifier());
		}
		case ASTNode.FIELD_ACCESS: {
			/*
			 * A().B, onde A � uma EXPRESSION e B � um SimpleName
			 */
			FieldAccess fa = (FieldAccess)exp;
			if(hasSideEffect(fa.getExpression()))
				return true;
			else
				return false;
		}
		case ASTNode.QUALIFIED_NAME: {
			/*
			 * A.B, onde A � um NAME e B � um SimpleName
			 */
			QualifiedName qn = (QualifiedName)exp;

			if(hasSideEffect(qn.getQualifier()))
				return true;
			else
				return false;
		}
		case ASTNode.SIMPLE_NAME: {
			SimpleName sn = (SimpleName)exp;
			return false;
		}
		case ASTNode.TYPE_LITERAL: {
			TypeLiteral tl = (TypeLiteral)exp;
			return false;
		}
		default: {
			System.out.println(">>> Expression n�o tratada (hasSideEffect): " + exp.nodeClassForType(exp.getNodeType()) + " - " + exp.toString());
			return false;
		}
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		shell = targetPart.getSite().getShell();
	}

	private void marca(CompilationUnitFeaturesManager managerForFile, ASTNode n, Feature c) {
		if(podeMarcar(n, managerForFile, c))
			managerForFile.setFeature(n, c);
	}

	private boolean podeMarcar(ASTNode n, CompilationUnitFeaturesManager managerForFile, Feature c) {
		ASTNode aux = n;
		boolean pode = managerForFile.hasFeature(aux, c);
		while(aux != null && !pode) {
			aux = aux.getParent();
			if(aux != null) {
				//System.out.println(">>"+aux.nodeClassForType(aux.getNodeType()));
				if(aux.getNodeType() == ASTNode.METHOD_DECLARATION)
					pode |=	seeds.contains("4" + ((MethodDeclaration)aux).resolveBinding().getKey());
				else
					if(aux.getNodeType() == ASTNode.TYPE_DECLARATION) {
						if(((TypeDeclaration)aux).isInterface())
							pode |= seeds.contains("3" + ((TypeDeclaration)aux).resolveBinding().getKey());
						else
							pode |= seeds.contains("2" + ((TypeDeclaration)aux).resolveBinding().getKey());
					}

				pode |= managerForFile.hasFeature(aux, c);
			}
		}
		return !pode;
	}

	private void log(int se, ASTNode n) {
		switch(se) {
		case 1:
			contSE1++;
			break;
		case 2:
			contSE2++;
			break;
		case 3:
			contSE3++;
			break;
		case 4:
			contSE4++;
			break;
		case 5:
			contSE5++;
			break;
		case 6:
			contSE6++;
			break;
		}

		if(n.getRoot().getNodeType() == n.COMPILATION_UNIT) {
			String msg = "SE" + se + "~" +
					n.nodeClassForType(n.getNodeType()).getSimpleName() + "~"+
					((CompilationUnit)n.getRoot()).getJavaElement().getPath().toOSString() + "~" +
					n.getStartPosition() + "~" +
					n.getLength() + "~\"" +
					n.toString() + "\"";
			System.out.println(msg);
			arqLog.println(msg);
		}
		else
			System.out.println(">>>>>> Erro no LOG. Verifique!");
	}

	//	public void searchIFDEF(IJavaProject jproject, Feature feature) {
	//		try {
	//			/* Para todo PACOTE do PROJETO fa�a: */
	//			IPackageFragment[] pkgs = jproject.getPackageFragments();
	//			if(pkgs == null)
	//				return;
	//			for (IPackageFragment pkg : pkgs) {
	//				ICompilationUnit[] compUnits = pkg.getCompilationUnits();
	//				if(compUnits == null)
	//					continue;
	//				for (ICompilationUnit compUnit : compUnits) {
	//					CompilationUnitFeaturesManager managerForFile = getSafeManager(jproject, compUnit);
	//					CompilationUnit ast = getAst(compUnit);
	////					IColoredJavaSourceFile source = ColoredJavaSourceFile.getColoredJavaSourceFile(compUnit);
	////					IColorManager nodeColors = source.getColorManager();
	//					Stack<Integer> pilha = new Stack<Integer>();
	//
	//					for(Comment co : (List<Comment>)ast.getCommentList()) {
	//						if(co.isLineComment()) {
	//							int tam = co.getLength();
	//							int pos = co.getStartPosition();
	//							String com = compUnit.getSource().substring(pos, pos+tam);
	//							/* //#if defined(LOGGING)
	//							 * //#if defined(ACTIVITYDIAGRAM)
	//							 * //#if defined(UMLSTATEDIAGRAM)
	//							 * //#if defined(COGNITIVE)
	//							 * */
	//							if(com.startsWith("//#if")) {
	//								if(com.contains("defined(LOGGING)"))// &&  !com.contains("defined(UMLSTATEDIAGRAM)") )
	//									pilha.push(pos);
	//								else
	//									pilha.push(-1);
	//							}
	//							else if(com.startsWith("//#endif")) {
	//								int aux = pilha.pop();
	//								if(aux != -1) {
	//									ToggleTextColorContext context = new ToggleTextColorContext(source, new TextSelection(aux, pos+tam - aux + 1));
	//									context.run(feature, true);
	//									if(context.getSelectedNodes().isEmpty()) {
	//										System.out.println(">> Zero n�s marcados: " + compUnit.getSource().substring(aux, pos+tam));
	//										System.out.println(">> Local: " + compUnit.getElementName() + "; posi��o: " + co.getStartPosition());
	//									}
	//								}
	//							}
	//
	//						}
	//
	//					}
	//				}
	//			}
	//		}
	//		catch(CoreException e){
	//			e.printStackTrace();
	//		}
	//	}
}