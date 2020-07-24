package vn.name.chanhdai.chatapp.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

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

    public Server(int port) {
        this.port = port;
    }

    public List<ServiceWorker> getServiceWorkerList() {
        return this.serviceWorkerList;
    }

    public void removeServiceWorker(ServiceWorker serviceWorker) {
        this.serviceWorkerList.remove(serviceWorker);
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(this.port);

            // noinspection InfiniteLoopStatement
            while (true) {
                System.out.println("Dang doi ket noi!");
                Socket clientSocket = serverSocket.accept();
                System.out.println("Co ket noi! " + clientSocket.getPort());

                ServiceWorker serviceWorker = new ServiceWorker(this, clientSocket);
                this.serviceWorkerList.add(serviceWorker);
                serviceWorker.start();
            }

        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }
}
