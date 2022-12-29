package edu.alexey.oopchat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

import edu.alexey.network.Connection;
import edu.alexey.network.ConnectionListener;

public class ChatServer implements ConnectionListener {
	public static final int PORT = 8189;

	public static void main(String[] args) {
		new ChatServer();
	}

	private final List<Connection> connections = new ArrayList<>();

	public ChatServer() {
		System.out.println("Сервер чата: старт.");
		try (ServerSocket serverSocket = new ServerSocket(PORT)) {

			while (true) {
				try {
					System.out.println("Сервер чата: в ожидании соединения...");
					new Connection(this, serverSocket.accept());

				} catch (IOException ex) {
					System.err.println("Сервер чата: Произошла ошибка при установлении соединения.");
					System.err.println(ex.getMessage());
				}
			}

		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	@Override
	public synchronized void onConnectionReady(Connection connection) {
		connections.add(connection);
		sendToAllConnections("Пользователь вошёл в чат. " + connection.toString());
	}

	@Override
	public synchronized void onReceiveMessage(Connection connection, String message) {
		sendToAllConnections(message);
	}

	@Override
	public synchronized void onDisconnect(Connection connection) {
		connections.remove(connection);
	}

	@Override
	public synchronized void onException(Connection connection, Exception ex) {
		System.err.println("Произошло исключение при работе с соединением:");
		System.err.println(ex.getMessage());
		ex.printStackTrace();
	}

	private void sendToAllConnections(String message) {
		System.out.println(message);
		for (Connection connection : connections) {
			connection.sendMessage(message);
		}
	}
}
