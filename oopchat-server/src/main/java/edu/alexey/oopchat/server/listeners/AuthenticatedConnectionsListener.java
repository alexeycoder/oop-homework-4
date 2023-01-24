package edu.alexey.oopchat.server.listeners;

import java.security.InvalidParameterException;
import java.util.Objects;
import java.util.Optional;
import edu.alexey.oopchat.network.Connection;
import edu.alexey.oopchat.network.Message;
import edu.alexey.oopchat.network.MessageType;
import edu.alexey.oopchat.server.entities.User;

public class AuthenticatedConnectionsListener extends AbstractConnectionListener {
	private static final String SERVER_FROM = "Server";

	public AuthenticatedConnectionsListener() {
		super();
	}

	public void register(Connection connection, String userName) {
		Objects.requireNonNull(connection);
		Objects.requireNonNull(userName);
		if (userName.isBlank()) {
			throw new InvalidParameterException();
		}
		if (userName.equals(SERVER_FROM)) {
			return;
		}

		// String token = UUID.randomUUID().toString();
		// User user = new User(userName, token);
		addConnection(connection, new User(userName));
		connection.addListener(this);

		// Подтверждаем рукопожатие отправкой пользователю его токена
		connection.sendMessage(new Message(
				MessageType.HANDSHAKE,
				SERVER_FROM,
				userName));

		// Оповещаем чат о новом участнике
		sendToAllConnections(new Message(
				MessageType.TEXT,
				SERVER_FROM,
				String.format("Добро пожаловать %s!", userName)));

		System.out.println(String.format("Пользователь назвался: %s <- %s", userName, connection.toString()));
	}

	@Override
	protected void handleConnectionReady(Connection connection) {
		// should not be raised
	}

	@Override
	protected void handleReceiveMessage(Connection connection, Message message) {
		// обрабатывать только не HANDSHAKE сообщения
		if (message == null || message.type() != MessageType.TEXT) {
			return;
		}

		Optional<ConnectionItem> itemOpt = findConnectionItem(connection);
		if (itemOpt.isPresent()) {
			var connectionItem = itemOpt.get();

			sendToAllConnections(new Message(
					MessageType.TEXT,
					connectionItem.user().name(),
					message.data()));

		} else {
			System.err.println("Странное поведение: сообщение от неопознанного соединения.");
		}
	}

	@Override
	protected void handleDisconnect(Connection connection) {
		Optional<ConnectionItem> itemOpt = findConnectionItem(connection);
		if (itemOpt.isPresent()) {
			var connectionItem = itemOpt.get();

			sendToAllConnections(new Message(
					MessageType.TEXT,
					SERVER_FROM,
					String.format(
							"Пользователь %s вышел из чата (%s).",
							connectionItem.user().name(),
							connection)));

			System.out.printf("Сервер чата: Закрыто соединение %s.%n", connection);
		}
	}

	private void sendToAllConnections(Message message) {
		System.out.println(message);
		list.forEach(item -> item.connection().sendMessage(message));
	}
}
