package com.venkata.tradestore.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.venkata.tradestore.service.TradeStoreService;

/**
 * @author vkopp
 *
 */
@Component
@EnableScheduling
public class TradeStoreExpiredJob {
	
	private static final Logger logger = LoggerFactory.getLogger(TradeStoreExpiredJob.class);
	
	@Autowired
	TradeStoreService tsbLayer;
	
	
	/**
	 * updateExpiry is a cron job runs every day at 12pm to update the expried flag
	 */
	@Scheduled(cron = "${scheduling.cron}")
	public void updateExpiry() {
		logger.info("Scheduler is running.... to update the expired flag");
		tsbLayer.updateExpire();
		
	}

}
