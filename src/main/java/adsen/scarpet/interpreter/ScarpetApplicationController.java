package adsen.scarpet.interpreter;

import adsen.scarpet.interpreter.parser.Expression;
import adsen.scarpet.interpreter.parser.util.Matrix;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

public class ScarpetApplicationController {
    @FXML
    private TextArea scriptInputArea;
    @FXML
    private TextArea scriptOutputArea;

    @FXML
    protected void onScriptInput() {
        scriptOutputArea.clear();
        String script = scriptInputArea.getText();
        try {
            new Expression(script, true, false).displayOutput(s -> scriptOutputArea.appendText(s + '\n'));
        } catch (Throwable e) {
            scriptOutputArea.appendText(e.getMessage());
            e.printStackTrace();
        }
        scriptOutputArea.appendText("Finished interpreting!");
    }
}