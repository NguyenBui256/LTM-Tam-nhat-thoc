package client.network;



import dto.Message;

public interface MessageListener {
    void onMessageReceived(Message msg);
}
