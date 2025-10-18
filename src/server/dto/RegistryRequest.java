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
		this.u.setPassword(password);
	}

	public String getEmail() {
		return getU().getEmail();
	}

	public void setEmail(String email) {
		this.setEmail(email);
	}

	public String getName() {
		return getU().getName();
	}

	public void setName(String name) {
		this.setName(name);
	}

    public String getUsername() {
        return this.getU().getUsername();
    }

    public String getPassword(){
        return this.getU().getPassword();
    }
}
