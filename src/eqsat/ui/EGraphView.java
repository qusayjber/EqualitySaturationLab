package eqsat.ui;

import eqsat.egraph.EClass;
import eqsat.egraph.EGraph;
import eqsat.egraph.ENode;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.*;

/** Interactive E-Graph canvas: zoom, pan, fit, select, drag. */
public final class EGraphView extends LabView {

    private final Canvas canvas = new Canvas(900, 600);
    private final Pane canvasPane = new Pane(canvas);
    private final Label info = new Label("—");
    private final TextField search = new TextField();

    private double scale = 1.0;
    private double tx = 0, ty = 0;
    private double dragStartX, dragStartY, txStart, tyStart;
    private int selected = -1;

    private final Map<Integer, double[]> layout = new LinkedHashMap<>();
    private List<EClass> snapshot = List.of();
    private String filter = "";

    public EGraphView(Lab lab) {
        super(lab, "E-Graph",
                "Every equivalence class groups E-Nodes that are known to be equal. "
              + "Congruence closure merges classes whose parents become identical.");

        canvasPane.setMinHeight(420);
        VBox.setVgrow(canvasPane, Priority.ALWAYS);
        canvas.widthProperty().bind(canvasPane.widthProperty());
        canvas.heightProperty().bind(canvasPane.heightProperty());
        canvas.widthProperty().addListener((o, a, b) -> draw());
        canvas.heightProperty().addListener((o, a, b) -> draw());

        canvasPane.setOnScroll(e -> {
            double factor = e.getDeltaY() > 0 ? 1.1 : 0.9;
            double newScale = Math.max(0.15, Math.min(6.0, scale * factor));
            double mx = e.getX(), my = e.getY();
            tx = mx - (mx - tx) * (newScale / scale);
            ty = my - (my - ty) * (newScale / scale);
            scale = newScale;
            draw();
        });

        canvasPane.setOnMousePressed(e -> {
            dragStartX = e.getX(); dragStartY = e.getY();
            txStart = tx; tyStart = ty;
            int hit = hitTest(e.getX(), e.getY());
            if (hit >= 0) { selected = hit; }
            else if (e.getButton() == javafx.scene.input.MouseButton.SECONDARY) selected = -1;
            draw();
        });

        canvasPane.setOnMouseDragged(e -> {
            tx = txStart + (e.getX() - dragStartX);
            ty = tyStart + (e.getY() - dragStartY);
            draw();
        });

        Button fit = new Button("Fit");
        Button reset = new Button("Reset View");
        Button zoomIn = new Button("+");
        Button zoomOut = new Button("−");
        for (Button b : new Button[]{fit, reset, zoomIn, zoomOut})
            b.getStyleClass().addAll("button", "ghost-button");

        zoomIn.setOnAction(e -> { scale = Math.min(6, scale * 1.2); draw(); });
        zoomOut.setOnAction(e -> { scale = Math.max(0.15, scale / 1.2); draw(); });
        fit.setOnAction(e -> fitToScreen());
        reset.setOnAction(e -> { scale = 1; tx = 0; ty = 0; draw(); });

        search.setPromptText("Filter: e.g. Add, Var, Const, #12");
        search.getStyleClass().add("text-field");
        search.textProperty().addListener((o, a, b) -> { filter = b == null ? "" : b.trim(); draw(); });

        info.getStyleClass().add("muted-label");

        HBox toolbar = new HBox(8, new Label("Search"), search, fit, zoomIn, zoomOut, reset);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(search, Priority.ALWAYS);

        VBox panel = new VBox(8, toolbar, canvasPane, info);
        panel.getStyleClass().add("panel");
        VBox.setVgrow(panel, Priority.ALWAYS);

        getChildren().add(panel);
        VBox.setVgrow(panel, Priority.ALWAYS);
    }

    // ---------------------------------------------------------------- layout

