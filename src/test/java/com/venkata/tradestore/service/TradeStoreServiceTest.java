package com.venkata.tradestore.service;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import com.venkata.tradestore.business.TradeStoreService;
import com.venkata.tradestore.business.TradeStoreValidityException;
import com.venkata.tradestore.dao.TradeRecordRepo;
import com.venkata.tradestore.entity.TradeRecord;


@RunWith(MockitoJUnitRunner.class)
public class TradeStoreServiceTest {

	@InjectMocks
	private TradeStoreService service = new TradeStoreService();

	private TradeRecordRepo repo = Mockito.mock(TradeRecordRepo.class);

	
	@Test
	@DisplayName("case -1 No existing record")
	public void createRecord1() throws TradeStoreValidityException, ParseException {
		TradeRecord record = new TradeRecord();
		record.setTradeId("T1");
		record.setVersion(1);
		record.setMaturityDate(new Date());
		doReturn(null).when(repo).findTop1ByTradeIdOrderByVersionDesc(Mockito.anyString());
		doReturn(null).when(repo).save(Mockito.any(TradeRecord.class));
		ReflectionTestUtils.setField(service, "repo", repo);
		service.createRecord(record);
	}

	
	@Test
	@DisplayName("case -2 where existing record version and new record version is same.")
	public void createRecord2() throws TradeStoreValidityException, ParseException {
		TradeRecord record = new TradeRecord();
		record.setTradeId("T1");
		record.setVersion(1);
		record.setMaturityDate(new Date());
		doReturn(record).when(repo).findTop1ByTradeIdOrderByVersionDesc(Mockito.anyString());
		doReturn(null).when(repo).save(Mockito.any(TradeRecord.class));
		ReflectionTestUtils.setField(service, "repo", repo);
		service.createRecord(record);
	}


	@Test
	@DisplayName("case -3 where existing record version and new record version is lower than")
	public void createRecord3() {
		TradeRecord record = new TradeRecord();
		record.setTradeId("T1");
		record.setVersion(1);
		record.setMaturityDate(new Date());
		TradeRecord existingrecord = new TradeRecord();
		existingrecord.setTradeId("T1");
		existingrecord.setVersion(2);
		existingrecord.setMaturityDate(new Date());
		doReturn(existingrecord).when(repo).findTop1ByTradeIdOrderByVersionDesc(Mockito.anyString());
		doReturn(null).when(repo).save(Mockito.any(TradeRecord.class));
		ReflectionTestUtils.setField(service, "repo", repo);
		try {
			service.createRecord(record);
			fail("Didn't throw an error");
		} catch (Exception e) {
			assertTrue(e.getMessage().contains("version is less than existing"));
		}
	}


	@Test
	@DisplayName("case -4 where existing record version and new record version is higher than existing")
	public void createRecord4() {
		TradeRecord record = new TradeRecord();
		record.setTradeId("T1");
		record.setVersion(2);
		record.setMaturityDate(new Date());
		TradeRecord existingrecord = new TradeRecord();
		existingrecord.setTradeId("T1");
		existingrecord.setVersion(1);
		existingrecord.setMaturityDate(new Date());
		doReturn(existingrecord).when(repo).findTop1ByTradeIdOrderByVersionDesc(Mockito.anyString());
		doReturn(null).when(repo).save(Mockito.any(TradeRecord.class));
		ReflectionTestUtils.setField(service, "repo", repo);
		try {
			service.createRecord(record);

		} catch (Exception e) {
			fail("Throwed an exception");
		}
	}


	@Test
	@DisplayName("case -5 where existing record version and new record version is same as existing but maturity date is in past.")
	public void createRecord5() {
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.DATE, -2);
		Date dateBefore2Days = cal.getTime();
		
		
		TradeRecord record = new TradeRecord();
		record.setTradeId("T1");
		record.setVersion(1);
		record.setMaturityDate(dateBefore2Days);
		TradeRecord existingrecord = new TradeRecord();
		existingrecord.setTradeId("T1");
		existingrecord.setVersion(1);
		existingrecord.setMaturityDate(new Date());
		doReturn(existingrecord).when(repo).findTop1ByTradeIdOrderByVersionDesc(Mockito.anyString());
		doReturn(null).when(repo).save(Mockito.any(TradeRecord.class));
		ReflectionTestUtils.setField(service, "repo", repo);
		try {
			service.createRecord(record);
			fail("Didn't throw an exception");
		} catch (Exception e) {
			assertTrue(e.getMessage().contains("The Maturity Date Cannot be Older than Current Date"));
		}
	}
	
	 
	@Test
	@DisplayName("case -6 where existing record version and new record version is higher than existing")
	public void createRecord6() {
		TradeRecord record = new TradeRecord();
		record.setTradeId("T1");
		record.setVersion(0);
		record.setMaturityDate(new Date());
		TradeRecord existingrecord = new TradeRecord();
		existingrecord.setTradeId("T1");
		existingrecord.setVersion(1);
		existingrecord.setMaturityDate(new Date());
		doReturn(existingrecord).when(repo).findTop1ByTradeIdOrderByVersionDesc(Mockito.anyString());
		doReturn(null).when(repo).save(Mockito.any(TradeRecord.class));
		ReflectionTestUtils.setField(service, "repo", repo);
		try {
			service.createRecord(record);
			fail("Didn't throw an exception");
		} catch (Exception e) {
			assertTrue(e.getMessage().contains("Either TradeId or Version is invalid"));
		}
	}

}
