package edu.alexey.oopchat.network;

public interface Connection extends AutoCloseable {

	void addListener(ConnectionListener listener);

	void removeListener(ConnectionListener listener);

	void sendMessage(Message message);

	boolean isClosed();
}
