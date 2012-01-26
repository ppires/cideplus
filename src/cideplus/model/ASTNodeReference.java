package cideplus.model;

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
	
	public int getOffset() {
		return offset;
	}

	public int getByteCount(){
		return bytes;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((identifier == null) ? 0 : identifier.hashCode());
		return result;
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
		if (identifier == null) {
			if (other.identifier != null)
				return false;
		} else if (!identifier.equals(other.identifier))
			return false;
		return true;
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
