package edu.alexey.oopchat.network;

import java.io.Serializable;

public class Message implements Serializable {

	private final MessageType type;
	private final String from;
	private final String data;

	public Message(MessageType type, String from, String data) {
		this.type = type;
		this.from = from;
		this.data = data;
	}

	public MessageType type() {
		return type;
	}

	public String from() {
		return from;
	}

	public String data() {
		return data;
	}

	@Override
	public String toString() {
		return String.format("%s:%s:%s", from, type, data);
	}
}