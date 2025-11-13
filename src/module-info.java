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
    opens client.model to javafx.base;
    opens server.dto to javafx.base, javafx.fxml;
    opens server.model to javafx.base;

}
