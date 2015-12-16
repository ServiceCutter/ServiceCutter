package ch.hsr.servicestoolkit.editor.web.filter.gzip;

import javax.servlet.ServletException;

@SuppressWarnings("serial")
public class GzipResponseHeadersNotModifiableException extends ServletException {

	public GzipResponseHeadersNotModifiableException(final String message) {
		super(message);
	}
}
