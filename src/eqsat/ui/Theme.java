package eqsat.ui;

public final class Theme {

    public enum Mode { DARK, LIGHT }

    private Theme() {}

    private static final String TEMPLATE = """
        .root-pane       { -fx-background-color: @bg; }
        .view            { -fx-background-color: @bg; }
        .header          { -fx-background-color: @panel; -fx-padding: 12 20 12 20; -fx-border-color: transparent transparent @border transparent; -fx-border-width: 0 0 1 0; }
        .app-title       { -fx-text-fill: @text; -fx-font-size: 17px; -fx-font-weight: bold; }
        .app-subtitle    { -fx-text-fill: @muted; -fx-font-size: 11.5px; }

        .sidebar         { -fx-background-color: @panel; -fx-padding: 10; -fx-border-color: transparent @border transparent transparent; -fx-border-width: 0 1 0 0; }
        .sidebar-title   { -fx-text-fill: @muted; -fx-font-size: 10.5px; -fx-font-weight: bold; -fx-padding: 6 8 6 8; }

        .nav-button      { -fx-background-color: transparent; -fx-text-fill: @muted; -fx-alignment: CENTER_LEFT;
                           -fx-padding: 8 12 8 12; -fx-background-radius: 8; -fx-font-size: 12.5px; -fx-cursor: hand; }
        .nav-button:hover{ -fx-background-color: @hover; -fx-text-fill: @text; }
        .nav-button.active { -fx-background-color: @accentSoft; -fx-text-fill: @accent; -fx-font-weight: bold; }

        .view-title      { -fx-text-fill: @text; -fx-font-size: 20px; -fx-font-weight: bold; }
        .view-subtitle   { -fx-text-fill: @muted; -fx-font-size: 12px; }

        .card            { -fx-background-color: @panel2; -fx-background-radius: 12; -fx-padding: 14;
                           -fx-border-color: @border; -fx-border-radius: 12; -fx-border-width: 1; }
        .card:hover      { -fx-border-color: @accent; }
        .card-title      { -fx-text-fill: @muted; -fx-font-size: 11px; -fx-font-weight: bold; }
        .card-value      { -fx-text-fill: @text; -fx-font-size: 22px; -fx-font-weight: bold; }
        .card-hint       { -fx-text-fill: @muted; -fx-font-size: 10.5px; }

        .panel           { -fx-background-color: @panel; -fx-background-radius: 12; -fx-padding: 14;
                           -fx-border-color: @border; -fx-border-radius: 12; -fx-border-width: 1; }
        .section-title   { -fx-text-fill: @text; -fx-font-size: 13.5px; -fx-font-weight: bold; }

        .mono            { -fx-font-family: "JetBrains Mono", "Consolas", "Menlo", monospace; }
        .code-area       { -fx-font-family: "JetBrains Mono", "Consolas", "Menlo", monospace;
                           -fx-control-inner-background: @panel2; -fx-text-fill: @text;
                           -fx-highlight-fill: @accentSoft; -fx-background-radius: 10; -fx-border-radius: 10;
                           -fx-border-color: @border; -fx-border-width: 1; -fx-font-size: 12.5px; }

        .text-field      { -fx-background-color: @panel2; -fx-text-fill: @text; -fx-prompt-text-fill: @muted;
                           -fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: @border;
                           -fx-border-width: 1; -fx-padding: 9 12 9 12; -fx-font-size: 13.5px; }
        .text-field:focused { -fx-border-color: @accent; }

        .button          { -fx-background-radius: 9; -fx-padding: 8 16 8 16; -fx-font-size: 12.5px; -fx-cursor: hand; }
        .primary-button  { -fx-background-color: @accent; -fx-text-fill: @accentText; -fx-font-weight: bold; }
        .primary-button:hover { -fx-background-color: @accentHover; }
        .ghost-button    { -fx-background-color: @panel2; -fx-text-fill: @text; -fx-border-color: @border; -fx-border-radius: 9; -fx-border-width: 1; }
        .ghost-button:hover { -fx-border-color: @accent; -fx-text-fill: @accent; }
        .danger-button   { -fx-background-color: transparent; -fx-text-fill: @danger; -fx-border-color: @danger; -fx-border-radius: 9; -fx-border-width: 1; }

        .pill            { -fx-background-radius: 999; -fx-padding: 3 10 3 10; -fx-font-size: 10.5px; -fx-font-weight: bold; }
        .pill-safe       { -fx-background-color: @okSoft;   -fx-text-fill: @ok; }
        .pill-conditional{ -fx-background-color: @warnSoft; -fx-text-fill: @warn; }
        .pill-assumption { -fx-background-color: @warnSoft; -fx-text-fill: @warn; }
        .pill-neutral    { -fx-background-color: @hover;    -fx-text-fill: @muted; }
        .pill-accent     { -fx-background-color: @accentSoft; -fx-text-fill: @accent; }

        .status-bar      { -fx-background-color: @panel; -fx-padding: 6 14 6 14;
                           -fx-border-color: @border transparent transparent transparent; -fx-border-width: 1 0 0 0; }
        .status-item     { -fx-text-fill: @muted; -fx-font-size: 11px; }
        .status-value    { -fx-text-fill: @text; -fx-font-size: 11px; -fx-font-weight: bold; }

        .list-view       { -fx-background-color: @panel2; -fx-control-inner-background: @panel2;
                           -fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: @border; -fx-border-width: 1; }
        .list-cell       { -fx-background-color: transparent; -fx-text-fill: @text; -fx-padding: 7 10 7 10; -fx-font-size: 12px; }
        .list-cell:filled:selected { -fx-background-color: @accentSoft; -fx-text-fill: @accent; }
        .list-cell:filled:hover    { -fx-background-color: @hover; }

        .table-view      { -fx-background-color: @panel2; -fx-control-inner-background: @panel2;
                           -fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: @border; -fx-border-width: 1; }
        .table-view .column-header-background { -fx-background-color: @panel; }
        .table-view .column-header, .table-view .filler { -fx-background-color: transparent; -fx-size: 30; }
        .table-view .column-header .label { -fx-text-fill: @muted; -fx-font-size: 11px; -fx-font-weight: bold; }
        .table-row-cell  { -fx-background-color: transparent; -fx-text-fill: @text; -fx-border-color: transparent; }
        .table-row-cell:odd { -fx-background-color: @rowAlt; }
        .table-row-cell:selected { -fx-background-color: @accentSoft; }
        .table-cell     { -fx-text-fill: @text; -fx-font-size: 12px; -fx-padding: 6 10 6 10; }

        .combo-box       { -fx-background-color: @panel2; -fx-background-radius: 9; -fx-border-radius: 9;
                           -fx-border-color: @border; -fx-border-width: 1; -fx-text-fill: @text; -fx-font-size: 12px; }
        .combo-box .list-cell { -fx-text-fill: @text; -fx-background-color: transparent; }
        .combo-box-popup .list-view { -fx-background-color: @panel; }

        .check-box       { -fx-text-fill: @text; -fx-font-size: 12px; }
        .check-box .box  { -fx-background-color: @panel2; -fx-border-color: @border; -fx-border-radius: 4; -fx-background-radius: 4; }
        .check-box:selected .mark { -fx-background-color: @accentText; }
        .check-box:selected .box  { -fx-background-color: @accent; -fx-border-color: @accent; }

        .scroll-pane     { -fx-background-color: transparent; -fx-background: transparent; }
        .scroll-pane > .viewport { -fx-background-color: transparent; }
        .scroll-bar:vertical, .scroll-bar:horizontal { -fx-background-color: transparent; }
        .scroll-bar .thumb { -fx-background-color: @border; -fx-background-radius: 6; }
        .scroll-bar .thumb:hover { -fx-background-color: @muted; }
        .scroll-bar .increment-button, .scroll-bar .decrement-button { -fx-background-color: transparent; -fx-padding: 0; }

        .separator .line { -fx-border-color: @border; -fx-border-width: 0.5; }

        .progress-indicator { -fx-progress-color: @accent; }
        .progress-bar .bar { -fx-background-color: @accent; }
        .progress-bar .track { -fx-background-color: @panel2; }

        .warn-label      { -fx-text-fill: @warn; -fx-font-size: 11px; }
        .ok-label        { -fx-text-fill: @ok; -fx-font-size: 11px; }
        .err-label       { -fx-text-fill: @danger; -fx-font-size: 11.5px; }
        .muted-label     { -fx-text-fill: @muted; -fx-font-size: 11.5px; }

        .tab-pane .tab-header-area .tab-header-background { -fx-background-color: transparent; }
        .tab-pane .tab { -fx-background-color: @panel2; -fx-background-radius: 8 8 0 0; }
        .tab-pane .tab:selected { -fx-background-color: @accentSoft; }
        .tab-pane .tab .tab-label { -fx-text-fill: @muted; -fx-font-size: 12px; }
        .tab-pane .tab:selected .tab-label { -fx-text-fill: @accent; -fx-font-weight: bold; }
        """;

