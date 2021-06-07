package server;

import io.netty.bootstrap.AbstractBootstrap;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.util.logging.LogManager;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import java.util.logging.Level;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.Channel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.nio.NioEventLoopGroup;
import java.util.logging.Logger;

public class Server
{
    private static final Logger logger = Logger.getLogger(Server.class.getName());

    private static final int PORT = 8765;
    public static final String ROOT_PATH = "server_dir";

    public Server() {
        EventLoopGroup auth = new NioEventLoopGroup(1);
        EventLoopGroup worker = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(auth, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<>() {
                @Override
                protected void initChannel(final Channel ch) {
                    ch.pipeline().addLast(
                            new ObjectEncoder()
                            , new ObjectDecoder(ClassResolvers.cacheDisabled(null))
                            , new InputHandler());
                }
            });
            ChannelFuture future = bootstrap.bind(8765).sync();
            Server.logger.info("server.Server started");
            future.channel().closeFuture().sync();
            Server.logger.info("server.Server closed");
        }
        catch (InterruptedException e) {
            Server.logger.log(Level.SEVERE, e.getMessage(), e);
            e.printStackTrace();
        }
        finally {
            auth.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }

    public static void main(final String[] args) {
        final LogManager logManager = LogManager.getLogManager();
        try {
            logManager.readConfiguration(new FileInputStream("logging.properties"));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        new Server();
    }

}