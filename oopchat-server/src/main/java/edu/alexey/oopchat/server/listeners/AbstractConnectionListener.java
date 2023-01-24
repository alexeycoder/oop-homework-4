package edu.alexey.oopchat.server.listeners;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import edu.alexey.oopchat.network.Connection;
import edu.alexey.oopchat.network.ConnectionListener;
import edu.alexey.oopchat.network.Message;
import edu.alexey.oopchat.server.entities.User;

abstract class AbstractConnectionListener implements ConnectionListener, AutoCloseable {
	protected final List<ConnectionItem> list;
	private boolean isClosed;

	AbstractConnectionListener() {
		this.list = new ArrayList<>();
		this.isClosed = false;
	}

	protected final void addConnection(Connection connection, User user) {
		if (isClosed) {
			return;
		}
		assert connection != null;
		list.add(new ConnectionItem(connection, LocalDateTime.now(), user));
	}

	protected final Optional<ConnectionItem> removeConnection(Connection connection) {
		if (isClosed) {
			return Optional.empty();
		}
		assert connection != null;
		var itemToRemoveOpt = findConnectionItem(connection);
		if (itemToRemoveOpt.isPresent()) {
			list.remove(itemToRemoveOpt.get());
		}
		return itemToRemoveOpt;
	}

	protected final Optional<ConnectionItem> findConnectionItem(Connection connection) {
		return list.stream().filter(item -> item.connection.equals(connection)).findAny();
	}

	@Override
	public final synchronized void onConnectionReady(Connection connection) {
		if (isClosed) {
			return;
		}
		handleConnectionReady(connection);
	}

	protected abstract void handleConnectionReady(Connection connection);

	@Override
	public final synchronized void onReceiveMessage(Connection connection, Message message) {
		if (isClosed) {
			return;
		}
		handleReceiveMessage(connection, message);
	}

	protected abstract void handleReceiveMessage(Connection connection, Message message);

	@Override
	public final synchronized void onDisconnect(Connection connection) {
		if (isClosed) {
			return;
		}
		removeConnection(connection);
		handleDisconnect(connection);
	}

	protected abstract void handleDisconnect(Connection connection);

	@Override
	public final synchronized void onException(Connection connection, Exception ex) {
		System.err.println("Произошло исключение при работе с соединением:");
		System.err.println(ex.getMessage());
		ex.printStackTrace();
	}

	@Override
	public final synchronized void close() {
		isClosed = true;
		if (list.isEmpty()) {
			return;
		}

		var iter = list.listIterator();
		while (iter.hasNext()) {
			try {
				var item = iter.next();
				item.connection().close();
				iter.remove();

			} catch (Exception ex) {
				System.err.println("Произошло исключение при закрытии соединения:");
				System.err.println(ex.getMessage());
				ex.printStackTrace();
			}
		}
	}

	// nested types:

	protected record ConnectionItem(Connection connection, LocalDateTime establishedDateTime, User user) {
	}
}
