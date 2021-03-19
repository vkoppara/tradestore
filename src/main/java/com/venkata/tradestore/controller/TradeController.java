package com.venkata.tradestore.controller;

import java.text.ParseException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.venkata.tradestore.business.TradeStoreService;
import com.venkata.tradestore.business.TradeStoreValidityException;
import com.venkata.tradestore.entity.Response;
import com.venkata.tradestore.entity.TradeRecord;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
public class TradeController {
	
	
	private static final Logger logger = LoggerFactory.getLogger(TradeController.class);

	
	@Autowired
	private TradeStoreService tsbLayer;
	

	@GetMapping(path="/tradeRecords", produces="application/json")
	public List<TradeRecord> getTradeRecords(){
		logger.info("Inside getTradeRecords");
		return tsbLayer.getTradeRecords();
		
	}
	
	@GetMapping(path="/tradeRecords/{tradeId}", produces="application/json")
	public List<TradeRecord> getTradeRecordByTradeId(@PathVariable String tradeId){
		logger.info("Inside getTradeRecordByTradeId");
		return tsbLayer.getTradeRecordByTradeId(tradeId);
	}
	
	@PostMapping(path="/tradeRecords",consumes="application/json")
	public Response createRecord(@RequestBody TradeRecord record) throws TradeStoreValidityException, ParseException {
		logger.info("Inside createRecord");
		Response response = new Response();
		response.setTradeId(record.getTradeId());
		response.setVersion(record.getVersion());		
		response.setStatusMessage("Inserted/Updated Successfully");
		tsbLayer.createRecord(record);
		return response;
	
	}
	
}
