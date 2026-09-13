package eqsat.util;

import eqsat.ui.Lab;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class Exporter {

    private Exporter() {}

    public static String buildReport(Lab lab) {
        StringBuilder sb = new StringBuilder();
        sb.append("EQUAALITY SATURATION LABORATORY — OPTIMISATION REPORT\n");
        sb.append("====================================================\n\n");
        sb.append("Input expression      : ").append(lab.ast == null ? "—" : lab.ast.pretty()).append('\n');
        sb.append("Final expression      : ").append(lab.extractedExpr == null ? "—" : lab.extractedExpr.pretty()).append('\n');
        sb.append("Cost model            : ").append(lab.costModel.name()).append('\n');
        sb.append("Enabled rules         : ").append(lab.enabledRules().size()).append('\n');
        sb.append("Iterations            : ").append(lab.iterations()).append('\n');
        sb.append("E-Classes             : ").append(lab.classCount()).append('\n');
        sb.append("E-Nodes               : ").append(lab.nodeCount()).append('\n');
        sb.append("Rewrite applications  : ").append(lab.rewriteApplications()).append('\n');
        sb.append("Original cost         : ").append(lab.originalCost).append('\n');
        sb.append("Final cost            : ").append(lab.extractedCost).append('\n');
        sb.append("Execution time (ms)   : ").append(lab.elapsedMillis()).append('\n');
        sb.append("Status                : ").append(lab.statusText()).append("\n\n");

        if (lab.graph != null) {
            sb.append("E-GRAPH\n------\n").append(lab.graph.dump()).append('\n');
        }

        if (lab.engine != null) {
            sb.append("REWRITE HISTORY\n---------------\n");
            lab.engine.history().forEach(s -> sb.append(s.summary()).append('\n'));
        }
        return sb.toString();
    }

    public static void exportReportToFile(Lab lab) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export Optimisation Report");
        chooser.setInitialFileName("eqsat-report.txt");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Text", "*.txt"),
                new FileChooser.ExtensionFilter("All files", "*.*"));
        java.io.File f = chooser.showSaveDialog(new Stage());
        if (f == null) return;
        try {
            Files.writeString(f.toPath(), buildReport(lab));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void writeTo(Path path, String content) throws IOException {
        Files.writeString(path, content);
    }
}