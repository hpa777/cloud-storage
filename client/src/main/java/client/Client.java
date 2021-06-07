package client;

import io.netty.util.concurrent.AbstractEventExecutorGroup;
import io.netty.bootstrap.AbstractBootstrap;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.channel.ChannelHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.Channel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.nio.NioEventLoopGroup;

public class Client
{
    private static final Client instance = new Client();
    public static Client getInstance() {
        return Client.instance;
    }

    private static final String HOST = "localhost";
    private static final int PORT = 8765;
    private volatile boolean isReady;

    private ClientHandler clientHandler;
    

    
    public boolean isIsReady() {
        return this.isReady;
    }
    
    public Client() {
        clientHandler = new ClientHandler();
        EventLoopGroup group = new NioEventLoopGroup();
        Thread t = new Thread(() -> {
            try {
                Bootstrap b = new Bootstrap();
                b.group(group)
                        .channel(NioSocketChannel.class)
                        .handler(new ChannelInitializer<SocketChannel>() {
                    public void initChannel(final SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(
                                new ObjectEncoder()
                                , new ObjectDecoder(ClassResolvers.cacheDisabled(null))
                                , clientHandler);
                    }
                });
                ChannelFuture future = b.connect("localhost", 8765).sync();
                System.out.println("Client started");
                isReady = true;
                future.channel().closeFuture().sync();
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            finally {
                group.shutdownGracefully();
            }
        });
        t.setDaemon(true);
        t.start();
    }
    
    public Object sendMsg(Object msg) throws InterruptedException, ExecutionException, TimeoutException {
        final Future<Object> result = this.clientHandler.sendMessage(msg);
        return result.get(1000L, TimeUnit.MILLISECONDS);
    }
    
    public void stopClient() {
        this.clientHandler.channelClose();
    }
    

}
