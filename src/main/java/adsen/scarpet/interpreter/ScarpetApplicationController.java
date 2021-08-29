package adsen.scarpet.interpreter;

import adsen.scarpet.interpreter.parser.Expression;
import adsen.scarpet.interpreter.parser.exception.ExpressionException;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
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
            new Expression(script, true, false).displayOutput(scriptOutputArea::appendText);
        } catch (ExpressionException e) {
            System.out.println(e.getMessage());
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}