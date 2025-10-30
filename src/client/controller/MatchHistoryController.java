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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import server.dto.Message;

import java.io.IOException;
import java.util.List;

public class MatchHistoryController implements MessageListener {

    @FXML private TableView<MatchRecord> matchTable;
    @FXML private TableColumn<MatchRecord, String> opponentColumn;
    @FXML private TableColumn<MatchRecord, String> resultColumn;
    @FXML private TableColumn<MatchRecord, String> scoreColumn;
    @FXML private TableColumn<MatchRecord, String> eloChangeColumn;
    @FXML private Pagination pagination;
    @FXML private Label totalLabel;
    @FXML private Button backButton; // Thêm nút quay lại

    private static final int ROWS_PER_PAGE = 8;
    private ObservableList<MatchRecord> allMatches;
    private Network network;

    public void setNetwork(Network network) {
        this.network = network;
        if (this.network != null) {
            this.network.addMessageListener(this);
            System.out.println("[MatchHistoryController] Network set successfully and listener registered.");
            requestHistory();
        } else {
            System.err.println("[MatchHistoryController] Error: Network is null, cannot set up connection.");
            Platform.runLater(() -> {
                showAlert("Lỗi Mạng", "Không thể thiết lập kết nối mạng.");
            });
        }
    }

    @FXML
    public void initialize() {
        // Gán dữ liệu cho cột
        opponentColumn.setCellValueFactory(new PropertyValueFactory<>("opponent"));
        resultColumn.setCellValueFactory(new PropertyValueFactory<>("result"));
        scoreColumn.setCellValueFactory(new PropertyValueFactory<>("score"));
        eloChangeColumn.setCellValueFactory(new PropertyValueFactory<>("eloChange"));

        // Căn giữa nội dung cột
        centerColumn(opponentColumn);
        centerColumn(resultColumn);
        centerColumn(scoreColumn);
        centerColumn(eloChangeColumn);

        // Khởi tạo danh sách rỗng
        allMatches = FXCollections.observableArrayList();
        matchTable.setItems(allMatches);

        // Cố định chiều cao bảng
        matchTable.setFixedCellSize(40);
        matchTable.prefHeightProperty().bind(
                matchTable.fixedCellSizeProperty().multiply(ROWS_PER_PAGE + 1.01)
        );

        // Thiết lập phân trang
        setupPagination();

        // Thiết lập nút quay lại
        backButton.setOnAction(e -> onBackClicked());

        // Cập nhật nhãn tổng số trận
        totalLabel.setText("Tổng số trận đấu: 0");
    }

    private void requestHistory() {
        try {
            Message msg = new Message("GET_HISTORY", "CLIENT", null);
            System.out.println("[MatchHistoryController] Sending request for history");
            network.send(msg);
        } catch (IOException e) {
            System.err.println("[MatchHistoryController] Error sending GET_HISTORY request: " + e.getMessage());
            e.printStackTrace();
            Platform.runLater(() -> {
                showAlert("Lỗi Gửi Yêu Cầu", "Không thể gửi yêu cầu lấy lịch sử đấu: " + e.getMessage());
            });
        }
    }

    @Override
    public void onMessageReceived(Message msg) {
        System.out.println("[MatchHistoryController] Received message: command=" + (msg != null ? msg.getCommand() : "null"));
        if (msg != null && "HISTORY_RESPONSE".equals(msg.getCommand())) {
            List<MatchRecord> history = (List<MatchRecord>) msg.getContent();
            System.out.println("[MatchHistoryController] HISTORY_RESPONSE received: records=" + (history != null ? history.size() : "null"));
            if (history != null) {
                Platform.runLater(() -> {
                    allMatches.setAll(history);
                    totalLabel.setText("Tổng số trận đấu: " + allMatches.size());
                    setupPagination();
                    System.out.println("[MatchHistoryController] Updated history with " + allMatches.size() + " records");
                });
            } else {
                System.err.println("[MatchHistoryController] Error: History data is null");
                Platform.runLater(() -> {
                    showAlert("Lỗi Phản Hồi", "Lịch sử đấu từ server không hợp lệ.");
                });
            }
        } else {
            System.out.println("[MatchHistoryController] Ignored message: command=" + (msg != null ? msg.getCommand() : "null"));
        }
    }

    private void setupPagination() {
        int pageCount = (int) Math.ceil(allMatches.size() / (double) ROWS_PER_PAGE);
        pagination.setPageCount(Math.max(pageCount, 1));
        pagination.setCurrentPageIndex(0);
        pagination.currentPageIndexProperty().addListener((obs, oldVal, newVal) -> updatePage(newVal.intValue()));
        updatePage(0);
        System.out.println("[MatchHistoryController] Pagination set up with " + pageCount + " pages");
    }

    private void updatePage(int pageIndex) {
        int fromIndex = pageIndex * ROWS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ROWS_PER_PAGE, allMatches.size());
        ObservableList<MatchRecord> pageData =
                FXCollections.observableArrayList(allMatches.subList(fromIndex, toIndex));
        matchTable.setItems(pageData);
        matchTable.refresh();
        System.out.println("[MatchHistoryController] Updated page " + pageIndex + ": " + (toIndex - fromIndex) + " records");
    }

    private <T> void centerColumn(TableColumn<MatchRecord, T> column) {
        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toString());
                    setStyle("-fx-alignment: CENTER;");
                }
            }
        });
    }

    @FXML
    private void onBackClicked() {
        System.out.println("[MatchHistoryController] Back button clicked, returning to main lobby...");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main_lobby.fxml"));
            Parent root = loader.load();
            MainLobbyController controller = loader.getController();
            controller.setNetwork(this.network);
            System.out.println("[MatchHistoryController] main_lobby.fxml loaded successfully.");

            network.removeMessageListener(this);
            System.out.println("[MatchHistoryController] Listener removed for MainLobbyController.");

            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Main Lobby");
            stage.show();
            System.out.println("[MatchHistoryController] Switched to Main Lobby scene.");
        } catch (IOException e) {
            System.err.println("[MatchHistoryController] Error loading main_lobby.fxml: " + e.getMessage());
            e.printStackTrace();
            Platform.runLater(() -> {
                showAlert("Lỗi Giao Diện", "Không thể tải màn hình chính: " + e.getMessage());
            });
        }
    }

    private void showAlert(String title, String message) {
        Platform.runLater(() -> {
            System.out.println("[MatchHistoryController] Showing alert: title=" + title + ", message=" + message);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    public void cleanup() {
        if (network != null) {
            System.out.println("[MatchHistoryController] Cleaning up: removing listener and closing network.");
            network.removeMessageListener(this);
            try {
                network.close();
            } catch (IOException e) {
                System.err.println("[MatchHistoryController] Error closing network: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public static class MatchRecord {
        private final String opponent;
        private final String result;
        private final String score;
        private final String eloChange;

        public MatchRecord(String opponent, String result, String score, String eloChange) {
            this.opponent = opponent;
            this.result = result;
            this.score = score;
            this.eloChange = eloChange;
        }

        public String getOpponent() { return opponent; }
        public String getResult() { return result; }
        public String getScore() { return score; }
        public String getEloChange() { return eloChange; }
    }
}