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
public class Server extends Thread {
    private final int port;
    private final List<ServiceWorker> serviceWorkerList = new ArrayList<>();

    private final Map<String, String> userList = new HashMap<>();

    public Server(int port) {
        this.port = port;
    }

    public List<ServiceWorker> getServiceWorkerList() {
        return this.serviceWorkerList;
    }

    public void removeServiceWorker(ServiceWorker serviceWorker) {
        this.serviceWorkerList.remove(serviceWorker);
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
            ServerSocket serverSocket = new ServerSocket(this.port);

            // noinspection InfiniteLoopStatement
            while (true) {
                System.out.println("[Chat Server] is listening on port " + this.port);
                Socket clientSocket = serverSocket.accept();
                System.out.println("[Chat Server] has connected from port " + clientSocket.getPort());

                ServiceWorker serviceWorker = new ServiceWorker(this, clientSocket);
                this.serviceWorkerList.add(serviceWorker);
                serviceWorker.start();
            }

        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }
}
