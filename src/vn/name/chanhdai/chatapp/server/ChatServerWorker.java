package vn.name.chanhdai.chatapp.server;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.Socket;
import java.util.HashSet;
import java.util.List;

/**
 * vn.name.chanhdai.chatapp.server
 *
 * @created by ncdai3651408 - StudentID : 18120113
 * @date 7/17/20 - 8:09 PM
 * @description
 */
public class ChatServerWorker extends Thread {
    private final ChatServer chatServer;
    private final Socket clientSocket;
    private final String logPrefix;

    private String user = null;
    private final HashSet<String> groupSet = new HashSet<>();

    InputStream inputStream;
    OutputStream outputStream;

    public ChatServerWorker(ChatServer chatServer, Socket clientSocket) {
        this.chatServer = chatServer;
        this.clientSocket = clientSocket;

        this.logPrefix = "[Chat Server Thread @" + getName() + "] ";
    }

    public String getUser() {
        return this.user;
    }

    public boolean isMemberOfGroup(String groupKey) {
        return this.groupSet.contains(groupKey);
    }

    void send(String msg) {
        try {
            outputStream.write(msg.getBytes());
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    // command : login <username>
    void handleLogin(String[] tokens) {
        if (tokens.length != 3) {
            return;
        }

        String username = tokens[1];

        this.send("login ok\n");
        this.user = username;

        List<ChatServerWorker> chatServerWorkerList = this.chatServer.getChatServerWorkerList();

        // send to current user : all other online
        for (ChatServerWorker chatServerWorker : chatServerWorkerList) {
            if (chatServerWorker.getUser() != null && !chatServerWorker.getUser().equals(this.user)) {
                this.send("online " + chatServerWorker.getUser() + "\n");
            }
        }

        // send to other online users : current user's status
        for (ChatServerWorker chatServerWorker : chatServerWorkerList) {
            if (chatServerWorker.getUser() != null && !chatServerWorker.getUser().equals(this.user)) {
                chatServerWorker.send("online " + this.user + "\n");
            }
        }

        System.out.println(logPrefix + username + " dang nhap thanh cong!");
    }

    // command : logout
    void handleLogout() {
        try {
            this.chatServer.removeChatServerWorker(this);

            if (this.user != null) {
                List<ChatServerWorker> chatServerWorkerList = this.chatServer.getChatServerWorkerList();

                // send to other online users : current user's status (offline)
                String response = "offline " + this.user + "\n";
                for (ChatServerWorker chatServerWorker : chatServerWorkerList) {
                    if (chatServerWorker.getUser() != null) {
                        chatServerWorker.send(response);
                    }
                }

                System.out.println(logPrefix + this.user + " dang xuat thanh cong!");
            }

            System.out.println(logPrefix + clientSocket.getPort() + " ngat ket noi!");
            this.clientSocket.close();

        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    // command : message <to_user> <body ...>
    // result -> message <from_user>:<to_user> <body ...>

    // command : message #<to_group> <body ...>
    // result -> message <from_user>:#<to_group> <body ...>

    void handleMessage(String[] tokens) {
        if (tokens.length != 3) {
            return;
        }

        String sender = this.getUser();
        String receiver = tokens[1];
        String message = tokens[2];

        boolean isGroup = receiver.charAt(0) == '#';
        String response = "message " + sender + ":" + receiver + " " + message + "\n";

        List<ChatServerWorker> chatServerWorkerList = this.chatServer.getChatServerWorkerList();
        for (ChatServerWorker chatServerWorker : chatServerWorkerList) {
            if (chatServerWorker.getUser() == null) {
                continue;
            }

            if (isGroup) {
                if (chatServerWorker.isMemberOfGroup(receiver)) {
                    chatServerWorker.send(response);
                }
            } else {
                if (chatServerWorker.getUser().equals(receiver)) {
                    chatServerWorker.send(response);
                    break;
                }
            }
        }

        if (!isGroup) {
            this.send(response);
        }
    }

    // command : join <#group_key>
    void handleJoinGroup(String[] tokens) {
        if (tokens.length != 2) {
            return;
        }

        String groupKey = tokens[1];
        this.groupSet.add(groupKey);
        this.send("join " + groupKey + " ok\n");
    }

    // command : leave <#group_key>
    void handleLeaveGroup(String[] tokens) {
        if (tokens.length != 2) {
            return;
        }

        String groupKey = tokens[1];
        this.groupSet.remove(groupKey);
        this.send("leave " + groupKey + " ok\n");
    }

    @Override
    public void run() {
        try {
            inputStream = clientSocket.getInputStream();
            outputStream = clientSocket.getOutputStream();

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            logout:
            while ((line = bufferedReader.readLine()) != null) {
                String[] tokens = StringUtils.split(line);
                if (tokens != null && tokens.length > 0) {
                    String command = tokens[0];
                    switch (command) {
                        case "logout":
                            // logout
                            handleLogout();
                            break logout;

                        case "login":
                            // login <username>
                            handleLogin(tokens);
                            break;

                        case "message":
                            // message <to_user/#to_group> <body ...>
                            String[] newTokens = StringUtils.split(line, " ", 3);
                            handleMessage(newTokens);
                            break;

                        case "join":
                            // join <#group_key>
                            handleJoinGroup(tokens);
                            break;

                        case "leave":
                            // leave <#group_key>
                            handleLeaveGroup(tokens);
                            break;

                        default:
                            // unknown
                            this.send(command + " unknown\n");
                            break;
                    }
                }
            }
        } catch (IOException ioException) {
            System.err.println(logPrefix + "ServiceWorker.java -> run() -> IOException");
            ioException.printStackTrace();
        }
    }
}
