package com.hncboy.beehive.cell.midjourney.handler.listener;

import com.hncboy.beehive.base.config.ProxyConfig;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

/**
 * @author ll
 * @date 2023/5/18
 * Discord 启动类
 */
@Slf4j
@Component("discordStarter")
public class DiscordStarter implements InitializingBean {

    @Resource
    private ProxyConfig proxyConfig;

    @Resource
    private DiscordMessageListener discordMessageListener;

    //暂时注释掉，不使用discord
    @Override
    public void afterPropertiesSet() {
        //CellDO cellDO = CellHandler.getCell(CellCodeEnum.MIDJOURNEY);
        //if (Objects.isNull(cellDO) || cellDO.getStatus() != CellStatusEnum.PUBLISHED) {
        //    return;
        //}
        //
        //// 因为 Discord 配置需要初始化，所以改了 BotToken 改了要重启
        //MidjourneyProperties midjourneyProperties = MidjourneyProperties.init();
        //
        //// TODO 有时间可以尝试改成 userToken 试下效果
        //DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(midjourneyProperties.getBotToken(), GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT);
        //builder.addEventListeners(this.discordMessageListener);
        //log.info("Midjourney 开始启动");
        //
        //// 判断是否需要代理，下面和代理相关
        //if (proxyConfig.getEnabled()) {
        //    // 解决报错：java.net.SocketTimeoutException: Connect timed out
        //    OkHttpClient.Builder okhttpbuilder = new OkHttpClient.Builder();
        //    okhttpbuilder.proxy(proxyConfig.getProxy());
        //    builder.setHttpClientBuilder(okhttpbuilder);
        //
        //    // 解决报错：com.neovisionaries.ws.client.WebSocketException: Failed to connect to 'gateway.discord.gg:443': Connect timed out
        //    WebSocketFactory webSocketFactory = new WebSocketFactory();
        //    ProxySettings proxySettings = webSocketFactory.getProxySettings();
        //    proxySettings.setHost(proxyConfig.getHttpHost());
        //    proxySettings.setPort(proxyConfig.getHttpPort());
        //    builder.setWebsocketFactory(webSocketFactory);
        //}
        //
        //builder.build();
    }
}
