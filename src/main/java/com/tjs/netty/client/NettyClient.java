package com.tjs.netty.client;

import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.tjs.netty.constant.Constants;
import com.tjs.netty.handler.ClientHandler;
import com.tjs.netty.param.ClientRequest;
import com.tjs.netty.param.Response;
import com.tjs.netty.zk.ChannelManager;
import com.tjs.netty.zk.ZookeeperUtil;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

public class NettyClient {
	
	public static final Bootstrap  b = new Bootstrap();
	//static ChannelFuture f  = null;
	
	static Logger logger = LoggerFactory.getLogger(NettyClient.class);
	
	static {
		EventLoopGroup workGroup = new NioEventLoopGroup();
		b.group(workGroup);
		b.channel(NioSocketChannel.class);
		b.option(ChannelOption.SO_KEEPALIVE, true);
		b.handler(new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				ch.pipeline().addLast(new DelimiterBasedFrameDecoder(Integer.MAX_VALUE, Delimiters.lineDelimiter()[0]));
				ch.pipeline().addLast(new StringDecoder());
				ch.pipeline().addLast(new ClientHandler());
				ch.pipeline().addLast(new StringEncoder());
			}
		});
		try {
			//String oneServerPaths = ChannelManager.getOneServerPaths();
			//System.out.println(oneServerPaths);
			//f = b.connect(Constants.HOST, Constants.PORT).sync();
		} /*catch (InterruptedException e) {
			logger.error("Netty Client failed! 客户端连接失败");
			e.printStackTrace();
		} */catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 功能： 发送数据
	 * 注意：每一个请求都是同一个连接,并发问题
	 * 
	 * Request 
	 * 	  1、唯一请求id 
	 * 	  2、请求内容 
	 * Response 
	 * 	  1、响应唯一 识别码id 
	 * 	  2、响应结果
	 */	
	public static Response send(ClientRequest request) {
		
		ChannelFuture f = ChannelManager.getChannel(new AtomicInteger(1));
		f.channel().writeAndFlush(JSONObject.toJSONString(request));
		f.channel().writeAndFlush("\r\n");
		
		DefaultFuture df = new DefaultFuture(request);
		return df.get();
	}
	
	public static void registerToZookeeper() throws Exception {
		CuratorFramework client = ZookeeperUtil.getZookerClient(Constants.WORK_SPACE);

		String hostAddress = InetAddress.getLocalHost().getHostAddress();
		String path = "/" + hostAddress + "#";
		if (client.checkExists().forPath(path) != null) {
			client.delete().deletingChildrenIfNeeded().forPath(path);
		}
		client.create().creatingParentContainersIfNeeded().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(path);
	}
}
