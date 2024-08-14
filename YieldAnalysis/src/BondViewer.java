
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class BondViewer extends JFrame {
	private JTextField faceValueField;
    private JTextField couponRateField;
    private JTextField maturityDateField;
    private JComboBox<String> couponFrequencyField;
    private JTextField settlementDateField;
    private JTextField priceField;
    private JTextField ytmField;
    private JLabel resultLabel;
    private JLabel modDurationLabel;
    private JLabel macDurationLabel;
    private JLabel convexityLabel;
    private JLabel tenorLabel;
    private JLabel benchmarkYieldLabel;
    private JLabel spreadLabel;
    private JPanel chartPanel;
    
    private Bond bond;

    private boolean priceChanged = false;
    private boolean ytmChanged = false;
    
    private static DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.00");
    private static DecimalFormat SPREAD_FORMAT = new DecimalFormat("#");

    public BondViewer() {
    	setTitle("Bond Calculator");
        setSize(1000, 650);
        setLocationRelativeTo(null);
        ToolTipManager.sharedInstance().setInitialDelay(100);

        JPanel mainPanel = new JPanel(new BorderLayout());

        JPanel inputPanel = generateInputPanel();

        mainPanel.add(inputPanel, BorderLayout.CENTER);
        
        JPanel resultAndChartPanel = new JPanel(new BorderLayout());
        
        JPanel resultPanel = generateResultPanel();
        
        resultAndChartPanel.add(resultPanel, BorderLayout.WEST);
        
        chartPanel = new JPanel(new BorderLayout());
        chartPanel.setPreferredSize(new Dimension(700, 400));
        
        resultAndChartPanel.add(chartPanel, BorderLayout.CENTER);

        mainPanel.add(resultAndChartPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // Listen to the price and yield fields to see if anything's changed
        // Need this to determine what gets calculated
        priceField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                priceChanged = true;
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                priceChanged = false;
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                priceChanged = true;
            }
        });

        ytmField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                ytmChanged = true;
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                ytmChanged = false;
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                ytmChanged = true;
            }
        });

        
    }
    
    private JPanel generateInputPanel() {
    	JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));

        // First row of inputs
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel faceValueLabel = new JLabel("Face Value:");
        faceValueLabel.setPreferredSize(new Dimension(150, faceValueLabel.getPreferredSize().height));
        row1.add(faceValueLabel);
        faceValueField = new JTextField(10);
        row1.add(faceValueField);

        JLabel couponRateLabel = new JLabel("Coupon Rate (%):");
        couponRateLabel.setPreferredSize(new Dimension(150, couponRateLabel.getPreferredSize().height));
        row1.add(couponRateLabel);
        couponRateField = new JTextField(10);
        row1.add(couponRateField);
        inputPanel.add(row1);

        // Second row of inputs
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel maturityDateLabel = new JLabel("Maturity Date:");
        maturityDateLabel.setPreferredSize(new Dimension(150, maturityDateLabel.getPreferredSize().height));
        maturityDateLabel.setToolTipText("Enter the date in the format YYYY-MM-DD");
        row2.add(maturityDateLabel);
        maturityDateField = new JTextField(10);
        row2.add(maturityDateField);

        JLabel couponFrequencyLabel = new JLabel("Coupon Frequency:");
        couponFrequencyLabel.setPreferredSize(new Dimension(150, couponFrequencyLabel.getPreferredSize().height));
        row2.add(couponFrequencyLabel);
        couponFrequencyField = new JComboBox<>();
        couponFrequencyField.addItem("Annually");
        couponFrequencyField.addItem("Semi-Annually");
        couponFrequencyField.addItem("Quarterly");
        couponFrequencyField.addItem("Monthly");
        row2.add(couponFrequencyField);
        inputPanel.add(row2);

        // Third row of inputs
        JPanel row3 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel settlementDateLabel = new JLabel("Settlement Date:");
        settlementDateLabel.setPreferredSize(new Dimension(150, settlementDateLabel.getPreferredSize().height));
        settlementDateLabel.setToolTipText("Enter the date in the format YYYY-MM-DD");
        row3.add(settlementDateLabel);
        settlementDateField = new JTextField(10);
        row3.add(settlementDateField);

        JLabel priceLabel = new JLabel("Price:");
        priceLabel.setPreferredSize(new Dimension(150, priceLabel.getPreferredSize().height));
        row3.add(priceLabel);
        priceField = new JTextField(10);
        row3.add(priceField);
        inputPanel.add(row3);

        // Fourth row of inputs
        JPanel row4 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel ytmLabel = new JLabel("YTM (%):");
        ytmLabel.setPreferredSize(new Dimension(150, ytmLabel.getPreferredSize().height));
        row4.add(ytmLabel);
        ytmField = new JTextField(10);
        row4.add(ytmField);
        inputPanel.add(row4);
        
        // Fifth row for the calculate button
        JPanel row5 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton calculateButton = new JButton("Calculate");
        row5.add(calculateButton);
        inputPanel.add(row5);
        
        calculateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                calculate();
            }
        });
        
        return inputPanel;
    }
    
    private JPanel generateResultPanel() {
    	JPanel resultPanel = new JPanel();
        resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.Y_AXIS));
        
        resultLabel = new JLabel("");
        resultLabel.setForeground(Color.RED);
        resultPanel.add(resultLabel);

        resultPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        JLabel durationConvexityTitle = new JLabel("Duration and Convexity");
        durationConvexityTitle.setFont(new Font("Arial", Font.BOLD, 14));
        resultPanel.add(durationConvexityTitle);

        macDurationLabel = new JLabel("Macaulay Duration: ");
        resultPanel.add(macDurationLabel);

        modDurationLabel = new JLabel("Modified Duration: ");
        resultPanel.add(modDurationLabel);

        convexityLabel = new JLabel("Convexity: ");
        resultPanel.add(convexityLabel);

        resultPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        JLabel treasuryYieldSpreadTitle = new JLabel("Treasury Yield and Spread");
        treasuryYieldSpreadTitle.setFont(new Font("Arial", Font.BOLD, 14));
        resultPanel.add(treasuryYieldSpreadTitle);

        tenorLabel = new JLabel("Bond's Tenor: ");
        resultPanel.add(tenorLabel);

        benchmarkYieldLabel = new JLabel("Benchmark Treasury Yield (%): ");
        resultPanel.add(benchmarkYieldLabel);

        spreadLabel = new JLabel("Spread (Bps): ");
        resultPanel.add(spreadLabel);

        return resultPanel;
    }

    private void calculate() {
        try {
        	resultLabel.setText("");
        	
        	double faceValue = validateDouble(faceValueField.getText(), "Face Value");
            double couponRate = validateDouble(couponRateField.getText(), "Coupon Rate") / 100.0;
            
            LocalDate settlementDate = validateDate(settlementDateField.getText(), "Settlement Date");
            LocalDate maturityDate = validateDate(maturityDateField.getText(), "Maturity Date");
            
            if (settlementDate.isAfter(maturityDate)) {
            	clearPreviousResults();
                throw new IllegalArgumentException("Settlement Date cannot be later than Maturity Date.");
            }

            if (settlementDate.isAfter(LocalDate.now())) {
            	clearPreviousResults();
                throw new IllegalArgumentException("Settlement Date cannot be later than today.");
            }
            
            String couponSelection = (String) couponFrequencyField.getSelectedItem();
            int couponFrequency = 0;

            if (couponSelection.equals("Annually")) {
                couponFrequency = 1;
            } else if (couponSelection.equals("Semi-Annually")) {
                couponFrequency = 2;
            } else if (couponSelection.equals("Quarterly")) {
                couponFrequency = 4;
            } else if (couponSelection.equals("Monthly")) {
                couponFrequency = 12;
            }

            if (priceChanged && !ytmChanged) {
            	double price = validateDouble(priceField.getText(), "Price");
                bond = new Bond(faceValue, couponRate, maturityDate, couponFrequency, settlementDate, price, true);
                ytmField.setText(String.valueOf(DECIMAL_FORMAT.format(bond.getYieldToMaturity() * 100)));
            } else if (ytmChanged && !priceChanged) {
            	double ytm = validateDouble(ytmField.getText(), "YTM") / 100;
                bond = new Bond(faceValue, couponRate, maturityDate, couponFrequency, settlementDate, ytm, false);
                priceField.setText(String.valueOf(DECIMAL_FORMAT.format(bond.getPrice())));
            } else if (priceChanged && ytmChanged) {
            	double price = validateDouble(priceField.getText(), "Price");
                bond = new Bond(faceValue, couponRate, maturityDate, couponFrequency, settlementDate, price, true);
                ytmField.setText(String.valueOf(DECIMAL_FORMAT.format(bond.getYieldToMaturity() * 100)));
            } else {
            	double ytm = validateDouble(ytmField.getText(), "YTM") / 100;
                bond = new Bond(faceValue, couponRate, maturityDate, couponFrequency, settlementDate, ytm, false);
                priceField.setText(String.valueOf(DECIMAL_FORMAT.format(bond.getPrice())));
            }

            macDurationLabel.setText("Macaulay Duration: " + DECIMAL_FORMAT.format(bond.getMacaulayDuration()));
            modDurationLabel.setText("Modified Duration: " + DECIMAL_FORMAT.format(bond.getModifiedDuration()));
            convexityLabel.setText("Convexity: " + DECIMAL_FORMAT.format(bond.getConvexity()));
            tenorLabel.setText("Bond's Tenor: " + bond.getTenor());
            benchmarkYieldLabel.setText("Benchmark Treasury Yield (%): " + DECIMAL_FORMAT.format(bond.getTreasuryYield() * 100));
            spreadLabel.setText("Spread (Bps): " + SPREAD_FORMAT.format(bond.getSpread() * 10000));
            updateChart(bond);

            priceChanged = false;
            ytmChanged = false;
        } catch (NumberFormatException ex) {
            clearPreviousResults();
            resultLabel.setText("Please enter valid numeric values.");
        } catch (IllegalArgumentException ex) {
            clearPreviousResults();
            resultLabel.setText(ex.getMessage());
        } catch (Exception ex) {
            clearPreviousResults();
            resultLabel.setText(ex.getMessage());
        }
    }
    
    private void clearPreviousResults() {
        macDurationLabel.setText("Macaulay Duration: ");
        modDurationLabel.setText("Modified Duration: ");
        convexityLabel.setText("Convexity: ");
        tenorLabel.setText("Bond's Tenor: ");
        benchmarkYieldLabel.setText("Benchmark Treasury Yield (%)");
        spreadLabel.setText("Spread (Bps): ");
        chartPanel.removeAll();
        chartPanel.revalidate();
        chartPanel.repaint();
    }
    
    private double validateDouble(String input, String fieldName) throws IllegalArgumentException {
        double value;
        try {
            value = Double.parseDouble(input);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(fieldName + " must be a valid number.");
        }

        if (value < 0) {
            throw new IllegalArgumentException(fieldName + " cannot be negative.");
        }

        return value;
    }

    private LocalDate validateDate(String input, String fieldName) throws IllegalArgumentException {
        try {
            return LocalDate.parse(input, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (Exception e) {
            throw new IllegalArgumentException(fieldName + " must be in the format YYYY-MM-DD.");
        }
    }
    
    private void updateChart(Bond bond) {
    	chartPanel.removeAll();

        PriceYieldChart chart = new PriceYieldChart("Price-Yield Chart", bond);
        chart.setSize(800, 400);
        chartPanel.add(chart.getContentPane(), BorderLayout.CENTER);

        chartPanel.revalidate();
        chartPanel.repaint();
    }

    public static void main(String[] args) {
        JFrame frame = new BondViewer();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
