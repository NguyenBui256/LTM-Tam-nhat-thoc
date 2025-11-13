package client.controller;

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
import javafx.scene.layout.Region;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import server.dto.Message;
import server.dto.PlayerStatus;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

public class LeaderboardController implements MessageListener {

    @FXML
    private TableView<PlayerStatus> leaderboardTable;
    @FXML
    private TableColumn<PlayerStatus, Integer> rankColumn;
    @FXML
    private TableColumn<PlayerStatus, String> nameColumn;
    @FXML
    private TableColumn<PlayerStatus, Integer> eloColumn;
    @FXML
    private TableColumn<PlayerStatus, Integer> winsColumn; // Có thể bỏ nếu không cần
    @FXML
    private Pagination pagination;
    @FXML
    private ToggleButton scoreTab;
    @FXML
    private ToggleButton winTab;
    @FXML
    private TextField searchField;
    @FXML
    private Button backButton;
    @FXML
    private ToggleGroup tabGroup;

    private ObservableList<PlayerStatus> allPlayers;
    private ObservableList<PlayerStatus> filteredPlayers;
    private static final int ROWS_PER_PAGE = 8;
    private Network network;
    // Tên người chơi hiện tại (nếu cần hiển thị/logic)
    private String currentUser;

    public void setCurrentUser(String currentUser) {
        this.currentUser = currentUser;
        System.out.println("[LeaderboardController] currentUser set to: " + currentUser);
        if (leaderboardTable != null) leaderboardTable.refresh();
    }

    public void setNetwork(Network network) {
        this.network = network;
        if (this.network != null) {
            this.network.addMessageListener(this);
            System.out.println("[LeaderboardController] Network set successfully and listener registered.");
            requestRanking();
        } else {
            System.err.println("[LeaderboardController] Error: Network is null, cannot set up connection.");
            Platform.runLater(() -> {
                showAlert("Lỗi Mạng", "Không thể thiết lập kết nối mạng.");
            });
        }
    }

    @FXML
    public void initialize() {
        // Gán cột với thuộc tính
        rankColumn.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleIntegerProperty(getIndex(cellData.getValue()) + 1)
                        .asObject());
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        // mark current user in the leaderboard
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
        // winsColumn now bound to PlayerStatus.wins
        winsColumn.setCellValueFactory(new PropertyValueFactory<>("wins"));

        // Tắt sắp xếp bằng click cột
        rankColumn.setSortable(false);
        nameColumn.setSortable(false);
        eloColumn.setSortable(false);
        winsColumn.setSortable(false);
        leaderboardTable.setSortPolicy(param -> null);

        // Khởi tạo danh sách
        allPlayers = FXCollections.observableArrayList();
        filteredPlayers = FXCollections.observableArrayList();
        leaderboardTable.setItems(filteredPlayers);

        // Cố định chiều cao bảng
        fixTableHeight();
        if (pagination != null)
            pagination.setVisible(false);

        // Cột tự chia đều
        leaderboardTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        // Lọc realtime
        searchField.textProperty().addListener((obs, oldVal, newVal) -> updateFiltered(newVal));

        // Tab sắp xếp
        scoreTab.setOnAction(e -> onScoreTabClicked());
        winTab.setOnAction(e -> onWinTabClicked());

        // Nút quay lại
        backButton.setOnAction(e -> onBackClicked());

