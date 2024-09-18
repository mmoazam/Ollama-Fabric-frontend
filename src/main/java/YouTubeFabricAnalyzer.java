import lombok.Value;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.Properties;


public class YouTubeFabricAnalyzer {

    public static void main(String[] args) throws IOException {
        // Create the frame
        JFrame frame = new JFrame("Directory Selector");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);

        // Create components
        JComboBox<String> directoryComboBox1 = new JComboBox<>();
        JComboBox<String> directoryComboBox2 = new JComboBox<>();
        JTextField inputField = new JTextField();
        inputField.setToolTipText("Enter a youtube url here");
        JButton selectButton = new JButton("Run Command");

        // Large text area for showing output
        JTextArea outputArea = new JTextArea(10, 40); // 10 rows, 40 columns
        outputArea.setEditable(false); // Make it read-only
        outputArea.setLineWrap(true); // Enable line wrapping
        JScrollPane scrollPane = new JScrollPane(outputArea); // Add scrolling functionality

        // Layout components
        frame.setLayout(new BorderLayout());

        JPanel controlPanel = new JPanel(new GridLayout(4, 1)); // Panel for inputs
        controlPanel.add(inputField); // Add the input text field
        controlPanel.add(directoryComboBox1);
        controlPanel.add(directoryComboBox2);
        controlPanel.add(selectButton);

        frame.add(controlPanel, BorderLayout.NORTH);  // Add the controls to the top
        frame.add(scrollPane, BorderLayout.CENTER);   // Add the output area in the center

        // Populate the first combo box with directory names
        //String path1 = "C:\\Users\\user\\.config\\fabric\\patterns";  // Replace with your first directory path
        Properties props = new Properties();
        props.load(YouTubeFabricAnalyzer.class.getClassLoader().getResourceAsStream("application.properties"));
        String path1 = props.getProperty("app.fabric.pattern.path");

        populateComboBoxWithDirectories(directoryComboBox1, path1);
        directoryComboBox1.setSelectedItem("extract_wisdom");

        // Populate the second combo box with the NAME column from 'ollama list'
        populateComboBoxWithOllamaList(directoryComboBox2);
        directoryComboBox2.setSelectedItem("gemma2:27b");

        // Add action listener for the single button
        selectButton.addActionListener(e -> {
            String selectedDirectory1 = (String) directoryComboBox1.getSelectedItem();
            String selectedDirectory2 = (String) directoryComboBox2.getSelectedItem();
            String userInput = inputField.getText(); // Get user input from the text field

            if (selectedDirectory1 != null && selectedDirectory2 != null && userInput != null && !userInput.isEmpty()) {
                // Clear the output area before running the command
                outputArea.setText("");

                // Build and run the command with user input and environment variable
                runCommand(selectedDirectory1, selectedDirectory2, userInput, outputArea);
            } else {
                outputArea.setText("Please select both directories and enter a string.");
            }
        });

        // Display the frame
        frame.setVisible(true);
    }

    // Helper method to populate JComboBox with directories
    public static void populateComboBoxWithDirectories(JComboBox<String> comboBox, String path) {
        File directory = new File(path);

        if (directory.exists() && directory.isDirectory()) {
            File[] subDirs = directory.listFiles(File::isDirectory);
            if (subDirs != null) {
                for (File dir : subDirs) {
                    comboBox.addItem(dir.getName());
                }
            } else {
                comboBox.addItem("No subdirectories found");
            }
        } else {
            comboBox.addItem("Directory not found");
        }
    }

    // Method to populate comboBox2 with the NAME column from 'ollama list'
    public static void populateComboBoxWithOllamaList(JComboBox<String> comboBox) {
        try {
            // Run 'ollama list' command
            ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", "ollama list");
            builder.redirectErrorStream(true); // Merge stderr with stdout
            Process process = builder.start();

            // Read the command output
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            boolean isHeader = true;  // Skip the header line
            while ((line = reader.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;  // Skip the first line
                    continue;
                }

                // Split the line by whitespace and take the first column (NAME)
                String[] columns = line.split("\\s+");
                if (columns.length > 0) {
                    String nameColumn = columns[0];  // Extract NAME column
                    comboBox.addItem(nameColumn);    // Add to comboBox2
                }
            }
            process.waitFor();
        } catch (IOException | InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    // Method to execute the command with environment variable and show output in JTextArea
    public static void runCommand(String selectedDirectory1, String selectedDirectory2, String userInput, JTextArea outputArea) {
        // Construct the command with arguments, including user input
        String command = "yt --transcript " + userInput  +
                " | fabric --stream --model=" + selectedDirectory2 +
                " --pattern " + selectedDirectory1;
        System.out.println(command);

        try {
            // Set the environment variable OLLAMA_HOST=0.0.0.0
            ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", command);
            builder.environment().put("OLLAMA_HOST", "0.0.0.0");

            builder.redirectErrorStream(true); // Merge stdout and stderr
            Process process = builder.start();

            // Read the output and append to the JTextArea
            InputStream inputStream = process.getInputStream();
            new Thread(() -> {
                try {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        String output = new String(buffer, 0, bytesRead);
                        outputArea.append(output);  // Append the output to the JTextArea
                    }
                } catch (IOException e) {
                    outputArea.append("Error reading process output: " + e.getMessage());
                }
            }).start();
        } catch (IOException ex) {
            outputArea.append("Error executing command: " + ex.getMessage());
        }
    }
}
