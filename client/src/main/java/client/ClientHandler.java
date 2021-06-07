package client;

import java.util.concurrent.Future;
import io.netty.util.concurrent.Promise;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ClientHandler extends ChannelInboundHandlerAdapter
{
    private ChannelHandlerContext context;
    Promise<Object> promise;
    
    public Future<Object> sendMessage(Object message) {
        synchronized (this) {
            promise = this.context.executor().newPromise();
            context.writeAndFlush(message);
            return promise;
        }
    }
    
    public void channelClose() {
        context.channel().close();
    }
    
    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
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
