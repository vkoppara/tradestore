package com.venkata.tradestore.dao;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.venkata.tradestore.entity.TradePK;
import com.venkata.tradestore.entity.TradeRecord;

public interface TradeRecordRepo extends JpaRepository<TradeRecord, TradePK>{

	List<TradeRecord> findByTradeId(String tradeId);
	
	TradeRecord findTop1ByTradeIdOrderByVersionDesc(String tradeId);

	@Transactional
	@Modifying
	@Query("Update TradeRecord set expired=true where expired=false and maturityDate < CURRENT_DATE() ")
	void updateExpire();
	
}
