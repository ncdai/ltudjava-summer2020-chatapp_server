package vn.name.chanhdai.chatapp.server;

public class Main {
    public static void main(String[] args) {
        // Chat Server
        new Server(8080).start();

        // Media Server
        new SocketChannelServer(9999, "/Users/ncdai3651408/IdeaProjects/chatapp_server/uploads/").start();
    }
}
