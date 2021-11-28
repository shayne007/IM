package com.feng.chat.server.distribute;

import com.feng.chat.common.constants.ServerConstants;
import com.feng.chat.common.entity.ImNode;
import com.feng.chat.common.util.GsonUtil;
import com.feng.chat.common.zk.CuratorZKclient;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

/**
 * @Description IM节点的ZK协调客户端 Im server node注册到zk后通过该实例进行管理
 * @Author fengsy
 * @Date 9/29/21
 */
@Data
@Slf4j
public class ImWorker {
    private CuratorFramework client = null;
    private String pathRegistered = null;
    private ImNode localNode = null;

    private static ImWorker instance = null;
    private boolean inited = false;

    private ImWorker() {
    }

    /**
     * @return 单例对象
     */
    public synchronized static ImWorker getInst() {
        if (instance == null) {
            instance = new ImWorker();
            instance.localNode = new ImNode();
        }
        return instance;
    }

    /**
     * 初始化
     */
    public synchronized void init() {
        if (inited) {
            return;
        }
        inited = true;
        if (client == null) {
            this.client = CuratorZKclient.instance.getClient();
        }
        if (localNode == null) {
            localNode = new ImNode();
        }
        createParentIfNeeded(ServerConstants.MANAGE_PATH);

        byte[] payload = GsonUtil.object2JsonBytes(localNode);

        try {
            pathRegistered = client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                    .forPath(ServerConstants.PATH_PREFIX, payload);
            localNode.setId(getIdByPath(pathRegistered));
            log.info("本地节点，path={}, id={}", pathRegistered, localNode.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void setLocalNode(String ip, int port) {
        localNode.setHost(ip);
        localNode.setPort(port);
    }

    /**
     * 增加负载，表示有用户登录成功
     *
     * @return 成功状态
     */
    public boolean increLoad() {
        if (null == localNode) {
            throw new RuntimeException("还没有设置Node 节点");
        }
        // 增加负载：增加负载，并写回zookeeper
        while (true) {
            try {
                localNode.incrementLoad();
                byte[] payload = GsonUtil.object2JsonBytes(localNode);
                client.setData().forPath(pathRegistered, payload);
                return true;
            } catch (Exception e) {
                return false;
            }
        }

    }

    /**
     * 减少负载，表示有用户下线，写回zookeeper
     *
     * @return 成功状态
     */
    public boolean decreLoad() {
        if (null == localNode) {
            throw new RuntimeException("还没有设置Node 节点");
        }
        while (true) {
            try {

                localNode.decrementLoad();

                byte[] payload = GsonUtil.object2JsonBytes(localNode);
                client.setData().forPath(pathRegistered, payload);
                return true;
            } catch (Exception e) {
                return false;
            }
        }

    }

    /**
     * 从节点路径中获取IM节点编号
     *
     * @param path
     * @return
     */
    public long getIdByPath(String path) {
        String sid = null;
        if (null == path) {
            throw new RuntimeException("节点路径有误");
        }
        int index = path.lastIndexOf(ServerConstants.PATH_PREFIX);
        if (index >= 0) {
            index += ServerConstants.PATH_PREFIX.length();
            sid = index <= path.length() ? path.substring(index) : null;
        }
        if (null == sid) {
            throw new RuntimeException("节点Id获取失败");
        }
        return Long.parseLong(sid);
    }

    private void createParentIfNeeded(String managePath) {
        try {
            Stat stat = client.checkExists().forPath(managePath);
            if (stat == null) {
                client.create().creatingParentsIfNeeded().withProtection().withMode(CreateMode.PERSISTENT)
                        .forPath(managePath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
