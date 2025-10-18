package client.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import client.model.Player;

import java.util.Comparator;

public class LeaderboardController {

    @FXML private TableView<Player> leaderboardTable;
    @FXML private TableColumn<Player, Integer> rankColumn;
    @FXML private TableColumn<Player, String> nameColumn;
    @FXML private TableColumn<Player, Integer> eloColumn;
    @FXML private TableColumn<Player, Integer> winsColumn;
    @FXML private Pagination pagination;
    @FXML private ToggleButton scoreTab;
    @FXML private ToggleButton winTab;
    @FXML private TextField searchField;
    @FXML private Button backButton;
    @FXML private ToggleGroup tabGroup;

    private ObservableList<Player> allPlayers;
    private ObservableList<Player> filteredPlayers;
    private static final int ROWS_PER_PAGE = 8; // 🔹 8 dòng cố định

    @FXML
    public void initialize() {
        // --- Gán cột với thuộc tính ---
        rankColumn.setCellValueFactory(new PropertyValueFactory<>("rank"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        eloColumn.setCellValueFactory(new PropertyValueFactory<>("elo"));
        winsColumn.setCellValueFactory(new PropertyValueFactory<>("wins"));

        // --- Tắt sắp xếp bằng click cột ---
        rankColumn.setSortable(false);
        nameColumn.setSortable(false);
        eloColumn.setSortable(false);
        winsColumn.setSortable(false);
        leaderboardTable.setSortPolicy(param -> null);

        // --- Dữ liệu mẫu ---
        allPlayers = FXCollections.observableArrayList(
                new Player(1, "Nguyễn Văn A", 2450, 132),
                new Player(2, "Trần Thị B", 2400, 125),
                new Player(3, "Lê Văn C", 2380, 120),
                new Player(4, "Phạm Văn D", 2350, 115),
                new Player(5, "Hoàng Thị E", 2320, 110),
                new Player(6, "Vũ Văn F", 2280, 105),
                new Player(7, "Nguyễn Thị G", 2240, 100),
                new Player(8, "Trần Văn H", 2210, 95),
                new Player(9, "Đỗ Thị I", 2180, 90),
                new Player(10, "Phan Văn K", 2150, 85),
                new Player(11, "Ngô Thị L", 2100, 80),
                new Player(12, "Đặng Văn M", 2050, 75)
        );

        filteredPlayers = FXCollections.observableArrayList(allPlayers);

        sortByElo();
        setupPagination();
        fixTableHeight();

        // --- Cột tự chia đều ---
        leaderboardTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        // --- Lọc realtime ---
        searchField.textProperty().addListener((obs, oldVal, newVal) -> updateFiltered(newVal));
    }

    //  CỐ ĐỊNH CHIỀU CAO BẢNG

    private void fixTableHeight() {
        leaderboardTable.setFixedCellSize(35); // mỗi dòng 35px
        double headerHeight = 30; // chiều cao tiêu đề cột
        leaderboardTable.prefHeightProperty().bind(
                leaderboardTable.fixedCellSizeProperty().multiply(ROWS_PER_PAGE).add(headerHeight)
        );
        leaderboardTable.minHeightProperty().bind(leaderboardTable.prefHeightProperty());
        leaderboardTable.maxHeightProperty().bind(leaderboardTable.prefHeightProperty());
    }

    // TAB SẮP XẾP
    @FXML
    private void onScoreTabClicked() {
        sortByElo();
        scoreTab.setSelected(true);
        winTab.setSelected(false);
    }

    @FXML
    private void onWinTabClicked() {
        sortByWins();
        winTab.setSelected(true);
        scoreTab.setSelected(false);
    }

    // PHÂN TRANG
    private void setupPagination() {
        int pageCount = (int) Math.ceil(filteredPlayers.size() * 1.0 / ROWS_PER_PAGE);
        pagination.setPageCount(Math.max(pageCount, 1));
        pagination.setCurrentPageIndex(0);
        updatePage(0);

        pagination.currentPageIndexProperty().addListener((obs, oldIndex, newIndex) ->
                updatePage(newIndex.intValue()));
    }

    private void updatePage(int pageIndex) {
        int start = pageIndex * ROWS_PER_PAGE;
        int end = Math.min(start + ROWS_PER_PAGE, filteredPlayers.size());
        if (start < end) {
            leaderboardTable.setItems(FXCollections.observableArrayList(filteredPlayers.subList(start, end)));
        } else {
            leaderboardTable.setItems(FXCollections.observableArrayList());
        }
    }


    // TÌM KIẾM
    private void updateFiltered(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            filteredPlayers.setAll(allPlayers);
        } else {
            filteredPlayers.setAll(allPlayers.filtered(
                    p -> p.getName().toLowerCase().contains(keyword.toLowerCase())
            ));
        }
        setupPagination();
    }


    //  SẮP XẾP
    private void sortByElo() {
        allPlayers.sort(Comparator.comparingInt(Player::getElo).reversed());
        updateRanks();
        updateFiltered(searchField.getText());
    }

    private void sortByWins() {
        allPlayers.sort(Comparator.comparingInt(Player::getWins).reversed());
        updateRanks();
        updateFiltered(searchField.getText());
    }

    private void updateRanks() {
        for (int i = 0; i < allPlayers.size(); i++) {
            allPlayers.get(i).setRank(i + 1);
        }
    }


    // NÚT QUAY LẠI

    @FXML
    private void onBackClicked() {
        System.out.println("⟲ Quay lại màn chính...");
        // TODO: chuyển scene nếu cần
    }
}
