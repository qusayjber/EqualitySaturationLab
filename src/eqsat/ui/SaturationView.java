package eqsat.ui;

import eqsat.saturation.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public final class SaturationView extends LabView {

    private final ComboBox<Integer> maxIterations = new ComboBox<>();
    private final ComboBox<Integer> maxNodes = new ComboBox<>();
    private final Label statusLabel = new Label("IDLE");
    private final ProgressIndicator progress = new ProgressIndicator();
    private final ObservableList<String> history = FXCollections.observableArrayList();
    private final ListView<String> historyList = new ListView<>(history);

    private volatile boolean paused = false;
    private Task<SaturationResult> task;

    public SaturationView(Lab lab) {
        super(lab, "Saturation",
                "The engine applies every enabled rule to every matching E-Class, merges the results, "
              + "then runs congruence closure. It stops at a fixpoint or at a configured limit.");

        maxIterations.getItems().addAll(10, 25, 50, 100, 500);
        maxIterations.setValue(25);
        maxNodes.getItems().addAll(100, 500, 1_000, 5_000, 10_000);
        maxNodes.setValue(5_000);

        Button run = btn("Run", "primary-button");
        Button pause = btn("Pause", "ghost-button");
        Button resume = btn("Resume", "ghost-button");
        Button step = btn("Step", "ghost-button");
        Button reset = btn("Reset", "ghost-button");

        progress.setPrefSize(28, 28);
        progress.setVisible(false);

        statusLabel.getStyleClass().addAll("pill", "pill-neutral");

        run.setOnAction(e -> startRun());
        pause.setOnAction(e -> { paused = true; if (lab.engine != null) lab.engine.pause(); updateStatus(); });
        resume.setOnAction(e -> { paused = false; if (lab.engine != null) lab.engine.resume(); updateStatus(); });
        step.setOnAction(e -> {
            if (lab.graph == null) lab.buildGraph();
            lab.ensureEngine();
            SaturationStep s = lab.engine.step();
            if (s != null) history.add(s.summary());
            historyList.scrollTo(history.size() - 1);
            lab.fire();
        });
        reset.setOnAction(e -> {
            cancelTask();
            history.clear();
            lab.resetSaturation();
            paused = false;
            updateStatus();
            lab.fire();
        });

        HBox controls = new HBox(8, run, pause, resume, step, reset, progress, statusLabel);
        controls.setAlignment(Pos.CENTER_LEFT);

        HBox limits = new HBox(10,
                new Label("Max iterations"), maxIterations,
                new Label("Max E-Nodes"), maxNodes);
        limits.setAlignment(Pos.CENTER_LEFT);

        historyList.getStyleClass().add("list-view");
        historyList.setPlaceholder(new Label("No iterations executed yet."));

        VBox panel = new VBox(12, controls, limits, section("Iteration history"), historyList);
        panel.getStyleClass().add("panel");
        VBox.setVgrow(historyList, Priority.ALWAYS);
        VBox.setVgrow(panel, Priority.ALWAYS);

        getChildren().add(panel);
        VBox.setVgrow(panel, Priority.ALWAYS);

        maxIterations.valueProperty().addListener((o, a, b) -> rebuildLimits());
        maxNodes.valueProperty().addListener((o, a, b) -> rebuildLimits());
    }

    private void rebuildLimits() {
        lab.limits = SaturationLimits.of(maxIterations.getValue(), maxNodes.getValue());
        lab.engine = null;
    }

    private void startRun() {
        if (task != null && task.isRunning()) return;
        if (lab.graph == null && !lab.buildGraph()) return;

        rebuildLimits();
        lab.engine = new SaturationEngine(lab.graph, lab.enabledRules(), lab.limits);
        paused = false;
        progress.setVisible(true);
        history.clear();

        final SaturationEngine engine = lab.engine;

        task = new Task<>() {
            @Override protected SaturationResult call() throws Exception {
                while (!isCancelled()) {
                    while (paused) {
                        Thread.sleep(40);
                        if (isCancelled()) return null;
                    }
                    SaturationStep s = engine.step();
                    if (s != null) {
                        Platform.runLater(() -> {
                            history.add(s.summary());
                            historyList.scrollTo(history.size() - 1);
                            lab.fire();
                        });
                        if (!s.changed()) break;
                    } else break;
                    if (engine.limitReached()) break;
                    Thread.sleep(lab.animationsOn ? 90 : 0);
                }
                return engine.result();
            }
        };

        task.setOnSucceeded(e -> {
            progress.setVisible(false);
            lab.extract();
            updateStatus();
            lab.fire();
        });
        task.setOnCancelled(e -> { progress.setVisible(false); updateStatus(); });
        task.setOnFailed(e -> {
            progress.setVisible(false);
            statusLabel.setText("ERROR");
            if (task.getException() != null) task.getException().printStackTrace();
        });

        Thread t = new Thread(task, "saturation-worker");
        t.setDaemon(true);
        t.start();
        updateStatus();
    }

    private void cancelTask() {
        if (task != null && task.isRunning()) task.cancel(true);
    }

    private void updateStatus() {
        statusLabel.setText(lab.statusText());
        statusLabel.getStyleClass().removeAll("pill-safe", "pill-conditional", "pill-accent", "pill-neutral");
        String s = lab.statusText();
        if (s.equals("SATURATED")) statusLabel.getStyleClass().add("pill-safe");
        else if (s.equals("SATURATING…") || s.equals("RUNNING")) statusLabel.getStyleClass().add("pill-accent");
        else if (s.equals("LIMIT REACHED")) statusLabel.getStyleClass().add("pill-conditional");
        else statusLabel.getStyleClass().add("pill-neutral");
    }

    @Override public void refresh() { updateStatus(); }

    private static Button btn(String t, String style) {
        Button b = new Button(t);
        b.getStyleClass().addAll("button", style);
        return b;
    }

    private static Label section(String t) {
        Label l = new Label(t);
        l.getStyleClass().add("section-title");
        return l;
    }
}