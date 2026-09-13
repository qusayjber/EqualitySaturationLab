package eqsat.ui;

import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public final class DocumentationView extends LabView {

    public DocumentationView(Lab lab) {
        super(lab, "Documentation",
                "Concepts behind the E-Graph, congruence closure, rewriting, saturation and extraction.");

        TextArea doc = new TextArea(TEXT);
        doc.getStyleClass().add("code-area");
        doc.setEditable(false);
        doc.setWrapText(true);
        VBox.setVgrow(doc, Priority.ALWAYS);

        ScrollPane sp = new ScrollPane(doc);
        sp.setFitToWidth(true);
        sp.setFitToHeight(true);
        sp.getStyleClass().add("scroll-pane");
        VBox.setVgrow(sp, Priority.ALWAYS);

        getChildren().add(sp);
        VBox.setVgrow(sp, Priority.ALWAYS);
    }

    private static final String TEXT = """
            EQUALITY SATURATION — CONCEPTS
            ==============================

            What is Equality Saturation?
            ---------------------------
            Equality saturation is a program-optimisation technique that applies rewrite rules to an
            E-Graph instead of to a single expression. Rewrites only ever ADD information (they merge
            equivalence classes); they never commit to a decision. When no rule can add anything new,
            the E-Graph is "saturated" and a cost model extracts the cheapest representative.

            What is an E-Graph?
            -------------------
            An E-Graph is a data structure that compactly represents a set of equivalent expressions.
            It consists of:
              • E-Classes — sets of terms known to be equal, identified by a canonical id.
              • E-Nodes   — operator applications whose children are E-Class ids (not sub-expressions).
            Because children are class ids, one E-Node can describe many concrete expressions at once.

            What is an E-Class?
            -------------------
            An equivalence class. When two E-Nodes are found to be equal, their classes are merged with
            union-find. The class then holds both E-Nodes, meaning "either of these is a valid
            representation of the same value".

            What is an E-Node?
            ------------------
            A single operator together with references to the E-Classes of its operands:
                Add(#3, #5)
            Two E-Nodes are identical when their operator and their canonical child ids are identical.
            This canonical form is what makes hash-consing possible.

            Union-Find
            ----------
            The E-Graph stores E-Classes in a disjoint-set forest with path compression and union by
            rank. find(id) returns the canonical representative of a class in near-constant time.
            All class ids are canonicalised through find() before being used as a hash-cons key.

            Hash-consing
            ------------
            Whenever an E-Node is inserted, its children are first canonicalised. The resulting key
            (operator, label, canonical child ids) is looked up in a hash map. If an identical E-Node
            already exists, the existing class is returned instead of allocating a new one. This is
            what makes the E-Graph compact and makes "is this already present?" checks cheap.

            Congruence Closure
            ------------------
            The rule of congruence: if a == b then f(a) == f(b). After every batch of merges the E-Graph
            runs a rebuild() pass:
              1. Every E-Node's children are canonicalised against the current roots.
              2. Every E-Node is re-inserted into the hash-cons table.
              3. If two E-Nodes in different classes now hash to the same key, those classes are merged.
            Steps 1–3 repeat until no merge occurs. That fixpoint is the congruence closure of the
            current equivalence relation. In this application, rebuild() is the only mechanism that
            propagates equalities — it is not faked.

            Rewriting and Pattern Matching
            ------------------------------
            A rewrite rule is a pair of patterns LHS → RHS. A pattern is either
              • a pattern variable  ?x        (matches any E-Class, binds an id)
              • a literal leaf      Const(0)  (matches a specific E-Node)
              • a node pattern      Add(?x, ?y)
            Matching walks the E-Classes: for every E-Node in the class whose operator matches, the
            children are matched recursively, threading a substitution map. Because an E-Class can hold
            many E-Nodes, matching naturally produces many solutions.

            Instantiation and Merging
            -------------------------
            For each match, the RHS is instantiated: pattern variables are replaced by their bound
            class ids, literals by fresh E-Nodes. The resulting class is unioned with the matched
            class — this is the ONLY effect a rule has. Nothing is deleted, nothing is rewritten
            in place.

            Why is Equality Saturation useful?
            ----------------------------------
            Traditional rewriting is destructive. Once you replace (a+b)+c with a+(b+c), the first
            form is gone. If a later rule would have matched it, you have lost your chance — this is
            the classic "phase ordering" problem of compilers.

            Equality saturation keeps every form. Rules that would conflict in a traditional pipeline
            coexist peacefully, because they only add equivalences. Extraction happens once, at the
            end, using global cost information rather than local greedy heuristics.

            Extraction
            ----------
            Extraction assigns a cost to every E-Class using dynamic programming over the E-Graph.
            Because the graph may contain cycles (e.g. x + 0 ~ x), a simple bottom-up pass is not
            enough, so the extractor runs a least-fixed-point iteration until the cost of every class
            stops improving. The best E-Node of each class is recorded, and the answer is rebuilt
            top-down from the root class.

            Cost Models
            -----------
            Different cost models select different expressions from the SAME E-Graph:
              • AST Node Count  — every node costs 1 (minimise size)
              • Operator Count  — leaves are free (minimise work)
              • Weighted Cost   — per-operator weights, e.g. ADD=1, MUL=2, DIV=4, POW=5
              • Depth           — 1 + max(child depth) (minimise critical path)
            This is why the extracted expression can change when you switch models: the equivalence
            class is the same, the ranking is not.

            Rule Safety
            -----------
            Not every algebraic rewrite is universally valid:
              • SAFE         — valid for all values (x + 0 = x)
              • CONDITIONAL  — needs a precondition   (x / x = 1 requires x != 0)
              • ASSUMPTION   — needs domain assumptions (no overflow, real arithmetic)
            This laboratory marks conditional rules clearly and never hides the assumption.

            Traditional vs Equality Saturation
            ----------------------------------
            Traditional:
                expression → rewrite 1 → expression → rewrite 2 → expression → rewrite 3
                (each arrow commits; an early decision can block a better result)

            Equality saturation:
                expression → E-Graph → many equivalent representations → saturation
                           → cost-based extraction → best expression
                (no decision is made until all information is available)

            Glossary of the UI
            ------------------
              E-Classes / E-Nodes — live counters of the real E-Graph
              Rewrite Applications — number of (rule, match) pairs actually executed
              Iterations           — number of saturation rounds performed
              Congruence Merges    — merges caused by rebuild(), not by a rule directly
              Expert Mode          — exposes union-find parents, class ids and substitutions
              Educational Mode     — explains each applied rule in the Step-by-Step view
            """;

    @Override public void refresh() { }
}