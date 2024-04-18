import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PaymentDialog extends JDialog {

    private JTextField cardNumberField;
    private JPasswordField cvvField;
    private JTextField expiryField;
    private JButton payButton;

    public PaymentDialog(Frame parent) {


        super(parent, "Payment Details", true);
        setSize(300, 200);
        setLocationRelativeTo(parent);

        JPanel panel = new JPanel(new GridLayout(4, 2));
        panel.add(new JLabel("Card Number:"));
        cardNumberField = new JTextField();
        panel.add(cardNumberField);
        panel.add(new JLabel("CVV:"));
        cvvField = new JPasswordField();
        panel.add(cvvField);
        panel.add(new JLabel("Expiry (MM/YY):"));
        expiryField = new JTextField();
        panel.add(expiryField);

        payButton = new JButton("Pay");
        payButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Process payment here (e.g., call a payment gateway API)
                JOptionPane.showMessageDialog(PaymentDialog.this, "Payment successful!");
                dispose(); // Close the dialog

            }

        });
        panel.add(payButton);

        getContentPane().add(panel, BorderLayout.CENTER);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setVisible(true);
    }

    public String getCardNumber() {
        return cardNumberField.getText();
    }

    public String getCVV() {
        return new String(cvvField.getPassword());
    }

    public String getExpiry() {
        return expiryField.getText();
    }
}
