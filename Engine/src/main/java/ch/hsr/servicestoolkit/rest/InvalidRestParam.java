package ch.hsr.servicestoolkit.rest;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@SuppressWarnings("serial")
@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Call params not valid") // 404
public class InvalidRestParam extends RuntimeException {

}
