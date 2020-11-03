package io.github.kimmking.gateway.filter.impl;

import io.github.kimmking.gateway.filter.HttpRequestFilter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;

/**
 * @author liu_penghui
 * @date 2020/11/3.
 */
public class HttpRequestHeadFilterImpl implements HttpRequestFilter {

    @Override
    public void filter(FullHttpRequest fullRequest, ChannelHandlerContext ctx) {
        fullRequest.headers().set("nio", "liupenghui");
        fullRequest.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
    }
}
