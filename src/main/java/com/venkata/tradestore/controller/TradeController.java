package com.venkata.tradestore.controller;

import java.text.ParseException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.venkata.tradestore.business.TradeStoreService;
import com.venkata.tradestore.business.TradeStoreValidityException;
import com.venkata.tradestore.config.Intercepted;
import com.venkata.tradestore.entity.Response;
import com.venkata.tradestore.entity.TradeRecord;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author vkopp
 *
 */
@RestController
public class TradeController {
	
	
	private static final Logger logger = LoggerFactory.getLogger(TradeController.class);

	
	@Autowired
	private TradeStoreService tsbLayer;
	

	/**
	 * This is a rest controller method to retrieve all the trade records
	 * httpmethod: GET and produces a json output.
	 * @return
	 */
	@Intercepted
	@GetMapping(path="/tradeRecords", produces="application/json")
	public ResponseEntity<List<TradeRecord>> getTradeRecords(){
		logger.info("Inside getTradeRecords");
		return new ResponseEntity<>(tsbLayer.getTradeRecords(), HttpStatus.OK);
		
	}
	
	/**
	 * This is a rest controller method to retreive trade records for the given tradeId.
	 * httpmethod: GET and produces a json output.
	 * @param tradeId
	 * @return
	 */
	@Intercepted
	@GetMapping(path="/tradeRecords/{tradeId}", produces="application/json")
	public ResponseEntity<List<TradeRecord>> getTradeRecordByTradeId(@PathVariable String tradeId){
		logger.info("Inside getTradeRecordByTradeId");
		return new ResponseEntity<>(tsbLayer.getTradeRecordByTradeId(tradeId), HttpStatus.OK);
	}
	
	/**
	 * This is a rest controller method called to update/insert a trade records,
	 * it updates if the input record version is matching with the last inserted record's version
	 * it inserts if the input record version is latest.
	 * it also do pre and post validate the request. 
	 * @param record
	 * @return
	 * @throws TradeStoreValidityException
	 * @throws ParseException
	 */
	@Intercepted
	@PostMapping(path="/tradeRecords",consumes="application/json",produces="application/json")
	public ResponseEntity<Response> createRecord(@RequestBody TradeRecord record) throws TradeStoreValidityException, ParseException {
		logger.info("Inside createRecord");
		Response response = new Response();
		response.setTradeId(record.getTradeId());
		response.setVersion(record.getVersion());		
		response.setStatusMessage("Inserted/Updated Successfully");
		tsbLayer.createRecord(record);
		return new ResponseEntity<>(response, HttpStatus.OK);
	
	}
	
}
