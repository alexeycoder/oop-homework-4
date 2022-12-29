package edu.alexey.network;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;

/**
 * Класс Соединения, которое работает со строковыми (текстовыми) данными.
 */
public class Connection {
	public static final Charset CHARSET = Charset.forName("UTF-8");
	public static final String MESSAGE_END = "\r\n";

	/** Слушатель событий, предоставляемый создателем соединения. */
	private final ConnectionListener eventListener;

	/** Сокет соединения. */
	private final Socket socket;

	/**
	 * Поток исполнения -- слушатель входящих сообщений.
	 * (сокр.: rx - receiver; tx - transceiver)
	 */
	private final Thread rxThread;

	/** Поток ввода, работающий с текстовыми данными. */
	private final BufferedReader in;

	/** Поток вывода, работающий с текстовыми данными. */
	private final BufferedWriter out;

	private boolean isClosed;

	public Connection(ConnectionListener eventListener, String host, int port)
			throws UnknownHostException, IOException {
		this(eventListener, new Socket(host, port));
	}

	public Connection(ConnectionListener eventListener, Socket socket) throws IOException {
		this.eventListener = eventListener;
		this.socket = socket;

		var inputReader = new InputStreamReader(socket.getInputStream(), CHARSET);
		this.in = new BufferedReader(inputReader);

		var outputWriter = new OutputStreamWriter(socket.getOutputStream(), CHARSET);
		this.out = new BufferedWriter(outputWriter);

		this.rxThread = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					eventListener.onConnectionReady(Connection.this);
					while (!rxThread.isInterrupted()) {

						// StringBuilder sb = new StringBuilder();
						// int breakCharInt = '\n';
						// while (true) {
						// int ci = in.read();
						// System.out.println(ci + " ");
						// if (ci == breakCharInt) {
						// break;
						// } else if (ci < 0) {
						// continue;// must be exit
						// } else {
						// sb.append(Character.toChars(ci));
						// }
						// }
						// var msg = sb.toString();

						var msg = in.readLine();
						if (msg == null) {
							disconnect();
							return;
						}

						System.out.println("Соединение: получена строка:");
						System.out.println(msg);

						eventListener.onReceiveMessage(Connection.this, msg);
					}
				} catch (IOException ex) {
					eventListener.onException(Connection.this, ex);
				} finally {
					isClosed = true;
					eventListener.onDisconnect(Connection.this);
				}
			}
		});
		this.rxThread.start();
		this.isClosed = false;
	}

	public synchronized void sendMessage(String message) {
		try {
			out.write(message + MESSAGE_END);
			out.flush();
		} catch (IOException ex) {
			eventListener.onException(Connection.this, ex);
			disconnect();
		}
	}

	public synchronized void disconnect() {
		if (isClosed) {
			return;
		}

		rxThread.interrupt();

		try {
			socket.close();
		} catch (IOException ex) {
			eventListener.onException(Connection.this, ex);
		}
	}

	@Override
	public String toString() {
		return Connection.class.getSimpleName() + ": " + socket.getInetAddress() + ":" + socket.getPort();
	}
}
