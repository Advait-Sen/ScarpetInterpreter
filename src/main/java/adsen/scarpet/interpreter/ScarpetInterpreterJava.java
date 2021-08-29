package adsen.scarpet.interpreter;

import adsen.scarpet.interpreter.parser.Expression;
import adsen.scarpet.interpreter.parser.ScarpetScriptServer;
import adsen.scarpet.interpreter.parser.exception.ExpressionException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Scanner;

public class ScarpetInterpreterJava extends Application {
    public static ScarpetScriptServer scriptServer;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ScarpetInterpreterJava.class.getResource("scarpet-screen.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        System.out.println("Started Scarpet Interpreter");

        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();

        while (!input.equalsIgnoreCase("end")) {
            try {
                new Expression(input).displayOutput();
            } catch (ExpressionException e) {
                System.out.println(e.getMessage());
            } catch (Throwable t) {
                t.printStackTrace();
            }
            input = scanner.nextLine();
        }

        System.out.println("Finished interpreting");
        launch();
    }
}