package ru.geekbrains.java_two.chat.server.core;

import ru.geekbrains.java_two.chat.common.Library;
import ru.geekbrains.java_two.network.SocketThread;
import ru.geekbrains.java_two.network.SocketThreadListener;

import java.net.Socket;

public class ClientThread extends SocketThread {
    private String nickname;
    private boolean isAuthorized;
    private boolean isReconnecting;




    public ClientThread(SocketThreadListener listener, String name, Socket socket) {
        super(listener, name, socket);
    }


    public String getNickname() {
        return nickname;
    }

    public boolean isAuthorized() {
        return isAuthorized;
    }

    public boolean isReconnecting() {
        return isReconnecting;
    }

    void  reconnect() {
        isReconnecting = true;
        close();
    }




   // если сказали клиенту, что он авторизован,
    void authAccept(String nickname) {  // служебный метод : Мы тебя авторизовали
        isAuthorized = true; //  он устанавливает флажок
        this.nickname = nickname; //  устанавливать свойства никнейма
        sendMessage(Library.getAuthAccept(nickname));// и отправять сообщения на клиенсткую сторону (в клиентские сокет) о подтверждении авторизации
    }
// если отказали в авторизации
    void authFail(){  // служебный метод : Мы тебя не авторизовали
        sendMessage(Library.getAuthDenied()); // отправляем сообщение об отказе авторизации
        close(); // закрываем сокет
    }
// если ошибка формата сообщения
    void msgFormatError(String msg) { // служебный метод : Мы не поняли что ты сказал
        sendMessage(Library.getMsgFormatError(msg));// отправляет сообщение о том что сервер не понял сообщения
        close(); // и закрывает себя
    }


    // если клиент не ваторизовался
    void timeOutAuthorization() {
        sendMessage(Library.getAuthTimeout());
        close();
    }

}
