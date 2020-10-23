package ru.geekbrains.java_two.chat.server.core;

import ru.geekbrains.java_two.chat.common.Library;
import ru.geekbrains.java_two.network.ServerSocketThreadListener;
import ru.geekbrains.java_two.network.ServerSocketThread;
import ru.geekbrains.java_two.network.SocketThread;
import ru.geekbrains.java_two.network.SocketThreadListener;

import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Vector;

public class ChatServer implements ServerSocketThreadListener, SocketThreadListener {
    private final DateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss; ");
    private final ChatServerListener listener;
    private final Vector<SocketThread> clients;
    private ServerSocketThread thread;
    long b = System.currentTimeMillis();


    public ChatServer(ChatServerListener listener) {
    this.listener = listener;
    this.clients = new Vector<>();
}
    public void start(int port) {
        if (thread != null && thread.isAlive()) {
            putLog("Server already started");
        } else {
            thread = new ServerSocketThread(this,"Thread of server", port, 2000);
        }
    }

    public void stop() {
        if (thread == null || !thread.isAlive()) {
            putLog("Server is not running");
        } else {
            thread.interrupt();
        }
    }

    private void putLog(String msg) {  // говорим, что в такое-то время произошло такое-то событие
        msg = DATE_FORMAT.format(System.currentTimeMillis()) +
                Thread.currentThread().getName() + ": " + msg;
        listener.onChatServerMessage(msg);
    }

    private void handleNonAuthMessage(ClientThread client, String msg) {
        do {
            String[] arr = msg.split(Library.DELIMITER);
            if (arr.length != 3 || !arr[0].equals(Library.AUTH_REQUEST)) {
                client.msgFormatError(msg);
                return;
            }
            String login = arr[1];
            String password = arr[2];
            String nickname = SqlClient.getNickname(login, password);
            if (nickname == null) {
                putLog("invalid login attempt: " + login);
                client.authFail();
                return;
            } else {
                ClientThread oldClient = findClientByNickname(nickname);
                client.authAccept(nickname);
                if (oldClient == null) {
                    sendToAllAuthorizedClient(Library.getTypeBroadcast("Server", nickname + " connected"));
                } else {
                    oldClient.reconnect();
                    clients.remove(oldClient);
                }
            }

            sendToAllAuthorizedClient(Library.getUserList(getUsers()));
        }
        while (timeOutCount(b));
        client.timeOutAuthorization();
    }

    private void handleAuthMessage(ClientThread client, String msg) {
        String[] arr = msg.split(Library.DELIMITER);
        String msgType = arr[0];
        switch (msgType) {
            case Library.TYPE_BCAST_CLIENT:
                sendToAllAuthorizedClient(Library.getTypeBroadcast(client.getNickname(), arr[1]));
        break;
            default:
                client.msgFormatError(Library.getMsgFormatError(msg));


        }
    }

    public boolean timeOutCount(long b) {
        final long timedOut = 120000;
        long endTime = b + timedOut;
        if (System.currentTimeMillis() < endTime) {
            return true;
        } else {
            return false;
        }
    }

    private void sendToAllAuthorizedClient(String msg) {
        for (int i = 0; i < clients.size(); i++) {
            ClientThread recipient = (ClientThread) clients.get(i);
            if (!recipient.isAuthorized()) continue;
            recipient.sendMessage(msg);
        }
    }

    private String getUsers() {  //
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < clients.size(); i++) { // пробегаемся по всему вектору с клиентами
            ClientThread client = (ClientThread) clients.get(i); // превращаем их в клиентов потому, что там хранятся сокет треды
            if (!client.isAuthorized()) continue; // если они не авторизованные, то они нас не интересуют
            sb.append(client.isAuthorized()).append(Library.DELIMITER); // если они авторизованные, то мы их добавляем стрингбилдером по никнейму в некую строку разделяя делемитером
        }
        return sb.toString();
    }

    private synchronized ClientThread findClientByNickname(String nickname) { //метод поиска клиентов по никнейму
        for (int i = 0; i <clients.size() ; i++) {// бежим по всему списку клиентов
            ClientThread client = (ClientThread) clients.get(i); //
            if (!client.isAuthorized()) continue;
            if (client.getNickname().equals(nickname))
                return client;
        }
        return null;
    }

    @Override
    public void onServerStart(ServerSocketThread thread) {
        putLog("Server thread started");
        SqlClient.connect();
    }

    @Override
    public void onServerStop(ServerSocketThread thread) {
        putLog("Server thread stopped");
        SqlClient.disconnect();
        for (int i = 0; i < clients.size() ; i++) {
            clients.get(i).close();
        }
    }

    @Override
    public void onServerSocketCreated(ServerSocketThread thread, ServerSocket server) {
        putLog("Server socked created");
    }

    @Override
    public void onServerTimeout(ServerSocketThread thread, ServerSocket server) {
//        putLog("Server timeout");
    }

    @Override
    public void onServerException(ServerSocketThread thread, Throwable exception) {
        exception.printStackTrace();
    }

    @Override
    public void onSocketAccepted(ServerSocketThread thread, ServerSocket server, Socket socket) {
    putLog("Client connected");
    String name = "SocketThread " + socket.getInetAddress() + ":" + socket.getPort();
    new ClientThread(this, name, socket);
    }

    @Override
    public void onSocketStart(SocketThread thread, Socket socket) {
        putLog("Server started");
    }

    @Override
    public void onSocketStop(SocketThread thread) {
        putLog("Serves stopped");
        clients.remove(thread);
        ClientThread client = (ClientThread) thread;
        if (client.isAuthorized() && !client.isReconnecting()) {
            sendToAllAuthorizedClient(Library.getTypeBroadcast("Server",
                    client.getNickname() + "disconnected"));
        }
        sendToAllAuthorizedClient(Library.getUserList(getUsers()));
    }

    @Override
    public void onSocketReady(SocketThread thread, Socket socket) {
        putLog("Server ready");
        clients.add(thread);

    }

    @Override
    public void onReceiveString(SocketThread thread, Socket socket, String msg) {
        ClientThread client = (ClientThread) thread;
        if (client.isAuthorized())
            handleAuthMessage(client, msg);
        else
            handleNonAuthMessage(client, msg);
    }

    @Override
    public void onSocketException(SocketThread thread, Exception exception) {
        exception.printStackTrace();
    }
}