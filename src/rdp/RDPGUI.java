package rdp;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RDPGUI extends JFrame {
    private final JTextArea grammarInputArea;
    private final JTextField startSymbolField;
    private final JTextField inputStringField;
    private final JTextArea resultArea;

    public RDPGUI() {
        setTitle("Recursive Descent Parser");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 500);
        setLocationRelativeTo(null); 

        JLabel grammarLabel = new JLabel("Enter Grammar:");
        grammarInputArea = new JTextArea(5, 40);
        JScrollPane grammarScrollPane = new JScrollPane(grammarInputArea);

        JLabel startSymbolLabel = new JLabel("Start Symbol:");
        startSymbolField = new JTextField(10);

        JLabel inputStringLabel = new JLabel("Input String:");
        inputStringField = new JTextField(20);

        JButton validateButton = new JButton("Validate using RDP");

        resultArea = new JTextArea(12, 50);
        resultArea.setEditable(false);
        JScrollPane resultScrollPane = new JScrollPane(resultArea);

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
        inputPanel.add(grammarLabel);
        inputPanel.add(grammarScrollPane);

        JPanel smallInputPanel = new JPanel();
        smallInputPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        smallInputPanel.add(startSymbolLabel);
        smallInputPanel.add(startSymbolField);
        smallInputPanel.add(inputStringLabel);
        smallInputPanel.add(inputStringField);

        inputPanel.add(Box.createVerticalStrut(10));
        inputPanel.add(smallInputPanel);
        inputPanel.add(Box.createVerticalStrut(10));
        inputPanel.add(validateButton);
        inputPanel.add(Box.createVerticalStrut(10));
        inputPanel.add(new JLabel("Output:"));
        inputPanel.add(resultScrollPane);

        add(inputPanel);

        validateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                validateInput();
            }
        });
    }

    private void validateInput() {
        String grammar = grammarInputArea.getText();
        String startSymbol = startSymbolField.getText().trim();
        String inputString = inputStringField.getText().trim();
    
        if (grammar.isEmpty() || startSymbol.isEmpty() || inputString.isEmpty()) {
            resultArea.append("Please fill out all fields.\n");
            return;
        }
    
        try {
            RecursiveDescentParser parser = new RecursiveDescentParser(grammar, startSymbol);
            boolean isValid = parser.validateString(inputString);
    
            String formattedTree = parser.getFormattedParseTree();
    
            resultArea.append("Input String: " + inputString + "\n");
            resultArea.append("Parse Tree:\n" + formattedTree + "\n");
            resultArea.append("Result: " + (isValid ? "VALID" : "INVALID") + "\n");
            resultArea.append("-----------------------------------------------------\n");
        } catch (Exception ex) {
            resultArea.append("Error: " + ex.getMessage() + "\n");
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            RDPGUI gui = new RDPGUI();
            gui.setVisible(true);
        });
    }
}
