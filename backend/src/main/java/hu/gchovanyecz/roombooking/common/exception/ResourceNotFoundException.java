package hu.gchovanyecz.roombooking.common.exception;

/**
 * Base class for "the requested resource does not exist" errors; mapped to HTTP 404.
 */
public abstract class ResourceNotFoundException extends RuntimeException {

	protected ResourceNotFoundException(String message) {
		super(message);
	}

}
