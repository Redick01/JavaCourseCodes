package io.github.kimmking.gateway.outbound.netty4;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;

public class NettyHttpClient {

    private String host;

    private int port;

    public NettyHttpClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void handle(final FullHttpRequest fullRequest, final ChannelHandlerContext ctx, String proxyServer) throws Exception {
        EventLoopGroup workerGroup = new NioEventLoopGroup(16);
        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup);
            //b.remoteAddress(host, port);
            b.channel(NioSocketChannel.class);
            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.handler(new ChannelInitializer<Channel>() {
                @Override
                public void initChannel(Channel ch) throws Exception {
                    //包含编码器和解码器
                    ch.pipeline().addLast(new HttpClientCodec());
                    //聚合
                    ch.pipeline().addLast(new HttpObjectAggregator(512 * 1024));
                    //解压
                    ch.pipeline().addLast(new HttpContentDecompressor());
                    // 客户端接收到的是httpResponse响应，所以要使用HttpResponseDecoder进行解码
                    // 客户端接收到的是httpResponse响应，所以要使用HttpResponseDecoder进行解码
                    // ch.pipeline().addLast(new HttpResponseDecoder());
                    // 客户端发送的是httprequest，所以要使用HttpRequestEncoder进行编码
                    // ch.pipeline().addLast(new HttpRequestEncoder());
                    ch.pipeline().addLast(new NettyHttpClientOutboundHandler(ctx, fullRequest, proxyServer));
                }
            });
            // Start the client.
            ChannelFuture f = b.connect(host, port).sync();
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }
}