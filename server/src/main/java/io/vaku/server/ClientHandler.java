package io.vaku.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private static int userCount = 0;

    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String userName;

    public ClientHandler(Server server, Socket socket) throws IOException {
        this.server = server;
        this.socket = socket;
        this.in = new DataInputStream(socket.getInputStream());
        this.out = new DataOutputStream(socket.getOutputStream());
        this.userName = "user" + ++userCount;
        init();
    }

    private void init() {
        new Thread(() -> {
            try {
                System.out.println("Клиент подключился: " + getUserName());
                while (true) {
                    String message = in.readUTF();
                    if (message.startsWith("/")) {
                        if (message.startsWith("/exit")) {
                            sendMessage("/exitok");
                            break;
                        }
                        if (message.startsWith("/w")) {
                            String[] arr = message.split(" ");
                            if (arr.length < 3) {
                                server.sendMessageTo(getUserName(), "Invalid input, try again");
                                continue;
                            }
                            String addresseeName = arr[1];
                            server.sendMessageTo(
                                    getUserName(),
                                    addresseeName,
                                    message.substring(message.indexOf(addresseeName) + addresseeName.length())
                            );
                        }
                    } else {
                        server.broadcastMessage(userName + " : " + message);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                disconnect();
            }
        }).start();
    }

    public String getUserName() {
        return userName;
    }

    public void sendMessage(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        server.unsubscribe(this);
        try {
            in.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
