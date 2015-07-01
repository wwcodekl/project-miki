package wwckl.projectmiki.models;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Created by cheeyim on 2/2/2015.
 */
public class Item {
    final int fNOT_SELECTED = 1;

    private String description;
    private BigDecimal price;
    private int guestIndex;

    public Item(String description, BigDecimal price){
        this.description = description;
        this.price = price;
        this.guestIndex = fNOT_SELECTED; // to indicate not selected
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getGuestIndex() { return guestIndex; }

    public void setGuestIndex(int guestIndex) { this.guestIndex = guestIndex; }
}
