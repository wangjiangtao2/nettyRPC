package com.tjs.netty.param;

import java.util.concurrent.atomic.AtomicInteger;

public class ClientRequest {
	
	private final long id ;
	private Object content;
	private String command;// 类名全路径.方法名  eg: com.tjs.user.controller.UserController.test03

	
	private AtomicInteger iAtomicInteger = new AtomicInteger(1);
	
	public ClientRequest() {
		id = iAtomicInteger.incrementAndGet();
	}

	public Object getContent() {
		return content;
	}

	public void setContent(Object content) {
		this.content = content;
	}

	public AtomicInteger getiAtomicInteger() {
		return iAtomicInteger;
	}

	public void setiAtomicInteger(AtomicInteger iAtomicInteger) {
		this.iAtomicInteger = iAtomicInteger;
	}

	public long getId() {
		return id;
	}

	
	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	@Override
	public String toString() {
		return "ClientRequest [id=" + id + ", content=" + content + ", command=" + command + ", iAtomicInteger="
				+ iAtomicInteger + "]";
	}
	
}
