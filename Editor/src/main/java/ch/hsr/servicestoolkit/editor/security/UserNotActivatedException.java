package ch.hsr.servicestoolkit.editor.security;

import org.springframework.security.core.AuthenticationException;

/**
 * This exception is throw in case of a not activated user trying to
 * authenticate.
 */
@SuppressWarnings("serial")
public class UserNotActivatedException extends AuthenticationException {

	public UserNotActivatedException(final String message) {
		super(message);
	}

	public UserNotActivatedException(final String message, final Throwable t) {
		super(message, t);
	}
}
