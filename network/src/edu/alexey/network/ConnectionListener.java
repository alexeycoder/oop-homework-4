package edu.alexey.network;

/** События слушателя соединения. */
public interface ConnectionListener {
	/**
	 * Событие, возникающее по готовности соединения.
	 * 
	 * @param connection Источник события -- соединение.
	 */
	void onConnectionReady(Connection connection);

	/**
	 * Событие, возникающее когда клиент представляется серверу, непосредственно
	 * сразу после установления соединения.
	 * 
	 * @param connection   Источник события -- соединение.
	 * @param subscriberId Какой-либо идентификатор клиента чата.
	 */
	void onAuthenticate(Connection connection, String subscriberId);

	/**
	 * Событие, возникающее когда слушатель принимает строку данных.
	 * 
	 * @param connection Источник события -- соединение.
	 * @param message    Полученная строка.
	 */
	void onReceiveMessage(Connection connection, String message);

	/**
	 * Обрыв соединения.
	 * 
	 * @param connection Источник события -- соединение.
	 */
	void onDisconnect(Connection connection);

	/**
	 * Исключительная ситуация.
	 * 
	 * @param connection Источник события -- соединение.
	 * @param ex         Исключение.
	 */
	void onException(Connection connection, Exception ex);
}
