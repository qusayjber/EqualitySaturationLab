package eqsat.ui;

import eqsat.saturation.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public final class StepByStepView extends LabView {

    private final ObservableList<String> steps = FXCollections.observableArrayList();
    private final ListView<String> list = new ListView<>(steps);
    private final TextArea detail = new TextArea();

    public StepByStepView(Lab lab) {
        super(lab, "Step-by-Step",
                "Execute saturation one iteration at a time and inspect exactly which rule matched which E-Class.");

        Button step = new Button("Step");
        step.getStyleClass().addAll("button", "primary-button");
        Button clear = new Button("Clear");
        clear.getStyleClass().addAll("button", "ghost-button");

        step.setOnAction(e -> {
            if (lab.graph == null && !lab.buildGraph()) return;
            SaturationEngine engine = lab.ensureEngine();
            SaturationStep s = engine.step();
            if (s == null) { steps.add("— no further iterations (already saturated / limited) —"); return; }
            steps.add(s.summary());
            list.scrollTo(steps.size() - 1);
            list.getSelectionModel().selectLast();
            showDetail(s);
            lab.fire();
        });

        clear.setOnAction(e -> { steps.clear(); detail.clear(); });

        list.getStyleClass().add("list-view");
        list.getSelectionModel().selectedIndexProperty().addListener((o, a, b) -> {
            int i = b.intValue();
            if (lab.engine == null || i < 0 || i >= lab.engine.history().size()) return;
            showDetail(lab.engine.history().get(i));
        });

        detail.getStyleClass().add("code-area");
        detail.setEditable(false);
        detail.setPrefRowCount(16);

        HBox bar = new HBox(8, step, clear);
        bar.setAlignment(Pos.CENTER_LEFT);

        SplitPane split = new SplitPane(list, detail);
        split.setDividerPositions(0.42);
        VBox.setVgrow(split, Priority.ALWAYS);

        VBox panel = new VBox(10, bar, split);
        panel.getStyleClass().add("panel");
        VBox.setVgrow(panel, Priority.ALWAYS);

        getChildren().add(panel);
        VBox.setVgrow(panel, Priority.ALWAYS);
    }

    private void showDetail(SaturationStep s) {
        StringBuilder sb = new StringBuilder();
        sb.append("Iteration ").append(s.iteration()).append('\n');
        sb.append("Changed        : ").append(s.changed()).append('\n');
        sb.append("E-Nodes        : ").append(s.nodesBefore()).append(" → ").append(s.nodesAfter()).append('\n');
        sb.append("E-Classes      : ").append(s.classesBefore()).append(" → ").append(s.classesAfter()).append('\n');
        sb.append("Merges         : ").append(s.merges()).append('\n');
        sb.append("Elapsed        : ").append(s.elapsedMillis()).append(" ms\n\n");

        if (s.applications().isEmpty()) {
            sb.append("No rewrite rule matched in this iteration.\n");
        } else {
            for (RuleApplication a : s.applications()) {
                sb.append("Rule            : ").append(a.ruleName()).append("   [").append(a.category()).append("]\n");
                sb.append("Matched E-Class : #").append(a.matchedClass()).append('\n');
                sb.append("Result E-Class  : #").append(a.resultClass())
                  .append(a.merged() ? "   (merged)" : "   (already equal)").append('\n');
                if (lab.expertMode) {
                    sb.append("Substitution    :\n").append(a.substitutionText());
                }
                sb.append("Explanation     : ")
                  .append(explain(a)).append("\n\n");
            }
        }
        detail.setText(sb.toString());
    }

    private String explain(RuleApplication a) {
        for (eqsat.rewrite.RewriteRule r : lab.rules) {
            if (r.name().equals(a.ruleName())) {
                return lab.educationalMode
                        ? r.description() + "  (" + r.equationText() + ")"
                        : r.equationText();
            }
        }
        return a.ruleName();
    }

    @Override public void refresh() { }
}