package com.venkata.tradestore.controller;

import static org.mockito.Mockito.doNothing;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.venkata.tradestore.business.TradeStoreService;
import com.venkata.tradestore.entity.TradeRecord;

import junit.framework.Assert;


@RunWith(SpringRunner.class)
@WebMvcTest(value  = TradeController.class)
public class TradeControllerTest  {
	
	@Autowired
	private MockMvc mockMvc;
	
	@MockBean
	private TradeStoreService service;
	
	@Test
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
		Assert.assertTrue(result.getResponse().getContentAsString().contains("Successfully"));
		
		
	}
	

}
