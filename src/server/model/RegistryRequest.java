package server.model;

public class RegistryRequest {
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
