package server.model;

import java.io.Serializable;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    private String sender; // SERVER - CLIENT
    private Object content;
    private String command;

    public Message(String command, String sender, Object content) {
        this.command = command;
        this.sender = sender;
        this.content = content;
    }
    
    public Message(String sender, Object content) {
		super();
		this.sender = sender;
		this.content = content;
	}

	public String getSender() { return sender; }
    public Object getContent() { return content; }
    public String getCommand() { return command; }
}
