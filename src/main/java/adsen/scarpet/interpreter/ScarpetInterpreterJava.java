package adsen.scarpet.interpreter;

import adsen.scarpet.interpreter.parser.ScarpetScriptServer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ScarpetInterpreterJava extends Application {
    public static ScarpetScriptServer scriptServer;

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ScarpetInterpreterJava.class.getResource("scarpet-screen.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);

        stage.setTitle("Scarpet Interpreter");
        stage.setScene(scene);
        stage.show();
    }
}