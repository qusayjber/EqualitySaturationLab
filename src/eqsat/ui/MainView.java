package eqsat.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.*;

import java.util.*;

public final class MainView extends BorderPane {

    private final Lab lab;
    private final StackPane content = new StackPane();
    private final Map<String, LabView> views = new LinkedHashMap<>();
    private final Map<String, Button> navButtons = new LinkedHashMap<>();
    private final StatusBar statusBar;

    public MainView(Lab lab) {
        this.lab = lab;
        getStyleClass().add("root-pane");

        register("Overview", new OverviewView(lab));
        register("Expression Editor", new ExpressionEditorView(lab));
        register("Rewrite Rules", new RulesView(lab));
        register("E-Graph", new EGraphView(lab));
        register("Saturation", new SaturationView(lab));
        register("Extraction", new ExtractionView(lab));
        register("Cost Analysis", new CostAnalysisView(lab));
        register("Step-by-Step", new StepByStepView(lab));
        register("Examples", new ExamplesView(lab));
        register("Documentation", new DocumentationView(lab));

        statusBar = new StatusBar(lab);

        setTop(buildHeader());
        setLeft(buildSidebar());
        setCenter(content);
        setBottom(statusBar);

        lab.onChange(() -> {
            for (LabView v : views.values()) v.refresh();
            statusBar.refresh();
        });

        show("Overview");
    }

    private void register(String name, LabView view) { views.put(name, view); }

    // ------------------------------------------------------------------ header

    private HBox buildHeader() {
        Label title = new Label("Equality Saturation Laboratory");
        title.getStyleClass().add("app-title");

        Label subtitle = new Label("Explore E-Graphs, Rewrite Systems & Compiler Optimization");
        subtitle.getStyleClass().add("app-subtitle");

        VBox titles = new VBox(1, title, subtitle);

        CheckBox educational = new CheckBox("Educational Mode");
        educational.setSelected(lab.educationalMode);
        educational.selectedProperty().addListener((o, a, b) -> { lab.educationalMode = b; lab.fire(); });

        CheckBox expert = new CheckBox("Expert Mode");
        expert.setSelected(lab.expertMode);
        expert.selectedProperty().addListener((o, a, b) -> { lab.expertMode = b; lab.fire(); });

        CheckBox anim = new CheckBox("Animations");
        anim.setSelected(lab.animationsOn);
        anim.selectedProperty().addListener((o, a, b) -> lab.animationsOn = b);

        Button theme = new Button("Toggle Theme");
        theme.getStyleClass().addAll("button", "ghost-button");
        theme.setOnAction(e -> lab.toggleTheme());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox right = new HBox(12, educational, expert, anim, theme);
        right.setAlignment(Pos.CENTER_RIGHT);

        HBox header = new HBox(16, titles, spacer, right);
        header.getStyleClass().add("header");
        header.setAlignment(Pos.CENTER_LEFT);
        return header;
    }

    // ------------------------------------------------------------------ sidebar

    private VBox buildSidebar() {
        VBox bar = new VBox(2);
        bar.getStyleClass().add("sidebar");
        bar.setPrefWidth(210);
        bar.setMinWidth(180);

        Label caption = new Label("MODULES");
        caption.getStyleClass().add("sidebar-title");
        bar.getChildren().add(caption);

        for (String name : views.keySet()) {
            Button b = new Button(icon(name) + "  " + name);
            b.getStyleClass().add("nav-button");
            b.setMaxWidth(Double.MAX_VALUE);
            b.setOnAction(e -> show(name));
            navButtons.put(name, b);
            bar.getChildren().add(b);
        }
        return bar;
    }

    private static String icon(String name) {
        return switch (name) {
            case "Overview" -> "◎";
            case "Expression Editor" -> "✎";
            case "Rewrite Rules" -> "⟳";
            case "E-Graph" -> "⬡";
            case "Saturation" -> "∞";
            case "Extraction" -> "★";
            case "Cost Analysis" -> "∑";
            case "Step-by-Step" -> "⏭";
            case "Examples" -> "❏";
            case "Documentation" -> "❓";
            default -> "•";
        };
    }

    // ------------------------------------------------------------------ navigation

    public void show(String name) {
        LabView v = views.get(name);
        if (v == null) return;
        content.getChildren().setAll(v);
        navButtons.forEach((n, b) -> b.getStyleClass().remove("active"));
        Button active = navButtons.get(name);
        if (active != null) active.getStyleClass().add("active");
        v.refresh();
    }

    // ------------------------------------------------------------------ shortcuts

    public void installShortcuts(Scene scene) {
        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.ENTER, KeyCombination.CONTROL_DOWN),
                () -> { if (lab.graph != null) { lab.ensureEngine().runToCompletion(); lab.extract(); lab.fire(); } });

        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN),
                () -> { lab.resetSaturation(); lab.fire(); });

        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN),
                () -> eqsat.util.Exporter.exportReportToFile(lab));

        scene.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case SPACE -> { if (lab.engine != null) lab.engine.resume(); }
                case F -> show("E-Graph");
                default -> { }
            }
        });
    }
}