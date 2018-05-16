package com.tjs.netty.zk;

import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.zookeeper.WatchedEvent;

public class ServerWacther  implements CuratorWatcher {

	@Override
	public void process(WatchedEvent event) throws Exception {
		
	}
	
}
