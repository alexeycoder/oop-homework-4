package edu.alexey.oopchat.network;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

class TcpConnection implements Connection {

	private final List<ConnectionListener> listeners;
	private final List<ConnectionListener> listenersToAdd;
	private final List<ConnectionListener> listenersToRemove;

	private final Socket socket;

	private final Thread rxThread;

	private final ObjectInputStream in;
	private final ObjectOutputStream out;

	private boolean isClosed;

	public TcpConnection(List<ConnectionListener> listeners, Socket socket) throws IOException {
		this.listeners = new ArrayList<>(listeners);
		this.listenersToAdd = new ArrayList<>(listeners);
		this.listenersToRemove = new ArrayList<>(listeners);
		this.socket = socket;

		this.out = new ObjectOutputStream(socket.getOutputStream());
		this.out.flush();

		this.in = new ObjectInputStream(socket.getInputStream());

		this.isClosed = false;
		this.rxThread = new Thread(this::listening);
		this.rxThread.start();
	}

	private synchronized void notifyListeners(Consumer<? super ConnectionListener> action) {
		if (!listenersToRemove.isEmpty()) {
			listeners.removeAll(listenersToRemove);
			listenersToRemove.clear();
		}
		if (!listenersToAdd.isEmpty()) {
			for (var listenerToAdd : listenersToAdd) {
				if (!listeners.contains(listenerToAdd)) {
					listeners.add(listenerToAdd);
				}
			}
			listenersToAdd.clear();
		}
		listeners.stream().forEach(action);
	}

	private void listening() {
		try {
			notifyListeners(l -> l.onConnectionReady(this));

			while (!rxThread.isInterrupted()) {

				var messageRaw = in.readObject();
				if (messageRaw instanceof Message message) {

					System.out.printf("Соединение: Получено сообщение [%s:%s]%n", message.type(), message.data());
					notifyListeners(l -> l.onReceiveMessage(this, message));

				} else {
					System.out.println("Соединение: Получено нечитаемое сообщение!");
				}
			}
		} catch (EOFException ex) {
			// ok
		} catch (IOException | ClassNotFoundException ex) {
			System.out.println("Соединение: Произошла ошибка -- получение сообщения вызвало исключение: " + ex);
			notifyListeners(l -> l.onException(this, ex));
		} finally {
			close();
		}
	}

	@Override
	public synchronized void sendMessage(Message message) {
		if (message == null) {
			return;
		}
		try {
			out.writeObject(message);
			out.flush();
		} catch (IOException ex) {
			System.err.println("Соединение: Произошла ошибка -- отправка сообщения вызвала исключение: " + ex);
			notifyListeners(l -> l.onException(this, ex));
			close();
		}
	}

	@Override
	public synchronized boolean isClosed() {
		return isClosed;
	}

	@Override
	public synchronized void close() {
		if (isClosed) {
			return;
		}
		isClosed = true;

		rxThread.interrupt();

		try {
			socket.close();
		} catch (IOException ex) {
			isClosed = false;
			System.err.println(
					"Соединение: Произошла ошибка -- закрытие соединения вызвало исключение: " + ex.getMessage());
			notifyListeners(l -> l.onException(this, ex));
		}

		notifyListeners(l -> l.onDisconnect(this));
	}

	@Override
	public synchronized void addListener(ConnectionListener listener) {
		if (listener == null) {
			return;
		}
		listenersToAdd.add(listener);
	}

	@Override
	public synchronized void removeListener(ConnectionListener listener) {
		if (listener == null) {
			return;
		}
		listenersToRemove.add(listener);
	}

	@Override
	public String toString() {
		return socket.getLocalAddress().toString();
	}
}
