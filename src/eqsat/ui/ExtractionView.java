package eqsat.ui;

import eqsat.extract.*;
import eqsat.model.Expr;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public final class ExtractionView extends LabView {

    private final ComboBox<String> modelBox = new ComboBox<>();
    private final Label original = mono("—");
    private final Label optimized = mono("—");
    private final Label originalCost = value("—");
    private final Label optimizedCost = value("—");
    private final Label savings = value("—");
    private final TextArea explanation = new TextArea();

    public ExtractionView(Lab lab) {
        super(lab, "Extraction",
                "After saturation, the extractor picks the cheapest expression represented by the root E-Class "
              + "under the selected cost model.");

        modelBox.getItems().addAll("AST Node Count", "Operator Count", "Weighted Cost", "Depth");
        modelBox.setValue("AST Node Count");
        modelBox.valueProperty().addListener((o, a, b) -> {
            lab.costModel = switch (b) {
                case "Operator Count" -> new OperatorCost();
                case "Weighted Cost"  -> new WeightedCost();
                case "Depth"          -> new DepthCost();
                default               -> new NodeCountCost();
            };
            lab.extract();
        });

        Button extract = new Button("Extract Best Expression");
        extract.getStyleClass().addAll("button", "primary-button");
        extract.setOnAction(e -> { lab.extract(); });

        HBox controls = new HBox(10, new Label("Cost model"), modelBox, extract);
        controls.setAlignment(Pos.CENTER_LEFT);

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(10);
        grid.add(caption("ORIGINAL"), 0, 0);
        grid.add(original, 1, 0);
        grid.add(caption("BEST EXPRESSION"), 0, 1);
        grid.add(optimized, 1, 1);
        grid.add(caption("ORIGINAL COST"), 0, 2);
        grid.add(originalCost, 1, 2);
        grid.add(caption("OPTIMIZED COST"), 0, 3);
        grid.add(optimizedCost, 1, 3);
        grid.add(caption("SAVINGS"), 0, 4);
        grid.add(savings, 1, 4);

        explanation.getStyleClass().add("code-area");
        explanation.setEditable(false);
        explanation.setPrefRowCount(8);

        VBox panel = new VBox(12, controls, grid, section("Report"), explanation);
        panel.getStyleClass().add("panel");
        VBox.setVgrow(explanation, Priority.ALWAYS);

        getChildren().add(panel);
        VBox.setVgrow(panel, Priority.ALWAYS);
    }

    @Override public void refresh() {
        Expr in = lab.ast;
        original.setText(in == null ? "—" : in.pretty());
        optimized.setText(lab.extractedExpr == null ? "—" : lab.extractedExpr.pretty());

        originalCost.setText(Double.isNaN(lab.originalCost) ? "—" : fmt(lab.originalCost));
        optimizedCost.setText(Double.isNaN(lab.extractedCost) ? "—" : fmt(lab.extractedCost));

        if (!Double.isNaN(lab.originalCost) && !Double.isNaN(lab.extractedCost) && lab.originalCost > 0) {
            double pct = (lab.originalCost - lab.extractedCost) / lab.originalCost * 100.0;
            savings.setText(String.format("%.1f%%", pct));
        } else {
            savings.setText("—");
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Input expression   : ").append(in == null ? "—" : in.pretty()).append('\n');
        sb.append("Final expression   : ").append(lab.extractedExpr == null ? "—" : lab.extractedExpr.pretty()).append('\n');
        sb.append("Rules enabled      : ").append(lab.enabledRules().size()).append('\n');
        sb.append("Iterations         : ").append(lab.iterations()).append('\n');
        sb.append("E-Classes          : ").append(lab.classCount()).append('\n');
        sb.append("E-Nodes            : ").append(lab.nodeCount()).append('\n');
        sb.append("Rewrite applications: ").append(lab.rewriteApplications()).append('\n');
        sb.append("Original cost      : ").append(fmt(lab.originalCost)).append('\n');
        sb.append("Final cost         : ").append(fmt(lab.extractedCost)).append('\n');
        sb.append("Execution time     : ").append(lab.elapsedMillis()).append(" ms\n");
        explanation.setText(sb.toString());
    }

    private static String fmt(double d) {
        if (Double.isNaN(d)) return "—";
        if (d == Math.rint(d)) return Long.toString((long) d);
        return String.format("%.3f", d);
    }

    private static Label mono(String t) {
        Label l = new Label(t);
        l.getStyleClass().addAll("mono", "section-title");
        return l;
    }

    private static Label value(String t) {
        Label l = new Label(t);
        l.getStyleClass().addAll("mono", "card-value");
        l.setStyle("-fx-font-size: 16px;");
        return l;
    }

    private static Label caption(String t) {
        Label l = new Label(t);
        l.getStyleClass().add("card-title");
        return l;
    }

    private static Label section(String t) {
        Label l = new Label(t);
        l.getStyleClass().add("section-title");
        return l;
    }
}