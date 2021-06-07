package server;

import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class InputHandler extends ChannelInboundHandlerAdapter
{
    private static final Logger logger = Logger.getLogger(InputHandler.class.getName());
    private final ConcurrentHashMap<String, CommandHandler> clients;

    public InputHandler() {
        clients = new ConcurrentHashMap<>();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        clients.put(ctx.channel().id().asShortText(), new CommandHandler(ctx.channel().remoteAddress().toString()));
        InputHandler.logger.info("Client connected: " + ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        this.clients.remove(ctx.channel().id().asShortText());
        InputHandler.logger.info("Client disconnected: " + ctx.channel());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws IOException {
        ctx.writeAndFlush(clients.get(ctx.channel().id().asShortText()).doCommand(msg));
    }


}
