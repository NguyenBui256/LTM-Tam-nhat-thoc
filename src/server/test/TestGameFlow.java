package server.test;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Map;

import server.dto.GameStart;
import server.dto.GameUpdate;
import server.dto.Message;
import server.dto.LoginRequest;

/**
 * Simple test harness that connects two clients to the server and simulates a short game.
 * Run the server first (Server.main). Then run this class (it will connect to localhost:2207).
 */
public class TestGameFlow {

    static class TestClient {
        String name;
        Socket socket;
        ObjectOutputStream out;
        ObjectInputStream in;
        Thread reader;

        public TestClient(String name) throws Exception {
            this.name = name;
            socket = new Socket("localhost", 2207);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            startReader();
        }

        private void startReader() {
            reader = new Thread(() -> {
                try {
                    while (true) {
                        Object o = in.readObject();
                        if (o instanceof Message) {
                            Message m = (Message) o;
                            System.out.println("[" + name + "] RECV cmd=" + m.getCommand() + " sender=" + m.getSender() + " content=" + m.getContent());
                        } else {
                            System.out.println("[" + name + "] RECV unknown: " + o);
                        }
                    }
                } catch (Exception e) {
                    System.out.println("[" + name + "] reader stopped: " + e.getMessage());
                }
            });
            reader.setDaemon(true);
            reader.start();
        }

        public void send(Message m) throws Exception {
            out.writeObject(m);
            out.flush();
        }

        public void close() throws Exception {
            socket.close();
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Starting test clients...");
        TestClient alice = new TestClient("alice");
        TestClient bob = new TestClient("bob");

        // login both (server's LoginCommand accepts any username but will log an error message for non-admin)
        alice.send(new Message("LOGIN", "alice", new LoginRequest("alice", "pw")));
        bob.send(new Message("LOGIN", "bob", new LoginRequest("bob", "pw")));

        Thread.sleep(500);

        // alice invites bob
        alice.send(new Message("INVITE", "alice", "bob"));
        Thread.sleep(500);

        // bob accepts
        bob.send(new Message("INVITE", "bob", "ACCEPT:alice"));

        // wait for GAME_START
        Thread.sleep(1000);

        // Note: this test does not parse incoming Message content automatically; it's printed by the reader thread.
        // To simulate moves, we can craft some moves if we knew the gameId. For simplicity, we'll try to pick index 0..4.
        // Send a few moves from both players to exercise server move handling.
        for (int i = 0; i < 10; i++) {
            String moveA = "" + i + ":" + (i % 3); // invalid gameId but server will ignore; replace with real when received
            // We don't have the gameId here; in a more advanced test we'd capture GameStart content from reader.
            // To keep this harness simple, sleep and let manual testing observe GameStart/GameUpdate messages.
            Thread.sleep(200);
        }

        System.out.println("Test finished (note: this harness prints server messages; extend to programmatically respond to GameStart to play moves).");
    }
}
