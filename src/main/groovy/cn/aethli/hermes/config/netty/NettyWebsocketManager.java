package cn.aethli.hermes.config.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * channel管理
 */
public class NettyWebsocketManager {

    /**
     * key为业务的uuid值，thingId，accountId等
     */
    private static final ConcurrentHashMap<String, Channel> CHANNEL_POOL = new ConcurrentHashMap<>();

    public static ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    public static void add(String key, Channel channel) {
        CHANNEL_POOL.put(key, channel);
    }

    public static Channel getChannel(String key) {
        return CHANNEL_POOL.get(key);
    }

    /**
     * 根据部分值获取channel，通常用于业务类型获取channel
     *
     * @param key
     * @return
     */
    public static List<Channel> getChannelByPart(String key) {
        List<Channel> channels = new ArrayList<>();
        CHANNEL_POOL.keySet().stream().filter(a -> a.contains(key)).forEach(a -> channels.add(CHANNEL_POOL.get(a)));
        return channels;
    }


    /**
     * 断开时调用此方法
     *
     * @param channelId
     * @return
     */
    public static List<String> removeByChannelId(ChannelId channelId) {
        List<String> keys = getByChannelId(channelId);
        //遍历从CHANNEL_POOL移除，并从channelGroup移除
        keys.forEach(
                k -> {
                    Channel c = CHANNEL_POOL.remove(k);
                    channelGroup.remove(c);
                }
        );
        return keys;
    }

    public static Set<String> getKeySet() {
        return CHANNEL_POOL.keySet();
    }

    /**
     * 通过channelId获取key
     *
     * @param channelId
     * @return
     */
    public static List<String> getByChannelId(ChannelId channelId) {
        //找到所有channelId对应的Key，通常只有一个
        List<String> keys = NettyWebsocketManager.CHANNEL_POOL.keySet().stream().filter(
                a -> NettyWebsocketManager.CHANNEL_POOL.get(a).id().equals(channelId)
        ).collect(Collectors.toList());
        return keys;
    }

}