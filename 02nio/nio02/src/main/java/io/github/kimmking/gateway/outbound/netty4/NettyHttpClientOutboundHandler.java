package io.github.kimmking.gateway.outbound.netty4;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.apache.http.util.EntityUtils;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_0;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class NettyHttpClientOutboundHandler extends ChannelInboundHandlerAdapter {

    private ChannelHandlerContext ctx1;

    private FullHttpRequest fullRequest;

    private String backUrl;

    public NettyHttpClientOutboundHandler(ChannelHandlerContext ctx1, FullHttpRequest fullRequest, String url) {
        this.ctx1 = ctx1;
        this.fullRequest = fullRequest;
        this.backUrl = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
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
                response = (FullHttpResponse) msg;
                ByteBuf buf = response.content();
                String result = buf.toString(CharsetUtil.UTF_8);
                defaultFullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_0, response.status(), Unpooled.wrappedBuffer(result.getBytes("UTF-8")));
                defaultFullHttpResponse.headers().add(HttpHeaderNames.CONTENT_TYPE, "application/json");
                defaultFullHttpResponse.headers().add(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
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
        try {
            URI uri = new URI(backUrl + fullRequest.uri());
            // 获取Netty内置的请求头对象
            HttpHeaders header = fullRequest.headers();
            // 将包含的请求信息赋值到list中
            List<Map.Entry<String, String>> list = header.entries();
            DefaultFullHttpRequest request = new DefaultFullHttpRequest(HTTP_1_0, HttpMethod.GET, uri.toASCIIString());
            for (Map.Entry<String, String> map : list) {
                request.headers().add(map.getKey(), map.getValue());
            }
            request.setProtocolVersion(HTTP_1_0);
            ctx.writeAndFlush(request);
        } catch (Exception e) {
            e.printStackTrace();
            ctx.flush();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.flush();
        super.exceptionCaught(ctx, cause);
    }
}