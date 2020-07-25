package vn.name.chanhdai.chatapp.server;

import vn.name.chanhdai.chatapp.common.Config;

public class Main {
    public static void main(String[] args) {
        // Chat Server
        new Server(Config.CHAT_SERVER_PORT).start();

        // Media Server
        new SocketChannelServer(
            Config.MEDIA_SERVER_PORT,
            Config.MEDIA_SERVER_UPLOAD_DIR
        ).start();
    }
}
