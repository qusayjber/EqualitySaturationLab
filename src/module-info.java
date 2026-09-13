module eqsat.laboratory {
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.base;

    exports eqsat.app;
    exports eqsat.model;
    exports eqsat.parser;
    exports eqsat.egraph;
    exports eqsat.rewrite;
    exports eqsat.saturation;
    exports eqsat.extract;
    exports eqsat.ui;
    exports eqsat.util;
}