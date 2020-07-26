package vn.name.chanhdai.chatapp.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * vn.name.chanhdai.chatapp.server
 *
 * @created by ncdai3651408 - StudentID : 18120113
 * @date 7/17/20 - 9:08 PM
 * @description
 */
public class ChatServer extends Thread {
    private final int port;
    private final List<ChatServerWorker> chatServerWorkerList = new ArrayList<>();

    private final Map<String, String> userList = new HashMap<>();

    public ChatServer(int port) {
        this.port = port;
    }

    public List<ChatServerWorker> getChatServerWorkerList() {
        return this.chatServerWorkerList;
    }

    public void removeChatServerWorker(ChatServerWorker chatServerWorker) {
        this.chatServerWorkerList.remove(chatServerWorker);
    }

    public boolean addUser(String username, String password) {
        if (this.userList.get(username) == null) {
            this.userList.put(username, password);
            return true;
        }

        return false;
    }

    public String getUser(String username, String password) {
        String user = userList.get(username);
        if (user != null && user.equals(password)) {
            return user;
        }

        return null;
    }

    @Override
    public void run() {
        try {
            // Máy chủ khởi tạo một đối tượng ServerSocket với một cổng giao tiếp (port).
            ServerSocket serverSocket = new ServerSocket(this.port);

            // noinspection InfiniteLoopStatement
            while (true) {
                // Máy chủ luôn lắng nghe kết nối
                System.out.println("[Chat Server] is listening on port " + this.port);

                // Máy chủ gọi phương thức accept() của lớp ServerSocket
                // Phương thức mày trả về một tham chiếu đến một socket mới trên máy chủ được kết nối với socket của máy khách
                // Phương thức này đợi cho đến khi một máy khách kết nối đến máy chủ trên cổng đã cho
                Socket clientSocket = serverSocket.accept();

                System.out.println("[Chat Server] has connected from port " + clientSocket.getPort());

                // Tạo luồng và giao nhiệm vụ xử lí kết nối từ Client
                ChatServerWorker chatServerWorker = new ChatServerWorker(this, clientSocket);
                this.chatServerWorkerList.add(chatServerWorker);
                chatServerWorker.start();

                // Tiếp tục vòng lặp
                // Máy chủ lắng nghe kết nối mới
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }
}
