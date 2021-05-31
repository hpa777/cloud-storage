package controllers;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.string.StringEncoder;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Client {

    private static final Client instance = new Client();

    public static Client getInstance() {
        return instance;
    }

    private static final String HOST = "localhost";
    private static final int PORT = 8765;

    private volatile boolean isReady;

    public boolean isIsReady() {
        return isReady;
    }
    private ClientHandler clientHandler;

    public Client()  {
        clientHandler = new ClientHandler();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        Thread t = new Thread(()->{
            try {
                Bootstrap b = new Bootstrap();
                b.group(workerGroup);
                b.channel(NioSocketChannel.class);
                b.option(ChannelOption.SO_KEEPALIVE, true);
                b.handler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(
                                new StringEncoder(),
                                new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                                clientHandler
                        );
                    }
                });
                ChannelFuture future = b.connect(HOST, PORT).sync();
                System.out.println("Client started");
                isReady = true;
                future.channel().closeFuture().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                workerGroup.shutdownGracefully();
            }
        });
        t.setDaemon(true);
        t.start();
    }

    public Object sendMsg(String msg) throws InterruptedException, ExecutionException, TimeoutException {
        Future<Object> result = clientHandler.sendMessage(msg);
        return result.get(1000, TimeUnit.MILLISECONDS);
    }

}
