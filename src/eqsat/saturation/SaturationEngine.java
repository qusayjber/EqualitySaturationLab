package eqsat.saturation;

import eqsat.egraph.EGraph;
import eqsat.rewrite.Matcher;
import eqsat.rewrite.RewriteRule;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The real equality-saturation loop:
 *   repeat { apply rules; rebuild (congruence closure) } until fixpoint or a limit.
 */
public final class SaturationEngine {

    private final EGraph graph;
    private final List<RewriteRule> rules;
    private final SaturationLimits limits;

    private int iteration;
    private long rewriteApplications;
    private long rewriteMatches;
    private long startNanos;
    private long elapsedNanos;
    private int peakNodes;
    private int peakClasses;
    private SaturationStatus status = SaturationStatus.IDLE;
    private final List<SaturationStep> history = new ArrayList<>();

    public SaturationEngine(EGraph graph, List<RewriteRule> rules, SaturationLimits limits) {
        this.graph = graph;
        this.rules = List.copyOf(rules);
        this.limits = limits;
        this.peakNodes = graph.nodeCount();
        this.peakClasses = graph.classCount();
    }

    // ------------------------------------------------------------------ one iteration

    public SaturationStep step() {
        if (status == SaturationStatus.SATURATED || status == SaturationStatus.LIMIT_REACHED) return null;
        if (startNanos == 0) startNanos = System.nanoTime();
        status = SaturationStatus.RUNNING;

        iteration++;

        int nodesBefore = graph.nodeCount();
        int classesBefore = graph.classCount();
        long unionsBefore = graph.unionOperations();

        List<RuleApplication> apps = new ArrayList<>();

        for (RewriteRule rule : rules) {
            List<Integer> ids = graph.rootClassIds();
            for (int rawId : ids) {
                int c = graph.find(rawId);
                if (graph.nodesOf(c).isEmpty()) continue;

                List<Map<String, Integer>> matches = Matcher.match(rule.left(), c, graph);
                for (Map<String, Integer> sub : matches) {
                    int result = Matcher.instantiate(rule.right(), sub, graph);
                    boolean merged = graph.union(c, result);
                    apps.add(new RuleApplication(rule.name(), rule.category().name(),
                            c, Map.copyOf(sub), result, merged));
                    rewriteApplications++;
                    rewriteMatches++;
                }
            }
        }

        graph.rebuild();

        int nodesAfter = graph.nodeCount();
        int classesAfter = graph.classCount();
        int merges = (int) (graph.unionOperations() - unionsBefore);

        peakNodes = Math.max(peakNodes, nodesAfter);
        peakClasses = Math.max(peakClasses, classesAfter);
        elapsedNanos = System.nanoTime() - startNanos;

        boolean changed = (nodesAfter != nodesBefore) || (classesAfter != classesBefore);

        if (!changed) {
            status = SaturationStatus.SATURATED;
        } else if (limitReached()) {
            status = SaturationStatus.LIMIT_REACHED;
        } else {
            status = SaturationStatus.RUNNING;
        }

        SaturationStep s = new SaturationStep(iteration, nodesBefore, nodesAfter,
                classesBefore, classesAfter, merges, apps, elapsedNanos / 1_000_000L, changed);
        history.add(s);
        return s;
    }

    // ------------------------------------------------------------------ full run

    public SaturationResult runToCompletion() {
        while (true) {
            if (status == SaturationStatus.SATURATED || status == SaturationStatus.LIMIT_REACHED) break;
            SaturationStep s = step();
            if (s == null) break;
            if (!s.changed()) break;
            if (limitReached()) break;
        }
        if (status != SaturationStatus.SATURATED && status != SaturationStatus.LIMIT_REACHED) {
            status = SaturationStatus.SATURATED;
        }
        return result();
    }

    public boolean limitReached() {
        return iteration >= limits.maxIterations()
                || graph.nodeCount() >= limits.maxNodes()
                || graph.classCount() >= limits.maxClasses()
                || (elapsedNanos / 1_000_000L) >= limits.maxMillis();
    }

    public SaturationResult result() {
        return new SaturationResult(status, iteration,
                graph.nodeCount(), graph.classCount(),
                peakNodes, peakClasses,
                rewriteApplications, rewriteMatches,
                graph.unionOperations(), graph.congruenceMerges(),
                elapsedNanos / 1_000_000L,
                status == SaturationStatus.SATURATED);
    }

    public void pause()  { if (status == SaturationStatus.RUNNING) status = SaturationStatus.PAUSED; }
    public void resume() { if (status == SaturationStatus.PAUSED)  status = SaturationStatus.RUNNING; }
    public void cancel() { status = SaturationStatus.CANCELLED; }

    public SaturationStatus status() { return status; }
    public int iteration() { return iteration; }
    public long rewriteApplications() { return rewriteApplications; }
    public long rewriteMatches() { return rewriteMatches; }
    public long elapsedMillis() { return elapsedNanos / 1_000_000L; }
    public List<SaturationStep> history() { return List.copyOf(history); }
    public SaturationLimits limits() { return limits; }
    public EGraph graph() { return graph; }
}