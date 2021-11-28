package com.feng.chat.server.distribute;

import com.feng.chat.common.constants.ServerConstants;
import com.feng.chat.common.entity.ImNode;
import com.feng.chat.common.msg.proto.ProtoMsg;
import com.feng.chat.common.util.GsonUtil;
import com.feng.chat.common.util.ThreadUtil;
import com.feng.chat.common.zk.CuratorZKclient;
import com.feng.chat.server.protoBuilder.NotificationMsgBuilder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * @Description work node 路由管理，负责监听节点增加、删除事件，管理消息转发的sender
 * @Author fengsy
 * @Date 9/29/21
 */
@Data
@Slf4j
public class WorkerRouter {
    private CuratorFramework client = null;

    private String pathRegistered = null;
    private ImNode node = null;

    private static WorkerRouter singleInstance = null;
    private static final String path = ServerConstants.MANAGE_PATH;

    private ConcurrentHashMap<Long, PeerSender> workerMap = new ConcurrentHashMap<>();

    private BiConsumer<ImNode, PeerSender> runAfterAdd = (node, relaySender) -> {
        doAfterAdd(node, relaySender);
    };

    private Consumer<ImNode> runAfterRemove = (node) -> {
        doAfterRemove(node);
    };

    public synchronized static WorkerRouter getInst() {
        if (null == singleInstance) {
            singleInstance = new WorkerRouter();
        }
        return singleInstance;
    }

    private WorkerRouter() {
    }

    public PeerSender route(long nodeId) {
        log.info("workerMap: " + workerMap);
        PeerSender peerSender = workerMap.get(nodeId);
        if (null != peerSender) {
            return peerSender;
        }
        return null;
    }

    public void sendNotification(String json) {
        workerMap.keySet().stream().forEach(key -> {
            if (!key.equals(getLocalNode().getId())) {
                PeerSender peerSender = workerMap.get(key);
                ProtoMsg.Message pkg = NotificationMsgBuilder.buildNotification(json);
                peerSender.writeAndFlush(pkg);
            }
        });

    }

    private ImNode getLocalNode() {
        return ImWorker.getInst().getLocalNode();
    }

    private boolean inited = false;

    /**
     * 初始化节点管理
     */
    public void init() {
        if (inited) {
            return;
        }
        inited = true;
        try {
            if (null == client) {
                this.client = CuratorZKclient.instance.getClient();
            }
            //订阅节点的增加和删除事件
            PathChildrenCache childrenCache = new PathChildrenCache(client, path, true);
            PathChildrenCacheListener childrenCacheListener = (client, event) -> {
                log.info("开始监听其他的ImWorker子节点:-----");
                ChildData data = event.getData();
                switch (event.getType()) {
                    case CHILD_ADDED:
                        log.info("CHILD_ADDED : " + data.getPath() + "  数据:" + data.getData());
                        processNodeAdded(data);
                        break;
                    case CHILD_REMOVED:
                        log.info("CHILD_REMOVED : " + data.getPath() + "  数据:" + data.getData());
                        processNodeRemoved(data);
                        break;
                    case CHILD_UPDATED:
                        log.info("CHILD_UPDATED : " + data.getPath() + "  数据:" + new String(data.getData()));
                        break;
                    default:
                        log.debug("[PathChildrenCache]节点数据为空, path={}", data == null ? "null" : data.getPath());
                        break;
                }
            };
            childrenCache.getListenable().addListener(childrenCacheListener, ThreadUtil.getIoIntenseTargetThreadPool());
            System.out.println("Register zk watcher successfully!");
            childrenCache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 节点增加的处理
     *
     * @param data 新节点
     */
    private void processNodeAdded(ChildData data) {
        byte[] payload = data.getData();
        ImNode node = GsonUtil.jsonBytes2Object(payload, ImNode.class);

        long id = ImWorker.getInst().getIdByPath(data.getPath());
        node.setId(id);

        log.info("[PathChildrenCache]节点更新端口, path={}, data={}", data.getPath(), GsonUtil.pojoToJson(node));

        if (node.equals(getLocalNode())) {
            log.info("[PathChildrenCache]本地节点, path={}, data={}", data.getPath(), GsonUtil.pojoToJson(node));
            return;
        }
        PeerSender relaySender = workerMap.get(node.getId());
        //重复收到注册的事件
        if (null != relaySender && relaySender.getRemoteNode().equals(node)) {
            log.info("[PathChildrenCache]节点重复增加, path={}, data={}",
                    data.getPath(), GsonUtil.pojoToJson(node));
            return;
        }

        if (runAfterAdd != null) {
            runAfterAdd.accept(node, relaySender);
        }
    }

    private void doAfterAdd(ImNode node, PeerSender relaySender) {
        if (null != relaySender) {
            //关闭老的连接
            relaySender.stopConnecting();
        }
        //创建一个消息转发器
        relaySender = new PeerSender(node);
        //建立转发的连接
        relaySender.doConnect();
        workerMap.put(node.getId(), relaySender);
    }

    private void processNodeRemoved(ChildData data) {
        byte[] payload = data.getData();
        ImNode node = GsonUtil.jsonBytes2Object(payload, ImNode.class);

        long id = ImWorker.getInst().getIdByPath(data.getPath());
        node.setId(id);
        log.info("[PathChildrenCache]节点删除, path={}, data={}",
                data.getPath(), GsonUtil.pojoToJson(node));
        if (runAfterRemove != null) {
            runAfterRemove.accept(node);
        }
    }

    private void doAfterRemove(ImNode node) {
        PeerSender peerSender = workerMap.get(node.getId());
        if (null != peerSender) {
            peerSender.stopConnecting();
            workerMap.remove(node.getId());
        }
    }
}
