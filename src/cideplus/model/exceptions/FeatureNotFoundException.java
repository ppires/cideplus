package cideplus.model.exceptions;

public class FeatureNotFoundException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public FeatureNotFoundException(int feature_id) {
		super("The project does not contain the feature with ID " + feature_id + ".");
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
