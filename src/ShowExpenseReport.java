import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.awt.print.Printable;
import java.awt.print.PrinterJob;


public class ShowExpenseReport extends JFrame {

    private JLabel totalExpenseLabel;
    private JPanel categoryExpensesPanel;

    public String username1;

    public ShowExpenseReport() {
        setTitle("Expense Report");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        totalExpenseLabel = new JLabel("Total Expense: $0.00");
        categoryExpensesPanel = new JPanel();
        categoryExpensesPanel.setLayout(new BoxLayout(categoryExpensesPanel, BoxLayout.Y_AXIS));

        add(totalExpenseLabel, BorderLayout.NORTH);
        add(new JScrollPane(categoryExpensesPanel), BorderLayout.CENTER);

        // Create and add the "Download Report" button
        JButton downloadButton = new JButton("Download Report");
        downloadButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    // Assuming you have a method to retrieve the current logged-in username
//                    String username = getCurrentLoggedInUsername(); // Implement this method to get the current username
                    // Call downloadExpenseReport() with the obtained username
                    downloadExpenseReport(username1);
//                  downloadExpenseReport();
                } catch (PrinterException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        add(downloadButton, BorderLayout.SOUTH);

        setSize(400, 300);
        setLocationRelativeTo(null);
        setVisible(true);


    }

    public void fetchAndShowExpenseReport(String username) {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/expensecraft",
                "root", "Ashirwad@9763")) {

            username1 = username;

            // Fetch total expense for the current user
            double totalExpense = getTotalExpenseForUser(connection, username);
            System.out.println(totalExpense);
            totalExpenseLabel.setText(String.format("Total Expense: $%.2f", totalExpense));

            // Fetch category-wise expenses for the current user
            String[] categories = {"Food", "Transportation", "Shopping", "Utilities", "Entertainment"};
            double[] categoryExpenses = getCategoryExpensesForUser(connection, username, categories);

            categoryExpensesPanel.removeAll(); // Clear existing components

            for (int i = 0; i < categories.length; i++) {
                JLabel categoryLabel = new JLabel(categories[i] + ": Rs. " + categoryExpenses[i]);
                categoryExpensesPanel.add(categoryLabel);
            }

            categoryExpensesPanel.revalidate(); // Refresh panel
            categoryExpensesPanel.repaint();

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching expense data: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private int getUserId(String username) throws SQLException {
        String sql = "SELECT user_id FROM users WHERE username = ?";
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/expensecraft",
                "root", "Ashirwad@9763");
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("user_id");
            }
        }
        throw new SQLException("Invalid credentials"); // Handle invalid login
    }

    private double getTotalExpenseForUser(Connection connection, String username) throws SQLException {
        double totalExpense = 0.0;
        String sql = "SELECT SUM(amount) FROM expenses WHERE user_id = ?";
        System.out.println(sql);
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, getUserId(username));
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    totalExpense = resultSet.getDouble(1);
                    System.out.println(totalExpense);
                }
            }
        }
        return totalExpense;

    }

    private double[] getCategoryExpensesForUser(Connection connection, String username, String[] categories)
            throws SQLException {
        double[] categoryExpenses = new double[categories.length];
        String sql = "SELECT category, SUM(amount) FROM expenses WHERE user_id = ? AND category IN (?, ?, ?, ?, ?) GROUP BY category";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, getUserId(username));
            for (int i = 0; i < categories.length; i++) {
                statement.setString(i + 2, categories[i]);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String category = resultSet.getString(1);
                    double expense = resultSet.getDouble(2);
                    for (int i = 0; i < categories.length; i++) {
                        if (categories[i].equals(category)) {
                            categoryExpenses[i] = expense;
                            break;
                        }
                    }
                }
            }
        }
        return categoryExpenses;
    }

private void printExpenseReport(double totalExpense, double[] categoryExpenses) throws PrinterException {
    PrinterJob job = PrinterJob.getPrinterJob();
    job.setPrintable(new ExpenseReportPrinter(totalExpense, categoryExpenses)); // Set the printable object
    boolean printDialogShown = job.printDialog(); // Display print dialog
    if (printDialogShown) {
        job.print(); // Perform the printing operation
    }
}

    // Inner class representing the Printable object for printing the expense report
