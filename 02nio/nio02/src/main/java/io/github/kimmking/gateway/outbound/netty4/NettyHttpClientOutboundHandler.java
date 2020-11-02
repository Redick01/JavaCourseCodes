package io.github.kimmking.gateway.outbound.netty4;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.apache.http.util.EntityUtils;

import java.net.URI;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class NettyHttpClientOutboundHandler extends ChannelInboundHandlerAdapter {

    private ChannelHandlerContext ctx1;

    private FullHttpRequest fullRequest;

    public NettyHttpClientOutboundHandler(ChannelHandlerContext ctx1, FullHttpRequest fullRequest) {
        this.ctx1 = ctx1;
        this.fullRequest = fullRequest;
    }

    /**
     * 读取数据
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        FullHttpResponse response = null;
        DefaultFullHttpResponse defaultFullHttpResponse = null;
        try {
            if (msg instanceof FullHttpResponse) {
                System.out.println(111111);
                response = (FullHttpResponse) msg;
                ByteBuf buf = response.content();
                String result = buf.toString(CharsetUtil.UTF_8);
                System.out.println("response -> "+result);
                defaultFullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_0, response.status(), Unpooled.wrappedBuffer(result.getBytes("UTF-8")));
                defaultFullHttpResponse.headers().add(HttpHeaderNames.CONTENT_TYPE, "application/json");
                defaultFullHttpResponse.headers().add(HttpHeaderNames.CONNECTION,HttpHeaderValues.KEEP_ALIVE);
                defaultFullHttpResponse.headers().add(HttpHeaderNames.CONTENT_LENGTH, buf.readableBytes());
            }
            if (fullRequest != null) {
                if (!HttpUtil.isKeepAlive(fullRequest)) {
                    ctx1.write(defaultFullHttpResponse).addListener(ChannelFutureListener.CLOSE);
                } else {
                    ctx1.write(defaultFullHttpResponse);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ctx1.flush();

        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        URI uri = new URI("http://127.0.0.1:8088/api/hello");
        String msg = "Are you ok?";
        DefaultFullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_0, HttpMethod.GET, uri.toASCIIString());
        // 构建http请求
        // request.headers().add(HttpHeaderNames.CONNECTION,HttpHeaderValues.CLOSE);
        request.headers().add(HttpHeaderNames.CONTENT_LENGTH,request.content().readableBytes());
        ctx.writeAndFlush(request);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.flush();
        super.exceptionCaught(ctx, cause);
    }
}