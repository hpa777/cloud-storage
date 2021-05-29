import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.Scanner;

public class Controller {

    private static final String HOST = "localhost";
    private static final int PORT = 8765;

    private SocketChannel channel;

    public void connect() throws InterruptedException {
        Thread t = new Thread(() -> {
            NioEventLoopGroup workerGroup = new NioEventLoopGroup();
            try {
                Bootstrap b = new Bootstrap();
                b.group(workerGroup);
                b.channel(NioSocketChannel.class);
                b.option(ChannelOption.SO_KEEPALIVE, true);
                b.handler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        channel = ch;
                        ch.pipeline().addLast(
                                new RequestDataEncoder(),
                                new ResponseDataDecoder(),
                                new ClientHandler()
                        );
                    }
                });
                //b.option(ChannelOption.SO_KEEPALIVE, true);
                ChannelFuture future = b.connect(HOST, PORT).sync();
                future.channel().closeFuture().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                workerGroup.shutdownGracefully();
            }
        });
        t.start();
    }

    private void sendMsg() {
        RequestData msg = new RequestData();
        msg.setIntValue(123);
        msg.setStringValue(
                "all work and no play makes jack a dull boy");
        channel.writeAndFlush(msg);
        channel.writeAndFlush(msg);
    }

    public static void main(String[] args) throws InterruptedException {
        Controller controller = new Controller();
        controller.connect();
        Scanner sc = new Scanner(System.in);
        while (true) {
            sc.nextLine();
            controller.sendMsg();
        }
    }

}
