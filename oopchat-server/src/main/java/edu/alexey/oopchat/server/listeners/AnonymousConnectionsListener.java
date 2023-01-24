package edu.alexey.oopchat.server.listeners;

import java.util.Objects;
import java.util.function.BiConsumer;

import edu.alexey.oopchat.network.Connection;
import edu.alexey.oopchat.network.Message;
import edu.alexey.oopchat.network.MessageType;

public class AnonymousConnectionsListener extends AbstractConnectionListener {

	private final BiConsumer<Connection, String> authenticatedConnectionConsumer;

	public AnonymousConnectionsListener(BiConsumer<Connection, String> authenticatedConnectionConsumer) {
		super();
		Objects.requireNonNull(authenticatedConnectionConsumer);
		this.authenticatedConnectionConsumer = authenticatedConnectionConsumer;
	}

	@Override
	protected void handleConnectionReady(Connection connection) {
		addConnection(connection, null);
		System.out.println("Сервер чата: Зарегистрировано новое анонимное подключение.");
	}

	@Override
	protected void handleReceiveMessage(Connection connection, Message message) {
		// игнорировать любые сообщения, кроме HANDSHAKE
		if (message == null || message.type() != MessageType.HANDSHAKE) {
			return;
		}

		var userName = message.data();
		if (userName == null || userName.isBlank()) {
			return;
		}

		var itemOpt = removeConnection(connection);
		if (itemOpt.isPresent()) {
			connection.removeListener(this);
			authenticatedConnectionConsumer.accept(connection, userName);
		}
	}

	@Override
	protected void handleDisconnect(Connection connection) {
		System.out.printf("Сервер чата: Закрыто анонимное соединение %s.%n", connection);
	}

}
