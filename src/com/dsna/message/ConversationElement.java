package com.dsna.message;

public class ConversationElement {
	
	private String name;
	private String lastMsg;
	private String lastTime;

	public String getName() {
	    return this.name;
	}

	public String getLastMsg() {
	    return this.lastMsg;
	}

	public String getLastTime() {
	    return this.lastTime;
	}

	public void setName(String name) {
	    this.name = name;
	}

	public void setLastMsg(String lastMsg) {
	    this.lastMsg = lastMsg;
	}

	public void setLastTime(String lastTime) {
	    this.lastTime = lastTime;
	}

}
