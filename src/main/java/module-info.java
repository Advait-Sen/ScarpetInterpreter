module adsen.scarpet.interpreter {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.apache.commons.lang3;

    requires org.controlsfx.controls;
    requires javafx.graphics;

    opens adsen.scarpet.interpreter to javafx.fxml;
    exports adsen.scarpet.interpreter;
}