    public static String css(Mode mode) {
        boolean dark = mode == Mode.DARK;

        String bg      = dark ? "#0d1117" : "#f2f4f7";
        String panel   = dark ? "#141a22" : "#ffffff";
        String panel2  = dark ? "#1a222c" : "#f8fafc";
        String rowAlt  = dark ? "#161d26" : "#f3f6fa";
        String border  = dark ? "#26313d" : "#d8dee6";
        String hover   = dark ? "#1f2a36" : "#eaeff5";
        String text    = dark ? "#e6edf3" : "#1b232c";
        String muted   = dark ? "#8b97a6" : "#5b6673";
        String accent  = dark ? "#5aa9ff" : "#0b62d0";
        String accentH = dark ? "#78baff" : "#0a55b5";
        String accentText = dark ? "#06101c" : "#ffffff";
        String accentSoft = dark ? "#12293f" : "#dce9fb";
        String ok      = dark ? "#6ee7a8" : "#0f7a45";
        String okSoft  = dark ? "#102a1e" : "#dcf5e6";
        String warn    = dark ? "#ffb454" : "#9a5b00";
        String warnSoft= dark ? "#2c2313" : "#fdf0d8";
        String danger  = dark ? "#ff7b72" : "#b3261e";

        return TEMPLATE
                .replace("@bg", bg)
                .replace("@panel2", panel2)
                .replace("@panel", panel)
                .replace("@rowAlt", rowAlt)
                .replace("@border", border)
                .replace("@hover", hover)
                .replace("@text", text)
                .replace("@muted", muted)
                .replace("@accentHover", accentH)
                .replace("@accentText", accentText)
                .replace("@accentSoft", accentSoft)
                .replace("@accent", accent)
                .replace("@okSoft", okSoft)
                .replace("@ok", ok)
                .replace("@warnSoft", warnSoft)
                .replace("@warn", warn)
                .replace("@danger", danger);
    }
}