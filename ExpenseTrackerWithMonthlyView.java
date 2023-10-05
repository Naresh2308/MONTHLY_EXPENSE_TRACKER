import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.io.*;

public class ExpenseTrackerWithMonthlyView {
    private JFrame frame;
    private JTextField descriptionField;
    private JTextField amountField;
    private JTextArea expenseListArea;
    private JLabel totalExpenseLabel;
    private List<Expense> expenses;
    private double totalExpense;

    public ExpenseTrackerWithMonthlyView() {
        expenses = new ArrayList<>();
        totalExpense = 0.0;

        frame = new JFrame("Expense Tracker");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(3, 2));

        JLabel descriptionLabel = new JLabel("Description:");
        descriptionField = new JTextField(20);
        JLabel amountLabel = new JLabel("Amount:");
        amountField = new JTextField(10);
        JButton addButton = new JButton("Add Expense");
        addButton.addActionListener(new AddExpenseListener());

        JButton monthlyViewButton = new JButton("Show Monthly Expenses");
        monthlyViewButton.addActionListener(new MonthlyViewListener());

        inputPanel.add(descriptionLabel);
        inputPanel.add(descriptionField);
        inputPanel.add(amountLabel);
        inputPanel.add(amountField);
        inputPanel.add(addButton);
        inputPanel.add(monthlyViewButton);

        expenseListArea = new JTextArea();
        expenseListArea.setEditable(false);

        totalExpenseLabel = new JLabel("Total Expense: $0.00");
        totalExpenseLabel.setHorizontalAlignment(JLabel.CENTER);

        frame.add(inputPanel, BorderLayout.NORTH);
        frame.add(new JScrollPane(expenseListArea), BorderLayout.CENTER);
        frame.add(totalExpenseLabel, BorderLayout.SOUTH);
    }

    private class AddExpenseListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String description = descriptionField.getText();
            double amount = parseAmount(amountField.getText());

            if (!description.isEmpty() && amount >= 0) {
                Expense expense = new Expense(description, amount);
                expenses.add(expense);
                totalExpense += amount;
                updateExpenseList();
                updateTotalExpenseLabel();
                clearFields();

                // Save the expense to the monthly file
                saveExpenseToMonthlyFile(expense);
            } else {
                JOptionPane.showMessageDialog(frame, "Invalid input. Please enter a valid description and amount.");
            }
        }
    }

    private class MonthlyViewListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            showMonthlyExpenseDialog();
        }
    }

    private void updateExpenseList() {
        expenseListArea.setText("");
        for (Expense expense : expenses) {
            expenseListArea.append(expense.toString() + "\n");
        }
    }

    private void updateTotalExpenseLabel() {
        DecimalFormat df = new DecimalFormat("#0.00");
        totalExpenseLabel.setText("Total Expense: $" + df.format(totalExpense));
    }

    private void clearFields() {
        descriptionField.setText("");
        amountField.setText("");
    }

    // Save the expense to the monthly file
    private void saveExpenseToMonthlyFile(Expense expense) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM");
        Date currentDate = new Date();
        String monthYear = dateFormat.format(currentDate);
        String filename = monthYear + ".txt";

        try (FileWriter fw = new FileWriter(filename, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.println(expense.getDescription() + "," + expense.getAmount());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Error saving expense to file.");
        }
    }

    // Read expenses from the monthly file and calculate monthly total
    private double calculateMonthlyTotal(String monthYear) {
        double monthlyTotal = 0.0;
        String filename = monthYear + ".txt";

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    double amount = parseAmount(parts[1]);
                    monthlyTotal += amount;
                }
            }
        } catch (IOException e) {
            // File not found or error reading, no expenses for this month yet
        }

        return monthlyTotal;
    }

    private double parseAmount(String text) {
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            return -1.0;
        }
    }

    private void showMonthlyExpenseDialog() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM");
        Date currentDate = new Date();
        String monthYear = dateFormat.format(currentDate);
        String filename = monthYear + ".txt";
        double monthlyTotal = calculateMonthlyTotal(monthYear);

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            StringBuilder monthlyExpenseText = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                monthlyExpenseText.append(line).append("\n");
            }

            if (monthlyExpenseText.length() == 0) {
                JOptionPane.showMessageDialog(frame, "No expenses for this month.");
            } else {
                JTextArea monthlyExpenseTextArea = new JTextArea(monthlyExpenseText.toString());
                monthlyExpenseTextArea.setEditable(false);
                JScrollPane scrollPane = new JScrollPane(monthlyExpenseTextArea);

                JOptionPane.showMessageDialog(frame, scrollPane, "Monthly Expenses (" + monthYear + ")", JOptionPane.PLAIN_MESSAGE);
                DecimalFormat df = new DecimalFormat("#0.00");
                JOptionPane.showMessageDialog(frame, "Monthly Total: $" + df.format(monthlyTotal), "Monthly Expenses (" + monthYear + ")", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Error reading monthly expenses.");
        }
    }

    public void displayGUI() {
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                ExpenseTrackerWithMonthlyView tracker = new ExpenseTrackerWithMonthlyView();
                tracker.displayGUI();
            }
        });
    }

    private class Expense {
        private String description;
        private double amount;

        public Expense(String description, double amount) {
            this.description = description;
            this.amount = amount;
        }

        public String getDescription() {
            return description;
        }

        public double getAmount() {
            return amount;
        }

        @Override
        public String toString() {
            return description + " - $" + amount;
        }
    }
}
