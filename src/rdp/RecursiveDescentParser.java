package rdp;
import java.util.*;

public class RecursiveDescentParser {
    private final Map<String, List<List<String>>> grammar;
    private final String startSymbol;
    private List<String> inputString;
    private int currentTokenIndex;
    private final Map<String, Object> parseTree;

    public RecursiveDescentParser(String grammarInput, String startSymbol) {
        this.grammar = parseGrammar(grammarInput);
        this.startSymbol = startSymbol;
        this.parseTree = new LinkedHashMap<>();
    }

    private Map<String, List<List<String>>> parseGrammar(String grammarInput) {
        Map<String, List<List<String>>> rules = new LinkedHashMap<>();
        String[] lines = grammarInput.split("\\n");

        for (String line : lines) {
            String[] parts = line.split("->");
            String head = parts[0].trim();
            String[] productions = parts[1].split("\\|");

            rules.putIfAbsent(head, new ArrayList<>());
            for (String production : productions) {
                List<String> symbols = Arrays.asList(production.trim().split(" "));
                rules.get(head).add(symbols);
            }
        }
        return rules;
    }

    public boolean validateString(String inputString) {
        this.inputString = new ArrayList<>(List.of(inputString.split(" ")));
        this.inputString.add("$");
        this.currentTokenIndex = 0;
        this.parseTree.clear();
        this.parseTree.put(this.startSymbol, new ArrayList<>());

        boolean valid = parseNonTerminal(this.startSymbol, (List<Object>) this.parseTree.get(this.startSymbol));
        boolean atEnd = currentToken().equals("$");

        return valid && atEnd;
    }

    private boolean parseNonTerminal(String nonTerminal, List<Object> subtree) {
        int originalIndex = this.currentTokenIndex;
        List<List<String>> productions = this.grammar.getOrDefault(nonTerminal, new ArrayList<>());

        for (List<String> production : productions) {
            List<Object> tempTree = new ArrayList<>();
            this.currentTokenIndex = originalIndex;

            boolean allMatched = true;
            for (String symbol : production) {
                if (!parseSymbol(symbol, tempTree)) {
                    allMatched = false;
                    break;
                }
            }

            if (allMatched) {
                subtree.add(Map.of(nonTerminal, tempTree));
                return true;
            }
        }
        return false;
    }

    private boolean parseSymbol(String symbol, List<Object> subtree) {
        if (this.grammar.containsKey(symbol)) {
            List<Object> childTree = new ArrayList<>();
            if (parseNonTerminal(symbol, childTree)) {
                subtree.add(Map.of(symbol, childTree));
                return true;
            }
            return false;
        } else if (symbol.equals("epsilon")) {
            return true;
        } else { // Terminal
            if (currentToken().equals(symbol)) {
                subtree.add(symbol);
                this.currentTokenIndex++;
                return true;
            }
            return false;
        }
    }

    private String currentToken() {
        if (this.currentTokenIndex < this.inputString.size()) {
            return this.inputString.get(this.currentTokenIndex);
        }
        return null;
    }

    public Map<String, Object> getParseTree() {
        return this.parseTree;
    }

    public String formatParseTree(Object tree, int level, StringBuilder sb) {
        if (tree instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) tree;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                indent(sb, level);
                sb.append(entry.getKey()).append(":\n");
                formatParseTree(entry.getValue(), level + 1, sb);
            }
        } else if (tree instanceof List) {
            List<?> list = (List<?>) tree;
            for (Object item : list) {
                formatParseTree(item, level, sb);
            }
        } else {
            indent(sb, level);
            sb.append(tree).append("\n");
        }
    
        return sb.toString();
    }
    
    private void indent(StringBuilder sb, int level) {
        sb.append(" ".repeat(level * 2));
    }
    
    public String getFormattedParseTree() {
        StringBuilder sb = new StringBuilder();
        return formatParseTree(this.parseTree, 0, sb);
    }
    
}
