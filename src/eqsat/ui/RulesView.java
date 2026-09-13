package eqsat.ui;

import eqsat.rewrite.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.LinkedHashMap;
import java.util.Map;

public final class RulesView extends LabView {

    private final Map<String, CheckBox> boxes = new LinkedHashMap<>();
    private final VBox list = new VBox(6);

    public RulesView(Lab lab) {
        super(lab, "Rewrite Rules",
                "Enable or disable the rewrite system. Conditional rules require assumptions and are flagged.");

        for (RewriteRule r : lab.rules) {
            list.getChildren().add(ruleRow(r));
        }

        Button all = new Button("Enable All");
        Button none = new Button("Disable All");
        Button reset = new Button("Reset Defaults");
        for (Button b : new Button[]{all, none, reset}) { b.getStyleClass().addAll("button", "ghost-button"); }

        all.setOnAction(e -> { for (CheckBox c : boxes.values()) c.setSelected(true); sync(); });
        none.setOnAction(e -> { for (CheckBox c : boxes.values()) c.setSelected(false); sync(); });
        reset.setOnAction(e -> {
            for (RewriteRule r : lab.rules) boxes.get(r.name()).setSelected(true);
            sync();
        });

        HBox actions = new HBox(8, all, none, reset);
        actions.setAlignment(Pos.CENTER_LEFT);

        VBox panel = new VBox(10, actions, list);
        panel.getStyleClass().add("panel");

        ScrollPane sp = new ScrollPane(panel);
        sp.setFitToWidth(true);
        sp.getStyleClass().add("scroll-pane");
        getChildren().add(sp);
        VBox.setVgrow(sp, Priority.ALWAYS);
    }

    private VBox ruleRow(RewriteRule r) {
        CheckBox cb = new CheckBox();
        cb.setSelected(lab.enabledRuleNames.contains(r.name()));
        cb.selectedProperty().addListener((o, a, b) -> sync());
        boxes.put(r.name(), cb);

        Label name = new Label(r.name());
        name.getStyleClass().addAll("section-title", "mono");

        Label equation = new Label(r.equationText());
        equation.getStyleClass().addAll("mono", "muted-label");

        Label desc = new Label(r.description());
        desc.getStyleClass().add("muted-label");
        desc.setWrapText(true);

        Label safety = new Label(r.safety().label());
        safety.getStyleClass().addAll("pill", switch (r.safety()) {
            case SAFE -> "pill-safe";
            case CONDITIONAL -> "pill-conditional";
            case ASSUMPTION -> "pill-assumption";
        });

        Label cat = new Label(r.category().name());
        cat.getStyleClass().addAll("pill", "pill-neutral");

        Label warning = new Label(r.safety().explanation());
        warning.getStyleClass().add("warn-label");
        warning.setWrapText(true);
        warning.setVisible(r.safety() != RuleSafety.SAFE);
        warning.setManaged(r.safety() != RuleSafety.SAFE);

        HBox top = new HBox(8, cb, name, cat, safety);
        top.setAlignment(Pos.CENTER_LEFT);

        VBox box = new VBox(4, top, equation, desc, warning);
        box.getStyleClass().add("card");
        box.setPadding(new Insets(10, 12, 10, 12));
        return box;
    }

    private void sync() {
        lab.enabledRuleNames.clear();
        boxes.forEach((name, cb) -> { if (cb.isSelected()) lab.enabledRuleNames.add(name); });
        lab.engine = null;   // rules changed → next run builds a fresh engine
        lab.fire();
    }
}