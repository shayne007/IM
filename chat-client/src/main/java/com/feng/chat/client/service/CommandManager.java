package com.feng.chat.client.service;

import com.feng.chat.client.command.BaseCommand;
import com.feng.chat.client.command.ChatConsoleCommand;
import com.feng.chat.client.command.ClientCommandMenu;
import com.feng.chat.client.command.LoginConsoleCommand;
import com.feng.chat.client.command.LogoutConsoleCommand;
import com.feng.chat.client.feign.WebOperator;
import com.feng.chat.client.listener.ChannelConnectedListener;
import com.feng.chat.client.sender.ChatSender;
import com.feng.chat.client.sender.LoginSender;
import com.feng.chat.client.session.ClientSession;
import com.feng.chat.common.concurrent.FutureTaskScheduler;
import com.feng.chat.common.entity.ImNode;
import com.feng.chat.common.entity.LoginBackMsg;
import com.feng.chat.common.msg.UserDTO;
import com.feng.chat.common.util.GsonUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/**
 * @Description 客户端命令执行管理器，读取用户输入，根据不同的命令类型执行不同的操作
 * @Author fengsy
 * @Date 9/30/21
 */
@Slf4j
@Data
@Service("CommandManager")
public class CommandManager {

    private Channel channel;
    public int reConnectCount = 0;
    private boolean connectFlag = false;
    @Resource
    private NettyClient nettyClient;
    private ClientSession session;

    @Resource
    ChatConsoleCommand chatConsoleCommand;

    @Resource
    LoginConsoleCommand loginConsoleCommand;

    @Resource
    LogoutConsoleCommand logoutConsoleCommand;

    @Resource
    ClientCommandMenu clientCommandMenu;

    private Map<String, BaseCommand> commandMap;

    private String menuString;

    @Resource
    private ChatSender chatSender;

    @Resource
    private LoginSender loginSender;

    private UserDTO user;

    private Scanner scanner;

    private GenericFutureListener<ChannelFuture> connectedListener;


    public void initCommandMap() {
        commandMap = new HashMap<>();
        commandMap.put(clientCommandMenu.getKey(), clientCommandMenu);
        commandMap.put(chatConsoleCommand.getKey(), chatConsoleCommand);
        commandMap.put(loginConsoleCommand.getKey(), loginConsoleCommand);
        commandMap.put(logoutConsoleCommand.getKey(), logoutConsoleCommand);

        Set<Map.Entry<String, BaseCommand>> entrys = commandMap.entrySet();
        Iterator<Map.Entry<String, BaseCommand>> iterator = entrys.iterator();

        StringBuilder menus = new StringBuilder();
        menus.append("[menu] ");
        while (iterator.hasNext()) {
            BaseCommand next = iterator.next().getValue();
            menus.append(next.getKey()).append("->").append(next.getTip()).append(" | ");

        }
        menuString = menus.toString();
        clientCommandMenu.setAllCommandsShow(menuString);
    }

    public void startCommandThread() throws InterruptedException {
        scanner = new Scanner(System.in);
        Thread.currentThread().setName("命令线程");

        while (true) {
            //建立连接,处理登录命令
            while (connectFlag == false) {
                userLoginAndConnectToServer();
            }
            //建立建立后，处理用户输入的不同操作命令
            while (null != session) {
                clientCommandMenu.exec(scanner);
                String key = clientCommandMenu.getCommandInput();
                BaseCommand command = commandMap.get(key);

                if (null == command) {
                    System.err.println("无法识别[" + command + "]指令，请重新输入!");
                    continue;
                }

                switch (key) {
                    case ChatConsoleCommand.KEY:
                        boolean ok = command.exec(scanner);
                        if (ok) {
                            startOneChat((ChatConsoleCommand) command);
                        }

                        break;
                    case LoginConsoleCommand.KEY:
                        boolean okLogin = command.exec(scanner);
                        if (okLogin) {
                            userLoginAndConnectToServer();
                        }
                        break;

                    case LogoutConsoleCommand.KEY:
                        boolean logoutOk = command.exec(scanner);
                        if (logoutOk) {
                            startLogout(command);
                        }
                        break;
                }
            }
        }
    }

    /**
     * 开始连接服务器
     */
    private void userLoginAndConnectToServer() {

        //登录
        if (isConnectFlag()) {
            log.info("已经登录成功，不需要重复登录");
            return;
        }
        LoginConsoleCommand command = (LoginConsoleCommand) commandMap.get(LoginConsoleCommand.KEY);
        command.exec(scanner);

        UserDTO user = new UserDTO();
        user.setUserId(command.getUserName());
        user.setToken(command.getPassword());
        user.setDevId("1111");

        log.info("step1：开始登录WEB GATE");
        LoginBackMsg webBack = WebOperator.login(command.getUserName(), command.getPassword());
        List<ImNode> nodeList = webBack.getImNodeList();
        log.info("step1 WEB GATE 返回的node节点列表是：{}", GsonUtil.pojoToJson(nodeList));

        log.info("step2：开始连接Netty 服务节点");
        if (!CollectionUtils.isEmpty(nodeList)) {
            // 根据load值由小到大排序
            Collections.sort(nodeList);
        } else {
            log.error("step2-1：服务器节点为空，无法连接");
        }
        connectedListener = new ChannelConnectedListener(this);
        nettyClient.setConnectedListener(connectedListener);
        //获取服务器节点信息，选择负载最低的节点连接
        ImNode bestNode;
        for (int i = 0; i < nodeList.size(); i++) {
            bestNode = nodeList.get(i);
            log.info("尝试连接最佳的节点为：{}", GsonUtil.pojoToJson(bestNode));
            nettyClient.setHost(bestNode.getHost());
            nettyClient.setPort(bestNode.getPort());
            nettyClient.doConnect();
            waitCommandThread();
            //客户端选择的节点可能已经挂掉，但是zk的节点信息还在，连接不了，需要重试其他节点
            if (connectFlag) {
                break;
            }
            if (i == nodeList.size()) {
                log.error("尝试所有节点连接失败");
                return;
            }
        }
//        waitCommandThread();
        log.info("step2：Netty 服务节点连接成功");

        log.info("step3：开始登录Netty 服务节点");
        this.user = user;
        session.setUser(user);
        loginSender.setUser(user);
        loginSender.setSession(session);
        loginSender.sendLoginMsg();
        waitCommandThread();

        connectFlag = true;
    }

    public synchronized void waitCommandThread() {
        try {
            this.wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public synchronized void notifyCommandThread() {
        this.notify();
    }

    public void startConnectServer() {
        FutureTaskScheduler.add(() ->
        {
            nettyClient.setConnectedListener(connectedListener);
            nettyClient.doConnect();
        });
    }


    //发送单聊消息
    private void startOneChat(ChatConsoleCommand c) {
        //登录
        if (!isLogin()) {
            log.info("还没有登录，请先登录");
            return;
        }
        chatSender.setSession(session);
        chatSender.setUser(user);
        chatSender.sendChatMsg(c.getToUserId(), c.getMessage());


    }


    private void startLogout(BaseCommand command) {
        //登出
        if (!isLogin()) {
            log.info("还没有登录，请先登录");
            return;
        }
        session.close();
    }


    public boolean isLogin() {
        if (null == session) {
            log.info("session is null");
            return false;
        }

        return session.isLogin();
    }

}
