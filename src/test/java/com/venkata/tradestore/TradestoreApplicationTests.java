package com.venkata.tradestore;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import com.venkata.tradestore.controller.TradeController;

/**
 * TradeStoreApplicationTests
 * @author vkopp
 *
 */
@RunWith(MockitoJUnitRunner.class)
@TestPropertySource(locations="classpath:application-test.properties")
@SpringBootTest
class TradestoreApplicationTests {
	
	@Autowired
	private TradeController tradeController;

	@Test
	@DisplayName("Trade Store Application Test")
	void contextLoads() throws Exception {
		assertThat(tradeController).isNotNull();
	}

}
