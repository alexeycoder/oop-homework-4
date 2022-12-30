package edu.alexey.oopchat.client;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import edu.alexey.network.Connection;
import edu.alexey.network.ConnectionListener;

public class ClientWindow extends JFrame implements ActionListener, ConnectionListener {
	private final String nickname;
	private final JTextArea messagesLog = new JTextArea(30, 1);
	private final JTextArea filedInput = new JTextArea(5, 1);

	private final Connection connection;

	ClientWindow() {
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setSize(Settings.WIDTH, Settings.HEIGHT);
		setLocationRelativeTo(null);
		setAlwaysOnTop(true);

		nickname = askNickname();

		var pane = getContentPane();
		setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		JLabel title = new JLabel(Settings.CHAT_TITLE);
		var font = title.getFont();
		title.setFont(font.deriveFont(font.getSize() * 1.5f));
		title.setAlignmentX(Component.CENTER_ALIGNMENT);
		title.setBorder(BorderFactory.createLineBorder(Color.orange, Settings.BORDER_WIDTH));
		JLabel labelNickname = new JLabel("\uD83D\uDE0C Пользователь: " + this.nickname);
		labelNickname.setAlignmentX(Component.CENTER_ALIGNMENT);
		JButton sendButton = new JButton("Отправить");
		sendButton.setAlignmentX(Component.CENTER_ALIGNMENT);

		messagesLog.setEditable(false);
		messagesLog.setLineWrap(true);
		messagesLog.setBackground(Color.lightGray);
		JScrollPane scrollPane = new JScrollPane(messagesLog,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		messagesLog.setAlignmentX(Component.CENTER_ALIGNMENT);
		filedInput.setLineWrap(true);
		filedInput.setBackground(Color.white);
		filedInput.setBorder(BorderFactory.createLineBorder(Color.black, 2));
		filedInput.setAlignmentX(Component.CENTER_ALIGNMENT);
		JScrollPane inputScrollPane = new JScrollPane(filedInput,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		pane.add(title);
		pane.add(labelNickname);
		pane.add(scrollPane);
		pane.add(inputScrollPane);
		pane.add(sendButton);

		sendButton.addActionListener(this);

		// pack();
		setVisible(true);

		Connection con;
		try {
			con = new Connection(this, Settings.CHAT_SERVER_IP, Settings.CHAT_SERVER_PORT);
			con.authenticate(nickname);
		} catch (IOException ex) {
			con = null;
			printMessage("\t\u26A0 Произошла ошибка во время подключения: " + ex.getMessage());
		}
		this.connection = con;
	}

	private String askNickname() {
		boolean wrong = false;
		do {
			String prompt = "Введите ваш никнейм:";
			if (wrong) {
				prompt = ("Некорректный ввод.\nДопускаются буквы, цифры,"
						+ " пробел и знак подчёркивания."
						+ "\nПожалуйста попробуйте ещё раз.\n" + prompt);
			}
			String nickname = JOptionPane.showInputDialog(this, prompt, "");
			wrong = nickname == null || nickname.isBlank() || !nickname.matches("^\\w+(?:\\w|\\s|_)+$");
			if (!wrong) {
				return nickname;
			}
		} while (true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (connection == null) {
			return;
		}

		String msg = filedInput.getText();
		if (msg == null || msg.isBlank()) {
			return;
		}
		filedInput.setText(null);
		connection.sendMessage(
				String.format(msg));
	}

	@Override
	public void onConnectionReady(Connection connection) {
		printMessage("\t\uD83C\uDD97 Соединение установлено.");
	}

	@Override
	public void onReceiveMessage(Connection connection, String message) {
		printMessage(message);
	}

	@Override
	public void onDisconnect(Connection connection) {
		printMessage("\t\uD83D\uDCF4 Оффлайн.");
	}

	@Override
	public void onException(Connection connection, Exception ex) {
		printMessage("\t\u26A0 Ошибка соединения: " + ex.getMessage());
	}

	private synchronized void printMessage(String message) {
		SwingUtilities.invokeLater(() -> {
			messagesLog.append(message + System.lineSeparator().repeat(2));
			messagesLog.setCaretPosition(messagesLog.getDocument().getLength());
		});
	}

	@Override
	public void onAuthenticate(Connection connection, String subscriberId) {
		// do nothing
	}
}
