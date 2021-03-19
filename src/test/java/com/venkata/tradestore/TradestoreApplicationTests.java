package com.venkata.tradestore;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.venkata.tradestore.controller.TradeController;

@RunWith(SpringRunner.class)
@SpringBootTest
class TradestoreApplicationTests {
	
	@Autowired
	private TradeController tradeController;

	@Test
	void contextLoads() throws Exception {
		assertThat(tradeController).isNotNull();
	}

}
