package client.controller;

import client.model.Game;
import client.model.MatchRecord;
import client.network.MessageListener;
import client.network.Network;
import dto.GameDTO;
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
import dto.Message;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class MatchHistoryController implements MessageListener {

    @FXML private TableView<MatchRecord> matchTable;
    @FXML private TableColumn<MatchRecord, String> opponentColumn;
    @FXML private TableColumn<MatchRecord, String> resultColumn;
    @FXML private TableColumn<MatchRecord, String> scoreColumn;
    @FXML private TableColumn<MatchRecord, String> scoreOpp;
    @FXML private TableColumn<MatchRecord, String> eloChangeColumn;
    @FXML private TableColumn<MatchRecord, String> time;

    @FXML private Pagination pagination;
    @FXML private Label totalLabel;
    @FXML private Button backButton;

    private static final int ROWS_PER_PAGE = 8;

    private ObservableList<MatchRecord> allMatches;
    private Network network;
    private String currentUser;

    public void setCurrentUser(String currentUser) {
        this.currentUser = currentUser;

        Platform.runLater(() -> {
            System.out.println(currentUser);
            if (totalLabel != null) {
                System.out.println("[MatchHistoryController] totalLabel is NOT null. Setting text.");
                totalLabel.setText("Người chơi: " + currentUser + " — Tổng số trận đấu: "
                        + (allMatches == null ? 0 : allMatches.size()));
            }
        });
    }

    public void setNetwork(Network network) {
        this.network = network;
        if (network != null) {
            network.addMessageListener(this);
            requestHistory();
        } else {
            showAlert("Lỗi Mạng", "Không thể kết nối server.");
        }
    }

    @FXML
    public void initialize() {
        opponentColumn.setCellValueFactory(new PropertyValueFactory<>("opponent"));
        resultColumn.setCellValueFactory(new PropertyValueFactory<>("result"));
        resultColumn.setCellFactory(column -> new TableCell<MatchRecord, String>() {

            // Bỏ style căn giữa của hàm centerColumn() cũ
            // và thêm style CSS động
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                // Xóa style cũ để tránh lỗi khi cuộn bảng
                getStyleClass().removeAll("cell-win", "cell-lose", "cell-draw");
                setStyle("-fx-alignment: CENTER;"); // Căn giữa lại

                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);

                    // Thêm style class mới dựa trên nội dung "Thắng/Thua/Hòa"
                    if (item.equals("Thắng")) {
                        getStyleClass().add("cell-win");
                    } else if (item.equals("Thua")) {
                        getStyleClass().add("cell-lose");
                    } else if (item.equals("Hòa")) {
                        getStyleClass().add("cell-draw");
                    }
                }
            }
        });
        scoreColumn.setCellValueFactory(new PropertyValueFactory<>("score"));
        scoreOpp.setCellValueFactory(new PropertyValueFactory<>("scoreOpp"));
        eloChangeColumn.setCellValueFactory(new PropertyValueFactory<>("eloChange"));
        time.setCellValueFactory(new PropertyValueFactory<>("startTime"));

        centerColumn(opponentColumn);

        centerColumn(scoreColumn);
        centerColumn(scoreOpp);
        eloChangeColumn.setCellFactory(column -> new TableCell<MatchRecord, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                // Xóa style cũ để tránh lỗi khi cuộn
                getStyleClass().removeAll("cell-win", "cell-lose", "cell-draw");
                setStyle("-fx-alignment: CENTER;"); // Căn giữa

                if (empty || item == null) {
                    setText(null);
                } else {
                    try {
                        // Chuyển String ("10", "-5") thành số
                        int eloValue = Integer.parseInt(item);

                        if (eloValue > 0) {
                            // 1. Thêm dấu "+" cho đẹp
                            setText(item);
                            // 2. Thêm class màu xanh
                            getStyleClass().add("cell-win");

                        } else if (eloValue < 0) {
                            // 1. Giữ nguyên text (vì đã có dấu "-")
                            setText(item);
                            // 2. Thêm class màu đỏ
                            getStyleClass().add("cell-lose");

                        } else { // eloValue == 0
                            // 1. Hiển thị là "0"
                            setText("0");
                            // 2. Thêm class màu vàng (hoặc trung tính)
                            getStyleClass().add("cell-draw");
                        }

                    } catch (NumberFormatException e) {
                        // Nếu dữ liệu không phải là số (phòng hờ)
                        setText(item);
                    }
                }
            }
        });
        centerColumn(time);

        allMatches = FXCollections.observableArrayList();
        matchTable.setItems(allMatches);

        pagination.currentPageIndexProperty().addListener(
                (obs, oldV, newV) -> updatePage(newV.intValue())
        );

        backButton.setOnAction(e -> onBackClicked());

        totalLabel.setText("Tổng số trận đấu: 0");
    }

    private void requestHistory() {
        try {

            network.send(new Message("GET_HISTORY", currentUser, null));
            System.out.println("[MatchHistoryController] send request GET_HISTORY");
        } catch (IOException e) {
            showAlert("Lỗi", "Không thể gửi GET_HISTORY: " + e.getMessage());
        }
    }

    /** ------------------ FORMAT HISTORY ------------------- **/

    private List<MatchRecord> format(List<GameDTO> history) {
        List<MatchRecord> result = new ArrayList<>();
        if (history == null || currentUser == null) return result;

        for (GameDTO g : history) {

            boolean isUser1 = g.getUserId_1().equals(currentUser);
            String opponent = isUser1 ? g.getUserId_2() : g.getUserId_1();

            int myScore = Integer.parseInt(isUser1 ? g.getUserResult_1() : g.getUserResult_2());
            int oppScore = Integer.parseInt(isUser1 ? g.getUserResult_2() : g.getUserResult_1());

            String matchResult = myScore > oppScore ? "Thắng" : myScore < oppScore ? "Thua" : "Hòa";

            String score = String.valueOf(myScore);
            String eloMark = myScore > oppScore ? "+" : myScore < oppScore ? "-" : "";

            MatchRecord record = new MatchRecord(
                    opponent,
                    matchResult,
                    score,
                    eloMark + (Math.abs(myScore - oppScore) / 2 + 1),
                    String.valueOf(oppScore)
            );

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");
            record.setStartTime(g.getTime().format(dtf));

            result.add(record);
        }

        return result;
    }

    /** ---------------- MESSAGE RECEIVED ---------------- **/

    @Override
    public void onMessageReceived(Message msg) {
        System.out.println("[MatchHistoryController] received" );
        if (msg == null) return;

        if ("GET_HISTORY_RESPONSE".equals(msg.getCommand())) {
            List<GameDTO> list = (List<GameDTO>) msg.getContent();
            System.out.println("[MatchHistoryController] history len:" + list.size());
            List<MatchRecord> formatted = format(list);

            Platform.runLater(() -> {
                allMatches.setAll(formatted);
                totalLabel.setText("Người chơi: " + currentUser
                        + " — Tổng số trận đấu: " + allMatches.size());
                setupPagination();
            });
        }
    }

    /** ---------------- PAGINATION ---------------- **/

    private void setupPagination() {
        int pageCount = (int) Math.ceil(allMatches.size() / (double) ROWS_PER_PAGE);
        pagination.setPageCount(Math.max(pageCount, 1));
        pagination.setCurrentPageIndex(0);
        updatePage(0);
    }

    private void updatePage(int pageIndex) {
        int from = pageIndex * ROWS_PER_PAGE;
        int to = Math.min(from + ROWS_PER_PAGE, allMatches.size());
        matchTable.setItems(FXCollections.observableArrayList(allMatches.subList(from, to)));
        matchTable.refresh();
    }

    /** ----------- UI UTILS ---------------- **/

    private <T> void centerColumn(TableColumn<MatchRecord, T> col) {
        col.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(item.toString());
                setStyle("-fx-alignment: CENTER;");
            }
        });
    }

    @FXML
    private void onBackClicked() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main_lobby.fxml"));
            Parent root = loader.load();

            MainLobbyController controller = loader.getController();
            controller.setNetwork(this.network);

            Stage stage = (Stage) backButton.getScene().getWindow();
            controller.setPrimaryStage(stage);

            network.removeMessageListener(this);

            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            showAlert("Lỗi", "Không thể quay lại lobby: " + e.getMessage());
        }
    }

    private void showAlert(String title, String msg) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(msg);
            alert.showAndWait();
        });
    }

    /** ---------------- MODEL CLASS ---------------- **/


}