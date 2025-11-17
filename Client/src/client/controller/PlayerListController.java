package client.controller;

import client.model.PlayerRankView;
import client.network.MessageListener;
import client.network.Network;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import dto.InviteRequest;
import dto.Message;
import dto.PlayerRank;
import dto.GameRoom;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PlayerListController implements MessageListener {

    @FXML
    private TableView<PlayerRankView> playerTable;
    @FXML
    private TableColumn<PlayerRankView, String> nameColumn;
    @FXML
    private TableColumn<PlayerRankView, Integer> eloColumn;
    @FXML
    private TableColumn<PlayerRankView, Integer> winsColumn;
    @FXML
    private TableColumn<PlayerRankView, String> statusColumn;
    @FXML
    private TableColumn<PlayerRankView, Void> actionColumn;
    @FXML
    private Pagination pagination;
    @FXML
    private Button backButton;

    private final int rowsPerPage = 8;
    private final ObservableList<PlayerRankView> allPlayers = FXCollections.observableArrayList();
    private Network network;
    // Tên người chơi hiện tại (được truyền từ LoginController ->
    // MainLobbyController -> PlayerListController)
    private String currentUser;

    public void setCurrentUser(String currentUser) {
        this.currentUser = currentUser;
        System.out.println("[PlayerListController] currentUser set to: " + currentUser);
        if (playerTable != null) playerTable.refresh();
    }

    // --- Khởi tạo ---
    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        // show current user with a marker
        nameColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String name, boolean empty) {
                super.updateItem(name, empty);
                if (empty || name == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    if (currentUser != null && currentUser.equals(name))
                        setText(name + " (Bạn)");
                    else
                        setText(name);
                }
            }
        });
        eloColumn.setCellValueFactory(new PropertyValueFactory<>("elo"));
        winsColumn.setCellValueFactory(new PropertyValueFactory<>("wins"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        setupCustomCells();
        setupPagination();
        // register pagination listener once
        pagination.currentPageIndexProperty().addListener(
                (obs, oldIdx, newIdx) -> updateTable(newIdx.intValue()));
        backButton.setOnAction(e -> onBackClicked());

        // Highlight the current user's row with a different background
        playerTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(PlayerRankView item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                } else if (currentUser != null && currentUser.equals(item.getName())) {
                    // light yellow background for self
                    setStyle("-fx-background-color: #fff9c4;");
                } else {
                    setStyle("");
                }
            }
        });
    }

    // --- Thiết lập network ---
    public void setNetwork(Network network) {
        this.network = network;
        if (network != null) {
            System.out.println("[PlayerListController] setNetwork called. currentUser=" + this.currentUser);
            network.addMessageListener(this);
            System.out.println("[PlayerListController] registered as MessageListener");
            requestOnlinePlayers();
            System.out.println("[PlayerListController] requested online players");
        } else {
            System.err.println("[PlayerListController] Network is null — skipping online request.");
        }
    }

    // --- Cell tuỳ chỉnh cho cột trạng thái và hành động ---
    private void setupCustomCells() {
        // Cột trạng thái
        statusColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }
                Label label = new Label(status);
                label.getStyleClass().add(
                        status.equalsIgnoreCase("ONLINE") || status.equalsIgnoreCase("WAITING")
                                ? "status-label-available"
                                : "status-label-busy");
                setGraphic(label);
                setStyle("-fx-alignment: CENTER;");
            }
        });

        // Cột hành động
        actionColumn.setCellFactory(col -> new TableCell<>() {
            private final Button inviteBtn = new Button("Mời đấu");

            {
                inviteBtn.setOnAction(e -> {
                    PlayerRankView p = getTableView().getItems().get(getIndex());
                    sendInvite(p);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }

                PlayerRankView p = getTableView().getItems().get(getIndex());
                boolean canInvite = p.getStatus().equalsIgnoreCase("ONLINE")
                        || p.getStatus().equalsIgnoreCase("WAITING");
                // Disable invite button for self
                boolean isSelf = currentUser != null && currentUser.equals(p.getName());
                boolean enabled = canInvite && !isSelf;
                inviteBtn.setDisable(!enabled);
                inviteBtn.getStyleClass().setAll(enabled
                        ? "action-button-invite"
                        : "action-button-disabled");
                setGraphic(inviteBtn);
                setStyle("-fx-alignment: CENTER;");
            }
        });
    }

    // --- Gửi lời mời ---
    private void sendInvite(PlayerRankView player) {
        try {
            Message msg = new Message("INVITE", currentUser, new InviteRequest(currentUser, player.getName()));
            System.out.println("[PlayerListController] Sending INVITE from " + currentUser + " to " + player.getName());
            network.send(msg);
            showAlert(Alert.AlertType.INFORMATION, "Mời đấu",
                    "Đã gửi lời mời đến " + player.getName());
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi",
                    "Không thể gửi lời mời: " + e.getMessage());
        }
    }

    // --- Xử lý thông điệp từ server ---
    @Override
    public void onMessageReceived(Message msg) {
        if (msg == null)
            return;

        System.out.println("[PlayerListController] onMessageReceived -> command=" + msg.getCommand()
                + " | content=" + String.valueOf(msg.getContent()));

        switch (msg.getCommand()) {
            case "ONLINE_PLAYERS_RESPONSE" -> handlePlayerList(msg);
            case "PLAYER_STATUS_CHANGE" -> handleStatusChange(msg);
            case "ACCEPT_NOTIFY" -> handleAcceptNotify(msg);
            case "GAME_ROOM_CREATED" -> handleGameRoomCreated(msg);
            case "REJECT_NOTIFY" -> handleRejectNotify(msg);
        }
    }

    // ✅ Xử lý GAME_ROOM_CREATED từ server cho người gửi lời mời (inviter)
    private void handleGameRoomCreated(Message msg) {
        Object content = msg.getContent();
        if (!(content instanceof GameRoom gameRoom)) {
            return;
        }
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/GameScene.fxml"));
                Parent root = loader.load();
                GameController controller = loader.getController();
                if (this.network != null)
                    controller.setNetwork(this.network);

                // ✅ KHÔNG assume - dùng GameRoom từ server để xác định ai là ai
                String player1 = gameRoom.getPlayer1();
                String player2 = gameRoom.getPlayer2();

                // Xác định opponent dựa trên currentUser và GameRoom
                String opponent;
                if (this.currentUser != null && this.currentUser.equals(player1)) {
                    opponent = player2;
                } else if (this.currentUser != null && this.currentUser.equals(player2)) {
                    opponent = player1;
                } else {
                    System.err.println("[PlayerListController] currentUser=" + this.currentUser
                            + " không khớp với player nào trong GameRoom (player1=" + player1 + ", player2=" + player2 + ")");
                    InviteNotificationManager.getInstance().showSimpleNotification("Lỗi: Không tìm thấy thông tin người chơi");
                    return;
                }

                controller.setPlayers(this.currentUser, opponent);
                controller.setCurrentPlayerName(this.currentUser);
                System.out.println("[PlayerListController] Game started: currentUser=" + this.currentUser
                        + ", opponent=" + opponent + " (from GameRoom: player1=" + player1 + ", player2=" + player2 + ")");

                Stage stage = (Stage) backButton.getScene().getWindow();
                InviteNotificationManager.getInstance().setPrimaryStage(stage);
                InviteNotificationManager.getInstance().setCurrentUsername(this.currentUser);
                stage.setScene(new Scene(root));
                stage.setTitle("Game");
                stage.show();
            } catch (IOException e) {
                System.err.println("[PlayerListController] Lỗi load GameScene.fxml: " + e.getMessage());
                InviteNotificationManager.getInstance().showSimpleNotification("Lỗi: Không thể tải giao diện game");
            }
        });
    }

    private void handleAcceptNotify(Message msg) {
        String text = msg.getContent() instanceof String ? (String) msg.getContent()
                : "Đối phương đã chấp nhận lời mời.";
//        Platform.runLater(() -> {
//            try {
//                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/waiting_room.fxml"));
//                Parent root = loader.load();
//                WaitingRoomController controller = loader.getController();
//                if (this.network != null)
//                    controller.setNetwork(this.network);
//                Stage stage = (Stage) backButton.getScene().getWindow();
//                InviteNotificationManager.getInstance().setPrimaryStage(stage);
//                stage.setScene(new Scene(root));
//                stage.setTitle("Waiting Room");
//                stage.show();
//            } catch (Exception e) {
//                InviteNotificationManager.getInstance().showSimpleNotification(text);
//            }
//        });
    }

    private void handleRejectNotify(Message msg) {
        String text = msg.getContent() instanceof String ? (String) msg.getContent() : "Đối phương đã từ chối lời mời.";
        Platform.runLater(() -> InviteNotificationManager.getInstance().showSimpleNotification(text));
    }

    private void handlePlayerList(Message msg) {
        Object content = msg.getContent();
        if (!(content instanceof List<?> list))
            return;

        try {
            @SuppressWarnings("unchecked")
            List<PlayerRank> players = (List<PlayerRank>) list;
            System.out.println("[PlayerListController] handlePlayerList received " + players.size() + " players");
            players.forEach(p -> System.out.println("[PlayerListController]  - " + p.getName() + " : " + p.getStatus()));
            List<PlayerRankView> uiPlayers = players.stream()
                    .map(p -> new PlayerRankView(p.getName(), p.getStatus(), p.getElo(), p.getWins()))
                    .collect(Collectors.toList());

            Platform.runLater(() -> {
                allPlayers.setAll(uiPlayers);
                sortPlayers();
                setupPagination();
                if (playerTable != null) playerTable.refresh();
                System.out.println("[PlayerListController] UI player list updated. total=" + allPlayers.size());
            });
        } catch (ClassCastException e) {
            System.err.println("Cannot cast to List<PlayerRank>: " + e.getMessage());
        }
    }

    private void handleStatusChange(Message msg) {
        if (!(msg.getContent() instanceof Map<?, ?> data))
            return;

        String name = (String) data.get("name");
        String newStatus = (String) data.get("status");
        System.out.println("[PlayerListController]: change status for " + name + " " + newStatus);
        Platform.runLater(() -> {
            allPlayers.stream()
                    .filter(p -> p.getName().equals(name))
                    .findFirst()
                    .ifPresent(p -> {
                        String old = p.getStatus();
                        p.setStatus(newStatus);
                        System.out.println("[PlayerListController] status updated for " + name + " : " + old + " -> " + newStatus);
                    });
            // sắp xếp và cập nhật UI hoàn toàn trong FX thread
            sortPlayers();
            updateTable(pagination.getCurrentPageIndex()); // refresh
            if (playerTable != null) playerTable.refresh();
        });
    }

    // --- Xử lý lời mời ---
    private void handleInvite(Message msg) {
        // Đã chuyển toàn bộ logic UI popup mời đấu sang InviteNotificationManager,
        // không xử lý ở đây nữa
    }

    // --- Phân trang ---
    private void setupPagination() {
        int pageCount = Math.max(1, (int) Math.ceil((double) allPlayers.size() / rowsPerPage));
        pagination.setPageCount(pageCount);
        updateTable(Math.min(pagination.getCurrentPageIndex(), Math.max(0, pageCount - 1)));
    }

    private void updateTable(int pageIndex) {
        int from = pageIndex * rowsPerPage;
        int to = Math.min(from + rowsPerPage, allPlayers.size());
        playerTable.setItems(FXCollections.observableArrayList(allPlayers.subList(from, to)));
    }

    // --- Quay lại ---
    @FXML
    private void onBackClicked() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main_lobby.fxml"));
            Parent root = loader.load();

            MainLobbyController controller = loader.getController();
            controller.setNetwork(network);
            // Truyền currentUser trở lại MainLobby để giữ trạng thái người dùng
            if (network != null && network.getCurrentUser() != null) {
                controller.setCurrentUser(network.getCurrentUser());
            }
            // Truyền Stage để InviteNotificationManager có thể hiển thị popup
            Stage stage = (Stage) backButton.getScene().getWindow();
            controller.setPrimaryStage(stage);
            if (network != null) {
                System.out.println("[PlayerListController] onBackClicked: removing message listener");
                network.removeMessageListener(this);
            }

            stage.setScene(new Scene(root));
            stage.setTitle("Main Lobby");
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi giao diện",
                    "Không thể tải màn hình chính: " + e.getMessage());
        }
    }

    // --- Tiện ích ---
    private void requestOnlinePlayers() {
        try {
            System.out.println("[PlayerListController] requestOnlinePlayers: sending GET_ONLINE_PLAYERS");
            network.send(new Message("GET_ONLINE_PLAYERS", currentUser, null));
        } catch (IOException e) {
            System.err.println("Error sending GET_ONLINE_PLAYERS: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(msg);
            alert.showAndWait();
        });
    }

    private void sortPlayers() {
        allPlayers.sort((a, b) -> {
            // Ưu tiên ONLINE > WAITING > các trạng thái khác
            int priorityA = statusPriority(a.getStatus());
            int priorityB = statusPriority(b.getStatus());

            if (priorityA != priorityB)
                return priorityA - priorityB;

            return a.getName().compareToIgnoreCase(b.getName()); // fallback sort alphabet
        });
    }

    private int statusPriority(String status) {
        if (status == null) return 99;

        return switch (status.toUpperCase()) {
            case "ONLINE" -> 0;
            case "WAITING" -> 1;
            default -> 2;  // BUSY, OFFLINE, IN_GAME...
        };
    }

}