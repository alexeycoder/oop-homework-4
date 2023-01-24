package edu.alexey.oopchat.server.entities;

public record User(String name) {
	public User(String name) {
		assert name != null && !name.isBlank();
		this.name = name;
	}
}

// public record User(String name, String token) {
// public User(String name, String token) {
// assert name != null && !name.isBlank();
// assert token != null && !token.isBlank();
// this.name = name;
// this.token = token;
// }
// }
