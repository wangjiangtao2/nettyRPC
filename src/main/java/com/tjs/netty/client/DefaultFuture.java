package com.tjs.netty.client;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tjs.netty.param.ClientRequest;
import com.tjs.netty.param.Response;

@SuppressWarnings("all")
public class DefaultFuture {
	
	public final static long timeout = 2 * 60;// 2min分钟;//链路超时模块
	public final static long startTime = System.currentTimeMillis();
	
	//存储所有请求的
	public final static ConcurrentHashMap<Long, DefaultFuture> allDefaultFuture = new ConcurrentHashMap<Long, DefaultFuture>();
	
	final Lock lock  = new ReentrantLock();
	private  Condition condition = lock.newCondition();

	private Response response;
	
	public Logger logger =  LoggerFactory.getLogger(DefaultFuture.class);
	
	public DefaultFuture(ClientRequest request) {
		allDefaultFuture.put(request.getId(), this);
	}

	//主线程获取数据，首先要等待结果
	public Response get() {
		lock.lock();
		try {
			while(!done()) {
				condition.await(timeout, TimeUnit.SECONDS);
				if((System.currentTimeMillis() - startTime) > timeout) {
					logger.warn("请求超时!!!!");
					break;
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			lock.unlock();
		}
		return this.response;
	}
	
	
	private boolean done() {
		if(this.response != null) {
			return true;
		}
		return false;
	}

	public static void receive(Response response) {
		
		DefaultFuture df = allDefaultFuture.get(response.getId());
		if(df != null) {
			Lock lock = df.lock;
			lock.lock();
			try {
				df.setResponse(response);
				df.condition.signal();;
				allDefaultFuture.remove(response.getId());
			} finally {
				lock.unlock();
			}
		}
		
	}
	
	static class FutureThread extends Thread {
		@Override
		public void run() {
			Set<Long> keySet = allDefaultFuture.keySet();
			for (Long id : keySet) {
				DefaultFuture df = allDefaultFuture.get(id);
				if(df == null) {
					allDefaultFuture.remove(df);
				} else {
					//链路超时
					if( (System.currentTimeMillis()- df.getStarttime()) > df.getTimeout()) {
						Response resp = new Response();
						resp.setId(id);
						resp.setCode("33333");
						resp.setMsg("链路请求 超时");
						DefaultFuture.receive(resp);
					}
				}
			}
		}
	}
	
	static {
		FutureThread futureThread = new FutureThread();
		futureThread.setDaemon(true);
		futureThread.start();
	}
	
	public Response getResponse() {
		return response;
	}

	public void setResponse(Response response) {
		this.response = response;
	}

	public static long getTimeout() {
		return timeout;
	}

	public static long getStarttime() {
		return startTime;
	}

}
