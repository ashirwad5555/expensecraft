import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.awt.image.BufferedImage;

  


public class Expense extends JFrame {

    private String username;
    public int flag = 0;
    private JPanel cardsPanel;
    private JButton addButton;
    private JButton payExpenseButton;
    private JTextField searchField;
    private JButton reportButton; // New button for expense report
    // Map to store category and its respective image URL
    private Map<String, String> categoryImages;

    private ArrayList<ExpenseCard> expenseCards;
    // JDBC Connection details
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/expensecraft";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "Ashirwad@9763"; // Replace with your database password

        public Expense(String username) throws SQLException {
            this.username = username;
            setTitle("Expense Tracker");
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLayout(new BorderLayout());

            // Create cards panel
            cardsPanel = new JPanel();
            cardsPanel.setLayout(new FlowLayout(FlowLayout.LEFT)); // Use FlowLayout
            cardsPanel.setPreferredSize(new Dimension(800, 0)); // Set initial width to 800

            // Create scroll pane for cards panel
            JScrollPane scrollPane = new JScrollPane(cardsPanel);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

            // Add scroll pane to the left side
            add(scrollPane, BorderLayout.CENTER);

            // Create Search Field
            searchField = new JTextField(20);
            JButton searchButton = new JButton("Search");

            // Add ActionListener to Search Button
            searchButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String searchTerm = searchField.getText();
                    if (!searchTerm.isEmpty()) {
                        searchExpenses(searchTerm);
                    } else {
                        // If search term is empty, fetch all expenses (or display a message)
                        fetchExpenses();
                    }
                }
            });

            // Create a panel for search components
            JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            searchPanel.add(new JLabel("Search by Description:"));
            searchPanel.add(searchField);
            searchPanel.add(searchButton);

            // Add searchPanel to the top of the JFrame
            add(searchPanel, BorderLayout.NORTH);

            // Create Report button
            reportButton = new JButton("View Expense Report");
            reportButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    ShowExpenseReport report = new ShowExpenseReport();

                    // Fetch and show expense report for a specific username
                    report.fetchAndShowExpenseReport(username);
                }
            });



            // Create Add button
            addButton = new JButton("+ Add Expense");
            addButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    showAddExpenseForm();
                }
            });

            // Create Pay Expense button
            payExpenseButton = new JButton("Pay Expense");
            payExpenseButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // Show Add Expense form first
                    flag = 1;
                    showAddExpenseForm();
                    // Logic to handle paying expenses (e.g., open payment dialog)
                    // For now, let's display a message

//                    // Show payment dialog
//                    PaymentDialog paymentDialog = new PaymentDialog(Expense.this);
//                    paymentDialog.setVisible(true);
//
//                    // Check if payment was successful
//                    if (!paymentDialog.isVisible()) {
//                        // Payment was completed
//                        // Additional logic here (e.g., mark expenses as paid)
//                        JOptionPane.showMessageDialog(Expense.this, "Payment successful. Expenses paid!");
//                        // Perform any necessary actions after payment (e.g., update database)
//                    }

                    // After adding expense, show payment dialog
//                    SwingUtilities.invokeLater(new Runnable() {
//                        @Override
//                        public void run() {
//                            PaymentDialog paymentDialog = new PaymentDialog(Expense.this);
//                            paymentDialog.setVisible(true);
//
//                            // Check if payment was successful
//                            if (!paymentDialog.isVisible()) {
//                                // Payment was completed
//                                // Additional logic here (e.g., mark expenses as paid)
//                                JOptionPane.showMessageDialog(Expense.this, "Payment successful. Expenses paid!");
//                                // Perform any necessary actions after payment (e.g., update database)
//                                fetchExpenses(); // Refresh expenses after payment
//                            }
//                        }
//                    });


                    //JOptionPane.showMessageDialog(Expense.this, "Payment functionality will be implemented here.");
                }

            });



