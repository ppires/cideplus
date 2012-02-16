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
import cideplus.ui.configuration.FeaturesConfigurationUtil;
import cideplus.ui.configuration.ICompilationUnitFeaturesManager;
import cideplus.ui.editor.FeaturerCompilationUnitEditor;

public class ColorPresentation implements ITextPresentationListener {

	private ASTParser astParser;
	private FeaturerCompilationUnitEditor editor;


	private CompilationUnit root;
	private ICompilationUnitFeaturesManager manager;
	private ITypeRoot input;

	public ColorPresentation(ISourceViewer sourceViewer, FeaturerCompilationUnitEditor compilationUnitEditor) {

		this.input = EditorUtility.getJavaInput(compilationUnitEditor);
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setResolveBindings(false);
		if (input instanceof ICompilationUnit) {
			parser.setSource((ICompilationUnit) input);
		} else {
			//			System.out.println("JavaInput = class file");
			parser.setSource((IClassFile) input);
		}
		this.root = (CompilationUnit) parser.createAST(null);
		refreshFeatures();

		this.astParser = parser;
		this.editor = compilationUnitEditor;
	}

	public void applyTextPresentation(TextPresentation textPresentation) {
		int offset = textPresentation.getExtent().getOffset();
		int length = textPresentation.getExtent().getLength();

		// Apply presentation to AST
		//checkRange(root, offset, length, manager, textPresentation);



		astParser.setResolveBindings(false);
		this.input = EditorUtility.getJavaInput(editor);
		if (input instanceof ICompilationUnit) {
			astParser.setSource((ICompilationUnit) input);
		} else {
			astParser.setSource((IClassFile) input);
		}
		checkRange(astParser.createAST(null), offset, length, manager, textPresentation);


		//		/* Failed attempts */
		//		List<RangeMarker> rangeMarkers = ((CompilationUnitFeaturesManager) manager).getRangeMarkers();
		//		System.out.println("range markers size: " + rangeMarkers.size());
		//
		//		IResource resource = ASTUtils.getCorrespondingResource(root);
		//		List<IMarker> markers = FeaturesMarkerFactory.findAllRelatedMarkers(resource);
		//		System.out.println("total markers: " + markers.size());
		//		for (IMarker marker : markers) {
		//			checkMarker(marker, offset, length, manager, textPresentation);
		//		}
	}

	private void checkRange(Object node, int offset, int length, ICompilationUnitFeaturesManager manager, TextPresentation textPresentation) {
		if (node instanceof ASTNode) {
			//			System.out.println("---" + System.getProperty("line.separator") + "checkRange!");
			//			System.out.println("   node: " + node);
			//			System.out.println("  offset: " + offset);
			//			System.out.println("  length: " + length + System.getProperty("line.separator"));

			checkNode(((ASTNode) node), offset, length, manager, textPresentation);
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

	private void checkNode(ASTNode astNode, int offset, int length, ICompilationUnitFeaturesManager manager, TextPresentation textPresentation) {
		//		System.out.println("checking " + astNode.toString().replace('\n', ' '));
		Set<Feature> features = manager.getFeatures(astNode);
		if (features.size() > 0) {
			RGB combinedRGB = FeaturesConfigurationUtil.getCombinedRGB(features);

			// Se o ASTNode estiver dentro do limite
			if (offset <= astNode.getStartPosition()
					&& astNode.getStartPosition() + astNode.getLength() <= offset + length) {
				//				System.out.println(offset+
				//						":"+astNode.getStartPosition()+"  ::  "+(astNode.getStartPosition()
				//								+ astNode.getLength())+":"+(offset+length));
				StyleRange range = new StyleRange();
				range.background = new Color(null, combinedRGB);
				range.start = astNode.getStartPosition();
				range.length = astNode.getLength();
				textPresentation.replaceStyleRange(range);
				//				textPresentation.mergeStyleRange(range);

				//
			} else if (offset > astNode.getStartPosition()) {
				//				System.out.println(offset+
				//						":"+astNode.getStartPosition()+"  ::  "+(astNode.getStartPosition()
				//								+ astNode.getLength())+":"+(offset+length));
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
					&& astNode.getStartPosition() + astNode.getLength() > offset + length) {
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
			StructuralPropertyDescriptor curr = (StructuralPropertyDescriptor) list.get(i);
			NodeProperty nodeProperty = new NodeProperty(astNode, curr);
			checkRange(nodeProperty, offset, length, manager, textPresentation);
		}

	}

	//	private void checkMarker(IMarker marker, int offset, int length, CompilationUnitFeaturesManager manager, TextPresentation textPresentation) {
	//		System.out.println("checking marker");
	//		Set<Feature> features = manager.getFeatures(root);
	//		if (features.size() > 0) {
	//			RGB combinedRGB = FeaturesConfigurationUtil.getCombinedRGB(features);
	//
	//			final int markerCharStart = MarkerUtilities.getCharStart(marker);
	//			final int markerCharEnd = MarkerUtilities.getCharEnd(marker);
	//			if (offset <= markerCharStart && markerCharEnd <= offset + length) {
	//				//				System.out.println(offset+
	//				//						":"+astNode.getStartPosition()+"  ::  "+(astNode.getStartPosition()
	//				//								+ astNode.getLength())+":"+(offset+length));
	//				StyleRange range = new StyleRange();
	//				range.background = new Color(null, combinedRGB);
	//				range.start = markerCharStart;
	//				range.length = marker.getAttribute("length", -1);
	//				textPresentation.mergeStyleRange(range);
	//			} else if (offset > markerCharStart) {
	//				//				System.out.println(offset+
	//				//						":"+astNode.getStartPosition()+"  ::  "+(astNode.getStartPosition()
	//				//								+ astNode.getLength())+":"+(offset+length));
	//				StyleRange range = new StyleRange();
	//				range.background = new Color(null, combinedRGB);
	//				range.start = offset;
	//				if (markerCharEnd > offset + length) {
	//					range.length = length;
	//				} else {
	//					range.length = marker.getAttribute("length", -1) - (offset - markerCharStart);
	//				}
	//				textPresentation.mergeStyleRange(range);
	//			} else if (offset <= markerCharStart && markerCharEnd > offset + length) {
	//				// System.out.println(offset+
	//				// ":"+astNode.getStartPosition()+"  ::  "+(astNode.getStartPosition()
	//				// + astNode.getLength())+":"+(offset+length));
	//				StyleRange range = new StyleRange();
	//				range.background = new Color(null, combinedRGB);
	//				range.start = markerCharStart;
	//				range.length = length - (markerCharStart - offset);
	//				if (range.length > 0) {
	//					textPresentation.mergeStyleRange(range);
	//				}
	//			}
	//		}
	//		FeaturesConfigurationUtil.updateEditors(PluginUtils.getActiveShell().getDisplay(), null);
	//	}


	private ICompilationUnitFeaturesManager getManager(ITypeRoot input) {
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
			@Override
			public IProblemRequestor getProblemRequestor(
					ICompilationUnit workingCopy) {
				return problemRequestor;
			}
		};
		ICompilationUnitFeaturesManager managerForFile;
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
	 * Lê as informacoes atualizadas das features
	 */
	public void refreshFeatures() {
		this.manager = getManager(input);
	}

}
