package com.venkata.tradestore.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.transaction.Transactional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.test.annotation.Commit;

import com.venkata.tradestore.entity.TradeRecord;

@DataJpaTest
@RunWith(MockitoJUnitRunner.class)
@AutoConfigureTestDatabase(replace = Replace.NONE)
public class TradeRecordRepoTest {

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private TradeRecordRepo repo;

	@Test
	@DisplayName("TradeRecordRepo test - for findAll")
	public void findAll() {
		assertThat(repo.findAll().size()).isGreaterThan(0);
	}

	@Test
	@DisplayName("TradeRecordRepo test - for findByTradeId")
	public void findByTradeId() {
		assertThat(repo.findByTradeId("T1").size()).isGreaterThan(0);
	}

	@Test
	@DisplayName("TradeRecordRepo test - for findTop1ByTradeIdOrderByVersionDesc")
	public void findTop1ByTradeIdOrderByVersionDesc() {
		assertThat(repo.findTop1ByTradeIdOrderByVersionDesc("T1").getTradeId()).isEqualTo("T1");
	}

	@Test
	@DisplayName("TradeRecordRepo test - for updateExpire")	
	public void updateExpire() throws ParseException {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.DATE, -2);
		Date dateBefore2Days = cal.getTime();
		repo.updateExpire();
		entityManager.persist(new TradeRecord("TEST100", 1, "CP-1", "B1",dateBefore2Days));
		assertThat(repo.findByTradeId("TEST100").get(0).getExpired()).isFalse();
		repo.updateExpire();		
		assertThat(repo.findByTradeId("TEST100").get(0).getExpired()).isTrue();
	}
	
		
	

}
