package com.qs.rpc.util;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * zookeeper工具类，提供获取zk连接和关闭zk连接方法
 */
public class ZkConnection {
    private ZooKeeper zooKeeper;

    private final CountDownLatch connectSignal = new CountDownLatch(1);

    public ZooKeeper connect(String host) throws IOException, InterruptedException {
        zooKeeper = new ZooKeeper(host, 2000, new Watcher() {
            @Override
            public void process(WatchedEvent watchedEvent) {
                if (watchedEvent.getState() == Event.KeeperState.SyncConnected) {
                    connectSignal.countDown();
                }
            }
        });

        connectSignal.await();
        return zooKeeper;
    }


    public void close() throws InterruptedException {
        zooKeeper.close();
    }

    /**
     * 注意：测试获取zk连接前，请确认zookeeper服务已经打开
     *
     * mac启动zookeeper命令：
     *  sudo zkServer start
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        ZkConnection zkConnection = new ZkConnection();
        ZooKeeper zk = zkConnection.connect("127.0.0.1");
        System.out.println(zk);
    }
}
