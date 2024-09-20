package io.vaku.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;

public class Server {
    private int port;
    private Map<String, ClientHandler> clients;

    public Server(int port) {
        this.port = port;
        this.clients = new HashMap<>();
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Сервер запущен на порту: " + port);
            while (true) {
                subscribe(new ClientHandler(this, serverSocket.accept()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void subscribe(ClientHandler clientHandler) {
        clients.put(clientHandler.getUserName(), clientHandler);
    }

    public synchronized void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler.getUserName());
    }

    public synchronized void broadcastMessage(String message) {
        for (ClientHandler client : clients.values()) {
            client.sendMessage(message);
        }
    }

    public synchronized void sendMessageTo(String from, String to, String message) {
        clients.get(to).sendMessage("Private message from " + from + ":" + message);
    }

    public synchronized void sendMessageTo(String from, String message) {
        clients.get(from).sendMessage(message);
    }
}
