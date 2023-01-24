package edu.alexey.oopchat.network;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

public class Connections {
	public static Connection create(List<ConnectionListener> listeners, Socket socket) throws IOException {
		return new TcpConnection(listeners, socket);
	}

	public static Connection create(ConnectionListener listener, Socket socket) throws IOException {
		return new TcpConnection(List.of(listener), socket);
	}

	public static Connection create(ConnectionListener listener, String host, int port)
			throws UnknownHostException, IOException {
		return new TcpConnection(List.of(listener), new Socket(host, port));
	}
}
