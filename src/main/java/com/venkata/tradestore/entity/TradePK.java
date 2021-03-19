package com.venkata.tradestore.entity;

import java.io.Serializable;
import java.util.Objects;

public class TradePK implements Serializable{
	

	private static final long serialVersionUID = 4631621874013960956L;
	private String tradeId;
	private int version;
	public String getTradeId() {
		return tradeId;
	}
	public void setTradeId(String tradeId) {
		this.tradeId = tradeId;
	}
	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TradePK tradePk = (TradePK) o;
        return tradeId.equals(tradePk.tradeId) &&
        		version == tradePk.version;
    }
    
	@Override
    public int hashCode() {
        return Objects.hash(tradeId, version);
    }
	
}
