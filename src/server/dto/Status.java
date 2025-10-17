package server.dto;

import server.common.StatusType;

import java.io.Serializable;

public class Status implements Serializable {
	private static final long serialVersionUID = 1L;
	private StatusType type;
	private String content;

	public void setMsg(StatusType type) {
		this.type = type;
	}

	public StatusType getType() {
		return type;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Status(StatusType type, String content) {
		super();
		this.type = type;
		this.content = content;
	}
}
