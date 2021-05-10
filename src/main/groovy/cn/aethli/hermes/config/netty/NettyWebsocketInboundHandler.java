package cn.aethli.hermes.config.netty;

import cn.hutool.http.HttpUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;


@Slf4j
public class NettyWebsocketInboundHandler extends ChannelInboundHandlerAdapter {


    private WebSocketServerHandshaker handshaker;

    /**
     * 拒绝不合法的请求，并返回错误信息
     */
    private static void sendHttpResponse(ChannelHandlerContext ctx,
                                         FullHttpRequest req, DefaultFullHttpResponse res) {
        // 返回应答给客户端
        if (res.status().code() != 200) {
            ByteBuf buf = Unpooled.copiedBuffer(res.status().toString(),
                    CharsetUtil.UTF_8);
            res.content().writeBytes(buf);
            buf.release();
        }
        ChannelFuture f = ctx.channel().writeAndFlush(res);
        // 如果是非Keep-Alive，关闭连接
        if (res.status().code() != 200) {//!isKeepAlive(req) ||
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("与客户端建立连接，通道开启！");

        //添加到channelGroup通道组
        NettyWebsocketManager.channelGroup.add(ctx.channel());
        ctx.fireChannelInactive();
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        log.info(date + " " + ctx.channel().remoteAddress() + " 新的客户端加入！");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        List<String> keys = NettyWebsocketManager.removeByChannelId(ctx.channel().id());
        keys.forEach(
                key -> {
                    log.info("websocket断开;" + key);
                }
        );
    }

    /**
     * 抛出异常
     *
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error(cause.getMessage(), cause);
        ctx.channel().writeAndFlush(new CloseWebSocketFrame());
        ctx.channel().close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //首次连接是FullHttpRequest，处理参数
        if (msg instanceof FullHttpRequest) {
            //要求Upgrade为websocket，过滤掉get/Post
            if (!((FullHttpRequest) msg).decoderResult().isSuccess()
                    || (!"websocket".equals(((FullHttpRequest) msg).headers().get("Upgrade")))) {
                //若不是websocket方式，则创建BAD_REQUEST的req，返回给客户端
                sendHttpResponse(ctx, (FullHttpRequest) msg, new DefaultFullHttpResponse(
                        HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
                return;
            }
            //webSocketURL疑似无影响，此处传入空字串
            WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
                    "", null, false);
            handshaker = wsFactory.newHandshaker((FullHttpRequest) msg);
            if (handshaker == null) {
                WebSocketServerHandshakerFactory
                        .sendUnsupportedVersionResponse(ctx.channel());
            } else {
                handshaker.handshake(ctx.channel(), (FullHttpRequest) msg);
            }

            FullHttpRequest request = (FullHttpRequest) msg;
            String uri = request.uri();

            //链接带参数则进行处理
            Map<String, String> paramMap = HttpUtil.decodeParamMap(uri, StandardCharsets.UTF_8);
            log.info("接收到的参数是：" + new ObjectMapper().writeValueAsString(paramMap));


        } else if (msg instanceof WebSocketFrame) {
            //处理websocket客户端的消息
            // 判断是否关闭链路的指令
            if (msg instanceof CloseWebSocketFrame) {
                handshaker.close(ctx.channel(), ((CloseWebSocketFrame) msg).retain());
            }
            // 判断是否ping消息
            if (msg instanceof PingWebSocketFrame) {
                ctx.channel().writeAndFlush(
                        new PongWebSocketFrame(((PingWebSocketFrame) msg).content().retain()));
            }
            if (msg instanceof PongWebSocketFrame) {
            }
            //仅支持文本消息，不支持二进制消息
            if (msg instanceof BinaryWebSocketFrame) {
            }
            if (msg instanceof TextWebSocketFrame) {
                //正常的TEXT消息类型
                TextWebSocketFrame frame = (TextWebSocketFrame) msg;
                log.info("客户端收到服务器数据：" + frame.text());
            }
        }
        super.channelRead(ctx, msg);
    }


    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            String[] eventType = new String[1];
            switch (event.state()) {
                case READER_IDLE:
                    eventType[0] = "读空闲";
                    break;
                case WRITER_IDLE:
                    eventType[0] = "写空闲";
                    break;
                case ALL_IDLE:
                    eventType[0] = "读写空闲";
                    break;
                default:
            }
            log.info(eventType[0]);
            NettyWebsocketManager.removeByChannelId(ctx.channel().id()).forEach(
                    key -> {
                        log.info(key + " " + eventType[0]);
                        log.info("websocket断开;" + key);
                    }
            );
            ctx.writeAndFlush(new CloseWebSocketFrame());
            ctx.close();
        }
    }

}