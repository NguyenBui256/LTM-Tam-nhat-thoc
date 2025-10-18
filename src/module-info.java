/**
 * 
 */
/**
 * 
 */
module BTL_LTM {
    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.base;
	requires java.sql;
    opens client to javafx.graphics, javafx.fxml;
    opens client.controller to javafx.fxml;
    exports client;
}
