package vn.name.chanhdai.chatapp.server;

import vn.name.chanhdai.chatapp.common.MediaServerUtils;

import java.io.IOException;
import java.nio.channels.SocketChannel;

/**
 * vn.name.chanhdai.chatapp
 *
 * @created by ncdai3651408 - StudentID : 18120113
 * @date 7/23/20 - 5:30 PM
 * @description
 */

class MediaServerWorker extends Thread {
    private final SocketChannel socketChannel;
    private final String uploadDir;

    public MediaServerWorker(SocketChannel socketChannel, String uploadDir) {
        this.socketChannel = socketChannel;
        this.uploadDir = uploadDir;
    }

    @Override
    public void run() {
        String command = MediaServerUtils.readStringFromSocket(socketChannel);
        String fileName = MediaServerUtils.readStringFromSocket(socketChannel);

        if (command != null) {
            if (command.equals("send")) {
                // client send file -> read and save to server
                String filePathToSave = this.uploadDir + fileName;
                MediaServerUtils.readFileFromSocket(socketChannel, filePathToSave);
            } else if (command.equals("download")) {
                // client download file -> send file to client
                String filePathToSend = this.uploadDir + fileName;
                MediaServerUtils.sendFileToSocket(socketChannel, filePathToSend);
            } else {
                // unknown command
                System.err.println("unknown " + command);
                closeChannel(socketChannel);
            }
        }
    }

    public static void closeChannel(SocketChannel socketChannel) {
        try {
            System.out.println("[Media Server] close connect from " + socketChannel.getRemoteAddress());
            socketChannel.close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }
}
