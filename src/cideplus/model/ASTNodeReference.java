package cideplus.model;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public class ASTNodeReference {

	final String identifier;
	int bytes;
	int offset;

	public ASTNodeReference(String identifier, int bytes, int offset) {
		super();
		this.identifier = identifier;
		this.bytes = bytes;
		this.offset = offset;
	}

	public ASTNodeReference(ASTNode astNode) {
		if(astNode == null){
			throw new IllegalArgumentException("Parameter astNode cannot be null");
		}
		int bytes = astNode.getLength();
		int offset = astNode.getStartPosition();
		String node = "";
		do {
			node += "$$";
			if(astNode instanceof CompilationUnit){
				node += "COMPILATION UNIT;;";
			} else if (astNode instanceof TypeDeclaration) {
				node += astNode.getClass().getSimpleName()+": "+((TypeDeclaration)astNode).getName()+" <<==\n";
			} else if (astNode instanceof MethodDeclaration) {
				Type returnType = ((MethodDeclaration)astNode).getReturnType2();
				SimpleName methodName = ((MethodDeclaration)astNode).getName();
				@SuppressWarnings("rawtypes")
				List parameters = ((MethodDeclaration)astNode).parameters();
				node += astNode.getClass().getSimpleName()+": "+returnType+" "+methodName+parameters+" <<==\n";
			} else if (astNode instanceof Block) {
				Block block = (Block) astNode;
				node += astNode.getClass().getSimpleName()+": "+block.properties()+"  <<==\n";
			} else if (astNode instanceof IfStatement) {
				IfStatement statement = (IfStatement) astNode;
				node += astNode.getClass().getSimpleName()+": "+statement.getExpression()+"  <<==\n";
			} else if (astNode instanceof DoStatement) {
				DoStatement statement = (DoStatement) astNode;
				node += astNode.getClass().getSimpleName()+": "+statement.getExpression()+"  <<==\n";
			} else if(astNode instanceof ForStatement) {
				ForStatement statement = (ForStatement) astNode;
				node += astNode.getClass().getSimpleName()+": "+statement.initializers()+" "+statement.getExpression()+" "+statement.updaters()+"  <<==\n";
			} else {
				node += astNode.getClass().getSimpleName()+": "+astNode+" <<==\n";
			}
			astNode = astNode.getParent();
		} while(astNode != null);
		node = node.replace('\n', ' ').replace('\r', ' ');
		this.identifier = node;
		this.bytes = bytes;
		this.offset = offset;
	}

	public String getIdentifier() {
		return this.identifier;
	}

	public int getOffset() {
		return offset;
	}

	public int getByteCount(){
		return bytes;
	}

	@Override
	public int hashCode() {
		final int prime = 127;
		int result = 1;
		result = prime * result + (identifier == null ? 0 : identifier.hashCode());
		return result + offset > bytes ? offset/bytes : bytes/offset;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ASTNodeReference other = (ASTNodeReference) obj;

		if (this.getIdentifier().equals(other.getIdentifier()) &&
				this.getByteCount() == other.getByteCount() &&
				this.getOffset() == other.getOffset()) {
			return true;
		}
		return false;
		//		if (identifier == null) {
		//			if (other.identifier != null)
		//				return false;
		//		} else if (!identifier.equals(other.identifier))
		//			return false;
		//		return true;
	}

	/**
	 * Retorna true se a referencia passada como parametro é filha desse objeto
	 * @param reference
	 * @return
	 */
	public boolean isChild(ASTNodeReference reference){
		if(this.equals(reference)){
			return false;
		}
		if(this.identifier.contains(reference.identifier)){
			return true;
		}
		return false;
	}

	@Override
	public String toString() {
		return identifier;
	}
}
