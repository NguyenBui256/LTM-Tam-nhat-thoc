package client.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import server.dto.Message;

public class Network {
    private Socket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    public Network(String host, int port) throws IOException{
        this.socket = new Socket(host, port);
        oos = new ObjectOutputStream(socket.getOutputStream());
        ois = new ObjectInputStream(socket.getInputStream());
    }
    public void send(Message msg) throws IOException {
        oos.writeObject(msg);
        oos.flush();
    }
    public Message receive() throws IOException, ClassNotFoundException {
        return (Message) ois.readObject();
    }
    public void close() throws IOException {
        socket.close();
    }

}
