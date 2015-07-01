package wwckl.projectmiki.models;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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
    private BigDecimal mTreatAmount = new BigDecimal(0.00);
    private BigDecimal mShareAmount = new BigDecimal(0.00);
    private int mNoOfPplSharing = 0;
    private List<Item> listOfItems = new ArrayList<Item>(); // List of items selected.

    public BillSplitType getSplitType() { return mSplitType; }

    // new BillSplit() initialisation method.
    public BillSplit (BigDecimal dutchTotal) {
        mSplitType = BillSplitType.DUTCH_TYPE;
        mSplitAmount = dutchTotal;
    }

    // FOR SHARE OR TREAT OPTIONS
    public BillSplit (BillSplitType splitType, BigDecimal shareTotal, int noOfPplSharing) {
        mSplitType = splitType;
        mSplitAmount = shareTotal;
        mNoOfPplSharing = noOfPplSharing;
    }

    // TODO: CALCULATE SPLIT AMOUNT ACCORDINGLY.
    public BigDecimal getSplitAmount() {
        if(isZero(mSplitAmount))
            return mSplitAmount;

        BigDecimal amount = mSplitAmount;

        switch (mSplitType) {
            case SHARE_TYPE:
            case TREAT_TYPE:
                if(mNoOfPplSharing != 0)
                    amount = mSplitAmount.divide(BigDecimal.valueOf(mNoOfPplSharing),
                                2, BigDecimal.ROUND_HALF_EVEN);
                break;
            case TREAT_DUTCH_TYPE:
            case TREAT_SHARE_TYPE:
                break;
            case DUTCH_TYPE:
            default:
                break;
        }

        return amount;
    }

    private Boolean isZero(BigDecimal bdAmount){
        if(bdAmount.compareTo(BigDecimal.ZERO) == 0)
            return  true;
        return false;
    }
}
