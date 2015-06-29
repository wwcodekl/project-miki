package wwckl.projectmiki.models;

import android.util.Log;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by cheeyim on 2/2/2015.
 */
public class Bill {
    private BigDecimal total = new BigDecimal(0.00);
    private BigDecimal subTotal = new BigDecimal(0.00);
    private BigDecimal gst = new BigDecimal(0.00);
    private int gstPercent = 0;
    private BigDecimal svc = new BigDecimal(0.00);
    private int svcPercent = 0;
    private int noOfPplSharing = 0;
    private List<Item> listOfAllItems = new ArrayList<Item>();
    private List<BillSplitter> listOfGuests = new ArrayList<>();

    // hash words 'library'
    private static final Set<String> fTOTAL_WORDS = new LinkedHashSet<>();
    private static final Set<String> fSUBTOTAL_WORDS = new LinkedHashSet<>();
    private static final Set<String> fGST_WORDS = new LinkedHashSet<>();
    private static final Set<String> fSVC_WORDS = new LinkedHashSet<>();
    private static final Set<String> fCASH_WORDS = new LinkedHashSet<>();
    private static final Set<String> fWHITELIST_WORDS = new LinkedHashSet<>();
    // initialise word tokens
    static {
        fTOTAL_WORDS.add("TOTAL");
        fTOTAL_WORDS.add("TOT");
        fTOTAL_WORDS.add("TTL");
        fTOTAL_WORDS.add("MASTER");
        fTOTAL_WORDS.add("VISA");
        fTOTAL_WORDS.add("AMEX");
        fTOTAL_WORDS.add("CASH");
        fTOTAL_WORDS.add("PAY");
        fTOTAL_WORDS.add("AMOUNT");
        fTOTAL_WORDS.add("BASE");
        fSUBTOTAL_WORDS.add("SUBTOT");
        //fSUBTOTAL_WORDS.add("SUBTOTAL");
        fSUBTOTAL_WORDS.add("SUB-TOT");
        fSUBTOTAL_WORDS.add("SUB\u2014TOT"); // \u2014 is unicode representation of a long dash
        //fSUBTOTAL_WORDS.add("SUB-TOTAL");
        fSUBTOTAL_WORDS.add("SUBTTL");
        fSUBTOTAL_WORDS.add("STTL");
        fSUBTOTAL_WORDS.add("TOTAL-EXCL-GST");
        fGST_WORDS.add("GST");
        fGST_WORDS.add("TAX");
        fGST_WORDS.add("G.S.T.");
        fSVC_WORDS.add("SVC");
        //fSVC_WORDS.add("SVC CHG");
        //fSVC_WORDS.add("SVC CHARGE");
        fSVC_WORDS.add("S.C");
        fSVC_WORDS.add("SVR CHRG");
        fSVC_WORDS.add("SERV CHARG");
        fSVC_WORDS.add("SERVICE");
        //fSVC_WORDS.add("SERVICE CHARGE");
        fCASH_WORDS.add("CASH");
        fWHITELIST_WORDS.add("CHANGE");
        fWHITELIST_WORDS.add("BALANCE");
        fWHITELIST_WORDS.add("QTY");
        fWHITELIST_WORDS.add("ROUNDING");
        fWHITELIST_WORDS.add("ROUNDOFF");
        fWHITELIST_WORDS.add("SAVINGS");
        //fWHITELIST_WORDS.add("TOTAL SAVINGS");
        fWHITELIST_WORDS.add("DISCOUNT");
        fWHITELIST_WORDS.add("ADJUST");
        //fWHITELIST_WORDS.add("ADJUSTMENT");
        //fWHITELIST_WORDS.add("TOTAL ADJUSTMENT");
    }

