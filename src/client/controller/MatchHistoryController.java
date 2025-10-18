package client.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class MatchHistoryController {

    @FXML private TableView<MatchRecord> matchTable;
    @FXML private TableColumn<MatchRecord, String> opponentColumn;
    @FXML private TableColumn<MatchRecord, String> resultColumn;
    @FXML private TableColumn<MatchRecord, String> scoreColumn;
    @FXML private TableColumn<MatchRecord, String> eloChangeColumn;
    @FXML private Pagination pagination;
    @FXML private Label totalLabel;

    private static final int ROWS_PER_PAGE = 8;
    private ObservableList<MatchRecord> allMatches;

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

        // Dữ liệu mẫu
        allMatches = FXCollections.observableArrayList(
                new MatchRecord("Minh Trí", "Thắng", "150 - 120", "+15"),
                new MatchRecord("Hoàng Anh", "Thua", "95 - 130", "-10"),
                new MatchRecord("Thùy Linh", "Thắng", "145 - 100", "+12"),
                new MatchRecord("Quang Huy", "Hòa", "125 - 125", "0"),
                new MatchRecord("Thanh Thảo", "Thắng", "160 - 145", "+8"),
                new MatchRecord("Văn Tùng", "Thua", "110 - 155", "-12"),
                new MatchRecord("Minh Ngọc", "Thắng", "140 - 105", "+14"),
                new MatchRecord("Thu Hà", "Thua", "120 - 140", "-8"),
                new MatchRecord("Anh Duy", "Hòa", "130 - 130", "0"),
                new MatchRecord("Hồng Nhung", "Thắng", "155 - 140", "+10")
        );

        // ✅ Khóa chiều cao đúng 8 dòng (không bị giãn hoặc thêm dòng trống)
        matchTable.setFixedCellSize(40);
        matchTable.prefHeightProperty().bind(
                matchTable.fixedCellSizeProperty().multiply(ROWS_PER_PAGE + 1.01)
        );

        setupPagination();

        totalLabel.setText("Tổng số trận đấu: " + allMatches.size());
    }

    private void setupPagination() {
        int pageCount = (int) Math.ceil(allMatches.size() / (double) ROWS_PER_PAGE);
        pagination.setPageCount(Math.max(pageCount, 1));
        pagination.setCurrentPageIndex(0);
        pagination.currentPageIndexProperty().addListener((obs, oldVal, newVal) -> updatePage(newVal.intValue()));
        updatePage(0);
    }

    private void updatePage(int pageIndex) {
        int fromIndex = pageIndex * ROWS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ROWS_PER_PAGE, allMatches.size());
        ObservableList<MatchRecord> pageData =
                FXCollections.observableArrayList(allMatches.subList(fromIndex, toIndex));

        matchTable.setItems(pageData);
        matchTable.refresh();
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

    // Lớp dữ liệu nội bộ
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
