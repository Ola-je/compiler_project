package LL1;
import java.util.*;

class LL1Parser {
    private final Map<String, List<List<String>>> rules;
    private final Set<String> nonTerminals;
    private final Set<String> terminals;
    private final Map<String, Set<String>> first;
    private final Map<String, Set<String>> follow;

    public LL1Parser(Map<String, List<List<String>>> rules, String startSymbol) {
        this.rules = rules;
        this.nonTerminals = rules.keySet();
        this.terminals = findTerminals();
        this.first = new HashMap<>();
        this.follow = new HashMap<>();

        for (String nt : nonTerminals) {
            first.put(nt, new HashSet<>());
            follow.put(nt, new HashSet<>());
        }

        follow.get(startSymbol).add("$");
    }

    public Set<String> getNonTerminals() {
        return nonTerminals;
    }

    public Map<String, Set<String>> getFirst() {
        return first;
    }

    public Map<String, Set<String>> getFollow() {
        return follow;
    }

    public Set<String> findTerminals() {
        Set<String> terminals = new HashSet<>();
        for (List<List<String>> productions : rules.values()) {
            for (List<String> production : productions) {
                for (String symbol : production) {
                    if (!nonTerminals.contains(symbol) && !symbol.equals("epsilon")) {
                        terminals.add(symbol);
                    }
                }
            }
        }
        return terminals;
    }

    public Set<String> findFirst(String symbol) {
        if (terminals.contains(symbol)) {
            return new HashSet<>(Collections.singletonList(symbol));
        }

        if (symbol.equals("epsilon")) {
            return new HashSet<>(Collections.singletonList("epsilon"));
        }

        Set<String> firstSet = first.get(symbol);
        if (!firstSet.isEmpty()) {
            return firstSet;
        }

        for (List<String> production : rules.getOrDefault(symbol, Collections.emptyList())) {
            for (String charSymbol : production) {
                Set<String> charFirst = findFirst(charSymbol);
                firstSet.addAll(charFirst);
                firstSet.remove("epsilon");
                if (!charFirst.contains("epsilon")) {
                    break;
                }
            }
            if (production.stream().allMatch(s -> findFirst(s).contains("epsilon"))) {
                firstSet.add("epsilon");
            }
        }

        return firstSet;
    }

    public void computeFollow(String startSymbol) {
        boolean updated;
        do {
            updated = false;
            for (String nt : nonTerminals) {
                for (List<String> production : rules.get(nt)) {
                    Set<String> trailer = new HashSet<>(follow.get(nt));
                    for (int i = production.size() - 1; i >= 0; i--) {
                        String symbol = production.get(i);
                        if (nonTerminals.contains(symbol)) {
                            if (follow.get(symbol).addAll(trailer)) {
                                updated = true;
                            }
                            if (findFirst(symbol).contains("epsilon")) {
                                trailer.addAll(first.get(symbol));
                                trailer.remove("epsilon");
                            } else {
                                trailer = new HashSet<>(first.get(symbol));
                            }
                        } else {
                            trailer = new HashSet<>(findFirst(symbol));
                        }
                    }
                }
            }
        } while (updated);

        follow.get(startSymbol).add("$");
    }

    public Map<String, Map<String, String>> generateParsingTable(String startSymbol) {
        computeFollow(startSymbol);
        Map<String, Map<String, String>> parsingTable = new HashMap<>();
        boolean isValid = true;

        for (String nt : nonTerminals) {
            parsingTable.put(nt, new HashMap<>());
            for (List<String> production : rules.get(nt)) {
                Set<String> firstSet = new HashSet<>();
                boolean containsEpsilon = false;

                for (String symbol : production) {
                    Set<String> symbolFirst = findFirst(symbol);
                    firstSet.addAll(symbolFirst);
                    if (!symbolFirst.contains("epsilon")) {
                        break;
                    }
                }

                if (firstSet.contains("epsilon")) {
                    firstSet.remove("epsilon");
                    containsEpsilon = true;
                }

                for (String terminal : firstSet) {
                    if (parsingTable.get(nt).containsKey(terminal)) {
                        isValid = false;
                    }
                    parsingTable.get(nt).put(terminal, String.join(" ", production));
                }

                if (containsEpsilon) {
                    for (String terminal : follow.get(nt)) {
                        if (parsingTable.get(nt).containsKey(terminal)) {
                            isValid = false;
                        }
                        parsingTable.get(nt).put(terminal, "epsilon");
                    }
                }
            }
        }
        return parsingTable;
    }

    public boolean validateString(List<String> tokens, Map<String, Map<String, String>> parsingTable, 
                                String startSymbol, List<String> traceTable) {
        Stack<String> stack = new Stack<>();
        stack.push("$");
        stack.push(startSymbol);

        int index = 0;
        String matched = "";

        while (!stack.isEmpty()) {
            String top = stack.pop();
            String currentToken = tokens.get(index);

            String stackContent = stack.toString();
            String inputRemaining = String.join(" ", tokens.subList(index, tokens.size()));
            String action = "";

            if (top.equals("$") && currentToken.equals("$")) {
                action = "Accept";
                traceTable.add(formatTraceRow(matched, stackContent, inputRemaining, action));
                return true;
            }

            if (top.equals(currentToken)) {
                matched += currentToken + " ";
                index++;
                action = "Match " + currentToken;
            } else if (parsingTable.containsKey(top) && parsingTable.get(top).containsKey(currentToken)) {
                String production = parsingTable.get(top).get(currentToken);
                List<String> rhs = Arrays.asList(production.split(" "));
                action = top + " -> " + production;
                for (int i = rhs.size() - 1; i >= 0; i--) {
                    if (!rhs.get(i).equals("epsilon")) {
                        stack.push(rhs.get(i));
                    }
                }
            } else {
                action = "Error";
                traceTable.add(formatTraceRow(matched, stackContent, inputRemaining, action));
                return false;
            }

            traceTable.add(formatTraceRow(matched, stackContent, inputRemaining, action));
        }

        return false;
    }

    private String formatTraceRow(String matched, String stack, String input, String action) {
        return String.format("%-30s %-40s %-40s %-30s", 
                             matched, stack, input, action);
    }    

    public static Map<String, List<List<String>>> parseGrammar(String inputGrammar) {
        Map<String, List<List<String>>> rules = new HashMap<>();
        for (String line : inputGrammar.split("\n")) {
            if (line.trim().isEmpty()) continue;
            String[] parts = line.split("->");
            String head = parts[0].trim();
            List<List<String>> productions = new ArrayList<>();
            for (String prod : parts[1].split("\\|")) {
                productions.add(Arrays.asList(prod.trim().split(" ")));
            }
            rules.put(head, productions);
        }
        return rules;
    }
}
