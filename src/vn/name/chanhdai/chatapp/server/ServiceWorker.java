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
public class ServiceWorker extends Thread {
    private final Server server;
    private final Socket clientSocket;
    private final String logPrefix;

    private String user = null;
    private final HashSet<String> groupSet = new HashSet<>();

    InputStream inputStream;
    OutputStream outputStream;

    public ServiceWorker(Server server, Socket clientSocket) {
        this.server = server;
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

    // command : login <username> <password>
    void handleLogin(String[] tokens) {
        if (tokens.length != 3) {
            return;
        }

        String username = tokens[1];
        String password = tokens[2];

        if (
            username.equals("guest") && password.equals("guest") ||
                username.equals("ncdai") && password.equals("ncdai") ||
                username.equals("nttam") && password.equals("nttam") ||
                server.getUser(username, password) != null
        ) {
            this.send("login ok\n");
            this.user = username;

            List<ServiceWorker> serviceWorkerList = this.server.getServiceWorkerList();

            // send to current user : all other online
            for (ServiceWorker serviceWorker : serviceWorkerList) {
                if (serviceWorker.getUser() != null && !serviceWorker.getUser().equals(this.user)) {
                    this.send("online " + serviceWorker.getUser() + "\n");
                }
            }

            // send to other online users : current user's status
            for (ServiceWorker serviceWorker : serviceWorkerList) {
                if (serviceWorker.getUser() != null && !serviceWorker.getUser().equals(this.user)) {
                    serviceWorker.send("online " + this.user + "\n");
                }
            }

            System.out.println(logPrefix + username + " dang nhap thanh cong!");
            return;
        }

        this.send("login failed\n");
        System.out.println(logPrefix + username + " dang nhap that bai!");
    }

    private void handleRegister(String[] tokens) {
        if (tokens.length != 3) {
            return;
        }

        String username = tokens[1];
        String password = tokens[2];

        if (server.addUser(username, password)) {
            this.send("register ok\n");
            return;
        }

        this.send("register failed\n");
    }

    // command : logout
    void handleLogout() {
        try {
            this.server.removeServiceWorker(this);

            if (this.user != null) {
                List<ServiceWorker> serviceWorkerList = this.server.getServiceWorkerList();

                // send to other online users : current user's status (offline)
                String response = "offline " + this.user + "\n";
                for (ServiceWorker serviceWorker : serviceWorkerList) {
                    if (serviceWorker.getUser() != null) {
                        serviceWorker.send(response);
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

    private void handleMessage(String[] tokens) {
        if (tokens.length != 3) {
            return;
        }

        String sender = this.getUser();
        String receiver = tokens[1];
        String message = tokens[2];

        boolean isGroup = receiver.charAt(0) == '#';
        String response = "message " + sender + ":" + receiver + " " + message + "\n";

        List<ServiceWorker> serviceWorkerList = this.server.getServiceWorkerList();
        for (ServiceWorker serviceWorker : serviceWorkerList) {
            if (serviceWorker.getUser() == null) {
                continue;
            }

            if (isGroup) {
                if (serviceWorker.isMemberOfGroup(receiver)) {
                    serviceWorker.send(response);
                }
            } else {
                if (serviceWorker.getUser().equals(receiver)) {
                    serviceWorker.send(response);
                    break;
                }
            }
        }

        if (!isGroup) {
            this.send(response);
        }
    }

    // command : join <#group_key>
    private void handleJoinGroup(String[] tokens) {
        if (tokens.length != 2) {
            return;
        }

        String groupKey = tokens[1];
        this.groupSet.add(groupKey);
        this.send("join " + groupKey + " ok\n");
    }

    // command : leave <#group_key>
    private void handleLeaveGroup(String[] tokens) {
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
                            // login <username> <password>
                            handleLogin(tokens);
                            break;

                        case "register":
                            handleRegister(tokens);
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
