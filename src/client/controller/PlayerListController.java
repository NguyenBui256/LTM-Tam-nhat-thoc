package client.controller;

import client.model.Player;
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
import server.dto.OnlinePlayerList;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class PlayerListController implements MessageListener {

    @FXML
    private TableView<Player> playerTable;
    @FXML
    private TableColumn<Player, String> nameColumn;
    @FXML
    private TableColumn<Player, Integer> eloColumn;
    @FXML
    private TableColumn<Player, Integer> winsColumn;
    @FXML
    private TableColumn<Player, String> statusColumn;
    @FXML
    private TableColumn<Player, Void> actionColumn;
    @FXML
    private Pagination pagination;
    @FXML
    private Button backButton;

    private final int rowsPerPage = 8;
    private ObservableList<Player> allPlayers;
    private Network network;

    public void setNetwork(Network network) {
        this.network = network;
        if (this.network != null) {
            this.network.addMessageListener(this);
            System.out.println("[PlayerListController] Network set successfully and listener registered.");
            requestOnlinePlayers();
        } else {
            System.err.println("[PlayerListController] Network is null → using mock data for testing UI.");
            // Dòng này không cần thiết vì initialize đã gọi rồi
            // loadMockData();
        }
    }

    @FXML
    public void initialize() {
        // Liên kết cột với thuộc tính trong Player
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        eloColumn.setCellValueFactory(new PropertyValueFactory<>("elo"));
        winsColumn.setCellValueFactory(new PropertyValueFactory<>("wins"));
        // Không dùng PropertyValueFactory cho các cột tùy chỉnh
        // statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        allPlayers = FXCollections.observableArrayList();
        playerTable.setItems(allPlayers);

        // ✅ GỌI PHƯƠNG THỨC MỚI ĐỂ CÀI ĐẶT GIAO DIỆN TÙY CHỈNH
        setupCustomCells();
        setupPagination();

        backButton.setOnAction(e -> onBackClicked());

        // Load dữ liệu giả để test giao diện ngay lập tức
        loadMockData();
    }

    /**
     * ✅ PHẦN QUAN TRỌNG NHẤT:
     * Cài đặt giao diện tùy chỉnh cho các ô Trạng thái và Hành động
     * bằng cách áp dụng các style class từ file CSS.
     */
    private void setupCustomCells() {
        statusColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setGraphic(null);
                    // Đặt lại style để tránh lỗi tái sử dụng cell
                    getStyleClass().removeAll("status-label-available", "status-label-busy");
                } else {
                    Label statusLabel = new Label(status);
                    // Áp dụng style class dựa trên nội dung status
                    if ("ONLINE".equalsIgnoreCase(status) || "WAITING".equalsIgnoreCase(status)) {
                        statusLabel.getStyleClass().add("status-label-available");
                    } else { // BUSY, IN_GAME
                        statusLabel.getStyleClass().add("status-label-busy");
                    }
                    setGraphic(statusLabel);
                    setText(null);
                    setStyle("-fx-alignment: CENTER;");
                }
            }
        });

        // Gán dữ liệu cho cột Trạng thái sau khi đã setCellFactory
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        // --- Cột Hành động (Action Column) ---
        actionColumn.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Mời đấu");
            {
                btn.setOnAction(e -> {
                    Player p = getTableView().getItems().get(getIndex());
                    try {
                        // Gửi lời mời đến người chơi khác qua server
                        Message inviteMsg = new Message(
                                "INVITE_REQUEST",      // Loại lệnh
                                "CLIENT",              // Ai gửi
                                p.getName() // Nội dung (tên người được mời)
                        );

                        network.send(inviteMsg);  // Gửi qua mạng

                        // Hiển thị thông báo trên giao diện
                        showAlert(
                                Alert.AlertType.INFORMATION,
                                "Mời đấu",
                                "Đã gửi lời mời đến " + p.getName()
                        );

                    } catch (IOException ex) {
                        showAlert(
                                Alert.AlertType.ERROR,
                                "Lỗi",
                                "Không thể gửi lời mời: " + ex.getMessage()
                        );
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Player p = getTableView().getItems().get(getIndex());
                    // Áp dụng style class và bật/tắt nút dựa trên status
                    if ("ONLINE".equalsIgnoreCase(p.getStatus()) || "WAITING".equalsIgnoreCase(p.getStatus())) {
                        btn.setDisable(false);
                        btn.getStyleClass().setAll("action-button-invite");
                    } else { // BUSY, IN_GAME
                        btn.setDisable(true);
                        btn.getStyleClass().setAll("action-button-disabled");
                    }
                    setGraphic(btn);
                    setStyle("-fx-alignment: CENTER;");
                }
            }
        });
    }

    /** Dữ liệu giả lập để test giao diện */
    private void loadMockData() {
        List<Player> mockPlayers = Arrays.asList(
                new Player("MinhTri", "ONLINE", 1520, 10),
                new Player("HoangAnh", "WAITING", 1640, 15),
                new Player("ThuyLinh", "BUSY", 1725, 8),
                new Player("QuangHuy", "IN_GAME", 1450, 12),
                new Player("ThanhThao", "ONLINE", 1820, 22),
                new Player("VanTung", "WAITING", 1560, 5),
                new Player("MinhNgoc", "BUSY", 1605, 7),
                new Player("ThuHa", "IN_GAME", 1775, 18),
                new Player("AnhDuy", "ONLINE", 1620, 11),
                new Player("HongNhung", "WAITING", 1690, 14),
                new Player("BaoLong", "ONLINE", 1590, 6),
                new Player("KimNgan", "BUSY", 1750, 19),
                new Player("ThanhDat", "WAITING", 1480, 3),
                new Player("NgocAnh", "IN_GAME", 1705, 16),
                new Player("KhanhLinh", "ONLINE", 1800, 25),
                new Player("HuuPhuoc", "WAITING", 1580, 4),
                new Player("TuanKiet", "BUSY", 1625, 9),
                new Player("PhuongVy", "IN_GAME", 1500, 10),
                new Player("MyDung", "ONLINE", 1685, 13),
                new Player("BaoTran", "WAITING", 1730, 17)
        );
        allPlayers.setAll(mockPlayers);
        System.out.println("[PlayerListController] Loaded mock data with " + allPlayers.size() + " players");
        // Cập nhật lại phân trang sau khi có dữ liệu mới
        setupPagination();
    }

    /** Gửi yêu cầu thật đến server (chưa dùng khi test mock) */
    private void requestOnlinePlayers() {
        try {
            Message msg = new Message("GET_ONLINE_PLAYERS", "CLIENT", null);
            network.send(msg);
        } catch (IOException e) {
            System.err.println("[PlayerListController] Error sending GET_ONLINE_PLAYERS: " + e.getMessage());
        }
    }

    @Override
    public void onMessageReceived(Message msg) {
        if (msg != null && "ONLINE_PLAYERS_RESPONSE".equals(msg.getCommand())) { // Sửa getCommand() thành getType() cho nhất quán
            OnlinePlayerList list = (OnlinePlayerList) msg.getContent();
            if (list != null && list.getPlayers() != null) {
                Platform.runLater(() -> {
                    // Cập nhật danh sách người chơi từ server
                });
            }
        }
    }

    private void setupPagination() {
        int pageCount = (int) Math.ceil((double) allPlayers.size() / rowsPerPage);
        pagination.setPageCount(pageCount > 0 ? pageCount : 1);
        if (pagination.getCurrentPageIndex() >= pageCount) {
            pagination.setCurrentPageIndex(0);
        }
        pagination.currentPageIndexProperty().addListener((obs, oldIndex, newIndex) -> updateTable(newIndex.intValue()));
        updateTable(pagination.getCurrentPageIndex());
    }



    private void updateTable(int pageIndex) {
        int fromIndex = pageIndex * rowsPerPage;
        int toIndex = Math.min(fromIndex + rowsPerPage, allPlayers.size());
        if (fromIndex < allPlayers.size()) {
            playerTable.setItems(FXCollections.observableArrayList(allPlayers.subList(fromIndex, toIndex)));
        } else {
            playerTable.setItems(FXCollections.observableArrayList());
        }
        playerTable.refresh();
    }

    @FXML
    private void onBackClicked() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main_lobby.fxml"));
            Parent root = loader.load();
            MainLobbyController controller = loader.getController();
            controller.setNetwork(this.network);

            if (network != null)
                network.removeMessageListener(this);

            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Main Lobby");
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Giao Diện", "Không thể tải màn hình chính: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}