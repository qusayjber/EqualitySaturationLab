package eqsat.ui;

import eqsat.model.Expr;
import eqsat.model.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.util.ArrayList;
import java.util.List;

/** Draws the parsed AST as a classic node/link tree. */
public final class AstView extends Pane {

    private final Canvas canvas = new Canvas(600, 260);
    private Expr root;
    private boolean dark = true;

    public AstView() {
        getChildren().add(canvas);
        canvas.widthProperty().bind(widthProperty());
        canvas.heightProperty().bind(heightProperty());
        widthProperty().addListener((o, a, b) -> draw());
        heightProperty().addListener((o, a, b) -> draw());
        setMinHeight(220);
        setPrefHeight(260);
    }

    public void setDark(boolean dark) { this.dark = dark; draw(); }

    public void setExpression(Expr e) { this.root = e; draw(); }

    // --------------------------------------------------------------- layout node

    private static final class N {
        final String label;
        final List<N> kids = new ArrayList<>();
        double x, y;
        N(String label) { this.label = label; }
    }

    private static N build(Expr e) {
        if (e instanceof VarExpr v)   return new N(v.name());
        if (e instanceof ConstExpr c) return new N(Long.toString(c.value()));
        if (e instanceof UnaryExpr u) { N n = new N(u.op().symbol()); n.kids.add(build(u.operand())); return n; }
        BinaryExpr b = (BinaryExpr) e;
        N n = new N(b.op().symbol());
        n.kids.add(build(b.left()));
        n.kids.add(build(b.right()));
        return n;
    }

    private static int assign(N n, int depth, int[] counter) {
        n.y = depth;
        if (n.kids.isEmpty()) { n.x = counter[0]++; return (int) n.x; }
        int first = -1, last = -1;
        for (N k : n.kids) {
            int v = assign(k, depth + 1, counter);
            if (first < 0) first = v;
            last = v;
        }
        n.x = (first + last) / 2.0;
        return (int) n.x;
    }

    private static void collect(N n, List<N> out) { out.add(n); for (N k : n.kids) collect(k, out); }

    // --------------------------------------------------------------- drawing

    private void draw() {
        GraphicsContext g = canvas.getGraphicsContext2D();
        double w = getWidth(), h = getHeight();
        g.clearRect(0, 0, w, h);
        if (root == null || w <= 0 || h <= 0) {
            g.setFill(dark ? Color.web("#8b97a6") : Color.web("#5b6673"));
            g.setFont(Font.font("Consolas", 12));
            g.fillText("No AST — enter a valid expression.", 16, 26);
            return;
        }

        N tree = build(root);
        int[] counter = {0};
        assign(tree, 0, counter);

        List<N> all = new ArrayList<>();
        collect(tree, all);

        double maxX = 1, maxY = 1;
        for (N n : all) { maxX = Math.max(maxX, n.x); maxY = Math.max(maxY, n.y); }

        double cellW = 56, cellH = 62;
        double neededW = (maxX + 1) * cellW + 40;
        double neededH = (maxY + 1) * cellH + 40;
        double scale = Math.min(1.0, Math.min((w - 20) / neededW, (h - 20) / neededH));

        double offX = (w - neededW * scale) / 2 + 20 * scale;
        double offY = (h - neededH * scale) / 2 + 20 * scale;

        Color edge = dark ? Color.web("#3a4757") : Color.web("#b9c2cd");
        Color nodeBg = dark ? Color.web("#1a222c") : Color.web("#ffffff");
        Color nodeBorder = dark ? Color.web("#26313d") : Color.web("#d8dee6");
        Color nodeText = dark ? Color.web("#e6edf3") : Color.web("#1b232c");
        Color leafBg = dark ? Color.web("#12293f") : Color.web("#dce9fb");
        Color leafBorder = dark ? Color.web("#5aa9ff") : Color.web("#0b62d0");
        Color leafText = dark ? Color.web("#5aa9ff") : Color.web("#0b62d0");

        // edges
        g.setStroke(edge);
        g.setLineWidth(1.4 * scale);
        for (N n : all) {
            double x = offX + n.x * cellW * scale;
            double y = offY + n.y * cellH * scale;
            for (N k : n.kids) {
                double kx = offX + k.x * cellW * scale;
                double ky = offY + k.y * cellH * scale;
                g.strokeLine(x, y + 15 * scale, kx, ky - 15 * scale);
            }
        }

        // nodes
        g.setFont(Font.font("Consolas", Math.max(9, 13 * scale)));
        g.setTextAlign(TextAlignment.CENTER);
        for (N n : all) {
            double x = offX + n.x * cellW * scale;
            double y = offY + n.y * cellH * scale;
            double r = 15 * scale;
            boolean leaf = n.kids.isEmpty();
            g.setFill(leaf ? leafBg : nodeBg);
            g.fillRoundRect(x - r * 1.35, y - r, r * 2.7, r * 2, r, r);
            g.setStroke(leaf ? leafBorder : nodeBorder);
            g.setLineWidth(1.2 * scale);
            g.strokeRoundRect(x - r * 1.35, y - r, r * 2.7, r * 2, r, r);
            g.setFill(leaf ? leafText : nodeText);
            g.fillText(n.label, x, y + 4.5 * scale);
        }
        g.setTextAlign(TextAlignment.LEFT);
    }
}