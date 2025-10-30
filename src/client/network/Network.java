package client.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import server.dto.Message;

public class Network {
    private Socket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private List<MessageListener> listeners = new ArrayList<>();
    private boolean running = true;

    public Network(String host, int port) throws IOException {
        this.socket = new Socket(host, port);
        oos = new ObjectOutputStream(socket.getOutputStream());
        ois = new ObjectInputStream(socket.getInputStream());
        startListening();
    }

    public void send(Message msg) throws IOException {
        oos.writeObject(msg);
        oos.flush();
    }

    public void addMessageListener(MessageListener listener) {
        listeners.add(listener);
    }

    public void removeMessageListener(MessageListener listener) {
        listeners.remove(listener);
    }

    private void startListening() {
        Thread thread = new Thread(() -> {
            try {
                while (running) {
                    Message msg = (Message) ois.readObject();
                    notifyListeners(msg);
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private void notifyListeners(Message msg) {
        for (MessageListener listener : listeners) {
            listener.onMessageReceived(msg);
        }
    }

    public void close() throws IOException {
        running = false;
        socket.close();
    }
}
