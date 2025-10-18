package server.dto;

import java.io.Serializable;


import server.model.User;

public class RegistryRequest implements Serializable {
	private static final long serialVersionUID = 102L;
	private User u;
	public User getU() {
		return u;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
