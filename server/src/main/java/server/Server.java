package server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Server
{
    private static final Logger logger = Logger.getLogger(Server.class.getName());


    private static Connection connection;

    public static void main(final String[] args) {
        LogManager logManager = LogManager.getLogManager();
        try {
            logManager.readConfiguration(new FileInputStream("logging.properties"));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        Settings settings = Settings.getInstance();
        try {
            connection = DriverManager.getConnection(settings.getConnectionString()
                    , settings.getDbUser()
                    , settings.getDbPass()
            );
            new Server();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public Server() throws SQLException {
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
                            , new InputHandler(connection));
                }
            });
            ChannelFuture future = bootstrap.bind(Settings.getInstance().getPort()).sync();
            logger.info("server.Server started");
            future.channel().closeFuture().sync();
            logger.info("server.Server closed");
        }
        catch (InterruptedException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
        finally {
            auth.shutdownGracefully();
            worker.shutdownGracefully();
            connection.close();
        }
    }



}