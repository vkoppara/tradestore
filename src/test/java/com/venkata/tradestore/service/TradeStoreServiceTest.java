package com.venkata.tradestore.service;

import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;

import java.text.ParseException;
import java.util.Date;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import com.venkata.tradestore.business.TradeStoreService;
import com.venkata.tradestore.business.TradeStoreValidityException;
import com.venkata.tradestore.controller.TradeController;
import com.venkata.tradestore.dao.TradeRecordRepo;
import com.venkata.tradestore.entity.TradeRecord;

@RunWith(SpringRunner.class)
@WebMvcTest(value  = TradeStoreService.class)
public class TradeStoreServiceTest {
	

	@MockBean
	private TradeRecordRepo repo;
	
	@InjectMocks
	private TradeStoreService service;
	
	@Test
	public void createRecord() throws TradeStoreValidityException, ParseException {
		TradeRecord record = new TradeRecord();
		record.setTradeId("T1");
		record.setVersion(1);
		record.setMaturityDate(new Date());
  	    Mockito.when(repo.findTop1ByTradeIdOrderByVersionDesc(Mockito.anyString())).thenReturn(record);
  	    Mockito.when(repo.save(Mockito.any(TradeRecord.class))).thenReturn(null);
  	   // service.createRecord(record);
	}

}
