package example

import io.netty.buffer.Unpooled
import io.netty.channel.{ChannelHandlerContext, ChannelInboundHandlerAdapter, ChannelFutureListener}
import io.netty.handler.codec.http.HttpHeaders.Names._
import io.netty.handler.codec.http.HttpHeaders._
import io.netty.handler.codec.http.HttpResponseStatus._
import io.netty.handler.codec.http.HttpVersion._
import io.netty.handler.codec.http.{DefaultFullHttpResponse, HttpRequest}
import io.netty.util.CharsetUtil

class HttpHandler extends ChannelInboundHandlerAdapter {
  private final val CONTENT =
    Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("Hello World", CharsetUtil.US_ASCII))

  override def channelRead(ctx: ChannelHandlerContext, msg: AnyRef): Unit =
    msg match {
      case req: HttpRequest =>
        println("got request")
        if (is100ContinueExpected(req)) {
          ctx.write(new DefaultFullHttpResponse(HTTP_1_1, CONTINUE))
        }
        val keepAlive = isKeepAlive(req)
        val response = new DefaultFullHttpResponse(HTTP_1_1, OK, CONTENT)

        val future = ctx.writeAndFlush(response)
        future.addListener(ChannelFutureListener.CLOSE)

      case _ =>
    }

  override def channelReadComplete(ctx: ChannelHandlerContext): Unit = {
    ctx.flush()
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = {
    cause.printStackTrace()
    ctx.close()
  }
}
