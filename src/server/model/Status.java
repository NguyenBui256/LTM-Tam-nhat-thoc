package server.model;

public class Status {
	private String msg;
	private String content;
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public Status(String msg, String content) {
		super();
		this.msg = msg;
		this.content = content;
	}
}
