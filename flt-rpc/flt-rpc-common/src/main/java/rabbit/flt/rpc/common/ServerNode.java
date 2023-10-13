package rabbit.flt.rpc.common;

import java.util.Objects;

/**
 * 服务节点
 */
public class ServerNode {

    // 主机地址
    private String host;

    // 端口
    private int port;

    public ServerNode() {
    }

    public ServerNode(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public boolean isSameNode(Object o) {
        if (o == this) {
            return true;
        }
        if (null == o || getClass() != o.getClass()) {
            return false;
        }
        ServerNode that = (ServerNode) o;
        return Objects.equals(port, that.port) && Objects.equals(host, that.host);
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
