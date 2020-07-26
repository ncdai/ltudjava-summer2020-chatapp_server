package vn.name.chanhdai.chatapp.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;

/**
 * vn.name.chanhdai.chatapp.common
 *
 * @created by ncdai3651408 - StudentID : 18120113
 * @date 7/24/20 - 10:38 AM
 * @description
 */
public class MediaServerUtils {
    public static ByteBuffer getByteBufferFromString(String command) {
        byte[] commandBytes = command.getBytes();

        return ByteBuffer.allocate((Integer.SIZE / 8) + commandBytes.length)
            .putInt(commandBytes.length)
            .put(commandBytes)
            .rewind();
    }

    public static String readStringFromSocket(SocketChannel socketChannel) {
        try {
            // [Integer.SIZE / 8] bytes for string's [length]
            // [length] bytes for string' data

            ByteBuffer lengthBuffer = ByteBuffer.allocate(Integer.SIZE / 8);
            socketChannel.read(lengthBuffer);
            lengthBuffer.rewind();

            ByteBuffer stringBuffer = ByteBuffer.allocate(lengthBuffer.getInt());
            socketChannel.read(stringBuffer);
            stringBuffer.rewind();

            return new String(stringBuffer.array());

        } catch (IOException ioException) {
            ioException.printStackTrace();
            return null;
        }
    }

    public static boolean sendFileToSocket(SocketChannel socketChannel, String filePathToSend) {
        // noinspection DuplicatedCode
        try {
            File fileToSend = new File(filePathToSend);
            RandomAccessFile randomAccessFile = new RandomAccessFile(fileToSend, "r");

            FileChannel fileChannel = randomAccessFile.getChannel();
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

            socketChannel.write(getByteBufferFromString("send"));
            socketChannel.write(getByteBufferFromString(fileToSend.getName()));

            System.out.println("[Media Server] begin write data to " + socketChannel.getRemoteAddress());

            while (fileChannel.read(byteBuffer) > 0) {
                byteBuffer.flip();
                socketChannel.write(byteBuffer);
                byteBuffer.clear();
            }

            System.out.println("[Media Server] end write data to " + socketChannel.getRemoteAddress());

            fileChannel.close();
            randomAccessFile.close();

            return true;
        } catch (FileNotFoundException foundException) {
            try {
                System.err.println("[Media Server] file not found " + filePathToSend);
                socketChannel.write(getByteBufferFromString("file_not_found"));
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            closeChannel(socketChannel);
        }
    }

    public static boolean readFileFromSocket(SocketChannel socketChannel, String filePathToSave) {
        // noinspection DuplicatedCode
        try {
            File fileToSave = new File(filePathToSave);
            RandomAccessFile randomAccessFile = new RandomAccessFile(fileToSave, "rw");

            FileChannel fileChannel = randomAccessFile.getChannel();
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

            System.out.println("[Media Server] begin read data from " + socketChannel.getRemoteAddress());

            while (socketChannel.read(byteBuffer) > 0) {
                byteBuffer.flip();
                fileChannel.write(byteBuffer);
                byteBuffer.clear();
            }

            System.out.println("[Media Server] end read data from " + socketChannel.getRemoteAddress());

            fileChannel.close();
            randomAccessFile.close();

            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            closeChannel(socketChannel);
        }
    }

    public static boolean downloadFile(SocketChannel socketChannel, String filePathToDownload) {
        // noinspection DuplicatedCode
        try {
            String fileNameToDownload = new File(filePathToDownload).getName();

            socketChannel.write(getByteBufferFromString("download"));
            socketChannel.write(getByteBufferFromString(fileNameToDownload));

            String command = readStringFromSocket(socketChannel); // command
            if (command != null && command.equals("file_not_found")) {
                return false;
            }

            readStringFromSocket(socketChannel); // skip fileName

            return readFileFromSocket(socketChannel, filePathToDownload);

        } catch (IOException e) {
            e.printStackTrace();
            return false;
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
