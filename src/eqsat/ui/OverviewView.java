package eqsat.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

public final class OverviewView extends LabView {

    private final Label eClasses = big();
    private final Label eNodes = big();
    private final Label rewrites = big();
    private final Label iters = big();
    private final Label cost = big();
    private final Label status = big();

    public OverviewView(Lab lab) {
        super(lab, "Overview",
                "Equality saturation delays optimisation decisions. Instead of repeatedly transforming one expression, "
              + "it maintains many equivalent forms inside an E-Graph and extracts the cheapest one after saturation.");

        TilePane cards = new TilePane(12, 12);
        cards.setPrefColumns(3);
        cards.setPadding(new Insets(4, 0, 4, 0));
        cards.getChildren().addAll(
                card("E-CLASSES", eClasses, "Equivalence classes in the E-Graph"),
                card("E-NODES", eNodes, "Distinct canonical nodes"),
                card("REWRITE APPLICATIONS", rewrites, "Rule instances applied"),
                card("ITERATIONS", iters, "Saturation iterations executed"),
                card("BEST COST", cost, "Cost of the extracted expression"),
                card("SATURATION STATUS", status, "Current engine state"));

        VBox pipeline = new VBox(4);
        pipeline.getStyleClass().add("panel");
        Label pt = new Label("Pipeline");
        pt.getStyleClass().add("section-title");
        pipeline.getChildren().add(pt);
        for (String step : new String[]{
                "Expression", "AST", "E-Graph", "Rewrite Rules",
                "Equality Saturation", "Cost Analysis", "Best Expression"}) {
            Label l = new Label(step);
            l.getStyleClass().add("mono");
            l.setPadding(new Insets(4, 0, 4, 0));
            pipeline.getChildren().add(l);
            if (!step.equals("Best Expression")) {
                Label arrow = new Label("↓");
                arrow.getStyleClass().add("muted-label");
                pipeline.getChildren().add(arrow);
            }
        }

        VBox why = new VBox(8);
        why.getStyleClass().add("panel");
        Label wt = new Label("Why equality saturation?");
        wt.getStyleClass().add("section-title");
        Label body = new Label("""
                A traditional optimiser rewrites the expression step by step and commits to each decision. \
                An early rewrite can destroy a path to a better final program.

                Equality saturation keeps every equivalent form alive at once. Rewrites only ever add information \
                (they merge equivalence classes); nothing is discarded. After the E-Graph stops growing, a cost model \
                picks the cheapest representative.""");
        body.setWrapText(true);
        body.getStyleClass().add("muted-label");
        why.getChildren().addAll(wt, body);

        HBox row = new HBox(12, pipeline, why);
        HBox.setHgrow(why, Priority.ALWAYS);

        ScrollPaneShim scroll = new ScrollPaneShim(cards, row);
        getChildren().add(scroll);
        VBox.setVgrow(scroll, Priority.ALWAYS);
    }

    private static final class ScrollPaneShim extends javafx.scene.control.ScrollPane {
        ScrollPaneShim(javafx.scene.Node a, javafx.scene.Node b) {
            VBox box = new VBox(14, a, b);
            box.setFillWidth(true);
            setContent(box);
            setFitToWidth(true);
            getStyleClass().add("scroll-pane");
        }
    }

    private static Label big() {
        Label l = new Label("0");
        l.getStyleClass().addAll("card-value", "mono");
        return l;
    }

    private static VBox card(String title, Label value, String hint) {
        VBox box = new VBox(4, title(title), value, hint(hint));
        box.getStyleClass().add("card");
        box.setPrefWidth(230);
        box.setMinWidth(200);
        return box;
    }

    private static Label title(String t) {
        Label l = new Label(t);
        l.getStyleClass().add("card-title");
        return l;
    }

    private static Label hint(String t) {
        Label l = new Label(t);
        l.getStyleClass().add("card-hint");
        l.setWrapText(true);
        return l;
    }

    @Override public void refresh() {
        eClasses.setText(Integer.toString(lab.classCount()));
        eNodes.setText(Integer.toString(lab.nodeCount()));
        rewrites.setText(Long.toString(lab.rewriteApplications()));
        iters.setText(Integer.toString(lab.iterations()));
        cost.setText(Double.isNaN(lab.extractedCost) ? "—" : trim(lab.extractedCost));
        status.setText(lab.statusText());
    }

    private static String trim(double d) {
        if (d == Math.rint(d)) return Long.toString((long) d);
        return String.format("%.2f", d);
    }
}