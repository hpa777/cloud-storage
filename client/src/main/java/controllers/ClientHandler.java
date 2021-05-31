package controllers;
import io.netty.channel.*;
import io.netty.util.concurrent.Promise;
import java.util.concurrent.Future;

public class ClientHandler extends ChannelInboundHandlerAdapter {

    private ChannelHandlerContext context;

    Promise<Object> promise;

    public Future<Object> sendMessage(String message) {
        synchronized (this) {
            promise = context.executor().newPromise();
            context.writeAndFlush(message);
            return promise;
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        context = ctx;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        synchronized (this) {
            promise.setSuccess(msg);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}