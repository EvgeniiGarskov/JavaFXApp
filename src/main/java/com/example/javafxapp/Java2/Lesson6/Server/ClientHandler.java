package com.example.javafxapp.Java2.Lesson6.Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientHandler {

    private MainServ serv;
    private Socket socket;
    private String nick;

    DataInputStream in;
    DataOutputStream out;

    List<String> blacklist;

    public ClientHandler(MainServ serv, Socket socket) {
        try {
            this.serv = serv;
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            this.blacklist = new ArrayList<>();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        //Цикл, который отвечает за взаимодействие
                        while (true) {
                            String str = in.readUTF();
                            if (str.startsWith("/auth"));
                            String[] tokens = str.split(" ");
                            String currentNick = AuthService.getNickByLoginAndPass(tokens[1], tokens[2]);
                            if (currentNick != null) {
                                if (!serv.isNickBusy(currentNick)) {
                                    sendMsg("/authok");
                                    nick = currentNick;
                                    serv.subscribe(ClientHandler.this);
                                    break;
                                } else {
                                    sendMsg("Учетная запись уже используется");
                                }
                            } else {
                                sendMsg("Неверный логин/пароль");
                            }
                        }
                        //Создаем бесконечный цикл, в котором мы будем взаимодействовать с нашим клиентом (авторизация)
                        //У каждого клиента появляется такой цикл
                        while (true) {
                            //Мы в бесконечном цикле слушаем сообщение
                            String str = in.readUTF();
                            //Условие начали ли мы сообщение со слэша
                            if (str.startsWith("/")) {
                                //Условие выхода
                                if (str.equals("/end")) {
                                    out.writeUTF("/serverClosed");
                                    break;
                                }
                                //Если сообщение начинается со /w
                                if (str.startsWith("/w ")) {
                                    //то нам необходимо разделить сообщение на три части по пробелам
                                    String[] tokens = str.split(" ", 3);
                                    //String m = str.substring(tokens[1].length() + 4);
                                    serv.sendPersonalMsg(ClientHandler.this, tokens[1], tokens [2]);
                                }
                                //Реализация черного списка
                                //Если пришло сообщение, которое начинается со /blacklist
                                if (str.startsWith("/blacklist ")) {
                                    String[] tokens = str.split(" ");
                                    //то добавляем пользователя в черный список
                                    blacklist.add(tokens[1]);
                                    sendMsg("Вы добавили пользователя " + tokens[1] + " в черный список");
                                }
                            } else {
                                serv.broadcastMsg(ClientHandler.this, nick + " : " + str);
                            }
                            System.out.println("Client: " + str);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            in.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        serv.unsubscribe(ClientHandler.this);
                    }
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(String msg) {
        try {
            //У каждого клиента вызывается метод для отправки сообщения. То есть мы одно сообщение распределяем по всем клиентам
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getNick () {
        return nick;
    }

    public boolean checkBlackList(String nick) {
        return blacklist.contains(nick);
    }
}
