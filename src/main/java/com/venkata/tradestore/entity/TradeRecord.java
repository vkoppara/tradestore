package com.venkata.tradestore.entity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import org.springframework.data.annotation.CreatedDate;

import com.fasterxml.jackson.annotation.JsonFormat;


/**
 * TradeRecord is the main entity class to store the trade records.
 * @author vkopp
 *
 */
@Entity
@IdClass(TradePK.class)
public class TradeRecord {
	
	@Id
	private String tradeId;
	@Id
	private int version;
	private String counterPartyId;
	private String bookId;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy",  timezone = "IST")
	private Date maturityDate;
	@CreatedDate
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy",  timezone = "IST")
	private Date createdDate;	
	private Boolean expired;
	
	
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
	public String getCounterPartyId() {
		return counterPartyId;
	}
	public void setCounterPartyId(String counterPartyId) {
		this.counterPartyId = counterPartyId;
	}
	public String getBookId() {
		return bookId;
	}
	public void setBookId(String bookId) {
		this.bookId = bookId;
	}
	public Date getMaturityDate() {
		return maturityDate;
	}
	public void setMaturityDate(Date maturityDate) {
		this.maturityDate = maturityDate;
	}
	public Date getCreatedDate() {
		return createdDate;
	}
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}
	public Boolean getExpired() {
		return expired;
	}
	public void setExpired(Boolean expired) {
		this.expired = expired;
	}
		
	@PrePersist
	@PreUpdate
    protected void setCreatedDate() throws ParseException {
	    Date today = new Date();
	    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
	    today = sdf.parse(sdf.format(today));
        this.createdDate = today;
        expired=false;
    }
	
	

}