        // Highlight the current user row with a different background
        leaderboardTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(PlayerStatus item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                } else if (currentUser != null && currentUser.equals(item.getUsername())) {
                    // light green background for self
                    setStyle("-fx-background-color: #e8f5e9;");
                } else {
                    setStyle("");
                }
            }
        });
    }

    private void requestRanking() {
        try {
            Message msg = new Message("GET_RANKING", "CLIENT", null);
            System.out.println("[LeaderboardController] Sending request for ranking");
            network.send(msg);
        } catch (IOException e) {
            System.err.println("[LeaderboardController] Error sending GET_RANKING request: " + e.getMessage());
            e.printStackTrace();
            Platform.runLater(() -> {
                showAlert("Lỗi Gửi Yêu Cầu", "Không thể gửi yêu cầu lấy bảng xếp hạng: " + e.getMessage());
            });
        }
    }

    @Override
    public void onMessageReceived(Message msg) {
        System.out.println(
                "[LeaderboardController] Received message: command=" + (msg != null ? msg.getCommand() : "null"));
        if (msg != null && "RANKING_RESPONSE".equals(msg.getCommand())) {
            List<PlayerStatus> ranking = (List<PlayerStatus>) msg.getContent();
            System.out.println("[LeaderboardController] RANKING_RESPONSE received: players="
                    + (ranking != null ? ranking.size() : "null"));
            if (ranking != null) {
                Platform.runLater(() -> {
                    allPlayers.setAll(ranking);
                    sortByElo();
                    System.out
                            .println("[LeaderboardController] Updated ranking with " + allPlayers.size() + " players");
                });
            } else {
                System.err.println("[LeaderboardController] Error: Ranking data is null");
                Platform.runLater(() -> {
                    showAlert("Lỗi Phản Hồi", "Danh sách xếp hạng từ server không hợp lệ.");
                });
            }
        } else {
            System.out.println(
                    "[LeaderboardController] Ignored message: command=" + (msg != null ? msg.getCommand() : "null"));
        }
    }

    private void fixTableHeight() {
        leaderboardTable.setFixedCellSize(35);
        // Let the table compute its preferred height and allow scrolling
        leaderboardTable.setPrefHeight(Region.USE_COMPUTED_SIZE);
        leaderboardTable.setMinHeight(0);
        leaderboardTable.setMaxHeight(Double.MAX_VALUE);
    }

    @FXML
    private void onScoreTabClicked() {
        sortByElo();
        scoreTab.setSelected(true);
        winTab.setSelected(false);
        System.out.println("[LeaderboardController] Sorted by Elo");
    }

    @FXML
    private void onWinTabClicked() {
        // sortByWins(); // Bỏ vì PlayerStatus không có trường wins
        winTab.setSelected(true);
        scoreTab.setSelected(false);
        System.out.println("[LeaderboardController] Win tab clicked (no sorting due to missing wins data)");
        Platform.runLater(() -> {
            showAlert("Thông Báo", "Sắp xếp theo số trận thắng chưa được hỗ trợ.");
        });
    }

    // Pagination removed — using scroll to show all rows

    private void updateFiltered(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            filteredPlayers.setAll(allPlayers);
        } else {
            filteredPlayers.setAll(allPlayers.filtered(
                    p -> p.getUsername().toLowerCase().contains(keyword.toLowerCase())));
        }
        // show all filtered players in the table (scrollable)
        leaderboardTable.setItems(filteredPlayers);
        leaderboardTable.refresh();
        System.out.println("[LeaderboardController] Filtered players: " + filteredPlayers.size());
    }

    private void sortByElo() {
        allPlayers.sort(Comparator.comparingInt(PlayerStatus::getElo).reversed());
        updateFiltered(searchField.getText());
        System.out.println("[LeaderboardController] Sorted by Elo");
    }

    private int getIndex(PlayerStatus player) {
        // Show ranking index relative to the currently displayed (filtered) list
        return filteredPlayers.indexOf(player);
    }

    @FXML
    private void onBackClicked() {
        System.out.println("[LeaderboardController] Back button clicked, returning to main lobby...");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main_lobby.fxml"));
            Parent root = loader.load();
            MainLobbyController controller = loader.getController();
            controller.setNetwork(this.network);
            // Truyền Stage để InviteNotificationManager có thể hiển thị popup
            Stage stage = (Stage) backButton.getScene().getWindow();
            controller.setPrimaryStage(stage);
            System.out.println("[LeaderboardController] main_lobby.fxml loaded successfully.");

            network.removeMessageListener(this);
            System.out.println("[LeaderboardController] Listener removed for MainLobbyController.");

            stage.setScene(new Scene(root));
            stage.setTitle("Main Lobby");
            stage.show();
            System.out.println("[LeaderboardController] Switched to Main Lobby scene.");
        } catch (IOException e) {
            System.err.println("[LeaderboardController] Error loading main_lobby.fxml: " + e.getMessage());
            e.printStackTrace();
            Platform.runLater(() -> {
                showAlert("Lỗi Giao Diện", "Không thể tải màn hình chính: " + e.getMessage());
            });
        }
    }

    private void showAlert(String title, String message) {
        Platform.runLater(() -> {
            System.out.println("[LeaderboardController] Showing alert: title=" + title + ", message=" + message);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    public void cleanup() {
        if (network != null) {
            System.out.println("[LeaderboardController] Cleaning up: removing listener and closing network.");
            network.removeMessageListener(this);
            try {
                network.close();
            } catch (IOException e) {
                System.err.println("[LeaderboardController] Error closing network: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}