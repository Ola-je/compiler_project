package LL1;
import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class LL1ParserGUI {
    private LL1Parser parser;
    private JFrame frame;
    private JTextArea grammarInput;
    private JTextField startSymbolInput;
    private JTextArea outputArea;
    private JTextField inputStringField;

    public LL1ParserGUI() {
        frame = new JFrame("LL(1) Parser");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        JPanel grammarPanel = new JPanel(new BorderLayout());
        grammarInput = new JTextArea(10, 30);
        grammarPanel.add(new JLabel("Enter Grammar Rules:"), BorderLayout.NORTH);
        grammarPanel.add(new JScrollPane(grammarInput), BorderLayout.CENTER);

        JPanel startSymbolPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        startSymbolPanel.add(new JLabel("Start Symbol:"));
        startSymbolInput = new JTextField(10);
        startSymbolPanel.add(startSymbolInput);
        grammarPanel.add(startSymbolPanel, BorderLayout.SOUTH);

        mainPanel.add(grammarPanel, BorderLayout.NORTH);

        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton generateTableButton = new JButton("Generate Parsing Table");
        generateTableButton.addActionListener(e -> generateParsingTable());
        JButton validateStringButton = new JButton("Validate String");
        validateStringButton.addActionListener(e -> validateInputString());

        actionsPanel.add(generateTableButton);
        actionsPanel.add(new JLabel("Input String:"));
        inputStringField = new JTextField(20);
        actionsPanel.add(inputStringField);
        actionsPanel.add(validateStringButton);

        mainPanel.add(actionsPanel, BorderLayout.CENTER);

        outputArea = new JTextArea(15, 60);
        outputArea.setEditable(false);
        mainPanel.add(new JScrollPane(outputArea), BorderLayout.SOUTH);

        frame.add(mainPanel);
        frame.setVisible(true);
    }

    private void generateParsingTable() {
        String grammar = grammarInput.getText().trim();
        String startSymbol = startSymbolInput.getText().trim();

        if (grammar.isEmpty() || startSymbol.isEmpty()) {
            showAlert("Error", "Please enter grammar rules and start symbol.");
            return;
        }

        try {
            Map<String, List<List<String>>> rules = LL1Parser.parseGrammar(grammar);
            parser = new LL1Parser(rules, startSymbol);
            Map<String, Map<String, String>> parsingTable = parser.generateParsingTable(startSymbol);

            StringBuilder tableOutput = new StringBuilder();
            tableOutput.append("Parsing Table:\n\n");

            Set<String> terminals = parser.findTerminals();
            terminals.add("$");

            tableOutput.append("\t");
            for (String terminal : terminals) {
                tableOutput.append(terminal).append("\t");
            }
            tableOutput.append("\n");

            for (String nonTerminal : parser.getNonTerminals()) {
                tableOutput.append(nonTerminal).append("\t");
                for (String terminal : terminals) {
                    String entry = parsingTable.getOrDefault(nonTerminal, new HashMap<>()).get(terminal);
                    tableOutput.append(entry != null ? entry : "-").append("\t");
                }
                tableOutput.append("\n");
            }

            appendToOutputArea(tableOutput.toString());
        } catch (Exception e) {
            appendToOutputArea("Error generating parsing table: " + e.getMessage());
        }
    }

    private void validateInputString() {
        String inputString = inputStringField.getText().trim();
        String startSymbol = startSymbolInput.getText().trim();
    
        if (parser == null) {
            showAlert("Error", "Please generate the parsing table first.");
            return;
        }
    
        if (inputString.isEmpty()) {
            showAlert("Error", "Please enter an input string to validate.");
            return;
        }
    
        List<String> tokens = Arrays.asList((inputString + " $").split(" "));
        List<String> traceTable = new ArrayList<>();
    
        try {
            Map<String, Map<String, String>> parsingTable = parser.generateParsingTable(startSymbol);
            boolean isValid = parser.validateString(tokens, parsingTable, startSymbol, traceTable);
    
            appendToOutputArea("\nTrace Table:\n");
            appendToOutputArea(String.format("%-30s %-40s %-40s %-30s\n", "Matched", "Stack", "Input String", "Action"));
            appendToOutputArea("=".repeat(100) + "\n");
            for (String step : traceTable) {
                appendToOutputArea(step + "\n");
            }
    
            appendToOutputArea(isValid ? "\nThe string is VALID." : "\nThe string is INVALID.");
        } catch (Exception e) {
            appendToOutputArea("Error validating string: " + e.getMessage());
        }
    }
    
    private void appendToOutputArea(String text) {
        outputArea.append(text + "\n");
        outputArea.setCaretPosition(outputArea.getDocument().getLength()); 
    }

    private void showAlert(String title, String message) {
        JOptionPane.showMessageDialog(frame, message, title, JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LL1ParserGUI::new);
    }
}