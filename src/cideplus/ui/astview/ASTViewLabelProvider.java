/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package cideplus.ui.astview;

import java.util.Set;

import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TextElement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import cideplus.model.Feature;
import cideplus.model.RGB;
import cideplus.ui.configuration.CompilationUnitFeaturesManager;
import cideplus.ui.configuration.FeaturesConfigurationUtil;

public class ASTViewLabelProvider extends StyledCellLabelProvider implements IColorProvider, IFontProvider {
	
	/* CONFIGURATION */
	//Deve-se pintar toda a linha com cor de fundo (true)? Ou apenas o texto (false)?
	private boolean fullBackgroundColor = true;
	
	private int fSelectionStart;
	private int fSelectionLength;
	
	private final Color fBlue, fRed, fDarkGray, fDarkGreen, fDarkRed;
	private final Font fBold;
	
	//to dispose:
	private final Font fAllocatedBoldItalic;
	private final Color fLightBlue, fLightRed;
	private CompilationUnitFeaturesManager compilationUnitFeaturesManager;
	
	@Override
	public void update(final ViewerCell cell) {
		Object element = cell.getElement();
		//cell.setStyleRanges(new StyleRange[]{new stylera});
		String text = getText(element);
		cell.setText(text);
		Image image = getImage(element);
		cell.setImage(image);
		if(fullBackgroundColor){
			cell.setBackground(getBackground(element));
		}
		cell.setForeground(getForeground(element));
		cell.setFont(getFont(element));
		cell.setStyleRanges(new StyleRange[]{new StyleRange(0, text.length(), getForeground(element), getBackground(element))});
	}
	
	
	public ASTViewLabelProvider() {
		fSelectionStart= -1;
		fSelectionLength= -1;
		
		Display display= Display.getCurrent();
		
		fRed= display.getSystemColor(SWT.COLOR_RED);
		fDarkGray= display.getSystemColor(SWT.COLOR_DARK_GRAY);
		fBlue= display.getSystemColor(SWT.COLOR_DARK_BLUE);
		fDarkGreen= display.getSystemColor(SWT.COLOR_DARK_GREEN);
		fDarkRed= display.getSystemColor(SWT.COLOR_DARK_RED);
		
		fLightBlue= new Color(display, 232, 242, 254); // default for AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE_COLOR
		fLightRed= new Color(display, 255, 190, 190);
		
		fBold= PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getFontRegistry().getBold(JFaceResources.DEFAULT_FONT);
		FontData[] fontData= fBold.getFontData();
		for (int i= 0; i < fontData.length; i++) {
			fontData[i].setStyle(fontData[i].getStyle() | SWT.ITALIC);
		}
		fAllocatedBoldItalic= new Font(display, fontData);
	}
	
	public void setSelectedRange(int start, int length) {
		fSelectionStart= start;
		fSelectionLength= length;
		 // could be made more efficient by only updating selected node and parents (of old and new selection)
		fireLabelProviderChanged(new LabelProviderChangedEvent(this));
	}

	public String getText(Object obj) {
		StringBuffer buf= new StringBuffer();
		if (obj instanceof ASTNode) {
			getNodeType((ASTNode) obj, buf);
		} else if (obj instanceof ASTAttribute) {
			buf.append(((ASTAttribute) obj).getLabel());
		} else {
			buf.append(obj.toString());
		}
		return buf.toString(); 
	}
	
