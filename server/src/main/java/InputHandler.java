import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.concurrent.ConcurrentHashMap;

public class InputHandler extends SimpleChannelInboundHandler<String> {

    private final ConcurrentHashMap<String, CommandHandler> clients = new ConcurrentHashMap<>();

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String s) throws Exception {
        channelHandlerContext.writeAndFlush(clients.get(channelHandlerContext.channel().id().asShortText()).doCommand(s));
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        clients.put(ctx.channel().id().asShortText(), new CommandHandler(ctx.channel().remoteAddress().toString()));
        System.out.println("Client connected: " + ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        clients.remove(ctx.channel().id().asShortText());
        System.out.println("Client disconnected: " + ctx.channel());
    }
}
