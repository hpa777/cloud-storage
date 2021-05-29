import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;

public class ClientHandler extends SimpleChannelInboundHandler<ResponseData> {


    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ResponseData responseData) throws Exception {
        System.out.println(responseData);
    }


    /*  ChannelInboundHandlerAdapter
    private ChannelHandlerContext ctx;

    @Override
    public void channelActive(ChannelHandlerContext ctx)
            throws Exception {
        this.ctx = ctx;

        RequestData msg = new RequestData();
        msg.setIntValue(123);
        msg.setStringValue(
                "all work and no play makes jack a dull boy");
        ChannelFuture future = ctx.writeAndFlush(msg);


    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        System.out.println((ResponseData)msg);
        //ctx.close();
    }



     */
}