    private void computeLayout() {
        layout.clear();
        EGraph g = lab.graph;
        if (g == null) { snapshot = List.of(); return; }
        snapshot = g.classes();
        if (snapshot.isEmpty()) return;

        Map<Integer, Integer> depth = new HashMap<>();
        Deque<Integer> queue = new ArrayDeque<>();
        int root = g.find(lab.rootClass);
        depth.put(root, 0);
        queue.add(root);
        while (!queue.isEmpty()) {
            int c = queue.poll();
            int d = depth.get(c);
            EClass ec = find(c);
            if (ec == null) continue;
            for (ENode n : ec.nodes()) {
                for (int ch : n.children()) {
                    int r = g.find(ch);
                    if (!depth.containsKey(r) || depth.get(r) < d + 1) {
                        depth.put(r, d + 1);
                        queue.add(r);
                    }
                }
            }
        }
        for (EClass c : snapshot) depth.putIfAbsent(c.id(), 0);

        Map<Integer, List<Integer>> byDepth = new TreeMap<>();
        for (EClass c : snapshot) byDepth.computeIfAbsent(depth.get(c.id()), k -> new ArrayList<>()).add(c.id());

        double cellW = 240, cellH = 150;
        for (Map.Entry<Integer, List<Integer>> e : byDepth.entrySet()) {
            List<Integer> row = e.getValue();
            Collections.sort(row);
            for (int i = 0; i < row.size(); i++) {
                double x = 80 + i * cellW;
                double y = 60 + e.getKey() * cellH;
                layout.put(row.get(i), new double[]{x, y});
            }
        }
    }

    private EClass find(int id) {
        for (EClass c : snapshot) if (c.id() == id) return c;
        return null;
    }

    private void fitToScreen() {
        computeLayout();
        if (layout.isEmpty()) { draw(); return; }
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE, maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE;
        for (double[] p : layout.values()) {
            minX = Math.min(minX, p[0]); minY = Math.min(minY, p[1]);
            maxX = Math.max(maxX, p[0] + 220); maxY = Math.max(maxY, p[1] + 120);
        }
        double w = Math.max(1, canvas.getWidth()), h = Math.max(1, canvas.getHeight());
        scale = Math.min(w / (maxX - minX + 60), h / (maxY - minY + 60));
        scale = Math.max(0.15, Math.min(2.2, scale));
        tx = (w - (maxX - minX) * scale) / 2 - minX * scale;
        ty = (h - (maxY - minY) * scale) / 2 - minY * scale;
        draw();
    }

    // ---------------------------------------------------------------- drawing

    private int hitTest(double px, double py) {
        for (Map.Entry<Integer, double[]> e : layout.entrySet()) {
            double[] p = e.getValue();
            double x = p[0] * scale + tx;
            double y = p[1] * scale + ty;
            double w = 220 * scale, h = (60 + 22 * nodeCountOf(e.getKey())) * scale;
            if (px >= x && px <= x + w && py >= y && py <= y + h) return e.getKey();
        }
        return -1;
    }

    private int nodeCountOf(int id) {
        EClass c = find(id);
        return c == null ? 1 : c.size();
    }

