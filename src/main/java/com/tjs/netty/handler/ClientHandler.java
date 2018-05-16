package com.tjs.netty.handler;

import com.alibaba.fastjson.JSONObject;
import com.tjs.netty.client.DefaultFuture;
import com.tjs.netty.param.Response;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ClientHandler extends ChannelInboundHandlerAdapter {

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if("ping".equals(msg.toString())) {
			ctx.channel().writeAndFlush("ping\r\n");
			return ;
		}
		System.out.println("client received = " + msg.toString());
		
		Response response = JSONObject.parseObject(msg.toString(), Response.class);
		DefaultFuture.receive(response);
		
		//System.out.println("Handler AttributeKey Value = " + ctx.channel().attr(AttributeKey.valueOf(Constants.AttributeKey)).get());
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		ctx.close();
	}
	
	
}
