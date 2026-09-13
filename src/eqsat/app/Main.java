package eqsat.app;

import eqsat.ui.Lab;
import eqsat.ui.MainView;

import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public final class Main extends Application {

    @Override
    public void start(Stage stage) {

        stage.initStyle(StageStyle.DECORATED);

        stage.setResizable(true);
        stage.setMaximized(true);

        Lab lab = new Lab();
        MainView root = new MainView(lab);

        Scene scene = new Scene(root, 1440, 900);
        lab.attachScene(scene);
        root.installShortcuts(scene);

        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.M, KeyCombination.CONTROL_DOWN),
                () -> stage.setMaximized(!stage.isMaximized()));

        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.W, KeyCombination.CONTROL_DOWN),
                () -> stage.setIconified(true));

        stage.setTitle("Equality Saturation Laboratory");
        stage.setScene(scene);

        stage.setMinWidth(1120);
        stage.setMinHeight(720);

        Rectangle2D visual = Screen.getPrimary().getVisualBounds();
        if (visual.getWidth()  < 1440) stage.setWidth(visual.getWidth());
        if (visual.getHeight() < 900)  stage.setHeight(visual.getHeight());

        stage.centerOnScreen();

        stage.show();

        lab.ensureEngine().runToCompletion();
        lab.extract();
        lab.fire();
    }

    public static void main(String[] args) {
        launch(args);
    }
}