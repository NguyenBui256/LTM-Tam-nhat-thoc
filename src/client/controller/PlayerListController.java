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
import server.dto.InviteRequest;
import server.dto.Message;
import server.dto.PlayerRank;
import client.controller.WaitingRoomController;
import client.controller.InviteNotificationManager;

import java.io.IOException;
import java.util.Optional;
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
            network.addMessageListener(this);
            requestOnlinePlayers();
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
            Message msg = new Message("INVITE", "CLIENT", new InviteRequest(currentUser, player.getName()));
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

        switch (msg.getCommand()) {
            case "ONLINE_PLAYERS_RESPONSE" -> handlePlayerList(msg);
            case "PLAYER_STATUS_CHANGE" -> handleStatusChange(msg);
            case "ACCEPT_NOTIFY" -> handleAcceptNotify(msg);
            case "REJECT_NOTIFY" -> handleRejectNotify(msg);
        }
    }

    private void handleAcceptNotify(Message msg) {
        String text = msg.getContent() instanceof String ? (String) msg.getContent()
                : "Đối phương đã chấp nhận lời mời.";
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/waiting_room.fxml"));
                Parent root = loader.load();
                WaitingRoomController controller = loader.getController();
                if (this.network != null)
                    controller.setNetwork(this.network);
                Stage stage = (Stage) backButton.getScene().getWindow();
                InviteNotificationManager.getInstance().setPrimaryStage(stage);
                stage.setScene(new Scene(root));
                stage.setTitle("Waiting Room");
                stage.show();
            } catch (Exception e) {
                InviteNotificationManager.getInstance().showSimpleNotification(text);
            }
        });
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
            List<PlayerRankView> uiPlayers = players.stream()
                    .map(p -> new PlayerRankView(p.getName(), p.getStatus(), p.getElo(), p.getWins()))
                    .collect(Collectors.toList());

            Platform.runLater(() -> {
                allPlayers.setAll(uiPlayers);
                setupPagination();
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
                    .ifPresent(p -> p.setStatus(newStatus));
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
        pagination.currentPageIndexProperty().addListener(
                (obs, oldIdx, newIdx) -> updateTable(newIdx.intValue()));
        updateTable(0);
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
            if (network != null)
                network.removeMessageListener(this);

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
            network.send(new Message("GET_ONLINE_PLAYERS", "CLIENT", null));
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
}
