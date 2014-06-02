package com.db.tradefinder.histsim.web;

import com.db.tradefinder.histsim.domain.TradingDay;
import com.db.tradefinder.histsim.domain.TradingEntry;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Reads a CSV file (e.g. test_trade_XXX.csv) and constructs
 * {@link TradingEntry} and {@link TradingDay} objects
 *
 * @author Bernhard
 */
public class CsvFileReader {

    private static final Logger logger = Logger.getLogger(CsvFileReader.class.getName());
    private boolean[] colHasNonZeroValues = {
        false, false, false, false, false, false, false
    };
        private List<String> distinctMonths = new ArrayList<String>();
        private final String pathToCsvFile;
        private String[] csvHeaders;

        static final int INFO_COBDATE = 0;
        static final int INFO_FO_TRADEID = 1;
        static final int INFO_VAL_CCY = 2;
        static final int INFO_UNIT_CODE = 3;
        private List<String> infoGeneral = new ArrayList<String>(4);

        CsvFileReader(String pathToCsvFile) {
                this.pathToCsvFile = pathToCsvFile;
        }
       
        public List<String> getInfoGeneral() {
                return infoGeneral;
        }

    public boolean[] getColHasNonZeroValues() {
        return colHasNonZeroValues;
    }

    private void setColHasNonZeroValues(List<TradingDay> days) {
                for (int i = 0; i < 7; i++) {
            for (TradingDay day : days) {
                if (day.getEntries().get(i).getValue() != 0) {
                    colHasNonZeroValues[i] = true;
                    break;
                }
            }
        }
    }
   
    public String[] getCsvHeaders() {
            return csvHeaders;
    }
       
        private List<String> setDistinctMonths(List<TradingDay> days) {
                String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
                for (TradingDay day : days) {
                        @SuppressWarnings("deprecation")
                        int month = day.getEntries().get(0).getDate().getMonth();
                        if (!distinctMonths.contains(months[month])) {
                                getDistinctMonths().add(months[month]);
                        }
                }
                return getDistinctMonths();
        }

    /**
     * Make a list of TradingDay objects.<br>
     * A TradingDay consists of up to 7 {@link TradingEntry} objects that have
     * the same trading date
     *
     * @param lines All TradingEntry objects, no matter what date
     * @return List of {@link TradingEntry} objects
     */
    protected List<TradingDay> aggregateTradingEntries(List<TradingEntry> lines) {
        List<TradingDay> tradingDays = new ArrayList<TradingDay>();
        Date lastDate = null;
        TradingDay tradingDay = null;
        for (int i = 0, j = 0; i < lines.size(); i++) {
            TradingEntry line = lines.get(i);
            if (!line.getDate().equals(lastDate)) {
                tradingDay = new TradingDay(line.getDate(), j++);
                tradingDays.add(tradingDay);
            }
            if (tradingDay != null) // prevent Findbugs warning
            {
                tradingDay.addEntry(line);
            }
            lastDate = line.getDate();
        }
        setMinMaxValues(tradingDays);
        setColHasNonZeroValues(tradingDays);
                setDistinctMonths(tradingDays);
        return tradingDays;
    }

    /**
     * Read the CSV file that contains the trading entries.<br>
     * 1 line in the file corresponds to 1 {@link TradingEntry}.<br>
     * <code>TradingEntry</code> objects will later be aggregated to
     * {@link TradingDay}</code> objects, which consist of up to 7
     * <code>TradingEntry</code> objects
     *
     * @return A list of <code>TradingEntry</code> objects
     * @throws java.io.FileNotFoundException
     */
    protected List<TradingEntry> readCsvFile() throws FileNotFoundException, IOException {
                String filename = pathToCsvFile + "/test_trade_1655665M.csv";
        File csvFile = new File(filename);
       
        InputStreamReader isr = new InputStreamReader(new FileInputStream(csvFile));
        BufferedReader br = new BufferedReader(isr);
        List<TradingEntry> trdLines = new ArrayList<TradingEntry>();
        int lineNumber = 0;
        for (String line; (line = br.readLine()) != null; lineNumber++) {
            if (line.isEmpty()) {
                continue;
            }
            line = line.replace("\"", "");
            if (lineNumber == 0) {                 // first line contains headers
                csvHeaders = line.split(",");
            } else {                                                                    // next lines contain content
                String[] tokens = line.split(",");
                if (lineNumber == 1) {
                        infoGeneral.add(tokens[0]);        // COB_DATE
                        infoGeneral.add(tokens[4]);        // FO_TRADE_ID
                        infoGeneral.add(tokens[5]);        // VALUATION_CCY
                        infoGeneral.add(tokens[6]);        // UNIT_CODE
                }

                // Only parse lines with these RISK NAMES, ignore e.g. BasePrice or EODPrice
                if (!tokens[7].equals("CURVE")
                        && !tokens[7].equals("IRVOL")
                        && !tokens[7].equals("NET")
                        && !tokens[7].equals("Residual")) {
                    logger.info("Field RISK_NAME with value " + tokens[7] + " ignored");
                    continue;
                }

                try {
                    TradingEntry trdLine = new TradingEntry(tokens[7], tokens[8], tokens[9], tokens[10], tokens[11]);
                    trdLines.add(trdLine);
                } catch (ParseException pse) {
                    logger.log(Level.SEVERE, "Error in line: {0}", line);
                    logger.severe(pse.getMessage());
                }
            }
        }
        try {
                br.close();
        } catch (IOException ioe) {
                logger.fine(ioe.getMessage());
        }
        return trdLines;
    }

    /**
     * Looks for min and max values among the values of TradingDays objects.<br>
     * There are 7 different values/columns (CURVE Residual, ..., Residual
     * Residual),<br>
     * so there will be 7 {@link TradingEntry} objects with the BOOL flag
     * isMin/isMax
     *
     * @param days
     */
    private void setMinMaxValues(List<TradingDay> days) {
        double[] min = new double[7];
        double[] max = new double[7];

        for (TradingDay day : days) {
            int col = 0;
            for (TradingEntry entry : day.getEntries()) {
                min[col] = Math.min(min[col], entry.getValue());
                max[col] = Math.max(max[col], entry.getValue());
                col++;
            }
        }
        for (TradingDay day : days) {
            int col = 0;
            for (TradingEntry entry : day.getEntries()) {
                if (min[col] == entry.getValue() && min[col] != 0D) {
                    entry.setIsMin(true);
                }
                if (max[col] == entry.getValue() && max[col] != 0D) {
                    entry.setIsMax(true);
                }
                col++;
            }
        }
    }

        /**
         * @return the distinctMonths
         */
        public List<String> getDistinctMonths() {
                return distinctMonths;
        }
       
    /**
     * Returns a JSON array for {@link TradingDay} objects<br>
     * var tradingDays = [<br>
     * { "id": 1, "values": [{"name": "CURVE1","value": -0},
     * {"name":"CS","value":1}] },<br>
     * { "id": 2, "values": [{"name": "CURVE2","value": 2}] }<br> ]
     *
     * @param days List of TradingDay objects
     * @return JSON array
     */
    public String getJsonForTradingDays(List<TradingDay> days) {
        StringBuilder sb = new StringBuilder("[\n");
        for (TradingDay day : days) {
            sb.append(day.getJson()).append(",\n");
        }
        sb.delete(sb.length() - 2, sb.length());                 // delete last comma
        sb.append("\n]");
        return sb.toString();
    }

}