//    private class ExpenseReportPrinter implements Printable {
//        @Override
//        public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
//            if (pageIndex > 0) {
//                return Printable.NO_SUCH_PAGE;
//            }
//
//            Graphics2D g2d = (Graphics2D) graphics;
//            g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
//
//            // Draw the content of the expense report
//            drawExpenseReport(g2d, pageFormat);
//
//            return Printable.PAGE_EXISTS;
//        }
//
//        private void drawExpenseReport(Graphics2D g2d, PageFormat pageFormat) {
//            // Draw the expense report content (e.g., total expense, category expenses)
//            g2d.setFont(new Font("Arial", Font.BOLD, 14));
//            g2d.drawString("Expense Report", 100, 50);
//            g2d.setFont(new Font("Arial", Font.PLAIN, 12));
//            g2d.drawString("Total Expense: Rs. 100.00", 100, 80);
//            g2d.drawString("Category 1: Rs. 30.00", 100, 100);
//            g2d.drawString("Category 2: Rs. 40.00", 100, 120);
//            // Draw more category expenses as needed
//        }
//    }

    // Inner class representing the Printable object for printing the expense report
    private class ExpenseReportPrinter implements Printable {

        private double totalExpense;
        private double[] categoryExpenses;

        public ExpenseReportPrinter(double totalExpense, double[] categoryExpenses) {
            this.totalExpense = totalExpense;
            this.categoryExpenses = categoryExpenses;
        }

        @Override
        public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
            if (pageIndex > 0) {
                return Printable.NO_SUCH_PAGE;
            }

            Graphics2D g2d = (Graphics2D) graphics;
            g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

            // Draw the content of the expense report using real-time data
            drawExpenseReport(g2d, pageFormat);

            return Printable.PAGE_EXISTS;
        }

        private void drawExpenseReport(Graphics2D g2d, PageFormat pageFormat) {
            g2d.setFont(new Font("Arial", Font.BOLD, 14));
            g2d.drawString("Expense Report", 100, 50);
            g2d.setFont(new Font("Arial", Font.PLAIN, 12));

            // Draw total expense
            g2d.drawString(String.format("Total Expense: Rs. %.2f", totalExpense), 100, 80);

            // Draw category-wise expenses
            int startY = 100;
            int increment = 20;
            String[] categories = {"Food", "Transportation", "Shopping", "Utilities", "Entertainment"};
            for (int i = 0; i < categories.length; i++) {
                g2d.drawString(String.format("%s: Rs. %.2f", categories[i], categoryExpenses[i]), 100, startY);
                startY += increment;
            }
        }
    }


    private void saveExpenseReportToFile(String filename, double totalExpense, String[] categories, double[] categoryExpenses) {
        try (FileWriter writer = new FileWriter(filename)) {
            // Write total expense to the file
            writer.write("Total Expense: Rs. " + String.format("%.2f", totalExpense) + "\n");

            // Write category-wise expenses to the file
            for (int i = 0; i < categories.length; i++) {
                writer.write(categories[i] + ": Rs. " + String.format("%.2f", categoryExpenses[i]) + "\n");
            }

            JOptionPane.showMessageDialog(this, "Expense report saved successfully to " + filename,
                    "Save Success", JOptionPane.INFORMATION_MESSAGE);

        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving expense report: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

//    private void downloadExpenseReport() throws PrinterException {
//        // Method to handle downloading and printing the expense report
//        printExpenseReport(); // Print the expense report
//
//        JOptionPane.showMessageDialog(this, "Expense report downloaded and printed successfully!",
//                "Download Report", JOptionPane.INFORMATION_MESSAGE);
//    }

    private void downloadExpenseReport(String username) throws PrinterException {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/expensecraft",
                "root", "Ashirwad@9763")) {

            // Fetch total expense and category-wise expenses for the specified username
            double totalExpense = getTotalExpenseForUser(connection, username);
            String[] categories = {"Food", "Transportation", "Shopping", "Utilities", "Entertainment"};
            double[] categoryExpenses = getCategoryExpensesForUser(connection, username, categories);

            // Print expense report
            printExpenseReport(totalExpense, categoryExpenses);

            // Alternatively, save expense report to a file
            String filename = "expense_report.txt";
            saveExpenseReportToFile(filename, totalExpense, categories, categoryExpenses);

            JOptionPane.showMessageDialog(this, "Expense report downloaded and processed successfully!",
                    "Download Report", JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error downloading expense report: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }



}
