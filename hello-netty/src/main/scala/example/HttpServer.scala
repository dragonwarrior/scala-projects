package example

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel

import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.http.HttpServerCodec

object HttpServer extends App {
  val port = args.headOption.map(_.toInt).getOrElse(8080)

  val bossGroup, workerGroup = new NioEventLoopGroup()
  try {
    val b = new ServerBootstrap()
    b.option(ChannelOption.SO_BACKLOG, Int.box(1024))
      .group(bossGroup, workerGroup)
      .channel(classOf[NioServerSocketChannel])
      .childHandler(new ChannelInitializer[SocketChannel] {
        override def initChannel(ch: SocketChannel): Unit = {
            val p = ch.pipeline()
            p.addLast("codec", new HttpServerCodec())
            p.addLast("handler", new HttpHandler()) //handler of user's logic
        }
      })

    val ch = b.bind(port).sync().channel()
    ch.closeFuture().sync()
  } finally {
    bossGroup.shutdownGracefully()
    workerGroup.shutdownGracefully()
  }
}
