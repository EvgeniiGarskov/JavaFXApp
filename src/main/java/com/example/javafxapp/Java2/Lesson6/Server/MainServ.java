package com.example.javafxapp.Java2.Lesson6.Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class MainServ {

    //Задача заключается в том, чтобы написать сервер, у которого нет web-интерфейса,
    //запустить его и подключиться к нему
    private Vector<ClientHandler> clients;

    public MainServ() {
        // Write your code here

        //Создаем список клиентов из синхронизированной коллекции
        clients = new Vector<>();

        //Это наш сервер
        ServerSocket server = null;
        Socket socket = null;

        try {
            AuthService.connect();
//            AuthService.addUser("login1", "pass1", "nick1");
//            AuthService.addUser("login2", "pass2", "nick2");
//            AuthService.addUser("login3", "pass3", "nick3");

            //Создаём/запускаем новый сервер и определяем порт;
            server = new ServerSocket(8189);
            System.out.println("Сервер запущен!");

            while (true) {
                //Создаем точку подключения клиента и в цикле ожидаем подключения новых клиентов
                socket = server.accept();
                System.out.println("Клиент подключился!");
                //Как только клиент подключился мы добавляем его в список ClientHandler. В списке ClientHandler для каждого клиента создается отдельный
                //поток и входящий/исходящий потоки для взаимодействия по сети(in и out)
                //У всех клиентов разные сокеты
                new ClientHandler(this, socket);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            AuthService.disconnect();
        }
    }

    //Метод подключения клиента
    public void subscribe(ClientHandler client) {
        clients.add(client);
        broadcastClientsList();
    }
    //Метод отключения клиента
    public void unsubscribe(ClientHandler client) {
        clients.remove(client);
        broadcastClientsList();
    }

    public void broadcastMsg(ClientHandler from, String msg) {
        //Мы берем список клиентов и у каждого клиента вызываем метод sendMsg. То есть передаем сообщение всему списку клиентов и у каждого вызываем sendMsg ->
        for (ClientHandler o : clients) {
            //Если клиента нет в черном списке, то отправляем сообщение
            if (!o.checkBlackList(from.getNick())) {
                o.sendMsg(msg);
            }
        }
    }

    //Метод, который проверяет занят ли ник. Нам на вход приходит ник
    public boolean isNickBusy(String nick) {
        // мы перебираем всех наших клиентов
        for (ClientHandler o : clients) {
            //если ник есть в нашем списке возвращаем true
            if (o.getNick().equals(nick)) {
                return true;
            }
        }
        return false;
    }

    //Метод отправки персональных сообщений, который принимает на вход: от кого, куда и само сообщение
    public void sendPersonalMsg(ClientHandler from, String nickTo, String msg) {
        //Перебираем всех клиентов
        for (ClientHandler o : clients) {
            //Смотрим есть ли такой ник
            if (o.getNick().equals(nickTo)) {
                //Если такой ник есть, то отправляем ему сообщение
                o.sendMsg("from " + from.getNick() + ": " + msg);
                //И отбивка что такое сообщение отправленно
                from.sendMsg("to " + nickTo + ": " + msg);
                return;
            }
        }
        from.sendMsg("Клиент с ником " + nickTo + " не найден в чате");
    }

    public void broadcastClientsList() {
        StringBuilder sb = new StringBuilder();
        sb.append("/clientsList ");
        for (ClientHandler o : clients) {
            sb.append(o.getNick() + " ");
        }
        String out = sb.toString();
        for (ClientHandler o : clients) {
            o.sendMsg(out);
        }
    }
}
