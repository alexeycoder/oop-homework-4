package edu.alexey.oopchat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

import edu.alexey.network.Connection;
import edu.alexey.network.ConnectionListener;

/** Сервер Чата. */
public class ChatServer implements ConnectionListener {
	static final int PORT = 5177;

	private final List<NamedConnection> namedConnections = new ArrayList<>();

	ChatServer() {
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
		namedConnections.add(new NamedConnection(connection));
		sendToAllConnections(String.format("Новый участник чата (%s).", connection.toString()));
	}

	@Override
	public void onAuthenticate(Connection connection, String subscriberId) {
		var namedConnection = getNamedConnection(connection);
		namedConnection.setSubscriberId(subscriberId);
		sendToAllConnections(String.format("Добро пожаловать %s!", subscriberId));
		System.out.println(String.format("Пользователь назвался: %s <- %s", subscriberId, connection.toString()));
	}

	@Override
	public synchronized void onReceiveMessage(Connection connection, String message) {
		var namedConnection = getNamedConnection(connection);
		sendToAllConnections(String.format("%s: %s", namedConnection.getSubscriberId(), message));
	}

	@Override
	public synchronized void onDisconnect(Connection connection) {
		var namedConnection = getNamedConnection(connection);
		namedConnections.remove(namedConnection);
		sendToAllConnections(String.format("Пользователь %s вышел из чата (%s).",
				namedConnection.getSubscriberId(),
				connection.toString()));
	}

	@Override
	public synchronized void onException(Connection connection, Exception ex) {
		System.err.println("Произошло исключение при работе с соединением:");
		System.err.println(ex.getMessage());
		ex.printStackTrace();
	}

	private void sendToAllConnections(String message) {
		System.out.println(message);
		namedConnections.forEach(nc -> nc.getConnection().sendMessage(message));
	}

	private NamedConnection getNamedConnection(Connection connection) {
		return namedConnections.stream()
				.filter(nc -> nc.getConnection().equals(connection)).findFirst().get();
	}
}