	private void getNodeType(ASTNode node, StringBuffer buf) {
		if(node instanceof PackageDeclaration || node instanceof ImportDeclaration || node instanceof Modifier
				|| node instanceof TagElement || node instanceof TextElement || node instanceof SimpleName || node instanceof SimpleType
				|| node instanceof StringLiteral || node instanceof NumberLiteral || node instanceof PrimitiveType || node instanceof EnumConstantDeclaration){
			buf.append(node.toString().replace('\n', ' ') + "  ["+node.getClass().getSimpleName()+"]");
		} else if(node instanceof TypeDeclaration){
			buf.append(((TypeDeclaration)node).getName());
		} else if(node instanceof EnumDeclaration){
			buf.append(((EnumDeclaration)node).getName());
		} else {
			buf.append(Signature.getSimpleName(node.getClass().getName()));
			if(node instanceof Expression){
				buf.append("  -> "+node.toString().replace('\n', ' '));
			}
		}
		buf.append(" ["); //$NON-NLS-1$
		buf.append(node.getStartPosition());
		buf.append(", "); //$NON-NLS-1$
		buf.append(node.getLength());
		buf.append(']');
		if ((node.getFlags() & ASTNode.MALFORMED) != 0) {
			buf.append(" (malformed)"); //$NON-NLS-1$
		}
		if ((node.getFlags() & ASTNode.RECOVERED) != 0) {
			buf.append(" (recovered)"); //$NON-NLS-1$
		}
	}
	
	
	public Image getImage(Object obj) {
		if (obj instanceof ASTNode) {
			int nodeType = ((ASTNode)obj).getNodeType();
			String image = ISharedImages.IMG_OBJS_CFILE;
			//new JavaElementImageDescriptor()
			switch (nodeType) {
				case ASTNode.ENUM_CONSTANT_DECLARATION:
					image = ISharedImages.IMG_OBJS_ENUM_DEFAULT;
					break;
				case ASTNode.FIELD_DECLARATION:
					image = ISharedImages.IMG_FIELD_DEFAULT;
					break;
				case ASTNode.METHOD_DECLARATION:
					image = ISharedImages.IMG_OBJS_PUBLIC;
					break;
				case ASTNode.MODIFIER:
					image = ISharedImages.IMG_OBJS_DEFAULT;
					break;
				case ASTNode.JAVADOC:
					image = ISharedImages.IMG_OBJS_JAVADOCTAG;
					break;
				case ASTNode.PACKAGE_DECLARATION:
					image = ISharedImages.IMG_OBJS_PACKDECL;
					break;
				case ASTNode.IMPORT_DECLARATION:
					image = ISharedImages.IMG_OBJS_IMPDECL;
					break;
				case ASTNode.ENUM_DECLARATION:
					image = ISharedImages.IMG_OBJS_ENUM;
					break;
				case ASTNode.TYPE_DECLARATION:
					if(((TypeDeclaration)obj).isInterface()){
						image = ISharedImages.IMG_OBJS_INTERFACE;
					} else {
						image = ISharedImages.IMG_OBJS_CLASS;
					}
					break;
			}
			if(image != null){
				return JavaUI.getSharedImages().getImage(image);
			}
			return null;
		} else if (obj instanceof ASTAttribute) {
			Image image = ((ASTAttribute) obj).getImage();
			if(image == null){
				if(((ASTAttribute)obj).getChildren().length > 0){
					image = PlatformUI.getWorkbench().getSharedImages().getImage(org.eclipse.ui.ISharedImages.IMG_OBJ_FOLDER);
				} else {
					image = PlatformUI.getWorkbench().getSharedImages().getImage(org.eclipse.ui.ISharedImages.IMG_TOOL_FORWARD);
				}
			}
			return image;
		}
		
		return null;
//		String imageKey = ISharedImages.IMG_OBJ_ELEMENT;
//		if (obj instanceof ASTNode) {
//			imageKey = ISharedImages.IMG_OBJ_FOLDER;
//		}
//		return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IColorProvider#getForeground(java.lang.Object)
	 */
	public Color getForeground(Object element) {
		if ((element instanceof Error))
			return fRed;
		if ((element instanceof ExceptionAttribute) && ((ExceptionAttribute) element).getException() != null)
			return fRed;
		
		if (element instanceof ASTNode) {
			ASTNode node= (ASTNode) element;
			if ((node.getFlags() & ASTNode.MALFORMED) != 0) {
				return fRed;
			}
			return fDarkGray;
		} else if (element instanceof Binding) {
			Binding binding= (Binding) element;
			if (!binding.isRelevant())
				return fDarkGray;
			return fBlue;
		} else if (element instanceof NodeProperty) {
			return null; // normal color
		} else if (element instanceof BindingProperty) {
			BindingProperty binding= (BindingProperty) element;
			if (!binding.isRelevant())
				return fDarkGray;
			return fBlue;
		} else if (element instanceof JavaElement) {
			JavaElement javaElement= (JavaElement) element;
			if (javaElement.getJavaElement() == null || ! javaElement.getJavaElement().exists()) {
				return fRed;
			}
			return fDarkGreen;
		}
		return fDarkRed; // all extra properties
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IColorProvider#getBackground(java.lang.Object)
	 */
	public Color getBackground(Object element) {
		if(compilationUnitFeaturesManager != null && element instanceof ASTNode){
			Set<Feature> features = compilationUnitFeaturesManager.getFeatures((ASTNode) element);
			return new Color(Display.getCurrent(), FeaturesConfigurationUtil.getCombinedRGB(features));
		}
		if (isNotProperlyNested(element)) {
			return fLightRed;
		}
		if (fSelectionStart != -1 && isInside(element)) {
			return fLightBlue;
		}
		return null;
	}
	
	private Color toSystemRGB(RGB rgb) {
		return new Color(Display.getCurrent(), rgb.getRed(), rgb.getGreen(), rgb.getBlue());
	}

	private boolean isNotProperlyNested(Object element) {
		if (element instanceof ASTNode) {
			ASTNode node= (ASTNode) element;
			int start= node.getStartPosition();
			int end= start + node.getLength();
			
			ASTNode parent= node.getParent();
			if (parent != null) {
				int parentstart= parent.getStartPosition();
				int parentend= parentstart + parent.getLength();
				
				if (start < parentstart || end > parentend) {
					return true;
				}
			}
		}
		return false;
	}
	
	private boolean isInsideNode(ASTNode node) {
		int start= node.getStartPosition();
		int end= start + node.getLength();
		if (start <= fSelectionStart && (fSelectionStart + fSelectionLength) < end) {
			return true;
		}
		return false;
	}
	
	private boolean isInside(Object element) {
		if (element instanceof ASTNode) {
			return isInsideNode((ASTNode) element);
		} else if (element instanceof NodeProperty) {
			NodeProperty property= (NodeProperty) element;
			Object object= property.getNode();
			if (object instanceof ASTNode) {
				return isInsideNode((ASTNode) object);
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IFontProvider#getFont(java.lang.Object)
	 */
	public Font getFont(Object element) {
		if (element instanceof ASTNode) {
			ASTNode node= (ASTNode) element;
			if ((node.getFlags() & ASTNode.RECOVERED) != 0)
				return fAllocatedBoldItalic;
			else
				return fBold;
		}
		return null;
	}
	
	public void dispose() {
		super.dispose();
		fLightBlue.dispose();
		fLightRed.dispose();
		fAllocatedBoldItalic.dispose();
	}

	public CompilationUnitFeaturesManager getCompilationUnitFeaturesManager() {
		return compilationUnitFeaturesManager;
	}

	public void setCompilationUnitFeaturesManager(CompilationUnitFeaturesManager compilationUnitFeaturesManager) {
		this.compilationUnitFeaturesManager = compilationUnitFeaturesManager;
	}
	
}
