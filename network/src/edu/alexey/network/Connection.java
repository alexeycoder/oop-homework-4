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
	public static final String MESSAGE_END = System.lineSeparator(); // "\r\n";
	public static final String AUTH_MESSAGE_PREFIX = "\0AUTH:\0";
	public static final String NEWLINE_PLACEHOLDER = "\0:NL:\0";

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

		this.rxThread = new Thread(this::listening);
		this.rxThread.start();
		this.isClosed = false;
	}

	private void listening() {
		try {
			eventListener.onConnectionReady(Connection.this);
			String msg = in.readLine();
			if (msg != null && msg.startsWith(AUTH_MESSAGE_PREFIX)) {
				eventListener.onAuthenticate(this, msg.substring(AUTH_MESSAGE_PREFIX.length()));
				msg = null;
			}

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

				if (msg == null) {
					msg = in.readLine();
					if (msg == null) {
						disconnect();
						return;
					}
				}
				msg = decode(msg);

				System.out.println("Соединение: получена строка:");
				System.out.println(msg);

				eventListener.onReceiveMessage(this, msg);
				msg = null;
			}
		} catch (IOException ex) {
			eventListener.onException(this, ex);
		} finally {
			isClosed = true;
			eventListener.onDisconnect(this);
		}
	}

	public synchronized void authenticate(String yourId) {
		sendMessage(AUTH_MESSAGE_PREFIX + yourId);
	}

	public synchronized void sendMessage(String message) {
		try {
			message = encode(message);
			out.write(message + MESSAGE_END);
			out.flush();
		} catch (IOException ex) {
			eventListener.onException(Connection.this, ex);
			disconnect();
		}
	}

	private String encode(String message) {
		return message.replace(MESSAGE_END, NEWLINE_PLACEHOLDER);
	}

	private String decode(String message) {
		return message.replace(NEWLINE_PLACEHOLDER, MESSAGE_END);
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
