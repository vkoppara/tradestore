package com.venkata.tradestore;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.venkata.tradestore.business.TradeStoreService;

@SpringBootApplication
@EnableScheduling
public class TradestoreApplication {
	
	@Autowired
	TradeStoreService tsbLayer;

	public static void main(String[] args) {
		SpringApplication.run(TradestoreApplication.class, args);
	}

	@Scheduled(cron = "${scheduling.cron}")
	public void updateExpiry() {
		System.out.println("Scheduler is running");
		tsbLayer.updateExpire();
		
	}
}