package org.broker;

import javax.net.ssl.*; // Импортируем библиотеки для работы с SSL/TLS
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import javax.swing.*;

// Класс клиента для безопасного чата с графическим интерфейсом, использующего SSL/TLS для защищенного соединения
public class SecureChatClient {
    private SSLSocket socket; // Сокет для защищенного соединения с сервером
    private PrintWriter out; // Поток для отправки сообщений на сервер
    private BufferedReader in; // Поток для чтения сообщений с сервера
    private JTextArea textArea; // Область для отображения сообщений в GUI
    private String username; // Имя пользователя, заданное клиентом

    // Конструктор класса клиента, устанавливает защищенное SSL/TLS-соединение с сервером
    public SecureChatClient(String serverAddress, int serverPort) throws IOException {
        // Настраиваем trust store, который содержит доверенные сертификаты для проверки подлинности сервера
        System.setProperty("javax.net.ssl.trustStore", "client.truststore"); // Указываем trust store для клиента
        System.setProperty("javax.net.ssl.trustStorePassword", "YYNGskZGAcnwMfKRPVCF"); // Указываем пароль для trust store

        // Получаем фабрику SSL-сокетов и создаем защищенный сокет для подключения к серверу
        SSLSocketFactory ssf = (SSLSocketFactory) SSLSocketFactory.getDefault();
        socket = (SSLSocket) ssf.createSocket(serverAddress, serverPort);

        // Инициализируем потоки для обмена данными с сервером
        in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // Входящий поток для получения данных
        out = new PrintWriter(socket.getOutputStream(), true); // Исходящий поток для отправки данных
    }

    // Метод для отправки сообщений на сервер
    public void sendMessage(String message) {
        out.println(message); // Отправляем сообщение в сервер через выходной поток
    }

    // Метод создания и отображения графического интерфейса клиента
    private void createAndShowGUI() {
        // Создаем основное окно клиента
        JFrame frame = new JFrame("Безопасный клиент чата");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Задаем операцию закрытия окна
        frame.setSize(400, 300); // Размер окна
        frame.setLayout(new BorderLayout()); // Макет для компонентов окна

        // Создаем текстовую область для отображения сообщений, которая прокручивается и только для чтения
        textArea = new JTextArea();
        textArea.setEditable(false); // Делаем текстовую область только для чтения
        frame.add(new JScrollPane(textArea), BorderLayout.CENTER); // Добавляем ее в центр окна

        // Создаем панель в нижней части окна для отправки сообщений
        JPanel panel = new JPanel();
        JTextField recipientField = new JTextField(10); // Поле для ввода имени получателя
        JTextField messageField = new JTextField(20); // Поле для ввода текста сообщения
        JButton sendButton = new JButton("Отправить"); // Кнопка для отправки сообщения

        // Добавляем компоненты на панель
        panel.add(new JLabel("Получатель:")); // Метка для получателя
        panel.add(recipientField); // Поле для ввода получателя
        panel.add(new JLabel("Сообщение:")); // Метка для сообщения
        panel.add(messageField); // Поле для ввода сообщения
        panel.add(sendButton); // Кнопка отправки сообщения

        // Добавляем панель с полями и кнопкой в нижнюю часть окна
        frame.add(panel, BorderLayout.SOUTH);
        frame.setVisible(true); // Делаем окно видимым

        // Обработчик события нажатия кнопки "Отправить"
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Получаем текст получателя и сообщение из полей
                String recipient = recipientField.getText();
                String message = messageField.getText();
                // Формируем сообщение в формате "получатель: сообщение"
                String fullMessage = recipient + ": " + message;
                // Отображаем сообщение в текстовой области клиента
                textArea.append("Я -> " + recipient + ": " + message + "\n");
                sendMessage(fullMessage); // Отправляем сообщение на сервер
                messageField.setText(""); // Очищаем поле сообщения после отправки
            }
        });
    }

    // Метод для запуска клиента
    public void start() {
        // Запрашиваем у пользователя имя, чтобы идентифицировать его на сервере
        String inputUsername = JOptionPane.showInputDialog("Введите ваше имя пользователя:");
        if (inputUsername != null && !inputUsername.trim().isEmpty()) { // Проверяем, что имя не пустое
            username = inputUsername; // Сохраняем имя пользователя
            sendMessage(username); // Отправляем имя пользователя на сервер при подключении
            createAndShowGUI(); // Запускаем GUI для взаимодействия пользователя

            // Новый поток для постоянного чтения сообщений, поступающих от сервера
            new Thread(() -> {
                String receivedMessage;
                try {
                    while ((receivedMessage = in.readLine()) != null) { // Читаем сообщения от сервера
                        textArea.append(receivedMessage + "\n"); // Отображаем входящее сообщение в текстовой области
                    }
                } catch (IOException e) {
                    e.printStackTrace(); // Обрабатываем возможные ошибки при чтении данных
                } finally {
                    // Закрываем сокет при завершении работы
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start(); // Запускаем поток для приема сообщений от сервера
        }
    }

    // Главный метод для запуска клиента
    public static void main(String[] args) {
        try {
            // Создаем экземпляр клиента и подключаемся к указанному серверу
            SecureChatClient client = new SecureChatClient("192.168.0.21", 12345);
            client.start(); // Запускаем клиентское приложение
        } catch (Exception e) {
            e.printStackTrace(); // Выводим ошибки, если подключение не удалось
        }
    }
}
