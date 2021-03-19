package com.venkata.tradestore.business;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import com.venkata.tradestore.entity.ErrorResponse;

@ControllerAdvice
@RestController
public class TradeStoreExceptionHandler {
	
	    
	@ExceptionHandler(TradeStoreValidityException.class)
	public final ResponseEntity<ErrorResponse> handleValidityErrors(TradeStoreValidityException ex, WebRequest request){		
		ErrorResponse response = new ErrorResponse();
		response.setMessage("Failed");
		List<String> details = new ArrayList<String>();
		details.add(ex.getMessage());
		response.setDetails(details);
		return new ResponseEntity<>(response, HttpStatus.BAD_GATEWAY);
		
	}

}
