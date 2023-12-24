package com.example.javafxapp.Java2.Lesson6.Server;

import java.sql.*;

public class AuthService {

    //Объект, который позволяет соединить наше приложение и базу данных
    private static Connection connection;
    //Объект с помощью которого мы можем передавать запросы в базу данных и получать какой-то результат
    private static Statement stmt;

    //Метод подключения к базе данных
    public static void connect() {
        try {
            //Инициализация драйвера
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:main.db");
            stmt = connection.createStatement();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    public static String getNickByLoginAndPass(String login, String pass) {
//        String sql = String.format("SELECT nickname FROM users WHERE login = '%s' AND password = '%s'", login, pass);
//        try {
//            ResultSet rs = stmt.executeQuery(sql);
//            if (rs.next()) {
//                return rs.getString(1);
//            }
//        } catch (SQLException throwables) {
//            throwables.printStackTrace();
//        }
//        return null;
//    }

    //Метод добавления пользователей, который принимает на вход логин, пароль и ник
    public static void addUser(String login, String pass, String nick) {
        try {
            String query = "INSERT INTO users (login, password, nickname) VALUES (?, ?, ?);";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, login);
            //пароль добавляем через хэшкод
            ps.setInt(2, pass.hashCode());
            ps.setString(3, nick);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //В этом методе проверка на правильный пароль
    //Приходит пароль от пользователя
    public static String getNickByLoginAndPass(String login, String pass) {
        try {
            ResultSet rs = stmt.executeQuery("SELECT nickname, password FROM users WHERE login = '" + login + "'");
            //Вычисляем хэш код пришедшего пароля
            int myHash = pass.hashCode();
            //106438208
            if (rs.next()) {
                //Берем ник
                String nick = rs.getString(1);
                //Смотрим хэш код, который лежит в базе данных
                int dbHash = rs.getInt(2);
                //И просто сравниваем их
                if (myHash == dbHash) {
                    return nick;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    //Метод, который позволяет отключиться от баз данных
    public static void disconnect() {
        try {
            connection.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}
