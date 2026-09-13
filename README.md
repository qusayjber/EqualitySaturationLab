# Equality Saturation Laboratory

<div align="center">

### Explore Program Optimization Through E-Graphs

**A visual JavaFX laboratory for Equality Saturation, rewrite systems, E-Graphs, and cost-based optimization.**

<br>

[![Java](https://img.shields.io/badge/Java-25-ED8B00?style=for-the-badge\&logo=openjdk\&logoColor=white)](https://www.oracle.com/java/)
[![JavaFX](https://img.shields.io/badge/JavaFX-25-1B6AC6?style=for-the-badge\&logo=java\&logoColor=white)](https://openjfx.io/)
[![Platform](https://img.shields.io/badge/Platform-Desktop-111827?style=for-the-badge)](#)
[![Status](https://img.shields.io/badge/Status-Active%20Development-F59E0B?style=for-the-badge)](#project-status)
[![License](https://img.shields.io/badge/License-MIT-22C55E?style=for-the-badge)](LICENSE)

<br>

**[Explore the Repository](https://github.com/qusayjber/EqualitySaturationLab)**

</div>

---

## What Is This?

**Equality Saturation Laboratory** is an interactive desktop application for understanding and experimenting with **Equality Saturation** — a powerful technique used in compiler optimization, program transformation, and other systems that need to reason about equivalent representations.

Instead of transforming an expression into one new expression at a time, the system builds an **E-Graph** that can represent many equivalent forms simultaneously.

For example:

```text
(a + b) + c
```

can coexist with:

```text
a + (b + c)
```

and, when commutativity is enabled:

```text
c + (a + b)
```

Rather than deciding immediately which transformation is "best", Equality Saturation preserves the alternatives and postpones the decision until a **cost-based extraction** phase.

```text
                 ┌─────────────────────┐
                 │      Expression      │
                 └──────────┬──────────┘
                            │
                            ▼
                 ┌─────────────────────┐
                 │       E-Graph       │
                 └──────────┬──────────┘
                            │
              ┌─────────────┼─────────────┐
              ▼             ▼             ▼
           Form A        Form B        Form C
              │             │             │
              └─────────────┼─────────────┘
                            │
                            ▼
                    Equality Saturation
                            │
                            ▼
                     Cost-Based Search
                            │
                            ▼
                   Best Representation
```

The goal of this project is simple:

> **Make the internal mechanics of Equality Saturation visible.**

---

# Why This Project?

Compiler optimizations are often taught as a sequence of transformations:

```text
Input
  ↓
Optimization
  ↓
Optimization
  ↓
Optimization
  ↓
Output
```

The problem is that optimization decisions can interact.

A transformation that looks good locally may make a better global representation unreachable through the chosen sequence.

Equality Saturation changes the strategy:

```text
Don't immediately choose.

Preserve equivalent possibilities.

Then choose the best representation.
```

This project turns that idea into something you can **run, inspect, visualize, and experiment with**.

---

# Core Pipeline

The laboratory follows the complete optimization pipeline:

```text
        Expression
             │
             ▼
        ┌─────────┐
        │  Lexer  │
        └────┬────┘
             ▼
        ┌─────────┐
        │ Parser  │
        └────┬────┘
             ▼
           AST
             │
             ▼
        ┌─────────┐
        │ E-Graph │
        └────┬────┘
             │
             ▼
       Pattern Matching
             │
             ▼
       Rewrite Rules
             │
             ▼
      Equality Saturation
             │
             ▼
       Congruence Closure
             │
             ▼
        Saturated Graph
             │
             ▼
       Cost-Based Extraction
             │
             ▼
       Optimized Expression
```

Each stage exists for a reason.

---

# The E-Graph

An E-Graph represents equivalence without forcing every equivalent expression to become a separate tree.

The fundamental structures are:

```text
E-Graph
 ├── E-Classes
 │    ├── E-Nodes
 │    ├── E-Nodes
 │    └── ...
 │
 ├── Union-Find
 │
 └── Congruence information
```

### E-Node

Represents an operator and references to child E-Classes.

Conceptually:

```text
Add(EClass 4, EClass 8)
```

### E-Class

Represents a group of expressions known to be equivalent.

For example:

```text
EClass #12

├── Add(4, 8)
├── Add(8, 4)
└── Add(15, 2)
```

The important idea is:

> An E-Class represents **equivalence**, not merely one syntax tree.

---

# Rewrite Rules

The system operates through rewrite rules.

Examples:

### Identity

```text
x + 0 → x
0 + x → x

x * 1 → x
1 * x → x
```

### Zero

```text
x * 0 → 0
0 * x → 0
```

### Associativity

```text
(x + y) + z → x + (y + z)

(x * y) * z → x * (y * z)
```

### Commutativity

```text
x + y → y + x

x * y → y * x
```

### Distributivity

```text
x * (y + z)
      ↕
(x * y) + (x * z)
```

### Factorization

```text
(x * y) + (x * z)
      ↕
x * (y + z)
```

The important part is that the application does **not** simply replace strings.

The rules operate against the actual E-Graph representation.

---

# Equality Saturation

The saturation engine repeatedly discovers valid rewrites until it reaches a fixed point or a configured resource limit.

Conceptually:

```text
build E-Graph

      ↓

find applicable rewrites

      ↓

apply rewrites

      ↓

merge equivalent E-Classes

      ↓

rebuild congruence closure

      ↓

repeat
```

Until:

```text
No new useful rewrites
        OR
Maximum iterations reached
        OR
Maximum graph size reached
```

The resulting graph contains a large space of equivalent representations.

---

# Extraction

Saturation does not automatically tell us which representation is best.

That is the job of **extraction**.

Suppose the E-Graph represents:

```text
A
B
C
D
```

and the cost model produces:

```text
A → 8
B → 5
C → 7
D → 4
```

Then:

```text
D
```

is the preferred representation.

The application therefore separates:

```text
Equivalence
```

from:

```text
Optimization
```

This distinction is one of the central ideas behind the project.

---

# Cost Models

The laboratory supports experimenting with different notions of "best".

### Node Count

```text
cost(expression) =
number of AST nodes
```

### Operator Cost

Different operations can have different weights:

```text
ADD = 1
MUL = 2
DIV = 4
POW = 5
```

### Depth

Prefer expressions with smaller tree depth.

### Weighted Cost

Combine custom operator weights to experiment with different optimization objectives.

This makes it possible for the same saturated E-Graph to produce different extracted expressions depending on the cost model.

---

# Interactive Laboratory

The application is designed as a **laboratory**, not just a compiler demo.

You can inspect:

### Expression

```text
(a + b) + c
```

### AST

```text
        +
       / \
      +   c
     / \
    a   b
```

### E-Graph

```text
       ┌───────────────┐
       │   EClass #7   │
       ├───────────────┤
       │ Add(3,5)      │
       │ Add(5,3)      │
       │ ...           │
       └───────────────┘
```

### Rewrite History

```text
Iteration 1
    ↓
Associativity
    ↓
Iteration 2
    ↓
Commutativity
    ↓
Iteration 3
    ↓
Congruence Merge
```

### Extraction

```text
Candidates
────────────────────────
a + (b + c)       5
(a + b) + c       5
c + (a + b)       5
```

---

# Visualization

The JavaFX interface provides a graphical representation of the optimization process.

The E-Graph visualization is designed around:

* E-Classes
* E-Nodes
* relationships
* merges
* rewrite operations
* graph growth
* selected equivalence classes

Interactive controls include:

```text
Zoom
Pan
Fit
Select
Highlight
Reset
```

Graph changes can also be animated so that the user can observe how saturation modifies the structure.

---

# Step-by-Step Mode

One of the most important features is the ability to execute the optimization process incrementally.

For example:

```text
Input

(a + b) + c
```

Then:

```text
Rule:
Associativity

(a + b) + c
      ↓
a + (b + c)
```

The application can then expose the corresponding E-Graph changes.

This makes it possible to study Equality Saturation one operation at a time rather than watching an opaque optimization process.

---

# Educational Mode

Educational Mode explains the reasoning behind transformations.

Example:

```text
RULE

Associativity

BEFORE

(a + b) + c

AFTER

a + (b + c)

WHY?

Addition is associative, so regrouping the
operands preserves the result.
```

This mode is designed for learning:

* Compiler Design
* Programming Languages
* Optimization
* Graph Algorithms
* Rewriting Systems
* Formal Methods

---

# Expert Mode

Expert Mode exposes the internal machinery.

For example:

```text
EClass #12

Representative: #12

ENodes:
    Add(#4, #8)
    Add(#8, #4)

Children:
    #4
    #8
```

Pattern substitutions can be inspected:

```text
?x → EClass #4
?y → EClass #8
?z → EClass #12
```

This makes the application useful not only as a demonstration but also as a tool for understanding the implementation itself.

---

# Architecture

The project separates the optimization engine from the JavaFX presentation layer.

```text
src/
│
├── app/
│
├── model/
│   ├── Expr
│   ├── VarExpr
│   ├── ConstExpr
│   ├── UnaryExpr
│   └── BinaryExpr
│
├── parser/
│   ├── Lexer
│   ├── Token
│   └── Parser
│
├── egraph/
│   ├── EGraph
│   ├── EClass
│   ├── ENode
│   ├── ENodeKey
│   └── UnionFind
│
├── rewrite/
│   ├── RewriteRule
│   ├── Pattern
│   ├── PatternVariable
│   ├── PatternNode
│   └── BuiltInRules
│
├── saturation/
│   ├── SaturationEngine
│   ├── SaturationResult
│   └── SaturationStep
│
├── extract/
│   ├── Extractor
│   ├── CostModel
│   ├── NodeCountCost
│   ├── OperatorCost
│   ├── WeightedCost
│   └── DepthCost
│
├── visualization/
│   ├── EGraphView
│   ├── GraphNode
│   └── GraphEdge
│
└── ui/
    ├── DashboardView
    ├── ExpressionEditorView
    ├── RulesView
    ├── SaturationView
    ├── ExtractionView
    └── StatisticsView
```

---

# Technical Foundations

This project explores several areas of Computer Science simultaneously.

| Area                  | Concepts                     |
| --------------------- | ---------------------------- |
| Compilers             | Optimization, ASTs, IR       |
| Algorithms            | Union-Find, graph algorithms |
| Programming Languages | Rewriting, pattern matching  |
| Formal Methods        | Equivalence, congruence      |
| Optimization          | Cost models, extraction      |
| Graph Theory          | E-Graphs and relationships   |
| Software Engineering  | Modular architecture         |
| UI Engineering        | JavaFX visualization         |
| Concurrency           | Background computation       |

---

# Example

Input:

```text
(a + 0) * 1
```

Applicable transformations include:

```text
(a + 0) * 1
      │
      ├── a + 0 → a
      │
      └── x * 1 → x
```

The system can represent the equivalent forms inside the E-Graph and eventually extract:

```text
a
```

Another example:

```text
(a + b) + c
```

with associativity:

```text
(a + b) + c
      ≡
a + (b + c)
```

and with commutativity, additional equivalent forms become available.

---

# Built With

**Language**

Java 25

**UI**

JavaFX

**Architecture**

Modular Java application

**Core concepts**

E-Graphs · Equality Saturation · Rewrite Systems · Congruence Closure · Cost-Based Extraction

**Build philosophy**

No Maven
No Gradle
No FXML
No Scene Builder

The interface is constructed programmatically with JavaFX.

---

# Getting Started

## Requirements

* JDK 25
* JavaFX SDK
* Eclipse or IntelliJ IDEA

Check your Java installation:

```bash
java --version
```

Clone the repository:

```bash
git clone https://github.com/qusayjber/EqualitySaturationLab.git
```

Enter the project:

```bash
cd EqualitySaturationLab
```

Then configure JavaFX and run the application's main class.

For Eclipse:

1. Import the project.
2. Configure JDK 25.
3. Configure the JavaFX SDK.
4. Add the required JavaFX modules.
5. Run the application.

---

# Project Status

🚧 **Active Development**

The project is being developed as an experimental Computer Science laboratory.

Current development focuses on:

* E-Graph implementation
* Rewrite systems
* Saturation
* Extraction
* Visualization
* Interactive experimentation
* Educational tooling

---

# Roadmap

### Core Engine

* [x] Expression model
* [x] Parser
* [x] E-Nodes
* [x] E-Classes
* [x] Union-Find
* [x] Rewrite system
* [x] Saturation engine
* [x] Cost-based extraction

### Visualization

* [x] Expression visualization
* [x] E-Graph visualization
* [x] Statistics
* [x] Rule inspection
* [x] Step-by-step execution

---

# Why This Is Interesting

Equality Saturation is useful because it changes the optimization strategy from:

```text
"Which transformation should I apply next?"
```

to:

```text
"Which representations are equivalent,
and which one is ultimately cheapest?"
```

That seemingly small change creates a fundamentally different optimization workflow.

This project exists to make that workflow understandable.

---

# Project Philosophy

The project follows three principles:

### 1. Make the invisible visible

Compiler optimizations normally happen internally.

This laboratory exposes them.

### 2. Experiment, don't just observe

Users can change:

* expressions
* rules
* limits
* cost models
* execution mode

and observe the consequences.

### 3. Separate correctness from optimization

The E-Graph answers:

> **What is equivalent?**

The extractor answers:

> **What is best?**

---

# Repository

**GitHub**

https://github.com/qusayjber/EqualitySaturationLab

---

# Author

**Qusai Jaber**

Computer Science / Software Development

GitHub:
https://github.com/qusayjber

---

# License

MIT License.

See [`LICENSE`](LICENSE) for details.

---

<div align="center">

### Equality Saturation Laboratory

**Explore equivalence. Saturate possibilities. Extract the best.**

</div>
