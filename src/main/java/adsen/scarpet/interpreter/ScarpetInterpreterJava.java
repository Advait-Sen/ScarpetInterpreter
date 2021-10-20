package adsen.scarpet.interpreter;

import adsen.scarpet.interpreter.parser.ScarpetScriptServer;
import adsen.scarpet.interpreter.parser.util.Matrix;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ScarpetInterpreterJava extends Application {
    public static ScarpetScriptServer scriptServer;

    public static void main(String[] args) {
        Matrix a = new Matrix(
                new double[]{1, 2, 3},
                new double[]{4, 5, 6}
        );
        Matrix b = new Matrix(
                new double[]{7, 8, 9, 10},
                new double[]{11, 12, 13, 14},
                new double[]{15, 16, 17, 18}
        );
        Matrix c = a.multiply(b);
        System.out.println(a);
        System.out.println(b);
        System.out.println(c);
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