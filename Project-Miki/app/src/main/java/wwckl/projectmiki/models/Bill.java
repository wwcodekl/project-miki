package wwckl.projectmiki.models;

import android.util.Log;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by cheeyim on 2/2/2015.
 */
public class Bill {
    final int fMaxGSTpercent = 21;
    final int fMinGSTpercent = 5;
    final int fMaxSVCpercent = 21;
    final int fMinSVCpercent = 5;

    protected BigDecimal mTotal = new BigDecimal(0.00);
    protected BigDecimal mSubTotal = new BigDecimal(0.00);
    protected BigDecimal mGST = new BigDecimal(0.00);
    protected int mGSTpercent = 0;
    protected BigDecimal mSVC = new BigDecimal(0.00);
    protected int mSVCpercent = 0;
    protected BigDecimal mAdjust = new BigDecimal(0.00);
    protected int mNoOfPplSharing = 0;
    protected List<Item> mListOfAllItems = new ArrayList<>();
    protected Stack<BillSplit> mBillSplitStack = new Stack<>();
    protected Boolean mUseSubtotals = false; // whether to include subtotals in calculations.

    public BigDecimal getTotal() {
        return this.mTotal;
    }

    public void setTotal(BigDecimal total) {
        this.mTotal = total;
    }

    public BigDecimal getSubTotal() {
        return this.mSubTotal;
    }

    public void setSubTotal(BigDecimal subTotal) {
        this.mSubTotal = subTotal;
    }

    public BigDecimal getGst() {
        return this.mGST;
    }

    public void setGst(BigDecimal gst){
        this.mGST = gst;
    }

    public int getGstPercent(){
        return this.mGSTpercent;
    }

    public void setGstPercent(int gstPercent){
        this.mGSTpercent = gstPercent;
    }

    public BigDecimal getSvc() {
        return this.mSVC;
    }

    public void setSvc(BigDecimal svc){
        this.mSVC = svc;
    }

    public int getSvcPercent(){
        return this.mSVCpercent;
    }

    public void setSvcPercent(int svcPercent){
        this.mSVCpercent = svcPercent;
    }

    public BigDecimal getAdjust() {
        return this.mAdjust;
    }

    public void setAdjust(BigDecimal adjust){
        this.mAdjust = adjust;
    }

    public int getNoOfPplSharing() {
        return this.mNoOfPplSharing;
    }

    public void setNoOfPplSharing(int noOfPplSharing) {
        this.mNoOfPplSharing = noOfPplSharing;
    }

    public List<Item> getListOfAllItems() {
        return this.mListOfAllItems;
    }

    public Stack<BillSplit> getBillSplits() {
        return this.mBillSplitStack;
    }

    public int getNumOfBillSplits() {
        return mBillSplitStack.size();
    }

    public Boolean getUseSubtotals() { return mUseSubtotals; }

    // add item to offset total
    protected void addOffsetItem(BigDecimal total) {
        BigDecimal sumOfItems = sumOfItems();
        int comparison = total.compareTo(sumOfItems);
        if (comparison != 0) {
            // check common error where gst or svc become itemized.
            // because of font size or other formating
            int lastItemIndex = mListOfAllItems.size()-1;
            BigDecimal itemPrice = total.subtract(sumOfItems);
            BigDecimal lastItemPrice;

            // check that listOfAllItems is not empty
            if(lastItemIndex < 0)
                lastItemPrice = BigDecimal.ZERO;
            else
                lastItemPrice = mListOfAllItems.get(lastItemIndex).getPrice();

            // if last item is the same value as what we subtract, remove item
            if( isZero(itemPrice.add(lastItemPrice)) )
                mListOfAllItems.remove(lastItemIndex);
            else
                mListOfAllItems.add(new Item("UNKNOWN ITEM", itemPrice));
        }
    }

    // calculates the percentage of fracAmount/total
    protected int calculatePercent(BigDecimal fracAmount, BigDecimal total){
        if (isZero(fracAmount))
            return 0;
        if (isZero(total))
            return 0;

        BigDecimal percent;
        percent = fracAmount.multiply(BigDecimal.valueOf(100));
        percent = percent.divide(total, 0, BigDecimal.ROUND_HALF_EVEN);
        return percent.intValue();
    }

    // return amount * 0.0x%
    protected BigDecimal calculatePercentageAmount(BigDecimal amount, int percent) {
        BigDecimal percentageAmount = new BigDecimal(0.00);

        if (isZero(amount))
            return percentageAmount;
        if (percent == 0)
            return percentageAmount;

        percentageAmount = amount.multiply(BigDecimal.valueOf(percent));
        percentageAmount = percentageAmount.divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_EVEN);

