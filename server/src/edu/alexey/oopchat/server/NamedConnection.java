package edu.alexey.oopchat.server;

import edu.alexey.network.Connection;

/**
 * Вспомогательный тип для сопоставления соединения и идентификатора
 * пользователя (подписчика сервера).
 */
class NamedConnection {
	private final Connection connection;
	private String subscriberId;

	public NamedConnection(Connection connection) {
		if (connection == null) {
			throw new NullPointerException();
		}
		this.connection = connection;
		this.subscriberId = "undefined";
	}

	public Connection getConnection() {
		return connection;
	}

	public String getSubscriberId() {
		return subscriberId;
	}

	public void setSubscriberId(String subscriberId) {
		assert subscriberId != null && !subscriberId.isBlank();
		this.subscriberId = subscriberId;
	}
}
