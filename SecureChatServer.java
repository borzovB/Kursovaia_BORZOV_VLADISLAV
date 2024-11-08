package org.broker;

import javax.net.ssl.*; // Импортируем библиотеки для работы с SSL/TLS
import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// Класс безопасного чата, реализующего SSL/TLS для защиты связи
public class SecureChatServer {
    private static final int PORT = 12345; // Порт для подключения клиентов
    private static Set<ClientHandler> clientHandlers = new HashSet<>(); // Хранит всех подключенных клиентов
    private static Map<String, ClientHandler> userMap = new HashMap<>(); // Соответствует имени пользователя и обработчику клиента

    public static void main(String[] args) {
        System.out.println("Безопасный сервер запущен на порту " + PORT);

        // Настройка свойств для key store сервера (хранилище закрытых ключей и сертификатов)
        System.setProperty("javax.net.ssl.keyStore", "server.keystore"); // Указание файла key store
        System.setProperty("javax.net.ssl.keyStorePassword", "QGWOJIRNDYythoPWmNLL"); // Указание пароля для key store

        try {
            // Создаем SSL серверный сокет, который будет использовать TLS для защищенного соединения
            SSLServerSocketFactory ssf = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            SSLServerSocket serverSocket = (SSLServerSocket) ssf.createServerSocket(PORT);

            // Бесконечный цикл для ожидания и обработки подключений клиентов
            while (true) {
                SSLSocket clientSocket = (SSLSocket) serverSocket.accept(); // Принимаем новое соединение
                ClientHandler clientHandler = new ClientHandler(clientSocket); // Создаем новый обработчик для клиента
                clientHandlers.add(clientHandler); // Добавляем обработчик клиента в список
                new Thread(clientHandler).start(); // Запускаем обработчик клиента в новом потоке
            }
        } catch (IOException e) {
            e.printStackTrace(); // Выводим ошибки, связанные с созданием серверного сокета или подключением клиентов
        }
    }

    // Внутренний класс для обработки подключенных клиентов
    private static class ClientHandler implements Runnable {
        private SSLSocket socket; // Сокет для связи с конкретным клиентом
        private PrintWriter out; // Поток для отправки сообщений клиенту
        private String username; // Имя пользователя клиента

        // Конструктор обработчика клиента с инициализацией сокета
        public ClientHandler(SSLSocket socket) {
            this.socket = socket;
        }

        // Метод run() выполняется при запуске потока для обработки связи с клиентом
        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                out = new PrintWriter(socket.getOutputStream(), true); // Инициализируем поток вывода для отправки сообщений клиенту

                // Читаем имя пользователя при подключении клиента
                username = in.readLine();
                synchronized (userMap) { // Синхронизация доступа для добавления пользователя в карту
                    userMap.put(username, this); // Сохраняем пользователя и его обработчик в карте
                }
                System.out.println("Пользователь " + username + " присоединился.");

                // Читаем и обрабатываем сообщения от клиента
                String message;
                while ((message = in.readLine()) != null) {
                    // Сообщение в формате "получатель: сообщение"
                    String[] parts = message.split(":", 2); // Разделяем строку по первому двоеточию
                    if (parts.length == 2) {
                        String recipient = parts[0].trim(); // Имя получателя
                        String msg = parts[1].trim(); // Само сообщение

                        ClientHandler recipientHandler;
                        synchronized (userMap) { // Синхронизация доступа к userMap для получения обработчика получателя
                            recipientHandler = userMap.get(recipient); // Находим обработчик для указанного получателя
                        }

                        if (recipientHandler != null) {
                            // Если получатель найден, отправляем сообщение
                            recipientHandler.out.println(username + " -> " + recipient + ": " + msg);
                        } else {
                            // Если получатель не найден, отправляем сообщение отправителю об ошибке
                            out.println("Пользователь " + recipient + " не найден.");
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("Клиент отключился: " + username); // Сообщение при отключении клиента
            } finally {
                // Закрываем сокет и убираем клиента из списков при завершении работы
                try {
                    socket.close(); // Закрытие сокета клиента
                } catch (IOException e) {
                    e.printStackTrace();
                }
                synchronized (userMap) {
                    if (username != null) {
                        userMap.remove(username); // Удаление клиента из карты пользователей
                    }
                }
                synchronized (clientHandlers) {
                    clientHandlers.remove(this); // Удаление обработчика из списка подключенных клиентов
                }
            }
        }
    }
}
