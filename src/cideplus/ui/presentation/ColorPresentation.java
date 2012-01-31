package cideplus.ui.presentation;

import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IProblemRequestor;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jface.text.ITextPresentationListener;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

import cideplus.model.Feature;
import cideplus.ui.astview.EditorUtility;
import cideplus.ui.astview.NodeProperty;
import cideplus.ui.configuration.CompilationUnitFeaturesManager;
import cideplus.ui.configuration.FeaturesConfigurationUtil;
import cideplus.ui.editor.FeaturerCompilationUnitEditor;

public class ColorPresentation implements ITextPresentationListener {

	private CompilationUnit root;
	private CompilationUnitFeaturesManager manager;
	private ITypeRoot input;

	public ColorPresentation(ISourceViewer sourceViewer,
			FeaturerCompilationUnitEditor compilationUnitEditor) {
		this.input = EditorUtility.getJavaInput(compilationUnitEditor);
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setResolveBindings(false);
		if (input instanceof ICompilationUnit) {
			parser.setSource((ICompilationUnit) input);
		} else {
			parser.setSource((IClassFile) input);
		}
		this.root = (CompilationUnit) parser.createAST(null);
		refreshFeatures();
	}

	public void applyTextPresentation(TextPresentation textPresentation) {
		int offset = textPresentation.getExtent().getOffset();
		int length = textPresentation.getExtent().getLength();
		checkRange(root, offset, length, manager, textPresentation);
	}

	private void checkRange(Object node, int offset, int length,
			CompilationUnitFeaturesManager manager,
			TextPresentation textPresentation) {
		if (node instanceof ASTNode) {
			checkNode(((ASTNode) node), offset, length, manager,
					textPresentation);
		} else if (node instanceof NodeProperty) {
			NodeProperty nodeProperty = (NodeProperty) node;
			if (nodeProperty.getChildren().length > 0) {
				for (Object o : nodeProperty.getChildren()) {
					checkRange(o, offset, length, manager, textPresentation);
				}
			}
		} else if (node != null) {
			// System.out.println("unknown "+node.getClass());
		}

	}

	private void checkNode(ASTNode astNode, int offset, int length, CompilationUnitFeaturesManager manager, TextPresentation textPresentation) {
		//		System.out.println("checking " + astNode.toString().replace('\n', ' '));
		Set<Feature> features = manager.getFeatures(astNode);
		if (features.size() > 0) {
			RGB combinedRGB = FeaturesConfigurationUtil
					.getCombinedRGB(features);

			if (offset <= astNode.getStartPosition()
					&& astNode.getStartPosition() + astNode.getLength() <= offset
					+ length) {
				// System.out.println(offset+
				// ":"+astNode.getStartPosition()+"  ::  "+(astNode.getStartPosition()
				// + astNode.getLength())+":"+(offset+length));
				StyleRange range = new StyleRange();
				range.background = new Color(null, combinedRGB);
				range.start = astNode.getStartPosition();
				range.length = astNode.getLength();
				textPresentation.mergeStyleRange(range);
			} else if (offset > astNode.getStartPosition()) {
				// System.out.println(offset+
				// ":"+astNode.getStartPosition()+"  ::  "+(astNode.getStartPosition()
				// + astNode.getLength())+":"+(offset+length));
				StyleRange range = new StyleRange();
				range.background = new Color(null, combinedRGB);
				range.start = offset;
				if (astNode.getStartPosition() + astNode.getLength() > offset
						+ length) {
					range.length = length;
				} else {
					range.length = astNode.getLength()
							- (offset - astNode.getStartPosition());
				}
				textPresentation.mergeStyleRange(range);
			} else if (offset <= astNode.getStartPosition()
					&& astNode.getStartPosition() + astNode.getLength() > offset
					+ length) {
				// System.out.println(offset+
				// ":"+astNode.getStartPosition()+"  ::  "+(astNode.getStartPosition()
				// + astNode.getLength())+":"+(offset+length));
				StyleRange range = new StyleRange();
				range.background = new Color(null, combinedRGB);
				range.start = astNode.getStartPosition();
				range.length = length - (astNode.getStartPosition() - offset);
				if (range.length > 0) {
					textPresentation.mergeStyleRange(range);
				}
			}
		}

		@SuppressWarnings("rawtypes")
		List list = astNode.structuralPropertiesForType();
		for (int i = 0; i < list.size(); i++) {
			StructuralPropertyDescriptor curr = (StructuralPropertyDescriptor) list
					.get(i);
			NodeProperty nodeProperty = new NodeProperty(astNode, curr);
			checkRange(nodeProperty, offset, length, manager, textPresentation);
		}

	}

	private CompilationUnitFeaturesManager getManager(ITypeRoot input) {
		IProject project = input.getJavaProject().getProject();
		final IProblemRequestor problemRequestor = new IProblemRequestor() { // strange:
			// don't
			// get
			// bindings
			// when
			// supplying
			// null
			// as
			// problemRequestor
			public void acceptProblem(IProblem problem) {/* not interested */
			}

			public void beginReporting() {/* not interested */
			}

			public void endReporting() {/* not interested */
			}

			public boolean isActive() {
				return true;
			}
		};
		WorkingCopyOwner copyOwner = new WorkingCopyOwner() {
			public IProblemRequestor getProblemRequestor(
					ICompilationUnit workingCopy) {
				return problemRequestor;
			}
		};
		CompilationUnitFeaturesManager managerForFile;
		try {
			managerForFile = FeaturesConfigurationUtil.getFeaturesManager(
					project).getManagerForFile(
							input.getWorkingCopy(copyOwner, null));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return managerForFile;
	}

	/**
	 * L� as informacoes atualizadas das features
	 */
	public void refreshFeatures() {
		this.manager = getManager(input);
	}

}
