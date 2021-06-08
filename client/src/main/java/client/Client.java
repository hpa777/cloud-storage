package client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Client
{


    private static final Client instance = new Client();
    public static Client getInstance() {
        return Client.instance;
    }

    private String host;

    private Integer port;

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    private volatile boolean isReady;

    private ClientHandler clientHandler;

    
    public boolean isReady() {
        return this.isReady;
    }
    
    public void tryConnect() {
        if (host == null || port == null) return;
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
                    ChannelFuture future = b.connect(host, port).sync();
                    synchronized (Client.getInstance()) {
                        isReady = true;
                        Client.getInstance().notifyAll();
                    }
                    future.channel().closeFuture().sync();
                } catch (InterruptedException e) {
                    e.printStackTrace();

                } finally {
                    group.shutdownGracefully();
                    synchronized (Client.getInstance()) {
                        isReady = false;
                        Client.getInstance().notifyAll();
                    }
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
