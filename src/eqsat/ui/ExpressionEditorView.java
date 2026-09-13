package eqsat.ui;

import eqsat.model.Expr;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public final class ExpressionEditorView extends LabView {

    private final TextField input = new TextField();
    private final Label errorLabel = new Label();
    private final TextArea astText = new TextArea();
    private final AstView astView = new AstView();

    public ExpressionEditorView(Lab lab) {
        super(lab, "Expression Editor",
                "Write an expression, parse it into an AST, then build the E-Graph.");

        input.getStyleClass().addAll("text-field", "mono");
        input.setPromptText("e.g.  (a + b) + c");

        Button parse = button("Parse", "primary-button");
        Button build = button("Build E-Graph", "ghost-button");
        Button reset = button("Reset", "ghost-button");
        Button run   = button("Run Optimisation", "ghost-button");

        parse.setOnAction(e -> {
            if (lab.parseOnly()) {
                lab.expressionText = input.getText();
                errorLabel.setText("");
                astView.setExpression(lab.ast);
                showAst(lab.ast);
            } else {
                errorLabel.setText("Parse error: " + lab.lastError);
                astView.setExpression(null);
                astText.clear();
            }
            lab.fire();
        });

        build.setOnAction(e -> {
            lab.expressionText = input.getText();
            if (lab.buildGraph()) {
                errorLabel.setText("");
                astView.setExpression(lab.ast);
                showAst(lab.ast);
            } else {
                errorLabel.setText("Parse error: " + lab.lastError);
            }
        });

        reset.setOnAction(e -> {
            lab.expressionText = input.getText().isBlank() ? "(a + b) + c" : input.getText();
            input.setText(lab.expressionText);
            lab.buildGraph();
            astView.setExpression(lab.ast);
            showAst(lab.ast);
            errorLabel.setText("");
        });

        run.setOnAction(e -> {
            lab.expressionText = input.getText();
            if (!lab.buildGraph()) { errorLabel.setText("Parse error: " + lab.lastError); return; }
            astView.setExpression(lab.ast);
            showAst(lab.ast);
            lab.ensureEngine().runToCompletion();
            lab.extract();
            errorLabel.setText("");
        });

        HBox buttons = new HBox(8, parse, build, run, reset);
        buttons.setAlignment(Pos.CENTER_LEFT);

        errorLabel.getStyleClass().add("err-label");
        errorLabel.setWrapText(true);

        astText.getStyleClass().add("code-area");
        astText.setEditable(false);
        astText.setPrefRowCount(10);
        astText.setPromptText("AST dump");

        VBox astPanel = new VBox(8, section("Abstract Syntax Tree (graphical)"), astView);
        astPanel.getStyleClass().add("panel");
        VBox.setVgrow(astView, Priority.ALWAYS);

        VBox dumpPanel = new VBox(8, section("AST dump"), astText);
        dumpPanel.getStyleClass().add("panel");
        VBox.setVgrow(astText, Priority.ALWAYS);

        HBox center = new HBox(12, astPanel, dumpPanel);
        HBox.setHgrow(astPanel, Priority.ALWAYS);
        HBox.setHgrow(dumpPanel, Priority.ALWAYS);

        VBox headerBox = new VBox(6, new Label("Expression"), input, buttons, errorLabel);
        headerBox.getStyleClass().add("panel");
        headerBox.setPadding(new Insets(14));

        getChildren().addAll(headerBox, center);
        VBox.setVgrow(center, Priority.ALWAYS);

        input.setText(lab.expressionText);
        astView.setExpression(lab.ast);
        showAst(lab.ast);
    }

    private void showAst(Expr e) {
        if (e == null) { astText.clear(); return; }
        StringBuilder sb = new StringBuilder();
        dump(e, 0, sb);
        astText.setText(sb.toString());
    }

    private static void dump(Expr e, int depth, StringBuilder sb) {
        sb.append("  ".repeat(depth)).append(e.kind()).append("  ::  ").append(e.pretty()).append('\n');
        if (e instanceof eqsat.model.UnaryExpr u) dump(u.operand(), depth + 1, sb);
        else if (e instanceof eqsat.model.BinaryExpr b) { dump(b.left(), depth + 1, sb); dump(b.right(), depth + 1, sb); }
    }

    private static Label section(String t) {
        Label l = new Label(t);
        l.getStyleClass().add("section-title");
        return l;
    }

    private static Button button(String text, String style) {
        Button b = new Button(text);
        b.getStyleClass().add("button");
        b.getStyleClass().add(style);
        return b;
    }

    @Override public void refresh() {
        astView.setDark(lab.themeMode == Theme.Mode.DARK);
    }
}