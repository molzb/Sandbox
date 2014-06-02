package com.db.tradefinder.histsim.domain;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * A TradingEntry is technically the counterpart of 1 line in the corresponding
 * CSV file (e.g. trade_xxx.csv).<br>
 * <br>
 * Format of a TradingEntry in the CSV file<br><code>
 * RISK_NAME                 SENSITIVITY_NAME                 LABEL                 VALUE                                                   RPTCCYVALUE<br>
 * _________________________________________________ <br>
 * CURVE        Residual        120731        -0.0000595653        -0.0000595653<br>
 * </code><br>
 *
 * @author Bernhard
 */
public class TradingEntry {

    private final String riskName, sensitivityName;
    private final Date date;
    private final double value, rptccy;
    private boolean isMax = false;
    private boolean isMin = false;
    private final SimpleDateFormat sdfParse = new SimpleDateFormat("yyMMdd");
    private final DecimalFormat df = new DecimalFormat("0.#####", DecimalFormatSymbols.getInstance(Locale.ENGLISH));

    public TradingEntry(String riskName, String sensitivityName, String label, String value, String rptccy)
            throws ParseException {
        this.riskName = riskName;
        this.sensitivityName = sensitivityName;
        this.date = sdfParse.parse(label);
        this.value = Double.valueOf(value);
        this.rptccy = Double.valueOf(rptccy);
    }

    @Override
    public String toString() {
        return "SandboxEntry (Name=" + riskName + " " + sensitivityName + ", date=" + sdfParse.format(date)
                + ", value=" + value + " / " + rptccy + ")";
    }

    public String getRiskName() {
        return riskName;
    }

    public String getSensitivityName() {
        return sensitivityName;
    }

    public Date getDate() {
        return date;
    }

    public double getValue() {
        return value;
    }

    public String getFormattedValue() {
        return df.format(value);
    }

    public double getRptccy() {
        return rptccy;
    }

    public boolean isIsMax() {
        return isMax;
    }

    public void setIsMax(boolean isMax) {
        this.isMax = isMax;
    }

    public boolean isIsMin() {
        return isMin;
    }

    public void setIsMin(boolean isMin) {
        this.isMin = isMin;
    }
} 