package eqsat.ui;

import eqsat.extract.*;
import eqsat.model.Expr;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.util.*;

public final class CostAnalysisView extends LabView {

    public static final class Row {
        private final String expression;
        private final double cost;
        private final int size;
        private final int depth;

        Row(String expression, double cost, int size, int depth) {
            this.expression = expression; this.cost = cost; this.size = size; this.depth = depth;
        }
        public String getExpression() { return expression; }
        public double getCost() { return cost; }
        public int getSize() { return size; }
        public int getDepth() { return depth; }
    }

    private final ObservableList<Row> rows = FXCollections.observableArrayList();
    private final TableView<Row> table = new TableView<>(rows);
    private final ComboBox<String> sortBox = new ComboBox<>();
    private final Spinner<Integer> limit = new Spinner<>(10, 2000, 200, 10);

    public CostAnalysisView(Lab lab) {
        super(lab, "Cost Analysis",
                "All expressions represented by the root E-Class, ranked by the active cost model.");

        TableColumn<Row, String> exprCol = new TableColumn<>("Expression");
        exprCol.setCellValueFactory(new PropertyValueFactory<>("expression"));
        exprCol.setPrefWidth(420);

        TableColumn<Row, Double> costCol = new TableColumn<>("Cost");
        costCol.setCellValueFactory(new PropertyValueFactory<>("cost"));
        costCol.setPrefWidth(100);

        TableColumn<Row, Integer> sizeCol = new TableColumn<>("Nodes");
        sizeCol.setCellValueFactory(new PropertyValueFactory<>("size"));
        sizeCol.setPrefWidth(80);

        TableColumn<Row, Integer> depthCol = new TableColumn<>("Depth");
        depthCol.setCellValueFactory(new PropertyValueFactory<>("depth"));
        depthCol.setPrefWidth(80);

        table.getColumns().addAll(List.of(exprCol, costCol, sizeCol, depthCol));
        table.getStyleClass().add("table-view");
        table.setPlaceholder(new Label("No candidates — build an E-Graph first."));

        sortBox.getItems().addAll("Lowest Cost", "Highest Cost", "Shortest", "Lowest Depth");
        sortBox.setValue("Lowest Cost");
        sortBox.valueProperty().addListener((o, a, b) -> recompute());

        Button refresh = new Button("Refresh");
        refresh.getStyleClass().addAll("button", "ghost-button");
        refresh.setOnAction(e -> recompute());

        HBox bar = new HBox(10, new Label("Sort"), sortBox, new Label("Max results"), limit, refresh);
        bar.setAlignment(Pos.CENTER_LEFT);

        VBox panel = new VBox(10, bar, table);
        panel.getStyleClass().add("panel");
        VBox.setVgrow(table, Priority.ALWAYS);

        getChildren().add(panel);
        VBox.setVgrow(panel, Priority.ALWAYS);
    }

    private void recompute() {
        rows.clear();
        if (lab.graph == null) return;
        List<Expr> candidates = ExpressionEnumerator.enumerate(lab.graph, lab.rootClass, limit.getValue(), 6);

        List<Row> out = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (Expr e : candidates) {
            String text = e.pretty();
            if (!seen.add(text)) continue;
            out.add(new Row(text, CostCalculator.costOf(e, lab.costModel),
                    eqsat.model.Exprs.size(e), eqsat.model.Exprs.depth(e)));
        }

        switch (sortBox.getValue()) {
            case "Highest Cost" -> out.sort(Comparator.comparingDouble(Row::getCost).reversed());
            case "Shortest"     -> out.sort(Comparator.comparingInt(Row::getSize));
            case "Lowest Depth" -> out.sort(Comparator.comparingInt(Row::getDepth));
            default             -> out.sort(Comparator.comparingDouble(Row::getCost));
        }
        rows.setAll(out);
    }

    @Override public void refresh() { /* populated on demand to avoid heavy recomputation */ }
}