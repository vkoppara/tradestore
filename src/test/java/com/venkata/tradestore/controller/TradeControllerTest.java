package com.venkata.tradestore.controller;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.venkata.tradestore.business.TradeStoreServiceImpl;
import com.venkata.tradestore.entity.TradeRecord;



/**
 * TradeController JUnit tests
 * @author vkopp
 *
 */
@RunWith(MockitoJUnitRunner.class)
@WebMvcTest(value  = TradeController.class)
public class TradeControllerTest  {
	
	@Autowired
	private MockMvc mockMvc;
	
	@MockBean
	private TradeStoreServiceImpl service;
	
	@Test
	@DisplayName("TradeController createRecord Test")
	public void createRecord() throws Exception{
		String content="{\r\n" + 
				"    \"tradeId\": \"T1\",\r\n" + 
				"    \"version\": 2,\r\n" + 
				"    \"counterPartyId\": \"CP-1\",\r\n" + 
				"    \"bookId\": \"B2\",\r\n" + 
				"    \"maturityDate\": \"21/03/2021\"\r\n" + 
				"}";
		doNothing().when(service).createRecord(Mockito.any(TradeRecord.class));
		RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/tradeRecords")
				.contentType(MediaType.APPLICATION_JSON)
				.content(content);
		
		MvcResult result = mockMvc.perform(requestBuilder).andReturn();
		System.out.println(result.getResponse().getContentAsString());
		assertTrue(result.getResponse().getContentAsString().contains("Successfully"));			
	}
	
	@Test
	@DisplayName("TradeController getTradeRecords Test")
	public void getTradeRecords() throws Exception{
		List<TradeRecord> tradeRecords = new ArrayList<TradeRecord>();
		TradeRecord tr = new TradeRecord();
		tr.setTradeId("101");
		tr.setVersion(1);
		tradeRecords.add(tr);
		doReturn(tradeRecords).when(service).getTradeRecords();
		RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/tradeRecords")
				.contentType(MediaType.APPLICATION_JSON);
		
		MvcResult result = mockMvc.perform(requestBuilder).andReturn();
		System.out.println(result.getResponse().getContentAsString());
		assertTrue(result.getResponse().getContentAsString().contains("101"));			
	}
	
	
	@Test
	@DisplayName("TradeController getTradeRecordByTradeId Test")
	public void getTradeRecordByTradeId() throws Exception{
		List<TradeRecord> tradeRecords = new ArrayList<TradeRecord>();
		TradeRecord tr = new TradeRecord();
		tr.setTradeId("101");
		tr.setVersion(1);
		tradeRecords.add(tr);
		doReturn(tradeRecords).when(service).getTradeRecordByTradeId(Mockito.anyString());
		RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/tradeRecords/101")
				.contentType(MediaType.APPLICATION_JSON);
		
		MvcResult result = mockMvc.perform(requestBuilder).andReturn();
		System.out.println(result.getResponse().getContentAsString());
		assertTrue(result.getResponse().getContentAsString().contains("101"));			
	}

}
