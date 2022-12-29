package edu.alexey.network;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.Charset;

/**
 * Класс TCP соединения, которое работает со строковыми (текстовыми) данными.
 */
public class TcpConnection {
	public static final Charset CHARSET = Charset.forName("UTF-8");

	/** Сокет TCP соединения */
	private final Socket socket;

	/**
	 * Поток исполнения -- слушатель входящих сообщений
	 * (rx - receiver; tx - transceiver)
	 */
	private final Thread rxThread;

	/** Поток ввода, работающий с текстовыми данными */
	private final BufferedReader in;

	/** Поток вывода, работающий с текстовыми данными */
	private final BufferedWriter out;

	public TcpConnection(Socket socket) throws IOException {
		this.socket = socket;

		var inReader = new InputStreamReader(socket.getInputStream(), CHARSET);
		this.in = new BufferedReader(inReader);

		var outWriter = new OutputStreamWriter(socket.getOutputStream(), CHARSET);
		this.out = new BufferedWriter(outWriter);

		Runnable listening = () -> {
			try {
				String msg = in.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {

			}
		};

		this.rxThread = new Thread(listening);
		this.rxThread.start();
	}

}
