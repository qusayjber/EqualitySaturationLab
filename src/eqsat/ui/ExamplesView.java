package eqsat.ui;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public final class ExamplesView extends LabView {

    private record Example(String expression, String title, String description,
                           String interesting, String costModel) {}

    private static final Example[] EXAMPLES = {
        new Example("(a + b) + c", "Associativity playground",
            "Three operands added together. With associativity + commutativity the E-Graph contains every regrouping and every permutation.",
            "AddAssociativityRight/Left, AddCommutativity", "AST Node Count"),
        new Example("a + 0", "Additive identity",
            "The canonical identity-elimination example.",
            "AddZeroRight", "AST Node Count"),
        new Example("a * 1", "Multiplicative identity",
            "Multiplying by one should disappear.",
            "MulOneRight", "AST Node Count"),
        new Example("a * (b + c)", "Distributivity",
            "Two equivalent normal forms exist; the cost model decides which wins.",
            "Distributivity, Factorization, MulCommutativity", "Weighted Cost"),
        new Example("(a * b) + (a * c)", "Factoring",
            "The factored form is usually cheaper under a weighted model.",
            "Factorization, Distributivity", "Weighted Cost"),
        new Example("(x + 0) * 1", "Nested identities",
            "Both identities have to fire before the expression collapses to x.",
            "AddZeroRight, MulOneRight, MulCommutativity", "AST Node Count"),
        new Example("((a + 0) + b) + c", "Deep simplification",
            "The zero is buried three levels deep; saturation finds it.",
            "AddZeroRight, AddAssociativity*, AddCommutativity", "AST Node Count"),
        new Example("(a * 1) + (b * 0)", "Annihilator + identity",
            "b * 0 collapses to 0 and then a * 1 collapses to a.",
            "MulOneRight, MulZeroRight, AddZeroRight", "AST Node Count"),
        new Example("(x + y) + (z + 0)", "Mixed regrouping",
            "Associativity exposes the identity.",
            "AddAssociativity*, AddZeroRight, AddCommutativity", "AST Node Count"),
        new Example("a * (b + c) + 0", "Distribute then simplify",
            "Zero elimination plus distributivity interact.",
            "Distributivity, AddZeroRight, Factorization", "Weighted Cost"),
        new Example("x * x", "Strength reduction",
            "x*x can become x^2 (or vice versa) depending on operator weights.",
            "MulTwoToPow, PowTwoToMul", "Weighted Cost"),
        new Example("x / x", "Conditional rule",
            "Only valid when x != 0 — the UI flags this rule as CONDITIONAL.",
            "DivSelf (conditional)", "AST Node Count")
    };

    public ExamplesView(Lab lab) {
        super(lab, "Examples Library",
                "Curated inputs with the rules that matter and the cost model that shows them off best.");

        VBox list = new VBox(10);
        for (Example ex : EXAMPLES) list.getChildren().add(card(ex));

        ScrollPane sp = new ScrollPane(list);
        sp.setFitToWidth(true);
        sp.getStyleClass().add("scroll-pane");
        getChildren().add(sp);
        VBox.setVgrow(sp, Priority.ALWAYS);
    }

    private VBox card(Example ex) {
        Label title = new Label(ex.title());
        title.getStyleClass().add("section-title");

        Label expr = new Label(ex.expression());
        expr.getStyleClass().addAll("mono", "pill", "pill-accent");

        Label desc = new Label(ex.description());
        desc.getStyleClass().add("muted-label");
        desc.setWrapText(true);

        Label rules = new Label("Interesting rules: " + ex.interesting());
        rules.getStyleClass().add("muted-label");
        rules.setWrapText(true);

        Label model = new Label("Recommended cost model: " + ex.costModel());
        model.getStyleClass().add("muted-label");

        Button load = new Button("Load");
        load.getStyleClass().addAll("button", "ghost-button");
        load.setOnAction(e -> {
            lab.expressionText = ex.expression();
            lab.costModel = switch (ex.costModel()) {
                case "Weighted Cost" -> new eqsat.extract.WeightedCost();
                case "Operator Count" -> new eqsat.extract.OperatorCost();
                case "Depth" -> new eqsat.extract.DepthCost();
                default -> new eqsat.extract.NodeCountCost();
            };
            lab.buildGraph();
            lab.fire();
        });

        Button loadAndRun = new Button("Load & Saturate");
        loadAndRun.getStyleClass().addAll("button", "primary-button");
        loadAndRun.setOnAction(e -> {
            lab.expressionText = ex.expression();
            lab.buildGraph();
            lab.ensureEngine().runToCompletion();
            lab.extract();
            lab.fire();
        });

        HBox actions = new HBox(8, load, loadAndRun);

        VBox box = new VBox(6, new HBox(10, title, expr), desc, rules, model, actions);
        box.getStyleClass().add("card");
        box.setPadding(new Insets(12, 14, 12, 14));
        return box;
    }

    @Override public void refresh() { }
}