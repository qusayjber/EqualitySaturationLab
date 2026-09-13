package eqsat.ui;

import eqsat.egraph.EGraph;
import eqsat.extract.*;
import eqsat.model.Expr;
import eqsat.parser.Parser;
import eqsat.rewrite.BuiltInRules;
import eqsat.rewrite.RewriteRule;
import eqsat.saturation.*;

import javafx.scene.Scene;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/** Central application state. Holds the real engine objects; the UI only observes them. */
public final class Lab {

    // ---- document ----
    public String expressionText = "(a + b) + c";
    public Expr ast;
    public EGraph graph;
    public int rootClass = -1;

    // ---- rules ----
    public final List<RewriteRule> rules;
    public final Set<String> enabledRuleNames = new LinkedHashSet<>();

    // ---- engine ----
    public SaturationEngine engine;
    public SaturationLimits limits = SaturationLimits.defaults();

    // ---- extraction ----
    public CostModel costModel = new NodeCountCost();
    public Expr extractedExpr;
    public double extractedCost = Double.NaN;
    public double originalCost = Double.NaN;

    // ---- modes ----
    public boolean educationalMode = true;
    public boolean expertMode = false;
    public boolean animationsOn = true;
    public Theme.Mode themeMode = Theme.Mode.DARK;

    public String lastError = null;

    private Scene scene;
    private final List<Runnable> listeners = new ArrayList<>();

    public Lab() {
        rules = BuiltInRules.defaultRules();
        for (RewriteRule r : rules) enabledRuleNames.add(r.name());
        buildGraph();
    }

    // ------------------------------------------------------------------ observers

    public void onChange(Runnable r) { listeners.add(r); }
    public void fire() { for (Runnable r : listeners) r.run(); }

    // ------------------------------------------------------------------ pipeline

    public List<RewriteRule> enabledRules() {
        List<RewriteRule> out = new ArrayList<>();
        for (RewriteRule r : rules) if (enabledRuleNames.contains(r.name())) out.add(r);
        return out;
    }

    public boolean parseOnly() {
        lastError = null;
        try {
            ast = Parser.parse(expressionText);
            return true;
        } catch (RuntimeException ex) {
            ast = null;
            lastError = ex.getMessage();
            return false;
        }
    }

    public boolean buildGraph() {
        lastError = null;
        try {
            ast = Parser.parse(expressionText);
        } catch (RuntimeException ex) {
            ast = null;
            graph = null;
            rootClass = -1;
            engine = null;
            extractedExpr = null;
            lastError = ex.getMessage();
            fire();
            return false;
        }
        graph = new EGraph();
        rootClass = graph.addExpr(ast);
        graph.rebuild();
        engine = null;
        extractedExpr = null;
        extractedCost = Double.NaN;
        originalCost = CostCalculator.costOf(ast, costModel);
        fire();
        return true;
    }

    public void resetSaturation() {
        if (graph == null) { buildGraph(); return; }
        // Rebuild the graph from the AST so the saturation state is pristine.
        buildGraph();
    }

    public SaturationEngine ensureEngine() {
        if (engine == null) {
            engine = new SaturationEngine(graph, enabledRules(), limits);
        }
        return engine;
    }

    public void extract() {
        if (graph == null) return;
        ExtractionResult res = Extractor.extract(graph, rootClass, costModel);
        extractedExpr = res.expression();
        extractedCost = res.cost();
        if (ast != null) originalCost = CostCalculator.costOf(ast, costModel);
        fire();
    }

    // ------------------------------------------------------------------ theme

    public void attachScene(Scene scene) {
        this.scene = scene;
        applyTheme();
    }

    public void toggleTheme() {
        themeMode = themeMode == Theme.Mode.DARK ? Theme.Mode.LIGHT : Theme.Mode.DARK;
        applyTheme();
        fire();
    }

    public void applyTheme() {
        if (scene == null) return;
        try {
            Path p = Files.createTempFile("eqsat-theme-", ".css");
            Files.writeString(p, Theme.css(themeMode));
            p.toFile().deleteOnExit();
            scene.getStylesheets().setAll(p.toUri().toString());
        } catch (IOException ex) {
            // Styling is cosmetic; never fail the app because of it.
            System.err.println("Could not apply theme: " + ex.getMessage());
        }
    }

    // ------------------------------------------------------------------ helpers

    public String statusText() {
        if (engine == null) return "IDLE";
        return switch (engine.status()) {
            case IDLE -> "IDLE";
            case RUNNING -> "SATURATING…";
            case PAUSED -> "PAUSED";
            case SATURATED -> "SATURATED";
            case LIMIT_REACHED -> "LIMIT REACHED";
            case CANCELLED -> "CANCELLED";
        };
    }

    public int nodeCount()  { return graph == null ? 0 : graph.nodeCount(); }
    public int classCount() { return graph == null ? 0 : graph.classCount(); }
    public int iterations() { return engine == null ? 0 : engine.iteration(); }
    public long rewriteApplications() { return engine == null ? 0 : engine.rewriteApplications(); }
    public long elapsedMillis() { return engine == null ? 0 : engine.elapsedMillis(); }
}