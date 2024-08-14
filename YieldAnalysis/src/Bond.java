
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Bond {
	
	private double faceValue;
    private double couponRate;
    private LocalDate maturityDate;
    private int couponFrequency;
    private String tenor;
    private double treasuryYield;
    private double bondSpread;
    private LocalDate settlementDate;
    private double macaulayDuration;
    private double modifiedDuration;
    private double convexity;
    private double yieldToMaturity;
    private double price;

    // Need constructors for when price is not avail and when YTM is not avail?
    public Bond(double faceValue, double couponRate, LocalDate maturityDate, int couponFrequency, LocalDate settlementDate, double value, boolean isPrice) {
        this.faceValue = faceValue;
        this.couponRate = couponRate;
        this.maturityDate = maturityDate;
        this.couponFrequency = couponFrequency;
        this.settlementDate = settlementDate;
        this.treasuryYield = 0.0;
        this.bondSpread = 0.0;
        
        if (isPrice) {
            this.price = value;
            this.yieldToMaturity = calculateYTM();
        } else {
            this.yieldToMaturity = value;
            this.price = calculatePrice();
        }
        
        this.macaulayDuration = calculateMacaulayDuration();
        this.modifiedDuration = calculateModifiedDuration();
        this.convexity = calculateConvexity();
        this.tenor = calculateTenor();
        this.treasuryYield = fetchTreasuryYield();
        this.bondSpread = calculateSpread();
    }

    public double getFaceValue() {
        return faceValue;
    }

    public double getCouponRate() {
        return couponRate;
    }

    public LocalDate getMaturityDate() {
        return maturityDate;
    }

    public int getCouponFrequency() {
        return couponFrequency;
    }

    public LocalDate getSettlementDate() {
        return settlementDate;
    }

    public double getMacaulayDuration() {
        return macaulayDuration;
    }

    public double getModifiedDuration() {
        return modifiedDuration;
    }

    public double getConvexity() {
        return convexity;
    }

    public double getYieldToMaturity() {
        return yieldToMaturity;
    }

    public double getPrice() {
        return price;
    }
    
    public double getTreasuryYield() {
    	return treasuryYield;
    }
    
    public double getSpread() {
    	return yieldToMaturity - treasuryYield;
    }
    
    public String getTenor() {
    	return tenor;
    }

    public void setFaceValue(double faceValue) {
        this.faceValue = faceValue;
    }

    public void setCouponRate(double couponRate) {
        this.couponRate = couponRate;
    }

    public void setMaturityDate(LocalDate maturityDate) {
        this.maturityDate = maturityDate;
    }

    public void setCouponFrequency(int couponFrequency) {
        this.couponFrequency = couponFrequency;
    }

    public void setSettlementDate(LocalDate settlementDate) {
        this.settlementDate = settlementDate;
    }

    public void setTreasuryYield(double treasuryYield) {
        this.treasuryYield = treasuryYield;
    }

    public void setBondSpread(double bondSpread) {
        this.bondSpread = bondSpread;
    }

    private double calculatePrice() {
    	// C*  (1-(1+r)^-n /r ) + F/(1+r)^n
    	// r = ytm, C = coupon payment, n = num of periods until maturity
        int n = (int) ChronoUnit.MONTHS.between(settlementDate, maturityDate) / (12 / couponFrequency);
        double couponPayment = faceValue * couponRate / couponFrequency;
        double discountFactor = 1 / Math.pow(1 + yieldToMaturity / couponFrequency, n);
        double pvCoupons = couponPayment * (1 - discountFactor) / (yieldToMaturity / couponFrequency);
        double pvFaceValue = faceValue * discountFactor;
        
        return pvCoupons + pvFaceValue;
    }

    private double calculateYTM() {
    	double estimatedYTM = couponRate;
        double tolerance = 1e-6;
        int maxIterations = 1000;
        double ytm = estimatedYTM / couponFrequency;
        double priceCalc;

        for (int i = 0; i < maxIterations; i++) {
            priceCalc = 0.0;
            int n = (int) ChronoUnit.MONTHS.between(settlementDate, maturityDate) / (12 / couponFrequency);
            double couponPayment = faceValue * couponRate / couponFrequency;

            for (int j = 1; j <= n; j++) {
                priceCalc += couponPayment / Math.pow(1 + ytm / couponFrequency, j);
            }
            priceCalc += faceValue / Math.pow(1 + ytm / couponFrequency, n);

            double ytmDerivative = 0.0;

            for (int j = 1; j <= n; j++) {
                ytmDerivative -= (j * couponPayment) / (Math.pow(1 + ytm / couponFrequency, j + 1));
            }
            ytmDerivative -= (n * faceValue) / (Math.pow(1 + ytm / couponFrequency, n + 1));

            double newtonRaphsonStep = (price - priceCalc) / ytmDerivative;

            ytm += newtonRaphsonStep;

            if (Math.abs(newtonRaphsonStep) < tolerance) {
                break;
            }
        }

        this.yieldToMaturity = ytm;
        return this.yieldToMaturity;
    }

    private double calculateMacaulayDuration() {
    	int n = couponFrequency * (int) ChronoUnit.YEARS.between(settlementDate, maturityDate);
        double couponPayment = faceValue * couponRate / couponFrequency;
        double weightedSum = 0.0;
        double presentValueSum = 0.0;

        for (int i = 1; i <= n; i++) {
            double t = (double) i / couponFrequency;
            double discountFactor = Math.pow(1 + yieldToMaturity / couponFrequency, i);
            weightedSum += t * couponPayment / discountFactor;
            presentValueSum += couponPayment / discountFactor;
        }
        double finalDiscountFactor = Math.pow(1 + yieldToMaturity / couponFrequency, n);
        weightedSum += (double) n / couponFrequency * faceValue / finalDiscountFactor;
        presentValueSum += faceValue / finalDiscountFactor;

        return weightedSum / presentValueSum;
    }

    private double calculateModifiedDuration() {
        return macaulayDuration / (1 + (yieldToMaturity / couponFrequency));
    }
    
    private double calculateConvexity() {
    	int n = couponFrequency * (int) ChronoUnit.YEARS.between(settlementDate, maturityDate);
        double couponPayment = faceValue * couponRate / couponFrequency;
        double convexitySum = 0.0;

        // Calculate the convexity contribution from each coupon payment
        for (int i = 1; i <= n; i++) {
            convexitySum += (i * (i + 1)) / Math.pow(1 + yieldToMaturity / couponFrequency, i + 2);
        }

        // Contribution from coupon payments
        convexitySum *= couponPayment / price;

        // Add the convexity contribution from the face value payment
        convexitySum += (n * (n + 1) / Math.pow(1 + yieldToMaturity / couponFrequency, n + 2)) * (faceValue / price);

        // Divide by the square of the coupon frequency to account for payment frequency
        return convexitySum / Math.pow(couponFrequency, 2);
    }
    
    private String calculateTenor() {
        double yearsBetween = ChronoUnit.DAYS.between(settlementDate, maturityDate) / 365.25;

        if (yearsBetween <= 0.1667) {
            return "1 Month";
        } else if (yearsBetween <= 0.375) {
            return "3 Month";
        } else if (yearsBetween <= 0.75) {
            return "6 Month";
        } else if (yearsBetween <= 1.5) {
            return "1 Year";
        } else if (yearsBetween <= 2.5) {
            return "2 Year";
        } else if (yearsBetween <= 4) {
            return "3 Year";
        } else if (yearsBetween <= 7.5) {
            return "5 Year";
        } else if (yearsBetween <= 15 * 12) {
            return "10 Year";
        } else if (yearsBetween <= 25 * 12) {
            return "20 Year";
        } else {
            return "30 Year";
        }
    }
    
    private Double fetchTreasuryYield() {
        String seriesId = getSeriesIdForTenor(tenor);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        for (int i = 0; i < 7; i++) { // Attempt up to 7 times aka looking back 7 days
            String formattedDate = settlementDate.minusDays(i).format(formatter);
            String urlString = "https://api.stlouisfed.org/fred/series/observations?series_id=" + seriesId + 
                               "&observation_start=" + formattedDate + 
                               "&observation_end=" + formattedDate + 
                               "&api_key=" + API_KEY + "&file_type=xml";
            try {
                URL url = new URL(urlString);
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"))) {
                    StringBuilder xmlText = new StringBuilder();
                    for (String line; (line = reader.readLine()) != null;) {
                        xmlText.append(line);
                    }
                    Double yield = parseYield(xmlText.toString());
                    if (yield != null) {
                        return yield;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private String getSeriesIdForTenor(String tenor) {
        if (tenor == "1 Month") {
        	return "DGS1MO";
        } else if (tenor == "3 Month") {
        	return "DGS3MO";
        } else if (tenor == "6 Month") {
        	return "DGS6MO";
        } else if (tenor == "1 Year") {
        	return "DGS1";
        } else if (tenor == "2 Year") {
        	return "DGS2";
        } else if (tenor == "3 Year") {
        	return "DGS3";
        } else if (tenor == "5 Year") {
        	return "DGS5";
        } else if (tenor == "10 Year") {
        	return "DGS10";
        } else if (tenor == "20 Year") {
        	return "DGS20";
        } else {
        	return "DGS30";
        }
    }

    private Double parseYield(String xmlText) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new java.io.ByteArrayInputStream(xmlText.getBytes("UTF-8")));

            NodeList observations = doc.getElementsByTagName("observation");

            Node observationNode = observations.item(0);
            if (observationNode.getNodeType() == Node.ELEMENT_NODE) {
                Element observationElement = (Element) observationNode;

                String value = observationElement.getAttribute("value");
                
                if (value != null && !value.trim().isEmpty() && !value.equals(".")) {
                    return Double.parseDouble(value) / 100;
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    private double calculateSpread() {
    	return yieldToMaturity - treasuryYield;
    }

    @Override
    public String toString() {
        return "Bond{" +
                "faceValue=" + faceValue +
                ", couponRate=" + couponRate +
                ", maturityDate=" + maturityDate +
                ", couponFrequency=" + couponFrequency +
                ", settlementDate=" + settlementDate +
                ", price=" + price +
                ", yieldToMaturity=" + yieldToMaturity +
                ", macaulayDuration=" + macaulayDuration +
                ", modifiedDuration=" + modifiedDuration +
                ", convexity=" + convexity +
                ", treasuryYield=" + treasuryYield +
                ", bondSpread=" + bondSpread +
                ", tenor=" + tenor +
                '}';
    }
    
    public static void main(String[] args) {
        Bond bondWithYTM = new Bond(1000, 0.07, LocalDate.of(2035, 12, 31), 1, LocalDate.of(2020, 1, 1), 0.09, false);
        System.out.println(bondWithYTM);
        
        Bond bondUpdated = new Bond(1000, 0.08, LocalDate.of(2026, 01, 01), 2, LocalDate.of(2020, 1, 1), 911.37, true);
        System.out.println(bondUpdated);
    }

}
