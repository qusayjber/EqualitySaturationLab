package eqsat.saturation;

import java.util.Map;

public record RuleApplication(
        String ruleName,
        String category,
        int matchedClass,
        Map<String, Integer> substitution,
        int resultClass,
        boolean merged) {

    public String substitutionText() {
        StringBuilder sb = new StringBuilder();
        substitution.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> sb.append("  ?").append(e.getKey()).append("  →  EClass #").append(e.getValue()).append('\n'));
        return sb.toString();
    }
}