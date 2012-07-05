package cideplus.model.exceptions;

public class FeatureNotFoundException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public FeatureNotFoundException(int featureId) {
		super("The project does not contain the feature with ID " + featureId + ".");
	}

	public FeatureNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public FeatureNotFoundException(String message) {
		super(message);
	}

	public FeatureNotFoundException(Throwable cause) {
		super(cause);
	}

}
