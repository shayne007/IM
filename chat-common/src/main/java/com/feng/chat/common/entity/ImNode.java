package com.feng.chat.common.entity;

import lombok.Data;

import java.util.Objects;

/**
 * @Description chat-server服务器节点信息
 * @Author fengsy
 * @Date 9/29/21
 */
@Data
public class ImNode implements Comparable<ImNode> {
    private long id;
    private String host = "127.0.0.1";
    private int port;
    private int load = 0;

    public ImNode() {
    }

    public ImNode(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ImNode node = (ImNode) o;
        return Objects.equals(host, node.host) && Objects.equals(port, node.port);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, host, port);
    }

    @Override
    public int compareTo(ImNode o) {
        int load1 = this.load;
        int load2 = o.load;
        if (load1 > load2) {
            return 1;
        } else if (load1 < load2) {
            return -1;
        }
        return 0;
    }

    public void incrementLoad() {
        load++;
    }

    public void decrementLoad() {
        load--;
    }
}
