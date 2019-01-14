package com.qs.rpc.zktest;

import com.qs.rpc.util.ZkConnection;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * zookeeper提供的对znode的增删改查（zk api）
 *
 * @author qinyupeng
 * @since 2019-01-11 14:04:42
 */
public class TestZookeepter {

    private static ZooKeeper zooKeeper;

    private static ZkConnection zkConnection;

    private static String host = "localhost";


    static {
        try {
            zkConnection = new ZkConnection();
            zooKeeper = zkConnection.connect(host);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    //创建znode
    @Test
    public void createZnode() throws IOException, InterruptedException, KeeperException {
        String path = "/MyFirstZnode";
        byte[] data = "My First Zokeeper App".getBytes(StandardCharsets.UTF_8);

        /**
         * 参数：
         * acl：操作控制列表（操作权限的，当前设置为全部开发）
         * createMode：指定znode节点类型（当前节点设置为持久节点）
         *
         * 返回值：节点创建成功，返回创建成功的path节点字符串
         */
        String result = zooKeeper.create(path, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        System.out.println(result);
    }


    //判断znode是否存在
    @Test
    public void existZnode() throws KeeperException, InterruptedException {
        /**
         * 参数：
         * path：指定znode节点名称
         * watcher：是否监视指定znode节点
         *
         * 返回值：如果值存在，返回一个znode的元数据（一个stat仅提供一个znode的元数据。它由版本号，操作控制列表(ACL)，时间戳和数据长度组成。）
         */
        Stat existStat = zooKeeper.exists("/MyFirstZnode", false);
        System.out.println(existStat);
    }

    private Stat exist_znode(String path) throws KeeperException, InterruptedException {
        return zooKeeper.exists(path, false);
    }


    //获取znode信息并设置监听
    @Test
    public void getZnodeData() throws KeeperException, InterruptedException {
        final String path = "/MyFirstZnode";

        //线程等待，只要修改了znode节点值，程序才会退出。
        final CountDownLatch countDownLatch = new CountDownLatch(1);

        Stat stat = exist_znode(path);
        if (stat != null) {
            /**
             * 参数：
             * path：znode节点，
             * watcher：监听事件（需要实现process方法），
             * stat：znode对应元数据（可以为null）
             *
             *
             * 返回：获取path节点数据，并且开启一个监听事件（watcher），当再次修改该path的值时，会触发，并返回修改节点的值；
             *
             * 备注：mac测试时，可以使用sudo zkCli打开客户端，然后输入命令：set /MyFirstZnode hello，观察idea控制台日志输出hello；
             */
            byte[] b = zooKeeper.getData("/MyFirstZnode", new Watcher() {
                @Override
                public void process(WatchedEvent watchedEvent) {
                    if (watchedEvent.getType() == Event.EventType.None) {
                        switch (watchedEvent.getState()) {
                            case Expired:
                                countDownLatch.countDown();
                                break;
                        }
                    } else {
                        try {
                            String path = "/MyFirstZnode";
                            byte[] bytes = zooKeeper.getData(path, false, null);
                            String data = new String(bytes, StandardCharsets.UTF_8);
                            System.out.println(data);
                            countDownLatch.countDown();
                        } catch (KeeperException | InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }, null);

            System.out.println(new String(b, StandardCharsets.UTF_8));
            countDownLatch.await();
        } else {
            System.out.println("Node does not exists");
        }
    }

    //为节点赋值
    @Test
    public void setZnodeData() throws KeeperException, InterruptedException {
        String path = "/MyFirstZnode";
        byte[] data = "Success".getBytes(StandardCharsets.UTF_8);
        Stat stat = zooKeeper.exists(path, false);
        Stat newStat = zooKeeper.setData(path, data, stat.getVersion());
        System.out.println(newStat);
    }

    //获取子节点
    @Test
    public void getChildren() {
        String path = "/MyFirstZnode";
        try {
            Stat stat = zooKeeper.exists(path, null);
            if (stat != null) {
                List<String> children = zooKeeper.getChildren(path, false);
                for (String child : children) {
                    System.out.println(child);
                    byte[] data = zooKeeper.getData(path + "/" + child, false, null);
                    System.out.println(new String(data, StandardCharsets.UTF_8));
                }
            } else {
                System.out.println("Node dose not exist");
            }
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }


    //删除Znode
    @Test
    public void deleteZnode() throws KeeperException, InterruptedException {
        String path = "/MyFirstZnode/MySecondZnode";
        Stat stat = zooKeeper.exists(path, false);
        if (stat != null) {
            zooKeeper.delete(path, stat.getVersion());
        } else {
            System.out.println("Node does not exist");
        }
    }
}
