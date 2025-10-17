/**
 * 
 */
/**
 * 
 */
module BTL_LTM {
	requires java.sql;
    requires javafx.graphics;
    requires javafx.fxml;
    requires javafx.controls;
    opens client to javafx.graphics, javafx.fxml;
    opens client.controller to javafx.fxml;
}
