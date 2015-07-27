package wwckl.projectmiki.models;

import android.util.Log;

import java.math.BigDecimal;

/**
 * Created by Aryn on 6/29/15.
 */
public class BillSplit {
    public enum BillSplitType {
        DUTCH_TYPE,
        SHARE_TYPE,
        TREAT_TYPE, // transitioning type?
        TREAT_SHARE_TYPE,
        TREAT_DUTCH_TYPE
    }

    private BillSplitType mSplitType = BillSplitType.DUTCH_TYPE;
    private BigDecimal mSplitAmount = new BigDecimal(0.00);
    private int mNoOfPplSharing = 0;

    // new BillSplit
    public BillSplit (BillSplitType splitType, BigDecimal shareTotal, int noOfPplSharing) {
        mSplitType = splitType;
        mSplitAmount = shareTotal;
        mNoOfPplSharing = noOfPplSharing;
        Log.d("new BillSplit", getSplitString(splitType) + "," +
                mSplitAmount.toString() + "," + Integer.toString(mNoOfPplSharing));
    }

    public BillSplitType getSplitType() { return mSplitType; }

    public int getNoOfPplSharing () { return mNoOfPplSharing; }

    public BigDecimal getTotalAmount () { return mSplitAmount; }

    // TODO: CALCULATE SPLIT AMOUNT ACCORDINGLY.
    public BigDecimal getSplitAmount() {
        if(isZero(mSplitAmount))
            return mSplitAmount;

        BigDecimal amount = mSplitAmount;

        switch (mSplitType) {
            case SHARE_TYPE:
            case TREAT_TYPE:
            case TREAT_SHARE_TYPE:
                if(mNoOfPplSharing != 0)
                    amount = mSplitAmount.divide(BigDecimal.valueOf(mNoOfPplSharing),
                                2, BigDecimal.ROUND_HALF_EVEN);
                break;
            case TREAT_DUTCH_TYPE:
            case DUTCH_TYPE:
            default:
                break;
        }

        return amount;
    }

    public Boolean isZero(BigDecimal bdAmount){
        if(bdAmount.compareTo(BigDecimal.ZERO) == 0)
            return  true;
        return false;
    }

    private String getSplitString(BillSplitType splitType) {
        switch (splitType) {
            case DUTCH_TYPE:
                return "DUTCH";
            case SHARE_TYPE:
                return "SHARE";
            case TREAT_TYPE:
                return "TREAT";
            case TREAT_DUTCH_TYPE:
                return "TREAT DUTCH";
            case TREAT_SHARE_TYPE:
                return "TREAT SHARE";
            default:
                return splitType.toString();
        }
    }
}