    private void draw() {
        if (!isVisible()) return;
        GraphicsContext g = canvas.getGraphicsContext2D();
        double w = canvas.getWidth(), h = canvas.getHeight();
        g.clearRect(0, 0, w, h);

        boolean dark = lab.themeMode == Theme.Mode.DARK;
        g.setFill(dark ? Color.web("#0d1117") : Color.web("#f2f4f7"));
        g.fillRect(0, 0, w, h);

        computeLayout();
        if (snapshot.isEmpty()) {
            g.setFill(dark ? Color.web("#8b97a6") : Color.web("#5b6673"));
            g.setFont(Font.font("Consolas", 12.5));
            g.fillText("No E-Graph yet — build one from the Expression Editor.", 18, 30);
            info.setText("0 classes / 0 nodes");
            return;
        }

        Color edge    = dark ? Color.web("#3a4757") : Color.web("#b9c2cd");
        Color cardBg  = dark ? Color.web("#141a22") : Color.web("#ffffff");
        Color cardBd  = dark ? Color.web("#26313d") : Color.web("#d8dee6");
        Color text    = dark ? Color.web("#e6edf3") : Color.web("#1b232c");
        Color muted   = dark ? Color.web("#8b97a6") : Color.web("#5b6673");
        Color accent  = dark ? Color.web("#5aa9ff") : Color.web("#0b62d0");
        Color selBg   = dark ? Color.web("#12293f") : Color.web("#dce9fb");
        Color rootBd  = dark ? Color.web("#6ee7a8") : Color.web("#0f7a45");
        Color match   = dark ? Color.web("#ffb454") : Color.web("#9a5b00");

        EGraph graph = lab.graph;

        // edges first
        g.setStroke(edge);
        g.setLineWidth(1.3 * scale);
        for (EClass c : snapshot) {
            double[] p = layout.get(c.id());
            if (p == null) continue;
            double cx = p[0] * scale + tx;
            double cy = p[1] * scale + ty;
            for (ENode n : c.nodes()) {
                for (int ch : n.children()) {
                    double[] q = layout.get(graph.find(ch));
                    if (q == null) continue;
                    double qx = q[0] * scale + tx;
                    double qy = q[1] * scale + ty;
                    g.strokeLine(cx + 110 * scale, cy + (40 + 22 * c.size()) * scale,
                                 qx + 110 * scale, qy);
                }
            }
        }

        // class cards
        for (EClass c : snapshot) {
            double[] p = layout.get(c.id());
            if (p == null) continue;
            double x = p[0] * scale + tx;
            double y = p[1] * scale + ty;
            double cw = 220 * scale;
            double ch = (58 + 22 * c.size()) * scale;

            boolean isRoot = c.id() == graph.find(lab.rootClass);
            boolean isSel = c.id() == selected;
            boolean matchesFilter = filter.isEmpty() || matches(c);

            g.setFill(isSel ? selBg : cardBg);
            g.fillRoundRect(x, y, cw, ch, 12 * scale, 12 * scale);
            g.setLineWidth(1.4 * scale);
            g.setStroke(isSel ? accent : (isRoot ? rootBd : cardBd));
            g.strokeRoundRect(x, y, cw, ch, 12 * scale, 12 * scale);

            g.setFont(Font.font("Consolas", Math.max(8, 12 * scale)));
            g.setFill(isRoot ? rootBd : accent);
            g.fillText("EClass #" + c.id() + (isRoot ? "  (root)" : ""), x + 10 * scale, y + 18 * scale);

            double ly = y + 38 * scale;
            for (ENode n : c.nodes()) {
                g.setFill(matchesFilter ? text : muted);
                g.fillText(truncate(n.render(), 30), x + 12 * scale, ly);
                ly += 20 * scale;
            }

            if (isSel && lab.expertMode) {
                g.setFill(muted);
                g.fillText("uf-parent=" + graph.unionFindParent(c.id()), x + 10 * scale, y + ch - 8 * scale);
            }
        }

        info.setText(snapshot.size() + " classes / " + graph.nodeCount() + " nodes   •   zoom "
                + String.format("%.2f", scale) + (selected >= 0 ? "   •   selected EClass #" + selected : ""));
    }

    private boolean matches(EClass c) {
        String f = filter.toLowerCase();
        if (f.startsWith("#")) {
            try { return c.id() == Integer.parseInt(f.substring(1)); } catch (NumberFormatException ex) { return false; }
        }
        for (ENode n : c.nodes()) {
            if (n.render().toLowerCase().contains(f)) return true;
            if (n.label() != null && n.label().toLowerCase().contains(f)) return true;
        }
        return false;
    }

    private static String truncate(String s, int n) { return s.length() <= n ? s : s.substring(0, n - 1) + "…"; }

    @Override public void refresh() { draw(); }
}

/** Small helper: Label.setStyleClassSafe is not a real API — keep behaviour local. */
class LabelHack { }