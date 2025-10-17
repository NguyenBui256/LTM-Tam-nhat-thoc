package client.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import client.model.Player;

public class PlayerListController {

    @FXML
    private TableView<Player> playerTable;
    @FXML
    private TableColumn<Player, String> nameColumn;
    @FXML
    private TableColumn<Player, Integer> eloColumn;
    @FXML
    private TableColumn<Player, Integer> winsColumn; // ✅ thêm
    @FXML
    private TableColumn<Player, String> statusColumn;
    @FXML
    private TableColumn<Player, Void> actionColumn;
    @FXML
    private Pagination pagination;

    private final int rowsPerPage = 8;
    private ObservableList<Player> allPlayers;

    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        eloColumn.setCellValueFactory(new PropertyValueFactory<>("elo"));
        winsColumn.setCellValueFactory(new PropertyValueFactory<>("wins")); // ✅
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        centerColumn(nameColumn);
        centerColumn(eloColumn);
        centerColumn(winsColumn); // ✅
        centerColumn(statusColumn);

        // dữ liệu mock
        allPlayers = FXCollections.observableArrayList(
                new Player("Nguyen Van A", 1800, "Đang rỗi", 25),
                new Player("Tran Thi B", 1750, "Đang bận", 18),
                new Player("Le Van C", 2000, "Đang rỗi", 40),
                new Player("Pham Thi D", 2100, "Đang rỗi", 42),
                new Player("Hoang Van E", 1650, "Đang rỗi", 10),
                new Player("Nguyen Thi F", 1980, "Đang bận", 36),
                new Player("Tran Van G", 1790, "Đang rỗi", 20),
                new Player("Le Thi H", 2050, "Đang bận", 39),
                new Player("Pham Thi I", 1600, "Đang rỗi", 8),
                new Player("Do Van K", 1850, "Đang bận", 22)
        );

        addActionButton();
        setupPagination();
    }

    private void setupPagination() {
        int pageCount = (int) Math.ceil((double) allPlayers.size() / rowsPerPage);
        pagination.setPageCount(pageCount);
        pagination.setCurrentPageIndex(0);

        pagination.currentPageIndexProperty().addListener((obs, oldIndex, newIndex) -> updateTable(newIndex.intValue()));

        updateTable(0);
    }

    private void updateTable(int pageIndex) {
        int fromIndex = pageIndex * rowsPerPage;
        int toIndex = Math.min(fromIndex + rowsPerPage, allPlayers.size());
        playerTable.setItems(FXCollections.observableArrayList(allPlayers.subList(fromIndex, toIndex)));
        playerTable.refresh();
    }

    private <T> void centerColumn(TableColumn<Player, T> column) {
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

    private void addActionButton() {
        actionColumn.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Mời đấu");

            {
                btn.setOnAction(e -> {
                    Player p = getTableView().getItems().get(getIndex());
                    System.out.println("Mời đấu: " + p.getName());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Player p = getTableView().getItems().get(getIndex());
                    btn.setDisable("Đang bận".equals(p.getStatus()));
                    setGraphic(btn);
                    setStyle("-fx-alignment: CENTER;");
                }
            }
        });
    }
}
