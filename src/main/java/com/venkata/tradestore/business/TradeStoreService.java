package com.venkata.tradestore.business;

import java.text.ParseException;
import java.util.List;

import com.venkata.tradestore.entity.TradeRecord;

/**
 * @author vkopp
 *
 */
public interface TradeStoreService {

	/**
	 * This is called from the Controller to retrieve all the trade records
	 * @return
	 */
	List<TradeRecord> getTradeRecords();

	/**
	 * This is called from the Controller to retreive the trade records belong to the input
	 * trade Id.
	 * @param tradeId
	 * @return
	 */
	List<TradeRecord> getTradeRecordByTradeId(String tradeId);

	/**
	 * This is called from the Controller to update/create a trade record
	 * @param record
	 * @throws TradeStoreValidityException
	 * @throws ParseException
	 */
	void createRecord(TradeRecord record) throws TradeStoreValidityException, ParseException;

	/**
	 * This method is called from the cron job to update the expired field. 
	 */
	void updateExpire();

}