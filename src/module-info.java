module BTL_LTM {
	requires javafx.graphics;
	requires javafx.controls;
	requires javafx.fxml;
	requires java.sql;
  requires javafx.base;


	// Cho phép javafx.graphics tạo instance của ClientMain bằng reflection
	exports client;
}
