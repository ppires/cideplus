/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package cideplus.ui.astview;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.TextElement;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class ASTViewContentProvider implements ITreeContentProvider {

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer v, Object oldInput, Object newInput) {
	}
	
	/*(non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object parent) {
		return getChildren(parent);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
	 */
	public Object getParent(Object child) {
		if (child instanceof ASTNode) {
			ASTNode node= (ASTNode) child;
			ASTNode parent= node.getParent();
			if (parent != null) {
				StructuralPropertyDescriptor prop= node.getLocationInParent();
				return new NodeProperty(parent, prop);
			}
		} else if (child instanceof ASTAttribute) {
			return ((ASTAttribute) child).getParent();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object parent) {
		if (parent instanceof ASTAttribute) {
			return ((ASTAttribute) parent).getChildren();
		} else if (parent instanceof ASTNode) {
			return getNodeChildren((ASTNode) parent);
		}
		return new Object[0];
	}
	
	private Object[] getNodeChildren(ASTNode node) {
		ArrayList res= new ArrayList();

		/*if (node instanceof Expression) {
			Expression expression= (Expression) node;
			ITypeBinding expressionTypeBinding= expression.resolveTypeBinding();
			//res.add(createExpressionTypeBinding(node, expressionTypeBinding));
			
			// expressions:
			if (expression instanceof Name) {
				IBinding binding= ((Name) expression).resolveBinding();
				if (binding != expressionTypeBinding){
					//res.add(createBinding(expression, binding));
				}
			} else if (expression instanceof MethodInvocation) {
				MethodInvocation methodInvocation= (MethodInvocation) expression;
				IMethodBinding binding= methodInvocation.resolveMethodBinding();
				//res.add(createBinding(expression, binding));
				String inferred= String.valueOf(methodInvocation.isResolvedTypeInferredFromExpectedType());
				//res.add(new GeneralAttribute(expression, "ResolvedTypeInferredFromExpectedType", inferred)); //$NON-NLS-1$
			} else if (expression instanceof SuperMethodInvocation) {
				SuperMethodInvocation superMethodInvocation= (SuperMethodInvocation) expression;
				IMethodBinding binding= superMethodInvocation.resolveMethodBinding();
				//res.add(createBinding(expression, binding));
				String inferred= String.valueOf(superMethodInvocation.isResolvedTypeInferredFromExpectedType());
				//res.add(new GeneralAttribute(expression, "ResolvedTypeInferredFromExpectedType", inferred)); //$NON-NLS-1$
			} else if (expression instanceof ClassInstanceCreation) {
				IMethodBinding binding= ((ClassInstanceCreation) expression).resolveConstructorBinding();
				//res.add(createBinding(expression, binding));
			} else if (expression instanceof FieldAccess) {
				IVariableBinding binding= ((FieldAccess) expression).resolveFieldBinding();
				//res.add(createBinding(expression, binding));
			} else if (expression instanceof SuperFieldAccess) {
				IVariableBinding binding= ((SuperFieldAccess) expression).resolveFieldBinding();
				//res.add(createBinding(expression, binding));
			} else if (expression instanceof Annotation) {
				IAnnotationBinding binding= ((Annotation) expression).resolveAnnotationBinding();
				//res.add(createBinding(expression, binding));
			}
			// Expression attributes:
			//res.add(new GeneralAttribute(expression, "Boxing: " + expression.resolveBoxing() + "; Unboxing: " + expression.resolveUnboxing())); //$NON-NLS-1$ //$NON-NLS-2$
			//res.add(new GeneralAttribute(expression, "ConstantExpressionValue", expression.resolveConstantExpressionValue())); //$NON-NLS-1$
		
		// references:
		} else if (node instanceof ConstructorInvocation) {
			IMethodBinding binding= ((ConstructorInvocation) node).resolveConstructorBinding();
			//res.add(createBinding(node, binding));
		} else if (node instanceof SuperConstructorInvocation) {
			IMethodBinding binding= ((SuperConstructorInvocation) node).resolveConstructorBinding();
			//res.add(createBinding(node, binding));
		} else if (node instanceof MethodRef) {
			IBinding binding= ((MethodRef) node).resolveBinding();
			//res.add(createBinding(node, binding));
		} else if (node instanceof MemberRef) {
			IBinding binding= ((MemberRef) node).resolveBinding();
			//res.add(createBinding(node, binding));
		} else if (node instanceof Type) {
			IBinding binding= ((Type) node).resolveBinding();
			//res.add(createBinding(node, binding));
			
		// declarations:
		} else if (node instanceof AbstractTypeDeclaration) {
			IBinding binding= ((AbstractTypeDeclaration) node).resolveBinding();
			//res.add(createBinding(node, binding)); 
		} else if (node instanceof AnnotationTypeMemberDeclaration) {
			IBinding binding= ((AnnotationTypeMemberDeclaration) node).resolveBinding();
			//res.add(createBinding(node, binding));
		} else if (node instanceof EnumConstantDeclaration) {
			IBinding binding= ((EnumConstantDeclaration) node).resolveVariable();
			//res.add(createBinding(node, binding));
			IBinding binding2= ((EnumConstantDeclaration) node).resolveConstructorBinding();
			//res.add(createBinding(node, binding2));
		} else if (node instanceof MethodDeclaration) {
			IBinding binding= ((MethodDeclaration) node).resolveBinding();
			//res.add(createBinding(node, binding));
		} else if (node instanceof VariableDeclaration) {
			IBinding binding= ((VariableDeclaration) node).resolveBinding();
			//res.add(createBinding(node, binding));
		} else if (node instanceof AnonymousClassDeclaration) {
			IBinding binding= ((AnonymousClassDeclaration) node).resolveBinding();
			//res.add(createBinding(node, binding));
		} else if (node instanceof ImportDeclaration) {
			IBinding binding= ((ImportDeclaration) node).resolveBinding();
			//res.add(createBinding(node, binding));
			return new Object[0];//o import declaration
		} else if (node instanceof PackageDeclaration) {
			IBinding binding= ((PackageDeclaration) node).resolveBinding();
			//res.add(createBinding(node, binding));
			return new Object[0];//o package declaration não deve ter filhos
		} else if (node instanceof TypeParameter) {
			IBinding binding= ((TypeParameter) node).resolveBinding();
			//res.add(createBinding(node, binding));
		} else if (node instanceof MemberValuePair) {
			IBinding binding= ((MemberValuePair) node).resolveMemberValuePairBinding();
			//res.add(createBinding(node, binding));
		} else*/

		/*
		if(node instanceof Modifier){
			return new Object[0];
		} else if(node instanceof TextElement){
			return new Object[0];
		} else if(node instanceof SimpleName){
			return new Object[0];
		} else if(node instanceof SimpleType){
			return new Object[0];
		} else if(node instanceof StringLiteral){
			return new Object[0];
		} else if(node instanceof NumberLiteral){
			return new Object[0];
		} else if(node instanceof PrimitiveType){
			return new Object[0];
		} else if(node instanceof PackageDeclaration){
			return new Object[0];
		} else if(node instanceof ImportDeclaration){
			return new Object[0];
		} else if(node instanceof EnumConstantDeclaration){
			return new Object[0];
		}*/
		
		@SuppressWarnings("unchecked")
		List<Class<? extends ASTNode>> noChildNodes = Arrays.asList(
				Modifier.class, 
				TextElement.class,
				SimpleName.class,
				SimpleType.class,
				StringLiteral.class,
				NumberLiteral.class,
				PrimitiveType.class,
				PackageDeclaration.class,
				ImportDeclaration.class,
				EnumConstantDeclaration.class);
		for (Class<? extends ASTNode> class1 : noChildNodes) {
			if(class1.isAssignableFrom(node.getClass())){
				return new Object[0];
			}
		}
 		
		//essas propriedades não terão um parent (o filho ocupará o lugar do parent)
		List<String> childrenProperties = Arrays.asList("TAGS", "FRAGMENTS", "STATEMENTS", "BODY", "PACKAGE", "IMPORTS", "TYPES");
		List list= node.structuralPropertiesForType();
		for (int i= 0; i < list.size(); i++) {
			StructuralPropertyDescriptor curr= (StructuralPropertyDescriptor) list.get(i);
			NodeProperty nodeProperty = new NodeProperty(node, curr);
			if(childrenProperties.contains(nodeProperty.getPropertyName())){
				res.addAll(Arrays.asList(nodeProperty.getChildren()));
			} else {
				if(!(curr.isChildListProperty() && nodeProperty.getChildren().length == 0) && 
						!nodeProperty.getPropertyName().equals("INTERFACE") &&
						!(nodeProperty.getNode() == null)){
					res.add(nodeProperty);
				}
			}
		}
		
		if (node instanceof CompilationUnit) {
			CompilationUnit root= (CompilationUnit) node;
			//res.add(new JavaElement(root, root.getJavaElement()));
			//res.add(new CommentsProperty(root));
			//res.add(new ProblemsProperty(root));
			//res.add(new SettingsProperty(root));
			//res.add(new WellKnownTypesProperty(root));
		}
		
		return res.toArray();
	}
	
	private Binding createBinding(ASTNode parent, IBinding binding) {
		String label= Binding.getBindingLabel(binding);
		return new Binding(parent, label, binding, true);
	}

	private Object createExpressionTypeBinding(ASTNode parent, ITypeBinding binding) {
		String label= "> (Expression) type binding"; //$NON-NLS-1$
		return new Binding(parent, label, binding, true);
	}
	
	public boolean hasChildren(Object parent) {
		return getChildren(parent).length > 0;
	}
}
