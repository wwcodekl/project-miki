package wwckl.projectmiki.models;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Aryn on 6/29/15.
 */
public class BillSplitter {
    public enum BillSplitType {
        DUTCH_TYPE,
        SHARE_TYPE,
        TREAT_TYPE, // transitioning type?
        TREAT_SHARE_TYPE,
        TREAT_DUTCH_TYPE
    }

    private BillSplitType SplitType = BillSplitType.DUTCH_TYPE;
    private BigDecimal GuestAmount = new BigDecimal(0.00);
    private BigDecimal TreatAmount = new BigDecimal(0.00);
    private BigDecimal ShareAmount = new BigDecimal(0.00);
    private int NoOfPplSharing = 0;
    private List<Item> listOfItems = new ArrayList<Item>(); // List of items selected.

}