        return percentageAmount;
    }

    // Checks that the bill is balanced with 2 rules:
    // Rule 1: total sum of price from listOfAllItems equals to the SubTotal(if present, Total otherwise)
    // Rule 2: Sum of GST, SVC and SubTotal (if present) equals to the Total
    public Boolean isBillBalanced(){
        if (isZero(mTotal))
            return false;
        BigDecimal sumOfItems = sumOfItems();

        if (mUseSubtotals){
            if ( (mSubTotal.compareTo(sumOfItems) != 0))
                return false;

            if(mTotal.compareTo(sumOfTotals()) != 0)
                return false;
        }
        else {
            if (mTotal.compareTo(sumOfItems) != 0)
                return false;
        }

        return true;
    }

    // returns the sum of GST, SVC, SubTotal and adjust.
    protected BigDecimal sumOfTotals(){
        BigDecimal total = mSubTotal;
        total = total.add(mGST);
        total = total.add(mSVC);
        total = total.add(mAdjust);
        return total;
    }

    // Returns the sum of all items
    protected BigDecimal sumOfItems(){
        BigDecimal total = new BigDecimal(0.00);
        Iterator<Item> itemIterator = mListOfAllItems.iterator();

        while (itemIterator.hasNext()){
            total = total.add(itemIterator.next().getPrice());
        }
        Log.d("sumOfItems", total.toString());
        return total;
    }

    // ******************* END BALANCING THE BILL ***************************

    protected Boolean isZero(BigDecimal bdAmount){
        if(bdAmount.compareTo(BigDecimal.ZERO) == 0)
            return  true;
        return false;
    }

    // ******************* BILL SPLITTER ACTIVITY OPERATIONS ****************

    public void selectItem(int itemIndex, Boolean checked){
        Item item = mListOfAllItems.get(itemIndex);

        if(checked)
            item.setGuestIndex(mBillSplitStack.size());
        else
            item.setGuestIndex(item.fNOT_SELECTED);
    }

    // return total of selected item's price by guest, no Split type calculations
    public BigDecimal getGuestTotal(int guestIndex) {
        BigDecimal total = new BigDecimal(0.00);
        Iterator iterator = mListOfAllItems.iterator();

        while (iterator.hasNext()){
            Item item = (Item) iterator.next();
            if(item.getGuestIndex() == guestIndex) {
                BigDecimal itemPrice = item.getPrice();
                total = total.add(itemPrice);
            }
        }

        if (mUseSubtotals) {
            // Add SVC
            if (mSVCpercent > 0) {
                BigDecimal addPrice = calculatePercentageAmount(total, mSVCpercent);
                total = total.add(addPrice);
                Log.d("SVC of items", addPrice.toString());
            }
            // Add GST
            if (mGSTpercent > 0) {
                BigDecimal addPrice = calculatePercentageAmount(total, mGSTpercent);
                total = total.add(addPrice);
                Log.d("GST of total", addPrice.toString());
            }
        }
        return total;
    }

    // No error checking on data, assume all above board by this point.
    public void addBillSplit(BillSplit billSplit) {
        mBillSplitStack.push(billSplit);
    }

    public BillSplit removeLastBillSplit() {
        BillSplit lastBillSplit = null;

        if(!mBillSplitStack.empty()) {
            lastBillSplit = mBillSplitStack.pop();
            // get rid of selected items
            int billSplitSize = getNumOfBillSplits();
            Iterator<Item> itemIterator = mListOfAllItems.iterator();
            while (itemIterator.hasNext()) {
                Item item = itemIterator.next();
                if( item.getGuestIndex() > billSplitSize )
                    item.setGuestIndex(item.fNOT_SELECTED);
            }
        }
        return lastBillSplit;
    }

    // ******************* END BILL SPLITTER ACTIVITY OPERATIONS ****************

    // ******************* EDIT FRAGMENT OPERATIONS ********************
    public void setUseSubtotals(Boolean useSubtotals) {
        mUseSubtotals = useSubtotals;
        updateTotals();
    }

    // Return false if no changes
    public Boolean updateItem(String description, int index){
        Item item = mListOfAllItems.get(index);
        if (item == null)
            return false;

        item.setDescription(description);
        return true;
    }

    // Return false if no changes
    public Boolean updateItem(BigDecimal amount, int index){
        Item item = mListOfAllItems.get(index);
        if (item == null)
            return false;
        if (item.getPrice().compareTo(amount) == 0)
            return false;

        item.setPrice(amount);

        updateTotals();
        return true;
    }

    protected void updateTotals() {
        if (mUseSubtotals){
            mSubTotal = sumOfItems();

            if (mSVCpercent != 0)
                mSVC = calculatePercentageAmount(mSubTotal, mSVCpercent);
            else if (!isZero(mSVC))
                mSVCpercent = calculatePercent(mSVC, mSubTotal);

            if (mGSTpercent != 0)
                mGST = calculatePercentageAmount(mSubTotal.add(mSVC), mGSTpercent);
            else if (!isZero(mGST))
                mGSTpercent = calculatePercent(mGST, mSubTotal.add(mSVC));

            mTotal = sumOfTotals();
        }
        else {
            mTotal = sumOfItems();
        }
    }

    public void addItem() {
        mListOfAllItems.add(new Item("Item", new BigDecimal(0.00)));
    }

    // Return false if no changes to totals
    public Boolean updateSubTotal(BigDecimal amount) {
        if(amount.compareTo(mSubTotal) == 0)
            return false;

        mSubTotal = amount;
        if (mSVCpercent != 0)
            mSVC = calculatePercentageAmount(mSubTotal, mSVCpercent);
        else if (!isZero(mSVC))
            mSVCpercent = calculatePercent(mSVC, mSubTotal);

        if (mGSTpercent != 0)
            mGST = calculatePercentageAmount(mSubTotal.add(mSVC), mGSTpercent);
        else if (!isZero(mGST))
            mGSTpercent = calculatePercent(mGST, mSubTotal.add(mSVC));

        if(mUseSubtotals) {
            if(!isBillBalanced()){
                addOffsetItem(amount);
            }
            mTotal = sumOfTotals();
        }
        return true;
    }

    // Return false if no changes
    public Boolean updateSVC(BigDecimal amount){
        if (amount.compareTo(mSVC) == 0)
            return false;

        mSVC = amount;
        if (!isZero(mSubTotal))
            mSVCpercent = calculatePercent(mSVC, mSubTotal);
        if(mUseSubtotals)
            mTotal = sumOfTotals();
        return true;
    }

    // Return false if no changes
    public Boolean updateSvcPercent(int percent) {
        if (percent == mSVCpercent)
            return false;

        mSVCpercent = percent;
        if (!isZero(mSubTotal))
            mSVC = calculatePercentageAmount(mSubTotal, percent);
        if(mUseSubtotals)
            mTotal = sumOfTotals();
        return true;
    }

    // Return false if no changes
    public Boolean updateGST(BigDecimal amount){
        if (amount.compareTo(mGST) == 0)
            return false;

        mGST = amount;
        if (!isZero(mSubTotal))
            mGSTpercent = calculatePercent(mGST, mSubTotal.add(mSVC));
        if(mUseSubtotals)
            mTotal = sumOfTotals();
        return true;
    }

    // Return false if no changes
    public Boolean updateGstPercent(int percent){
        if (percent == mGSTpercent)
            return false;

        mGSTpercent = percent;
        if (!isZero(mSubTotal))
            mGST = calculatePercentageAmount(mSubTotal.add(mSVC), percent);
        if(mUseSubtotals)
            mTotal = sumOfTotals();
        return true;
    }

    // return false if there was no change
    public Boolean updateTotal(BigDecimal amount){
        if(amount.compareTo(mTotal) == 0)
            return false;

        mTotal = amount;
        if (mUseSubtotals) {
            // calculate subtotal
            int percent;
            if(mGSTpercent != 0) {
                percent = mGSTpercent;
                percent = percent + 100;
                amount = amount.multiply(BigDecimal.valueOf(100));
                amount = amount.divide(BigDecimal.valueOf(percent), 2, BigDecimal.ROUND_HALF_EVEN);
                mGST = calculatePercentageAmount(amount, mGSTpercent);
            }
            if(mSVCpercent != 0) {
                percent = mSVCpercent;
                percent = percent + 100;
                amount = amount.multiply(BigDecimal.valueOf(100));
                amount = amount.divide(BigDecimal.valueOf(percent), 2, BigDecimal.ROUND_HALF_EVEN);
                mSVC = calculatePercentageAmount(amount, mSVCpercent);
            }
            mSubTotal = amount;
        }

        if(!isBillBalanced()){
            addOffsetItem(amount);
        }
        return true;
    }

    public void deleteItem(int index) {
        if(index < mListOfAllItems.size()) {
            mListOfAllItems.remove(index);
            updateTotals();
        }
        else
            Log.d("deleteItem", "out of bounds: " + Integer.toString(index));
    }

    // ******************* END EDIT FRAGMENT OPERATIONS ****************
}