//            // Add button at the bottom
//            add(addButton, BorderLayout.SOUTH);

            // Create a panel for buttons and add them to the bottom
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.add(addButton);
            buttonPanel.add(payExpenseButton);
            buttonPanel.add(reportButton); // Add report button
            add(buttonPanel, BorderLayout.SOUTH);


            // Fetch and display expenses from the database
            fetchExpenses();

            setSize(800, 600);
            setLocationRelativeTo(null);
            setVisible(true);
        }

    private void showPaymentDialog() {
        PaymentDialog paymentDialog = new PaymentDialog(Expense.this); // Using Expense instance as the parent frame
        paymentDialog.setVisible(true);

        // Check if payment was successful
        if (!paymentDialog.isVisible()) {
            // Payment was completed
            JOptionPane.showMessageDialog(this, "Payment successful. Expenses paid!");
            fetchExpenses();// Refresh expenses after payment
        }

    }
    public void fetchExpenses() {
        // Clear existing expenses
        cardsPanel.removeAll();

        try {
            int userId = getUserId(username); // Retrieve user_id
            String sql = "SELECT * FROM expenses WHERE user_id = ?";
            try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
                 PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, userId);
                ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    String description = resultSet.getString("description");
                    double amount = resultSet.getDouble("amount");
                    String category = resultSet.getString("category");

                    int expenseId = getExpenseId(description, amount, category);
                    if (expenseId != -1) {
                        System.out.println("Expense ID for '" + description + "': " + expenseId);
                    } else {
                        System.out.println("Expense not found in the database.");
                    }

                    ExpenseCard expenseCard = new ExpenseCard(expenseId, description, amount, category);
                    cardsPanel.add(expenseCard);
                }
            }
            cardsPanel.revalidate();
            cardsPanel.repaint();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching expenses.");
        }
    }



    private void showAddExpenseForm() {

        JFrame addExpenseFrame = new JFrame("Add Expense");
        addExpenseFrame.setLayout(new GridLayout(2, 2));

        JLabel descriptionLabel = new JLabel("Description:");
        JTextField descriptionField = new JTextField();
        JLabel amountLabel = new JLabel("Amount:");
        JTextField amountField = new JTextField();
        JLabel categoryLabel = new JLabel("Category:");
        String[] categories = {"Food", "Transportation", "Shopping", "Utilities", "Entertainment"};
        JComboBox<String> categoryDropdown = new JComboBox<>(categories);
        JButton saveButton = new JButton("Save");

        addExpenseFrame.add(descriptionLabel);
        addExpenseFrame.add(descriptionField);
        addExpenseFrame.add(amountLabel);
        addExpenseFrame.add(amountField);
        addExpenseFrame.add(categoryLabel);
        addExpenseFrame.add(categoryDropdown);
        addExpenseFrame.add(saveButton);

        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String description = descriptionField.getText();
                double amount = Double.parseDouble(amountField.getText());
                String category = (String) categoryDropdown.getSelectedItem();

                // Insert expense into the database
                insertExpense( username, description, amount, category);


                if ( flag == 1) {
                    flag = 0;
                    // After saving expense, show the payment dialog

                    showPaymentDialog();
                }

                fetchExpenses();
                // Refresh the cards panel
//                refreshExpenseCards();
                addExpenseFrame.dispose();


            }
        });

        addExpenseFrame.pack();
        addExpenseFrame.setLocationRelativeTo(null);
        addExpenseFrame.setVisible(true);
    }

    private int getUserId(String username) throws SQLException {
        String sql = "SELECT user_id FROM users WHERE username = ?";
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("user_id");
            }
        }
        throw new SQLException("Invalid credentials"); // Handle invalid login
    }

    public int getExpenseId(String description, double amount, String category) {
        int expenseId = -1; // Default value in case expense ID is not found

        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String sql = "SELECT expense_id FROM expenses WHERE description = ? AND amount = ? AND category = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, description);
                statement.setDouble(2, amount);
                statement.setString(3, category);

                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    expenseId = resultSet.getInt("expense_id");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            // Handle database connection or query errors
        }

        return expenseId;
    }


    private void insertExpense(String username, String description, double amount, String category) {
        try {
            int userId = getUserId(username); // Retrieve user_id
            String sql = "INSERT INTO expenses (user_id, description, amount, category) VALUES (?, ?, ?, ?)";
            try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
                 PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, userId);
                statement.setString(2, description);
                statement.setDouble(3, amount);
                statement.setString(4, category);
                statement.executeUpdate();
            }
            JOptionPane.showMessageDialog(this, "Expense added successfully!");

            //fetchExpenses(); // Refresh expenses for the current user
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error adding expense.");
        }
    }


    private void refreshExpenseCards() {
        // Clear existing cards
        cardsPanel.removeAll();

        // Fetch expenses from database and create new cards
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String sql = "SELECT * FROM expenses";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                // Execute query and create cards
                // You can customize this part based on how you retrieve expenses from the database
                // and create ExpenseCard objects
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "An error occurred while fetching expenses.");
        }

        // Revalidate and repaint the cards panel
        cardsPanel.revalidate();
        cardsPanel.repaint();
    }

    private void searchExpenses(String searchTerm) {
        // Clear existing expenses
        cardsPanel.removeAll();

        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String sql = "SELECT * FROM expenses WHERE description LIKE ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, "%" + searchTerm + "%");
                ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    String description = resultSet.getString("description");
                    double amount = resultSet.getDouble("amount");
                    String category = resultSet.getString("category");

                    int expenseId = getExpenseId(description, amount, category);
                    if (expenseId != -1) {
                        System.out.println("Expense ID for '" + description + "': " + expenseId);
                    } else {
                        System.out.println("Expense not found in the database.");
                    }

                    ExpenseCard expenseCard = new ExpenseCard(expenseId, description, amount, category);
                    cardsPanel.add(expenseCard);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "An error occurred while searching expenses.");
        }

        // Refresh the cards panel
        cardsPanel.revalidate();
        cardsPanel.repaint();
    }

    public double getTotalExpense() {
        double totalExpense = 0.0;

        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String sql = "SELECT SUM(amount) AS total FROM expenses WHERE username = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, username);
                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    totalExpense = resultSet.getDouble("total");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return totalExpense;
    }

        public void main(String[] args) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        new Expense(username);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
    }

    class ExpenseCard extends JPanel {


        private final int expenseId;
        private JLabel categoryLabel;
        private JLabel amountLabel;
        private JLabel descriptionLabel; // New JLabel for description
        private JButton editButton;
        private JButton deleteButton;
        private BufferedImage categoryImage;

        private Map<String, String> categoryImages;

        public ExpenseCard(int expenseId, String description, double amount, String category) {
            this.expenseId = expenseId; // Set the expense ID
            initializeCategoryImages(); // Initialize category images map
            setLayout(new BorderLayout());
            setPreferredSize(new Dimension(300, 170));
            setOpaque(false); // Ensure the panel is opaque
            setBackground(Color.PINK);
            setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));

            categoryLabel = new JLabel("Category: " + category);
            amountLabel = new JLabel("Amount: Rs. " + amount);
            descriptionLabel = new JLabel("<html>Description: " + description + "</html>");


            // Set category image
            if (categoryImages.containsKey(category)) {
                String imageUrl = categoryImages.get(category);
                try {
                    ImageIcon icon = createImageIcon(imageUrl);
                    if (icon != null) {
                        JLabel categoryImageLabel = new JLabel(icon);
                        add(categoryImageLabel, BorderLayout.WEST);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            editButton = new JButton("Edit");
            deleteButton = new JButton("Delete");

            customizeButton(editButton);
            customizeButton(deleteButton);

            JPanel labelPanel = new JPanel();
            labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.Y_AXIS));
            labelPanel.add(categoryLabel);
            labelPanel.add(amountLabel);
            labelPanel.add(descriptionLabel);

            JPanel buttonPanel = new JPanel(new GridLayout(1, 2));
            buttonPanel.add(editButton);
            buttonPanel.add(deleteButton);

            add(labelPanel, BorderLayout.CENTER);
            add(buttonPanel, BorderLayout.SOUTH);

            // Add action listeners for edit and delete buttons
            editButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    editExpense();
                }
            });

            deleteButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    deleteExpense();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (categoryImage != null) {
                // Scale and draw the image to fit the panel size
                int imgWidth = categoryImage.getWidth();
                int imgHeight = categoryImage.getHeight();
                int panelWidth = getWidth();
                int panelHeight = getHeight();

                // Calculate the scaling factors to fit the image into the panel
                double scaleX = (double) panelWidth / imgWidth;
                double scaleY = (double) panelHeight / imgHeight;
                double scale = Math.max(scaleX, scaleY);

                int scaledWidth = (int) (scale * imgWidth);
                int scaledHeight = (int) (scale * imgHeight);

                int x = (panelWidth - scaledWidth) / 2;
                int y = (panelHeight - scaledHeight) / 2;

                g.drawImage(categoryImage, x, y, scaledWidth, scaledHeight, this);
            }
        }
        private void editExpense() {
            // Create dialog to input new amount and description
            JTextField amountField = new JTextField(String.valueOf(getAmount()));
            JTextField descriptionField = new JTextField(getDescription());
            JPanel inputPanel = new JPanel(new GridLayout(2, 2));
            inputPanel.add(new JLabel("New Amount:"));
            inputPanel.add(amountField);
            inputPanel.add(new JLabel("New Description:"));
            inputPanel.add(descriptionField);

            int result = JOptionPane.showConfirmDialog(this, inputPanel, "Edit Expense",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                // Update expense in the database
                double newAmount = Double.parseDouble(amountField.getText());
                String newDescription = descriptionField.getText();
                updateExpenseInDatabase(newAmount, newDescription);
            }
        }

        private void deleteExpense() {
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this expense?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                // Delete the expense from the database
                deleteExpenseFromDatabase(expenseId);
                // Remove this ExpenseCard from its parent container (e.g., a JScrollPane)
                Container parent = getParent();
                if (parent instanceof JScrollPane) {
                    JScrollPane scrollPane = (JScrollPane) parent;
                    JViewport viewport = scrollPane.getViewport();
                    viewport.remove(this);
                    viewport.revalidate();
                    viewport.repaint();
                }
            }
        }

        private void customizeButton(JButton button) {
            button.setBackground(new Color(51, 153, 255));
            button.setForeground(Color.WHITE);
            button.setBorder(BorderFactory.createLineBorder(new Color(0, 102, 204)));
            button.setFont(new Font("Arial", Font.BOLD, 12));
        }

        private ImageIcon createImageIcon(String imageUrl) throws IOException {
            URL url = new URL(imageUrl);
            BufferedImage image = ImageIO.read(url);
            if (image != null) {
                return new ImageIcon(image);
            }
            return null;
        }

        private void updateExpenseInDatabase(double newAmount, String newDescription) {
            try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/expensecraft",
                    "root", "Ashirwad@9763")) {
                String sql = "UPDATE expenses SET amount = ?, description = ? WHERE expense_id = ?";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setDouble(1, newAmount);
                    statement.setString(2, newDescription);
                    statement.setInt(3, expenseId);
                    int rowsUpdated = statement.executeUpdate();
                    if (rowsUpdated > 0) {
                        // Update UI after database update
                        amountLabel.setText("Amount: $" + newAmount);
                        descriptionLabel.setText("Description: " + newDescription);
                        revalidate(); // Refresh UI
                        JOptionPane.showMessageDialog(this, "Expense updated successfully.");
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to update expense.");
                    }
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "An error occurred while updating the expense.");
            }
        }

        public double getAmount() {
            // Get the text from the amountLabel
            String amountLabelText = amountLabel.getText();

            // Extract the numeric part of the amount (remove non-numeric characters)
            String numericAmountText = amountLabelText.replaceAll("[^\\d.]", ""); // Keep digits and decimal point

            // Parse the numeric text to a double
            try {
                return Double.parseDouble(numericAmountText);
            } catch (NumberFormatException e) {
                // Handle parsing error gracefully (e.g., log the error)
                e.printStackTrace();
                return 0.0; // Default value if parsing fails
            }
        }

        public String getDescription() {
            return descriptionLabel.getText().replace("Description: ", "");
        }

        private void deleteExpenseFromDatabase(int expenseId) {
            try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/expensecraft",
                    "root", "Ashirwad@9763")) {
                String sql = "DELETE FROM expenses WHERE expense_id = ?";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setInt(1, expenseId);
                    int rowsDeleted = statement.executeUpdate();
                    if (rowsDeleted > 0) {
                        JOptionPane.showMessageDialog(this, "Expense deleted successfully.");

                        // Remove the ExpenseCard from its parent container (assuming it's a JPanel)
                        Container parentContainer = getParent();
                        if (parentContainer instanceof JPanel) {
                            Component[] components = ((JPanel) parentContainer).getComponents();
                            for (Component component : components) {
                                if (component instanceof ExpenseCard) {
                                    ExpenseCard expenseCard = (ExpenseCard) component;
                                    if (expenseCard.expenseId == expenseId) {
                                        ((JPanel) parentContainer).remove(expenseCard);
                                        break;
                                    }
                                }
                            }

                            // Revalidate and repaint the parent container to reflect the changes
                            parentContainer.revalidate();
                            parentContainer.repaint();
                        }
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to delete expense.");
                    }
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "An error occurred while deleting the expense.");
            }
        }

        // Method to initialize category images
        public void initializeCategoryImages() {
            categoryImages = new HashMap<>();
            categoryImages.put("Food", "https://via.placeholder.com/150/FF0000/FFFFFF/?text=Food"); // Placeholder image URL for Food category
            categoryImages.put("Transportation", "https://via.placeholder.com/150/00FF00/FFFFFF/?text=Transportation"); // Placeholder image URL for Transportation category
            categoryImages.put("Shopping", "https://via.placeholder.com/150/0000FF/FFFFFF/?text=Shopping"); // Placeholder image URL for Shopping category
            categoryImages.put("Utilities", "https://via.placeholder.com/150/FFFF00/000000/?text=Utilities"); // Placeholder image URL for Utilities category
            categoryImages.put("Entertainment", "https://via.placeholder.com/150/FF00FF/FFFFFF/?text=Entertainment"); // Placeholder image URL for Entertainment category
        }


    }
