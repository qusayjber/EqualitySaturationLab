package eqsat.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public final class StatusBar extends HBox implements Refreshable {

    private final Lab lab;

    private final Label expr  = value();
    private final Label eclasses = value();
    private final Label enodes = value();
    private final Label rewrites = value();
    private final Label iters = value();
    private final Label status = value();
    private final Label time = value();

    public StatusBar(Lab lab) {
        this.lab = lab;
        getStyleClass().add("status-bar");
        setSpacing(10);
        setAlignment(Pos.CENTER_LEFT);

        getChildren().addAll(
                caption("EXPRESSION"), expr, sep(),
                caption("E-CLASSES"), eclasses, sep(),
                caption("E-NODES"), enodes, sep(),
                caption("REWRITES"), rewrites, sep(),
                caption("ITERATIONS"), iters, sep(),
                caption("STATUS"), status,
                spacer(),
                caption("TIME"), time);
        refresh();
    }

    @Override public void refresh() {
        expr.setText(lab.expressionText == null ? "—" : shorten(lab.expressionText, 40));
        eclasses.setText(Integer.toString(lab.classCount()));
        enodes.setText(Integer.toString(lab.nodeCount()));
        rewrites.setText(Long.toString(lab.rewriteApplications()));
        iters.setText(Integer.toString(lab.iterations()));
        status.setText(lab.statusText());
        time.setText(lab.elapsedMillis() + " ms");
    }

    private static String shorten(String s, int max) {
        s = s.replaceAll("\\s+", " ").trim();
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }

    private static Label caption(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("status-item");
        l.setPadding(new Insets(0, 0, 0, 4));
        return l;
    }

    private static Label value() {
        Label l = new Label("—");
        l.getStyleClass().add("status-value");
        l.getStyleClass().add("mono");
        return l;
    }

    private static Separator sep() {
        Separator s = new Separator();
        s.setOrientation(javafx.geometry.Orientation.VERTICAL);
        return s;
    }

    private static Region spacer() {
        Region r = new Region();
        HBox.setHgrow(r, Priority.ALWAYS);
        return r;
    }
}