    public BigDecimal getTotal() {
        return this.total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public BigDecimal getSubTotal() {
        return this.subTotal;
    }

    public void setSubTotal(BigDecimal subTotal) {
        this.subTotal = subTotal;
    }

    public BigDecimal getGst() {
        return this.gst;
    }

    public void setGst(BigDecimal gst){
        this.gst = gst;
    }

    public int getGstPercent(){
        return this.gstPercent;
    }

    public void setGstPercent(int gstPercent){
        this.gstPercent = gstPercent;
    }

    public BigDecimal getSvc() {
        return this.svc;
    }

    public void setSvc(BigDecimal svc){
        this.svc = svc;
    }

    public int getSvcPercent(){
        return this.svcPercent;
    }

    public void setSvcPercent(int svcPercent){
        this.svcPercent = svcPercent;
    }

    public int getNoOfPplSharing() {
        return this.noOfPplSharing;
    }

    public void setNoOfPplSharing(int noOfPplSharing) {
        this.noOfPplSharing = noOfPplSharing;
    }

    public List<Item> getListOfAllItems() {
        return this.listOfAllItems;
    }

    // parse receipt text and populate receipt values
    public Bill (String receiptText) {
        // Prep receipt
        receiptText = cleanupOCRerrors(receiptText);
        receiptText = receiptText.toUpperCase();
        String[] lines = receiptText.split("[\n]+");

        // Parse each line of receipt
        for(int i = 0; i<lines.length; i++){
            lines[i].trim();
            parseLine(lines[i]);
        }

        // Finished parsing, make sure bill is balanced
        balanceBill();
    }

    // Check for common OCR parsing errors
    private String cleanupOCRerrors(String receiptText){
        // common ocr error, where picture has noise at edges
        receiptText = receiptText.replaceAll("(?m)^[^A-Za-z0-9]+", "");
        receiptText = receiptText.replaceAll("(?m)[^A-Za-z0-9]+$", "");
        // common ocr errors:
        receiptText = receiptText.replaceAll("Tota1", "Total");
        receiptText = receiptText.replaceAll("tota1", "total");
        receiptText = receiptText.replaceAll("Totai", "Total");
        receiptText = receiptText.replaceAll("totai", "total");
        receiptText = receiptText.replaceAll("TDTAL", "TOTAL");
        receiptText = receiptText.replaceAll("(?m)^EST ", "GST ");
        receiptText = receiptText.replaceAll(" EST ", " GST ");

        return receiptText;
    }

    // ********************* PARSING OF RECEIPT ***************************

    // parse receipt line to match ether item amount, total, subtotal, gst or svc
    private void parseLine(String line){
        BigDecimal bdAmount;

        // Get amount from line
        bdAmount = getAmount(line);
        if (bdAmount.compareTo(BigDecimal.ZERO) == 0) {
            // line does not contain amount, skip
            Log.d("parseLine", line);
            return;
        }
        Log.d("amount", bdAmount.toString());

        // check for Total token
        if(containsToken(fSUBTOTAL_WORDS, line)){
            this.setSubTotal(bdAmount);
            Log.d("SUBTOTAL", line);
        }
        else if(containsToken(fWHITELIST_WORDS, line)){
            Log.d("WHITELIST", line);
        }
        else if(containsToken(fTOTAL_WORDS, line)){
            if(getTotal().compareTo(BigDecimal.ZERO) == 0) {
                // Cash line may have an associated change line.
                // Skip if we already have our total.
                if (containsToken(fCASH_WORDS, line))
                    return;
            }
            this.setTotal(bdAmount);
            Log.d("TOTAL", line);
        }
        else if(containsToken(fGST_WORDS, line)){
            this.setGst(bdAmount);
            this.setGstPercent(getPercent(line));
            Log.d("GST", line);
        }
        else if(containsToken(fSVC_WORDS, line)){
            this.setSvc(bdAmount);
            this.setSvcPercent(getPercent(line));
            Log.d("SVC", line);
        }
        else { // ITEM
            Log.d("ITEM", line);
            int qty = getQuantity(line);
            String desc = getDescription(line);
            BigDecimal amount = bdAmount;

            // Divide amount by the quantity.
            if(qty > 1) {
                BigDecimal bdQuantity = new BigDecimal(qty);
                amount = bdAmount.divide(bdQuantity, 2, RoundingMode.HALF_UP);
                Log.d("new amt", amount.toString());
            }

            for (int i = 0; i < qty; i++) {
                listOfAllItems.add(new Item(desc, amount));
            }
        }
    }

    // if there is no amount found at end of line, returns 0 instead of 0.00
    private BigDecimal getAmount(String line){
        BigDecimal bdAmount = new BigDecimal(0);
        String strAmount;

        // prep string, remove common ocr mistakes of various symbols
        strAmount = line.replaceAll("([0-9])[-,']([0-9])", "$1.$2");
        //Log.d("strAmount 0", strAmount);

        // receipts tend to have bigger font size of total amount
        // which may lend OCR to add spaces between the numbers and period.
        if(strAmount.contains(" .") || strAmount.contains(". ")) {
            strAmount = strAmount.replaceAll("O", "0");
            strAmount = strAmount.replaceAll("([0-9 ])[-,]([0-9 ])", "$1.$2");
            strAmount = strAmount.replaceAll("([0-9.]) ([0-9.])", "$1$2");
            strAmount = strAmount.replaceAll("([0-9.]) ([0-9.])", "$1$2");
            Log.d("strAmount 1", strAmount);
        }

        // Match end of line amount
        Pattern pattern = Pattern.compile("([0-9.]+)$");
        Matcher matcher = pattern.matcher(strAmount);

        if(!matcher.find())
            return bdAmount;
        strAmount = matcher.group(1);
        //Log.d("strAmount 2", strAmount);

        String strPattern = "[0-9]+[.][0-9][0-9]";
        // Check if we found amount match.
        if(!strAmount.matches(strPattern))
            return bdAmount;

        bdAmount = new BigDecimal(strAmount);
        return bdAmount;
    }

    // returns whether or not the line contains any of the keywords in the list
    private Boolean containsToken(Set<String> linkedHashSet, String line){
        Iterator it = linkedHashSet.iterator();
        Boolean hasToken = false;

        while(it.hasNext() && !hasToken){
            if(line.contains(it.next().toString()))
                hasToken = true;
        }

        return hasToken;
    }

    private int getQuantity(String line){
        int qty = 1;
        String strQuantity;
        Pattern pattern = Pattern.compile("^([0-9]+)[ ]");
        Matcher matcher = pattern.matcher(line);

        if(!matcher.find())
            return qty;

        strQuantity = matcher.group(1);
        // more likely not quantity
        if(strQuantity.length() > 2)
            return qty;
        qty  = Integer.parseInt(strQuantity);

        Log.d("qty", String.valueOf(qty));
        return qty;
    }

    private String getDescription(String line){
        String desc = "Item";

        line = line.replaceFirst("^[0-9][0-9]?[ ]", "");
        line = line.replaceFirst("[0-9., -$]+$", "");

        if(!line.isEmpty())
            desc = line;

        Log.d("desc", desc);
        return desc;
    }

    private int getPercent(String line){
        String strPercent;
        int percentage = 0;

        if(!line.contains("%"))
            return percentage;

        String tokens[] = line.split(" ");

        for (int i = 0; i < tokens.length; i++) {
            strPercent = tokens[i];

            if(strPercent.contains("%")){
                strPercent = strPercent.replaceAll("[.]00", "");
                strPercent = strPercent.replaceAll("[%().]", "");
                strPercent = strPercent.replaceAll("O","0");
                strPercent = strPercent.replaceAll("l","1");
                if(strPercent.matches("[0-9]+"))
                    percentage = Integer.parseInt(strPercent);
                break;
            }
        }

        Log.d("percent", String.valueOf(percentage));
        return percentage;
    }

    // ********************* END PARSING OF RECEIPT ************************

    // To be done at initiazation after parsing of receipt
    private void balanceBill(){
        // all items must add up to total amount.

    }
}
