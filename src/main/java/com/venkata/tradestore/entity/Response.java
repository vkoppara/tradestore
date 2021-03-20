package com.venkata.tradestore.entity;

/**
 * Response is used to generate success json responses
 * @author vkopp
 *
 */
public class Response {
	
	private String tradeId;
	private int version;
	private String statusMessage;

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
	public String getStatusMessage() {
		return statusMessage;
	}
	public void setStatusMessage(String statusMessage) {
		this.statusMessage = statusMessage;
	}

}
