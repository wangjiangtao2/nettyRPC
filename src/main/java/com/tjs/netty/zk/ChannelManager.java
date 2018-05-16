package com.tjs.netty.zk;

import java.util.List;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCache.StartMode;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent.Type;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;

import com.tjs.netty.client.NettyClient;
import com.tjs.netty.constant.Constants;

import io.netty.channel.ChannelFuture;

@SuppressWarnings("all")
public class ChannelManager {

	public static CopyOnWriteArrayList<ChannelFuture> channelFutures = new CopyOnWriteArrayList<ChannelFuture>();
	private static Vector<String> serverpaths = new Vector<String>();

	private static AtomicInteger position = new AtomicInteger(0);

	static {
		try {
			getAllServerPaths();
			updateServerPahts();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static ChannelFuture getChannel(AtomicInteger position) {
		int size = ChannelManager.channelFutures.size();
		ChannelFuture channelFuture;
		if (position.get() > size) {
			channelFuture = ChannelManager.channelFutures.get(0);
			ChannelManager.position = new AtomicInteger(1);
		} else {
			int i = ChannelManager.position.get();
			channelFuture = ChannelManager.channelFutures.get(0);
		}
		return channelFuture;
	}

	/**
	 * @Description:获取zookeeper上注册的所有 netty服务器地址 添加zk 监听器
	 */
	public static void updateServerPahts() throws Exception {

		CuratorFramework client = ZookeeperUtil.getZookerClient(Constants.WORK_SPACE);
		// 为子节点添加watcher
		// PathChildrenCache: 监听数据节点的增删改，会触发事件
		String childNodePathCache = "/";
		// cacheData: 设置缓存节点的数据状态
		final PathChildrenCache childrenCache = new PathChildrenCache(client, childNodePathCache, true);
		/**
		 * StartMode: 初始化方式 POST_INITIALIZED_EVENT：异步初始化，初始化之后会触发事件 NORMAL：异步初始化,
		 * 初始化之后不会触发事件 BUILD_INITIAL_CACHE：同步初始化
		 */
		try {
			childrenCache.start(StartMode.POST_INITIALIZED_EVENT);
		} catch (Exception e) {
			e.printStackTrace();
		}
		childrenCache.getListenable().addListener(new PathChildrenCacheListener() {
			public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
				Type eventType = event.getType();
				if (eventType.equals(PathChildrenCacheEvent.Type.CHILD_ADDED)
						|| eventType.equals(PathChildrenCacheEvent.Type.CHILD_REMOVED)
						|| eventType.equals(PathChildrenCacheEvent.Type.CHILD_UPDATED)) {
					getAllServerPaths();
				}
			}
		});
	}

	/**
	 * 
	 * @Description: 从 zookeeper 中获取最新的netty 服务器 @throws
	 */
	public static void getAllServerPaths() {

		CuratorFramework client = ZookeeperUtil.getZookerClient(Constants.WORK_SPACE);
		List<String> serverPaths = null;
		try {
			serverPaths = client.getChildren().forPath("/");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		ChannelManager.serverpaths.clear();
		for (String serverPath : serverPaths) {
			// TODO 这里加权注意点，空指针异常，因为有些是随机策略
			String hostAddress = serverPath.split("#")[0];
			int port = Integer.valueOf(serverPath.split("#")[1]);
			int weight = Integer.valueOf(serverPath.split("#")[2]);
			if (weight > 1) {
				for(int i =0 ;i< weight;i++) {
					ChannelManager.serverpaths.addElement(hostAddress + "#" + port + "#");
				}
			}
		}
		
		ChannelManager.channelFutures.clear();
		for (String serverPath : serverPaths) {
			
			String hostAddress = serverPath.split("#")[0];
			int port = Integer.valueOf(serverPath.split("#")[1]);
			ChannelFuture channel = NettyClient.b.connect(hostAddress, port);
			ChannelManager.addChannel(channel);
		}
	}

	/**
	 * @Description:获取单个netty服务器地址 以后这里可以定义策略 目前是随机
	 */
	public static String getOneServerPaths() {
		return serverpaths.get(new Random().nextInt(serverpaths.size()));
	}

	public static void removeChannel(ChannelFuture channel) {
		channelFutures.remove(channel);
	}

	public static void addChannel(ChannelFuture channel) {
		channelFutures.add(channel);
	}

	public static void clearChannel() {
		channelFutures.clear();
	}
}
