package vn.name.chanhdai.chatapp.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class MediaServer extends Thread {
    ServerSocketChannel serverSocketChannel;
    private final int port;
    private final String uploadDir;

    MediaServer(int port, String uploadDir) {
        this.port = port;
        this.uploadDir = uploadDir;
    }

    @Override
    public void run() {
        try {
            this.serverSocketChannel = ServerSocketChannel.open();
            this.serverSocketChannel.socket().bind(new InetSocketAddress(this.port));

            // noinspection InfiniteLoopStatement
            while (true) {
                System.out.println("[Media Server] is listening on port " + this.port);

                SocketChannel socketChannel = serverSocketChannel.accept();
                new MediaServerWorker(socketChannel, this.uploadDir).start();
                System.out.println("[Media Server] has connected from " + socketChannel.getRemoteAddress());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
