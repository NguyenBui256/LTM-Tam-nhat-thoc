package client.network;



import server.dto.Message;

public interface MessageListener {
    void onMessageReceived(Message msg);
}
