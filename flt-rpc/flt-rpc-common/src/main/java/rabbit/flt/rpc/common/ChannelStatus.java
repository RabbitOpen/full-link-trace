package rabbit.flt.rpc.common;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * tcp 通道的状态
 */
public class ChannelStatus {

    /**
     * 状态的值
     */
    private int value;

    public ChannelStatus(int value) {
        this.value = value;
    }

    /**
     * 判断当前状体是否包含了指定状态
     *
     * @param status
     * @return
     */
    private boolean contains(ChannelStatus status) {
        return (this.value & status.value) == status.value;
    }

    public boolean isConnected() {
        return contains(CONNECTED);
    }

    public boolean isInit() {
        return contains(INIT);
    }

    public boolean isAuthenticated() {
        return contains(AUTHENTICATED);
    }

    /**
     * 获取状态的名字
     * @return
     */
    public String getName() {
        for (Field field : getClass().getDeclaredFields()) {
            if (Modifier.isAbstract(field.getModifiers()) && ChannelStatus.class == field.getType()) {
                try {
                    field.setAccessible(true);
                    ChannelStatus status  = (ChannelStatus) field.get(null);
                    if (value == status.value) {
                        return field.getName();
                    }
                } catch (Exception e) {
                    throw new RpcException(e);
                }
            }
        }
        return null;
    }

    // 初始化
    public static final ChannelStatus INIT = new ChannelStatus(1);

    // 连接中
    public static final ChannelStatus CONNECTING = new ChannelStatus(1 << 1);

    // 连接成功
    public static final ChannelStatus CONNECTED = new ChannelStatus(1 << 2);

    // 认证过了
    public static final ChannelStatus AUTHENTICATED = new ChannelStatus(1 << 3 | 1 << 2);

    // 已关闭
    public static final ChannelStatus CLOSED = new ChannelStatus(1 << 4);
}
