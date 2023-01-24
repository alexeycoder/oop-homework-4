package edu.alexey.oopchat.server;

import java.io.IOException;
import java.net.ServerSocket;

import edu.alexey.oopchat.network.Connections;
import edu.alexey.oopchat.server.listeners.AnonymousConnectionsListener;
import edu.alexey.oopchat.server.listeners.AuthenticatedConnectionsListener;

public class ChatServer {

	// private final AuthenticatedConnectionsListener
	// authenticatedConnectionsListener;
	// private final AnonymousConnectionsListener anonymousConnectionsListener;
	// private boolean isClosed;

	ChatServer() {
		System.out.println("Сервер чата: старт.");
		// this.authenticatedConnectionsListener = new
		// AuthenticatedConnectionsListener();
		// this.anonymousConnectionsListener = new AnonymousConnectionsListener(
		// this.authenticatedConnectionsListener::register);

		try (ServerSocket serverSocket = new ServerSocket(Settings.PORT);
				var authenticatedConnectionsListener = new AuthenticatedConnectionsListener();
				var anonymousConnectionsListener = new AnonymousConnectionsListener(
						authenticatedConnectionsListener::register)) {
			Runtime.getRuntime().addShutdownHook(new Thread() {
				public void run() {
					System.out.println("Сервер чата: завершён.");
				};
			});

			while (true) {
				try {
					System.out.println("Сервер чата: в ожидании соединения...");
					var socket = serverSocket.accept();
					System.out.println("Сервер чата: входящее соединение...");
					Connections.create(anonymousConnectionsListener, socket);
					System.out.println("Сервер чата: входящее соединение установлено.");

				} catch (IOException ex) {
					System.err.println("Сервер чата: Произошла ошибка при установлении соединения.");
					System.err.println(ex.getMessage());
					ex.printStackTrace();
				}
			}
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}
}
