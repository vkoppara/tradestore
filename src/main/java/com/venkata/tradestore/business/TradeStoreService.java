package com.venkata.tradestore.business;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.venkata.tradestore.controller.TradeController;
import com.venkata.tradestore.dao.TradeRecordRepo;
import com.venkata.tradestore.entity.TradeRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author vkopp
 *
 */
@Component
public class TradeStoreService {
	
	private static final Logger logger = LoggerFactory.getLogger(TradeStoreService.class);


	@Autowired
	private TradeRecordRepo repo;

	/**
	 * This method is called to pre validate the input request before inserting or updating the trade records
	 * @param record
	 * @throws TradeStoreValidityException
	 * @throws ParseException
	 */
	private void preValidate(TradeRecord record) throws TradeStoreValidityException, ParseException {
		
		if(!record.getTradeId().isEmpty() && record.getVersion()>0) {
			Date today = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
			today = sdf.parse(sdf.format(today));
			if(record.getMaturityDate().before(today))
			{				
				throw new TradeStoreValidityException("The Maturity Date Cannot be Older than Current Date");
			}			
		}else {
			throw new TradeStoreValidityException("Either TradeId or Version is invalid");									
		}
	}

	/**
	 * This method is called to post validate and comaring the existing trade record with the new trade record
	 * @param record
	 * @param existingRecord
	 * @throws TradeStoreValidityException
	 */
	private void postValidate(TradeRecord record, TradeRecord existingRecord) throws TradeStoreValidityException {		
		if(existingRecord!=null) {			
			if(existingRecord.getVersion()>record.getVersion()) {
				throw new TradeStoreValidityException("version is less than existing.. cannot be inserted/updated");
			
			}
		}
		
	}

	
	/**
	 * This is called from the Controller to retrieve all the trade records
	 * @return
	 */
	public List<TradeRecord> getTradeRecords(){
		
		return repo.findAll();
		
	}
	
	/**
	 * This is called from the Controller to retreive the trade records belong to the input
	 * trade Id.
	 * @param tradeId
	 * @return
	 */
	public List<TradeRecord> getTradeRecordByTradeId(String tradeId){
		
		return repo.findByTradeId(tradeId);
	}
	
	
	/**
	 * This is called from the Controller to update/create a trade record
	 * @param record
	 * @throws TradeStoreValidityException
	 * @throws ParseException
	 */
	public void createRecord(TradeRecord record) throws TradeStoreValidityException, ParseException {
		try {
			this.preValidate(record);
			TradeRecord existingRecord = repo.findTop1ByTradeIdOrderByVersionDesc(record.getTradeId());
			this.postValidate(record,existingRecord);
			repo.save(record);
			logger.debug("Record has been saved/updated {}",record.getTradeId());
		}catch(Exception e) {
			logger.error("Error Occurred", e);
			throw e;
		}
	}

	
	/**
	 * This method is called from the cron job to update the expired field. 
	 */
	public void updateExpire() {
		repo.updateExpire();
		
	}
	

}
