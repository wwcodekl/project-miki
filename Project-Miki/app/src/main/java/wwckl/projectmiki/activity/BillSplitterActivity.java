package wwckl.projectmiki.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;
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
public class BillSplitterActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    final int fMAX_SHARING = 9;
    private BillSplitter.BillSplitType mBillSplitType = BillSplitter.BillSplitType.DUTCH_TYPE;
    private Bill mBill;
    private int mNumOfItems = 0;

    private MenuItem mMenuItemDutch;
    private MenuItem mMenuItemTreat;
    private MenuItem mMenuItemShare;
    private Button mButtonPrev;
    private LinearLayout mItemizedLayout;
    private TextView mSvcTextView;
    private TextView mGstTextView;
    private TextView mSubTotalTextView;
    private TextView mTotalTextView;
    private TextView mSplitTypeTextView;
    private TextView mSummaryTextView;
    private Spinner mShareSpinner;
    ArrayAdapter<String> mShareAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill_splitter);

        // Get the various layout objects
        mItemizedLayout = (LinearLayout)findViewById(R.id.layoutItemized);
        mSvcTextView = (TextView)findViewById(R.id.tvSVC);
        mGstTextView = (TextView)findViewById(R.id.tvGST);
        mSubTotalTextView = (TextView)findViewById(R.id.tvSubTotal);
        mTotalTextView = (TextView)findViewById(R.id.tvTotal);
        mSplitTypeTextView = (TextView)findViewById(R.id.tvSplitType);
        mSummaryTextView = (TextView)findViewById(R.id.tvSummary);

        // Prev button should be disabled for 1st Guest
        mButtonPrev = (Button)findViewById(R.id.button_prev);
        mButtonPrev.setVisibility(View.INVISIBLE);
        mButtonPrev.setEnabled(false);

        // Set up share among drop down spinner
        mShareSpinner = (Spinner)findViewById(R.id.spinnerSharing);
        String[] shareNumbers = new String[fMAX_SHARING];
        for (int i = 0; i < fMAX_SHARING; i++) {
            shareNumbers[i] = Integer.toString(i+2);
        }
        mShareAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, shareNumbers);
        mShareSpinner.setAdapter(mShareAdapter);

        // Initialise Bill contents
        if (Receipt.getRecognizedText().isEmpty())
            Toast.makeText(this, getString(R.string.could_not_read_bill), Toast.LENGTH_SHORT).show();
        else {
            mBill = new Bill(Receipt.getRecognizedText());
            drawItemizedLayout();
            updateTotals();
            if(!mBill.isBillBalanced()){
                Toast.makeText(this, getString(R.string.bill_not_balanced), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_bill_splitter, menu);
        mMenuItemDutch = menu.findItem(R.id.dutch);
        mMenuItemTreat = menu.findItem(R.id.treat);
        mMenuItemShare = menu.findItem(R.id.share);

        updateMenuButtons(getBillSplitString(mBillSplitType));
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

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        mBill.setNoOfPplSharing(getNumOfSharing());
        updateTotals();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    // Select all checkbox clicked
    public void onSelectAllChkBoxClicked(View view) {
        // Is the view now checked?
        boolean checked = ((CheckBox) view).isChecked();

        // Check which checkbox was clicked
        switch(view.getId()) {
            case R.id.cbSelectAll:
                if (checked)
                    setCheckAll(true);// uncheck all items
                else
                    setCheckAll(false);// check all items;
                break;
        }
    }

    // Clicked previous button
    public void previousGuest(View view) {}

    // User clicked next button
    public void nextGuest(View view) {}

    // User clicked DONE button
    public void finishBillSplit(View view) {}

    private void swapSplitType(BillSplitter.BillSplitType splitType){
        String billSplitString = getBillSplitString(splitType);

        mBillSplitType = splitType;

        updateMenuButtons(billSplitString);
        updateSummary();
    }

    // When shifting from one type of BillSplit type to another.
    private void updateMenuButtons(String billSplitString) {
        switch(mBillSplitType){
            default:
            case DUTCH_TYPE:
                mMenuItemDutch.setEnabled(false);
                mMenuItemTreat.setEnabled(true);
                mMenuItemShare.setEnabled(true);
                mShareSpinner.setVisibility(View.INVISIBLE);
                mBill.setNoOfPplSharing(0);
                mSplitTypeTextView.setText(billSplitString);
                break;
            case TREAT_TYPE:
                mMenuItemDutch.setEnabled(true);
                mMenuItemTreat.setEnabled(false);
                mMenuItemShare.setEnabled(true);
                mShareSpinner.setVisibility(View.VISIBLE);
                mBill.setNoOfPplSharing(getNumOfSharing());
                mSplitTypeTextView.setText(billSplitString + " by");
                break;
            case SHARE_TYPE:
                mMenuItemDutch.setEnabled(true);
                mMenuItemTreat.setEnabled(true);
                mMenuItemShare.setEnabled(false);
                mShareSpinner.setVisibility(View.VISIBLE);
                mBill.setNoOfPplSharing(getNumOfSharing());
                mSplitTypeTextView.setText(billSplitString + " by");
                break;
        }
    }

    // populate the itemized layout with bill items.
    private void drawItemizedLayout(){
        if(mBill == null)
            return;

        if(mBill.getListOfAllItems().isEmpty())
            return;

        mNumOfItems = mBill.getListOfAllItems().size();
        CheckBox checkBox;
        Item item;
        String checkBoxText;

        for (int i = 0; i < mNumOfItems; i++) {
            item = mBill.getListOfAllItems().get(i);
            checkBox = new CheckBox(this);
            checkBox.setId(i);
            checkBoxText = item.getDescription() + "\t" + getString(R.string.symbol_currency) + item.getPrice().toString();
            checkBox.setText(checkBoxText);
            checkBox.setOnClickListener(onItemCheckBoxClick(checkBox));
            mItemizedLayout.addView(checkBox);
        }
    }

    View.OnClickListener onItemCheckBoxClick(final Button button) {
        return new View.OnClickListener() {
            public void onClick(View v) {
                int itemIndex = button.getId();
                boolean checked = ((CheckBox) v).isChecked();

                mBill.selectItem(itemIndex, checked);
                updateSummary();
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

    private void setCheckAll (Boolean checked){
        CheckBox checkBox;
        for (int i = 0; i < mNumOfItems; i++) {
            checkBox = (CheckBox) mItemizedLayout.findViewById(i);
            if ((checkBox.isEnabled()) && (checkBox.isChecked() != checked)){
                checkBox.setChecked(checked);
                mBill.selectItem(i, checked);
            }
        }
        updateSummary();
    }

    private void updateSummary(){
        int numOfSplits = mBill.getBillSplitNum();
        String summaryText = "";

        for (int i = 0; i < numOfSplits; i++) {
            String guestText;
            BillSplitter billSplit = mBill.getListOfGuests().get(i);

            switch ( billSplit.getSplitType() ) {
                case DUTCH_TYPE:
                default:
                    guestText = getString(R.string.guest) +" : " + billSplit.getSplitAmount().toString() + "\n";
                case SHARE_TYPE:
                case TREAT_TYPE:
                    guestText = getString(R.string.guest) +"s : " + billSplit.getSplitAmount().toString() + "\n";
            }

            summaryText = summaryText + guestText;
        }

        BigDecimal amount = mBill.getGuestTotal(numOfSplits);
        if ( mBillSplitType == BillSplitter.BillSplitType.DUTCH_TYPE )
            summaryText = summaryText + getString(R.string.guest) +" : $" +
                    amount.toString() + "\n";
        else {
            amount = amount.divide(BigDecimal.valueOf(getNumOfSharing()), 2, BigDecimal.ROUND_HALF_EVEN);
            summaryText = summaryText + getString(R.string.guest) + "s : $" + amount + "\n";
        }

        mSummaryTextView.setText(summaryText);
    }

    private int getNumOfSharing(){
        return Integer.parseInt((String) mShareSpinner.getSelectedItem());
    }

    public String getBillSplitString(BillSplitter.BillSplitType splitType){
        switch (splitType) {
            case DUTCH_TYPE:
            case TREAT_DUTCH_TYPE:
                return getString(R.string.dutch);
            case SHARE_TYPE:
            case TREAT_SHARE_TYPE:
                return getString(R.string.share);
            case TREAT_TYPE:
                return getString(R.string.treat);
            default:
                return getString(R.string.unknown_type);
        }
    }
}
