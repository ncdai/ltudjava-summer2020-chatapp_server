package vn.name.chanhdai.chatapp.server;

import vn.name.chanhdai.chatapp.common.SocketChannelUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * vn.name.chanhdai.chatapp
 *
 * @created by ncdai3651408 - StudentID : 18120113
 * @date 7/23/20 - 5:30 PM
 * @description
 */

class SocketChannelWorker extends Thread {
    private final SocketChannel socketChannel;
    private String uploadDir;

    public SocketChannelWorker(SocketChannel socketChannel, String uploadDir) {
        this.socketChannel = socketChannel;
        this.uploadDir = uploadDir;
    }

    @Override
    public void run() {
        String command = SocketChannelUtils.readStringFromSocket(socketChannel);
        String fileName = SocketChannelUtils.readStringFromSocket(socketChannel);

        if (command != null) {
            if (command.equals("send")) {
                // client send file -> read and save to server
                String filePathToSave = this.uploadDir + fileName;
                SocketChannelUtils.readFileFromSocket(socketChannel, filePathToSave);
            } else if (command.equals("download")) {
                // client download file -> send file to client
                String filePathToSend = this.uploadDir + fileName;
                SocketChannelUtils.sendFileToSocket(socketChannel, filePathToSend);
            } else {
                // unknown command
                System.err.println("unknown " + command);
                closeChannel(socketChannel);
            }
        }
    }

    public static void closeChannel(SocketChannel socketChannel) {
        try {
            System.out.println(socketChannel.getLocalAddress() + " close connect from " + socketChannel.getRemoteAddress());
            socketChannel.close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }
}

public class SocketChannelServer extends Thread {
    ServerSocketChannel serverSocketChannel;
    private final int port;
    private final String uploadDir;

    public static void main(String[] args) {
        new SocketChannelServer(9999, "/Users/ncdai3651408/IdeaProjects/chatapp_server/uploads/").start();
    }

    SocketChannelServer(int port, String uploadDir) {
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
                System.out.println("listening on port " + this.port);

                SocketChannel socketChannel = serverSocketChannel.accept();
                System.out.println(socketChannel.getLocalAddress() + " has connect from " + socketChannel.getRemoteAddress());

                new SocketChannelWorker(socketChannel, this.uploadDir).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
