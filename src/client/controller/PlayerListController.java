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
import server.dto.Message;
import server.dto.PlayerRank;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PlayerListController implements MessageListener {

    @FXML private TableView<PlayerRankView> playerTable;
    @FXML private TableColumn<PlayerRankView, String> nameColumn;
    @FXML private TableColumn<PlayerRankView, Integer> eloColumn;
    @FXML private TableColumn<PlayerRankView, Integer> winsColumn;
    @FXML private TableColumn<PlayerRankView, String> statusColumn;
    @FXML private TableColumn<PlayerRankView, Void> actionColumn;
    @FXML private Pagination pagination;
    @FXML private Button backButton;

    private final int rowsPerPage = 8;
    private final ObservableList<PlayerRankView> allPlayers = FXCollections.observableArrayList();
    private Network network;

    // --- Khởi tạo ---
    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        eloColumn.setCellValueFactory(new PropertyValueFactory<>("elo"));
        winsColumn.setCellValueFactory(new PropertyValueFactory<>("wins"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        setupCustomCells();
        setupPagination();
        backButton.setOnAction(e -> onBackClicked());
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
                                ? "status-label-available" : "status-label-busy"
                );
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

                inviteBtn.setDisable(!canInvite);
                inviteBtn.getStyleClass().setAll(canInvite
                        ? "action-button-invite" : "action-button-disabled");
                setGraphic(inviteBtn);
                setStyle("-fx-alignment: CENTER;");
            }
        });
    }

    // --- Gửi lời mời ---
    private void sendInvite(PlayerRankView player) {
        try {
            Message msg = new Message("INVITE_REQUEST", "CLIENT", player.getName());
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
        if (msg == null) return;

        switch (msg.getCommand()) {
            case "ONLINE_PLAYERS_RESPONSE" -> handlePlayerList(msg);
            case "PLAYER_STATUS_CHANGE" -> handleStatusChange(msg);
        }
    }

    private void handlePlayerList(Message msg) {
        Object content = msg.getContent();
        if (!(content instanceof List<?> list)) return;

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
        if (!(msg.getContent() instanceof Map<?, ?> data)) return;

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

    // --- Phân trang ---
    private void setupPagination() {
        int pageCount = Math.max(1, (int) Math.ceil((double) allPlayers.size() / rowsPerPage));
        pagination.setPageCount(pageCount);
        pagination.currentPageIndexProperty().addListener(
                (obs, oldIdx, newIdx) -> updateTable(newIdx.intValue())
        );
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
            if (network != null) network.removeMessageListener(this);

            Stage stage = (Stage) backButton.getScene().getWindow();
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
