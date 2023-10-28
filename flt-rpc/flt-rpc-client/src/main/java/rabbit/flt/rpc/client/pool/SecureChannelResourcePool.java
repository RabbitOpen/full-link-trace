package rabbit.flt.rpc.client.pool;

import rabbit.flt.common.utils.AESUtils;
import rabbit.flt.rpc.client.Client;
import rabbit.flt.rpc.client.RequestFactory;
import rabbit.flt.rpc.common.ChannelStatus;
import rabbit.flt.rpc.common.rpc.Authentication;

/**
 * 安全的连接池，所有连接都需要认证
 */
public class SecureChannelResourcePool extends ChannelResourcePool {

    @Override
    public void init(PoolConfig config) {
        config.setChannelListener(channel -> {
            RequestFactory factory = new RequestFactory() {
                @Override
                protected Client getClient() {
                    return channel;
                }

                @Override
                protected int getMaxRetryTime() {
                    return 0;
                }

                @Override
                protected int getRequestTimeoutSeconds() {
                    return config.getRpcRequestTimeoutSeconds();
                }
            };
            try {
                Authentication proxy = factory.proxy(Authentication.class);
                String signature = AESUtils.encrypt(Long.toString(System.currentTimeMillis()), config.getPassword());
                proxy.authenticate(config.getApplicationCode(), signature);
                channel.setChannelStatus(ChannelStatus.AUTHENTICATED);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        });
        super.init(config);
    }

    @Override
    protected boolean isClientPrepared(ClientChannel clientChannel) {
        return clientChannel.getChannelStatus().isAuthenticated();
    }
}
