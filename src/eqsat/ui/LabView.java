package eqsat.ui;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public abstract class LabView extends VBox implements Refreshable {

    protected final Lab lab;

    protected LabView(Lab lab, String title, String subtitle) {
        this.lab = lab;
        getStyleClass().add("view");
        setSpacing(12);
        setPadding(new Insets(18, 20, 18, 20));

        Label t = new Label(title);
        t.getStyleClass().add("view-title");
        Label s = new Label(subtitle);
        s.getStyleClass().add("view-subtitle");
        s.setWrapText(true);
        getChildren().addAll(t, s);
    }

    @Override public void refresh() { }
}