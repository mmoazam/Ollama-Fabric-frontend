import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class CommandRunnerGUI {

    public static void main(String[] args) {
        // Create the frame
        JFrame frame = new JFrame("Windows Command Runner");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);

        // Create components
        JTextField commandField = new JTextField();
        JTextArea outputArea = new JTextArea();
        outputArea.setEditable(false);
        JButton runButton = new JButton("Run Command");

        // Layout components
        frame.setLayout(new BorderLayout());
        frame.add(commandField, BorderLayout.NORTH);
        frame.add(new JScrollPane(outputArea), BorderLayout.CENTER);
        frame.add(runButton, BorderLayout.SOUTH);

        // Action listener for the button
        runButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String command = commandField.getText();
                if (!command.isEmpty()) {
                    try {
                        // Execute the command
                        ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", command);
                        builder.redirectErrorStream(true);
                        Process process = builder.start();

                        // Capture output
                        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                        String line;
                        StringBuilder output = new StringBuilder();
                        while ((line = reader.readLine()) != null) {
                            output.append(line).append("\n");
                        }
                        outputArea.setText(output.toString());

                    } catch (Exception ex) {
                        outputArea.setText("Error: " + ex.getMessage());
                    }
                } else {
                    outputArea.setText("Please enter a command.");
                }
            }
        });

        // Display the frame
        frame.setVisible(true);
    }
}
