package com.db.tradefinder.histsim.domain;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * A TradingDay consists of up to 7
 * <code>TradingEntry</code> objects.<br>
 * Example for July 13 2012:<br><code>
 * RISK_NAME    SENSITIVITY_NAME    LABEL    VALUE            RPTCCYVALUE<br>
 * ----------------------------------------------------------------------<br>
 * CURVE Residual 120731 -0.0000595653 -0.0000595653 (1)<br>
 * CURVE SwapDelta 120731 -0.9505223401 -0.9505223401 (2)<br>
 * CURVE SwapGamma 120731 -0.0083920893 -0.0083920893 (3)<br>
 * IRVOL IRVega 120731 0 0 (4)<br>
 * IRVOL Residual 120731 0 0 (5)<br>
 * NET NET 120731 -0.9589739948 -0.9589739948 (6)<br>
 * Residual Residual 120731 0 0 (7)<br>
 * </code>
 *
 * @author Bernhard
 */
public class TradingDay {

    public static final int CURVE_RES = 0;
    public static final int CURVE_SDELTA = 1;
    public static final int CURVE_SGAMMA = 2;
    public static final int IRVOL_IRVEGA = 3;
    public static final int IRVOL_RES = 4;
    public static final int NET_NET = 5;
    public static final int RES_RES = 6;
    public static String[] HEADERS = {
        "CURVE Residual",
        "CURVE SwapDelta",
        "CURVE SwapGamma",
        "IRVOL IRVega",
        "IRVOL Residual",
        "NET NET",
        "Residual Residual"};
    private final int id;
    private final Date date;
    private double sum = 0D;
    private final List<TradingEntry> entries = new ArrayList<TradingEntry>(7);
    private final SimpleDateFormat sdfShort = new SimpleDateFormat("yyyyMMdd", Locale.ENGLISH);
    private final SimpleDateFormat sdf = new SimpleDateFormat("E, MMM dd yyyy", Locale.ENGLISH);
    private final DecimalFormat df6 = new DecimalFormat("0.######", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
        private final List<String> css = new ArrayList<String>(7);

    public TradingDay(Date date, int id) {
        this.date = date;
        this.id = id;
    }

    public List<TradingEntry> getEntries() {
        return entries;
    }

    public void addEntry(TradingEntry entry) {
        entries.add(entry);
        String cssClass = "";
        if (entry.getValue() > 0.01) {
            cssClass = "pos";
        }
        if (entry.getValue() < -0.01) {
            cssClass = "neg";
        }
        css.add(cssClass);
    }

    public String getDate() {
        return sdf.format(date);
    }
   
    public String getShortDate() {
            return sdfShort.format(date);
    }

    public int getId() {
        return id;
    }

    /**
     * CSS class for the table cell
     *
     * @return CSS class - "pos" for positive values, "neg" for negative values,
     * "" else
     */
    public List<String> getCss() {
        return css;
    }

    /**
     * Returns JSON. Example:<br>
     * { "id": 1, "values": [{"name": "CURVE1","value": -0.2},
     * {"name":"Curve2","value":1}] }<br>
     *
     * @return JSON formatted string
     */
        public String getJson() {
                return String.format(
                                "{\"id\": %s, \"sum\": %s, \"date\": \"%s\", \"fulldate\": \"%s\", \"dateLong\": %s, \"values\": [%s]}",
                                id, df6.format(getSum()), getShortDate(), getDate(), date.getTime(), getAmchartDataProvider());
        }

    /**
     * Returns JSON in the format that Amchart needs, see<br>
     * http://www.amcharts.com/demos/simple-pie-chart/ <br>
     * Format:<br>
     * <code>{"name": "CURVE1", "value": -0.2}, {"name": "CURVE2", "value": 1}</code><br>
     *
     * @return JSON formatted string
     * @see getJson()
     */
    private String getAmchartDataProvider() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < entries.size(); i++) {
            sb.append("{");
            sb.append("\"name\": \"" + HEADERS[i] + "\",");
            sb.append("\"value\": " + entries.get(i).getFormattedValue() + ",");
                        sb.append("\"css\": \"" + css.get(i) + "\",");
                        sb.append("\"min\": " + entries.get(i).isIsMin() + ",");
                        sb.append("\"max\": " + entries.get(i).isIsMax() + "");
            sb.append("},");
        }
        sb.delete(sb.length() - 1, sb.length());                 // delete last [,\n] after last record
        return sb.toString();
    }

    public double getSum() {
        sum = 0.0;
        for (int i = 0; i < entries.size(); i++) {
            sum += entries.get(i).getValue();
        }
        return sum;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getClass().getSimpleName() + " (" + sdf.format(date) + ")\n");
        for (TradingEntry entry : entries) {
            sb.append("\t" + entry.toString() + "\n");
        }
        return sb.toString();
    }
}
