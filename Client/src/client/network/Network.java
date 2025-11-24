package client.network;

import client.controller.InviteNotificationManager;
import dto.InviteRequest;
import dto.Message;
import dto.Status;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Network {
    private Socket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private List<MessageListener> listeners = new ArrayList<>();
    private boolean running = true;

    // Lưu username hiện tại trên client để truyền giữa các màn hình
    private String currentUser;

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

    public void setCurrentUser(String username) {
        this.currentUser = username;
    }

    public String getCurrentUser() {
        return currentUser;
    }

    private void startListening() {
        Thread thread = new Thread(() -> {
            try {
                while (running) {
                    Message msg = (Message) ois.readObject();

                    // Kích hoạt thông báo toàn cục cho INVITE / ACCEPT_NOTIFY / REJECT_NOTIFY
                    try {
                        String cmd = msg.getCommand();
                        if ("INVITE".equals(cmd)) {
                            Object content = msg.getContent();
                            String inviter = null;
                            if (content instanceof InviteRequest req)
                                inviter = req.getInviter();
                            else if (content instanceof String s)
                                inviter = s;
                            else if (content instanceof Status st) {
                                String c = st.getContent();
                                if (c != null && !c.isBlank())
                                    inviter = c.split("\\s+")[0];
                            }
                            if (inviter != null && !inviter.isBlank()) {
                                try {
                                    InviteNotificationManager.getInstance()
                                            .showInvite(new InviteRequest(inviter, getCurrentUser()));
                                } catch (Exception ignore) {
                                }
                            }
                        } else if ("ACCEPT_NOTIFY".equals(cmd) || "REJECT_NOTIFY".equals(cmd)) {
                            Object content = msg.getContent();
                            String text = content instanceof String ? (String) content
                                    : (content instanceof Status ? ((Status) content).getContent() : null);
                            if (text != null && !text.isBlank()) {
                                try {
                                    InviteNotificationManager.getInstance().showSimpleNotification(text);
                                } catch (Exception ignore) {
                                }
                            }
                        }
                    } catch (Throwable t) {
                        // Ignore notification errors
                    }

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
        if ("END_GAME".equals(msg.getCommand())) {
            System.out.println("[Network] Notifying " + listeners.size() + " listeners about END_GAME");
            for (int i = 0; i < listeners.size(); i++) {
                MessageListener listener = listeners.get(i);
                System.out.println(
                        "[Network] Notifying listener #" + (i + 1) + ": " + listener.getClass().getSimpleName());
                listener.onMessageReceived(msg);
            }
        } else {
            for (MessageListener listener : listeners) {
                listener.onMessageReceived(msg);
            }
        }
    }

    public void close() throws IOException {
        running = false;
        socket.close();
    }
}
