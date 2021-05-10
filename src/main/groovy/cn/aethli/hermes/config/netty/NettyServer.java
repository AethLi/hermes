package cn.aethli.hermes.config.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@ConfigurationProperties(prefix = "hermes.websocket")
@EnableConfigurationProperties
public class NettyServer {
    private final EventLoopGroup bossGroup = new NioEventLoopGroup();
    private final EventLoopGroup workerGroup = new NioEventLoopGroup();
    @Value("${hermes.websocket.port}")
    private Integer port;
    @Value("${hermes.websocket.readerIdleTime}")
    private Integer readerIdleTime;
    @Value("${hermes.websocket.writerIdleTime}")
    private Integer writerIdleTime;
    @Value("${hermes.websocket.allIdleTime}")
    private Integer allIdleTime;

    void start() throws Exception {
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.option(ChannelOption.SO_BACKLOG, 1024);//按序处理websocket请求
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);//允许tcp keep alive机制
        bootstrap.group(workerGroup, bossGroup) // 绑定线程池
                .channel(NioServerSocketChannel.class) // 指定使用的channel
                .localAddress(this.port)// 绑定监听端口
                .childHandler(new ChannelInitializer<SocketChannel>() { // 绑定客户端连接时候触发操作

                    @Override
                    protected void initChannel(SocketChannel ch) {
//                        log.info("收到新连接");
                        //websocket协议本身是基于http协议的，所以这边也要使用http解编码器
                        ch.pipeline().addLast(new HttpServerCodec());
                        //以块的方式来写的处理器
                        ch.pipeline().addLast(new ChunkedWriteHandler());
                        ch.pipeline().addLast(new HttpObjectAggregator(8192));
                        //启动空闲检查，于application.yml的websocket中配置检测间隔
                        ch.pipeline().addLast(new IdleStateHandler(readerIdleTime, writerIdleTime, allIdleTime, TimeUnit.SECONDS));
                        //入栈处理器
                        ch.pipeline().addLast(new NettyWebsocketInboundHandler());

                    }
                });
        try {
            ChannelFuture future = bootstrap.bind().sync();
            log.info("服务器启动开始监听: {}", future.channel().localAddress());
            //启动关闭钩子，当子线程结束时，该线程同步结束
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            //关闭主线程组
            bossGroup.shutdownGracefully();
            //关闭工作线程组
            workerGroup.shutdownGracefully();
        }
    }
}
