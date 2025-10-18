package server.dto;

import java.io.Serializable;

import server.model.User;

public class RegistryRequest implements Serializable {
	private static final long serialVersionUID = 102L;
	private User u;
	public User getU() {
		return u;
	}
	public void setU(User u) {
		this.u = u;
	}
	public RegistryRequest(User u) {
		super();
		this.u = u;
	}
}
