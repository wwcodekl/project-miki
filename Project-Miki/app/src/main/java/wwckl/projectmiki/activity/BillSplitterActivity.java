package wwckl.projectmiki.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;

import wwckl.projectmiki.R;
import wwckl.projectmiki.models.Bill;
import wwckl.projectmiki.models.BillSplitter;
import wwckl.projectmiki.models.Item;
import wwckl.projectmiki.models.Receipt;

/**
 * Created by Aryn on 5/17/15.
 * To accomodate user preferences.
 */
public class BillSplitterActivity extends AppCompatActivity {
    private BillSplitter.BillSplitType mBillSplitType = BillSplitter.BillSplitType.DUTCH_TYPE;
    private Bill mBill;

    private Menu mMenu;
    private Button mButtonPrev;
    private LinearLayout mItemizedLayout;
    private CheckBox mSelectAllChkBox;
    private TextView mSvcTextView;
    private TextView mGstTextView;
    private TextView mSubTotalTextView;
    private TextView mTotalTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill_splitter);

        // Get the various layout objects
        mItemizedLayout = (LinearLayout)findViewById(R.id.layoutItemized);
        mSelectAllChkBox = (CheckBox)findViewById(R.id.cbSelectAll);
        mSvcTextView = (TextView)findViewById(R.id.tvSVC);
        mGstTextView = (TextView)findViewById(R.id.tvGST);
        mSubTotalTextView = (TextView)findViewById(R.id.tvSubTotal);
        mTotalTextView = (TextView)findViewById(R.id.tvTotal);

        // Prev button should be disabled for 1st Guest
        mButtonPrev = (Button)findViewById(R.id.button_prev);
        mButtonPrev.setVisibility(View.INVISIBLE);
        mButtonPrev.setEnabled(false);

        // Initialise Bill contents
        if (Receipt.getRecognizedText().isEmpty())
            Toast.makeText(this, "Could not read bill!", Toast.LENGTH_SHORT).show();
        else {
            mBill = new Bill(Receipt.getRecognizedText());
            drawItemizedLayout();
            updateTotals();
            if(!mBill.isBillBalanced()){
                Toast.makeText(this, "Bill not balanced!\nPlease check totals.", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_bill_splitter, menu);
        this.mMenu = menu;

        updateMenuButtons();
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item) {
        // Action bar menu.
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            case R.id.dutch:
                swapSplitType(BillSplitter.BillSplitType.DUTCH_TYPE);
                return true;
            case R.id.treat:
                swapSplitType(BillSplitter.BillSplitType.TREAT_TYPE);
                return true;
            case R.id.share:
                swapSplitType(BillSplitter.BillSplitType.SHARE_TYPE);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void swapSplitType(BillSplitter.BillSplitType splitType){
        String toastMsg;
        switch (splitType) {
            case DUTCH_TYPE:
                toastMsg = getString(R.string.dutch);
                break;
            case SHARE_TYPE:
                toastMsg = getString(R.string.share);
                break;
            case TREAT_TYPE:
                toastMsg = getString(R.string.treat);
                break;
            default:
                Toast.makeText(this, "UNKNOWN TYPE", Toast.LENGTH_SHORT).show();
                return;
        }
        Toast toast = Toast.makeText(this, toastMsg, Toast.LENGTH_SHORT);
        toast.show();

        mBillSplitType = splitType;
        updateMenuButtons();
    }

    // When shifting from one type of BillSplit type to another.
    private void updateMenuButtons() {
        MenuItem itemDutch = mMenu.findItem(R.id.dutch);
        MenuItem itemTreat = mMenu.findItem(R.id.treat);
        MenuItem itemShare = mMenu.findItem(R.id.share);

        switch(mBillSplitType){
            default:
            case DUTCH_TYPE:
                itemDutch.setEnabled(false);
                itemTreat.setEnabled(true);
                itemShare.setEnabled(true);
                break;
            case TREAT_TYPE:
                itemDutch.setEnabled(true);
                itemTreat.setEnabled(false);
                itemShare.setEnabled(true);
                break;
            case SHARE_TYPE:
                itemDutch.setEnabled(true);
                itemTreat.setEnabled(true);
                itemShare.setEnabled(false);
                break;
        }
    }

    // populate the itemized layout with bill items.
    private void drawItemizedLayout(){
        if(mBill == null)
            return;

        if(mBill.getListOfAllItems().isEmpty())
            return;

        int numberOfItems = mBill.getListOfAllItems().size();
        CheckBox checkBox;
        Item item;
        String checkBoxText;

        for (int i = 0; i < numberOfItems; i++) {
            item = mBill.getListOfAllItems().get(i);
            checkBox = new CheckBox(this);
            checkBox.setId(i);
            checkBoxText = item.getDescription() + "\t$" + item.getPrice().toString();
            checkBox.setText(checkBoxText);
            checkBox.setOnClickListener(onItemCheckBoxClick(checkBox));
            mItemizedLayout.addView(checkBox);
        }
    }

    View.OnClickListener onItemCheckBoxClick(final Button button) {
        return new View.OnClickListener() {
            public void onClick(View v) {
                System.out.println("*************id******" + button.getId());
                System.out.println("and text***" + button.getText().toString());
            }
        };
    }

    private void updateTotals(){
        if (mBill == null)
            return;

        String tvText;
        String delim = ": $";

        // SERVICE CHARGE
        if (mBill.getSvc().compareTo(BigDecimal.ZERO) != 0) {
            mSvcTextView.setVisibility(View.VISIBLE);
            tvText = getString(R.string.svc);

            if (mBill.getSvcPercent() != 0)
                tvText = tvText + " " + Integer.toString(mBill.getSvcPercent()) + "%";

            tvText = tvText + delim + mBill.getSvc().toString();
            mSvcTextView.setText(tvText);
        }
        else {
            mSvcTextView.setVisibility(View.INVISIBLE);
        }

        // GST
        if (mBill.getGst().compareTo(BigDecimal.ZERO) != 0) {
            mGstTextView.setVisibility(View.VISIBLE);
            tvText = getString(R.string.gst);

            if (mBill.getGstPercent() != 0)
                tvText = tvText + " " + Integer.toString(mBill.getGstPercent()) + "%";

            tvText = tvText + delim + mBill.getGst().toString();
            mGstTextView.setText(tvText);
        }
        else {
            mGstTextView.setVisibility(View.INVISIBLE);
        }

        // SUBTOTAL
        if (mBill.getSubTotal().compareTo(BigDecimal.ZERO) != 0) {
            mSubTotalTextView.setVisibility(View.VISIBLE);
            tvText = getString(R.string.subtotal);

            tvText = tvText + delim + mBill.getSubTotal().toString();
            mSubTotalTextView.setText(tvText);
        }
        else {
            mSubTotalTextView.setVisibility(View.INVISIBLE);
        }

        // TOTAL, ALWAYS SHOW
        tvText = getString(R.string.total);
        tvText = tvText + delim + mBill.getTotal().toString();
        mTotalTextView.setText(tvText);
    }
}
