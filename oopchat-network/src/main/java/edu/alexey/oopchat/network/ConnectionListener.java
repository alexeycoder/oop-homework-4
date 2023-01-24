package edu.alexey.oopchat.network;

/** События для оповещения слушателя соединения. */
public interface ConnectionListener {
	/**
	 * Событие, возникающее по готовности соединения.
	 * 
	 * @param connection Соединение-источник события.
	 */
	void onConnectionReady(Connection connection);

	/**
	 * Событие, возникающее когда слушатель принимает строку данных.
	 * 
	 * @param connection Соединение-источник события.
	 * @param message    Полученное сообщение.
	 */
	void onReceiveMessage(Connection connection, Message message);

	/**
	 * Обрыв соединения.
	 * 
	 * @param connection Соединение источник события.
	 */
	void onDisconnect(Connection connection);

	/**
	 * Исключительная ситуация.
	 * 
	 * @param connection Соединение-источник события.
	 * @param ex         Исключение.
	 */
	void onException(Connection connection, Exception ex);
}
