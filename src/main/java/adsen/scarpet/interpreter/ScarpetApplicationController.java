package adsen.scarpet.interpreter;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ScarpetApplicationController {
    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to Scarpet Application!");
    }
}