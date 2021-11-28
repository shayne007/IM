package com.feng.chat.gateway.balance;

import com.feng.chat.common.entity.ImNode;

import java.util.List;

/**
 * @author fengsy
 * @date 8/19/21
 * @Description
 */

public interface LoadBalance {

    public ImNode getBestWorker();

    public List<ImNode> getWorkers();

    public long getIdByPath(String path);

    public void removeWorkers();
}
