package com.venkata.tradestore.entity;

import java.util.List;

/**
 * ErrorResponse is to build json responses during failures.
 * @author vkopp
 *
 */
public class ErrorResponse {
	  
    private String message;
    private List<String> details;
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public List<String> getDetails() {
		return details;
	}
	public void setDetails(List<String> details) {
		this.details = details;
	}

}
