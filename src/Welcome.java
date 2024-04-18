import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Welcome extends JFrame {
    private Timer timer;

    public Welcome() {
        setTitle("Welcome");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Load and display the welcome GIF
        ImageIcon welcomeGifIcon = new ImageIcon("D:/java_swing/expensecraft/src/assets/img1.gif"); // Change to your GIF file path
        JLabel welcomeGifLabel = new JLabel(welcomeGifIcon);
        add(welcomeGifLabel, BorderLayout.CENTER);

        // Create a panel for the Next button
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false); // Make the panel transparent

        // Create and configure the Next button
        JButton nextButton = new JButton("Next");
        nextButton.setFont(new Font("Arial", Font.BOLD, 16));
        nextButton.setForeground(Color.WHITE);
        nextButton.setBackground(new Color(0, 102, 204)); // Blue color
        nextButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20)); // Add padding
        nextButton.setVisible(false); // Initially invisible

        // Add the Next button to the button panel
        buttonPanel.add(nextButton);

        // Add the button panel to the frame's south position
        add(buttonPanel, BorderLayout.SOUTH);

        // Set frame size and center it on the screen
        setSize(800, 600); // Adjust the size as needed
        setLocationRelativeTo(null); // Center the frame on the screen
        setVisible(true);

        // Timer to show the Next button after 2 seconds
        timer = new Timer(2000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                nextButton.setVisible(true); // Show the Next button
                timer.stop(); // Stop the timer after showing the button
            }
        });
        timer.start(); // Start the timer

        // Add action listener to the Next button
        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Open the Expense page
              new LoginSignupPage().setVisible(true);
                // Close the Welcome page
                dispose();
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Welcome();
            }
        });
    }
}
