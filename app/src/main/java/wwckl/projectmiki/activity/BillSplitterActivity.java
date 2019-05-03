package wwckl.projectmiki.activity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;
import java.util.Iterator;

import wwckl.projectmiki.R;
import wwckl.projectmiki.fragment.EditFragment;
import wwckl.projectmiki.fragment.SummaryFragment;
import wwckl.projectmiki.models.Bill;
import wwckl.projectmiki.models.BillSplit;
import wwckl.projectmiki.models.Item;

/**
 * Created by Aryn on 5/17/15.
 * To accomodate user preferences.
 */
public class BillSplitterActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    final int fMAX_SHARING = 9;
    private BillSplit.BillSplitType mBillSplitType = BillSplit.BillSplitType.DUTCH_TYPE;
    private Bill mBill;
    private int mNumOfItems = 0;
    private String mShareText = "";

    private MenuItem mMenuItemDutch;
    private MenuItem mMenuItemTreat;
    private MenuItem mMenuItemShare;
    private Button mButtonPrev;
    private Button mButtonNext;
    private Button mButtonDone;
    private CheckBox mSelectAllCheckBox;
    private LinearLayout mItemizedLayout;
    private RelativeLayout mTotalsLayout;
    private LinearLayout mSummaryLayout;
    private TextView mSvcTextView;
    private TextView mGstTextView;
    private TextView mSubTotalTextView;
    private TextView mTotalTextView;
    private TextView mSplitTypeTextView;
    private TextView mSummaryTextView;
    private TextView mInstructionsTextView;
    private ScrollView mSummaryScrollView;
    private Spinner mShareSpinner;
    ArrayAdapter<String> mShareAdapter;
    private SummaryFragment mSummaryFragment;
    private FragmentManager mFragmentManager;
    private SharedPreferences mSharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill_splitter);

        // Set app icon to be displayed on action bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        // get preference manager
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        // get fragment manager
        mFragmentManager = getFragmentManager();

        // Get the various layout objects
        mSelectAllCheckBox = (CheckBox)findViewById(R.id.cbSelectAll);
        mItemizedLayout = (LinearLayout)findViewById(R.id.layoutItemized);
        mSvcTextView = (TextView)findViewById(R.id.tvSVC);
        mGstTextView = (TextView)findViewById(R.id.tvGST);
        mSubTotalTextView = (TextView)findViewById(R.id.tvSubTotalLabel);
        mTotalTextView = (TextView)findViewById(R.id.tvTotalLabel);
        mSplitTypeTextView = (TextView)findViewById(R.id.tvSplitType);
        mSummaryTextView = (TextView)findViewById(R.id.tvSummary);
        mSummaryScrollView = (ScrollView)findViewById(R.id.scrollSummary);
        mSummaryFragment = (SummaryFragment) mFragmentManager.findFragmentById(R.id.summaryFragment);
        mButtonNext = (Button)findViewById(R.id.button_next);
        mButtonDone = (Button)findViewById(R.id.button_done);
        mTotalsLayout = (RelativeLayout) findViewById(R.id.layoutTotals);
        mSummaryLayout = (LinearLayout) findViewById(R.id.layoutSummary);

        // Prev button should be disabled for 1st Guest
        mButtonPrev = (Button)findViewById(R.id.button_prev);
        mButtonPrev.setVisibility(View.INVISIBLE);

        // Set up share among drop down spinner
        mShareSpinner = (Spinner)findViewById(R.id.spinnerSharing);
        String[] shareNumbers = new String[fMAX_SHARING];
        for (int i = 0; i < fMAX_SHARING; i++) {
            shareNumbers[i] = Integer.toString(i+2);
        }
        mShareAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, shareNumbers);
        mShareSpinner.setAdapter(mShareAdapter);

        // set up helper instruction text
        mInstructionsTextView = (TextView)findViewById(R.id.tvInstructions);
        boolean displayHelper = mSharedPrefs.getBoolean(getString(R.string.pref_show_help_messages), true);
        if (displayHelper)
            mInstructionsTextView.setVisibility(View.VISIBLE);
        else
            mInstructionsTextView.setVisibility(View.GONE);

        // Initialise Bill contents
        Bundle b = getIntent().getExtras();
        mBill = b.getParcelable("Bill");

        // initialise contents
        drawItemizedLayout();
        updateTotals();
    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_bill_splitter, menu);
        mMenuItemDutch = menu.findItem(R.id.dutch);
        mMenuItemTreat = menu.findItem(R.id.treat);
        mMenuItemShare = menu.findItem(R.id.share);

        updateMenuButtons(mBillSplitType);
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
            case R.id.action_help:
                Intent myWebLink = new Intent(android.content.Intent.ACTION_VIEW);
                myWebLink.setData(Uri.parse("https://github.com/WomenWhoCode/KL-network/wiki/Project-Miki-Help-File"));
                startActivity(myWebLink);
                return true;
            case R.id.action_share:
                shareBill();
                return true;
            case R.id.dutch:
                swapSplitType(BillSplit.BillSplitType.DUTCH_TYPE);
                return true;
            case R.id.treat:
                swapSplitType(BillSplit.BillSplitType.TREAT_TYPE);
                return true;
            case R.id.share:
                swapSplitType(BillSplit.BillSplitType.SHARE_TYPE);
                return true;
            case R.id.action_edit:
                startEditFragment();
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

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
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
    public void previousGuest(View view) {
        BillSplit lastBillSplit = mBill.removeLastBillSplit();

        if(lastBillSplit == null)
            return;

        mSummaryFragment.prevBillSplit(lastBillSplit);

        // if TREAT_SHARE or TREAT_DUTCH, we have no easy way to recalculate TREAT amount pax.
        // Simpler to back track all the way to last TREAT_TYPE
        while ((lastBillSplit.getSplitType() == BillSplit.BillSplitType.TREAT_SHARE_TYPE) ||
                (lastBillSplit.getSplitType() == BillSplit.BillSplitType.TREAT_DUTCH_TYPE))
        {
            lastBillSplit = mBill.removeLastBillSplit();
            if(lastBillSplit == null) // this line of code should not be triggered
                return;
            mSummaryFragment.prevBillSplit(lastBillSplit);
        }

        mBillSplitType = lastBillSplit.getSplitType();
        updateCheckBoxes();
        updateSummary();
        updateButtons();
        updateMenuButtons(mBillSplitType);
    }

    // User clicked next button
    public void nextGuest(View view) {
        int numOfSplits = mBill.getNumOfBillSplits();
        BillSplit billSplit = mSummaryFragment.nextBillSplit(mBillSplitType,
                mBill.getGuestTotal(numOfSplits), getNumOfSharing());

        if (billSplit != null) {
            // Add new BillSplit to mBill
            mBill.addBillSplit(billSplit);
            updateCheckBoxes();
            updateSummary();
            updateButtons();
            // Default to TREAT_DUTCH_TYPE if click next on TREAT
            updateMenuButtons(mBillSplitType);
        }
    }

    // User clicked DONE button
    public void finishBillSplit(View view) {
        // We are already displaying Summary, new bill.
        if (getString(R.string.new_).contentEquals(mButtonDone.getText()))
            newBill();
        else {
            // Check all remaining items automatically
            setCheckAll(true);
            // then new billSplit
            nextGuest(view);
        }
    }

    // should be called after updateCheckBoxes.
    private void updateButtons() {
        int numOfCalculatedItems = 0;
        boolean displayHelper = mSharedPrefs.getBoolean(getString(R.string.pref_show_help_messages), true);

        for (int i = 0; i < mBill.getListOfAllItems().size(); i++) {
            CheckBox checkBox = (CheckBox) mItemizedLayout.findViewById(i);
            if (!checkBox.isEnabled())
                numOfCalculatedItems++;
        }

        if ((numOfCalculatedItems == mBill.getListOfAllItems().size()) &&
                (mSummaryFragment.getRemainingNumOfPplTreating() == 0)) {
            mButtonPrev.setVisibility(View.VISIBLE);
            mButtonNext.setVisibility(View.INVISIBLE);
            mButtonDone.setText(getString(R.string.new_));
            // Completed Bill split, blow up summary
            mSummaryFragment.largeView();
            mSummaryScrollView.setVisibility(View.VISIBLE);
            setSummaryScrollViewWeight(true);
            mInstructionsTextView.setVisibility(View.GONE);
            mSummaryLayout.setVisibility(View.GONE);
            mSelectAllCheckBox.setVisibility(View.GONE);

            // get split summary
            mShareText = mSummaryFragment.getBillSplitSummary();
        }
        else if (mBill.getNumOfBillSplits() == 0) {
            mButtonPrev.setVisibility(View.INVISIBLE);
            mButtonNext.setVisibility(View.VISIBLE);
            mButtonDone.setText(getString(R.string.done));
            mSummaryFragment.smallView();
            mSummaryScrollView.setVisibility(View.GONE);
            mSummaryLayout.setVisibility(View.VISIBLE);
            mSelectAllCheckBox.setVisibility(View.VISIBLE);
            if (displayHelper)
                mInstructionsTextView.setVisibility(View.VISIBLE);
            else
                mInstructionsTextView.setVisibility(View.GONE);
        }
        else {
            mButtonPrev.setVisibility(View.VISIBLE);
            mButtonNext.setVisibility(View.VISIBLE);
            mButtonDone.setText(getString(R.string.done));
            mSummaryFragment.smallView();
            mSummaryScrollView.setVisibility(View.VISIBLE);
            setSummaryScrollViewWeight(false);
            mInstructionsTextView.setVisibility(View.GONE);
            mTotalsLayout.setVisibility(View.VISIBLE);
            mSummaryLayout.setVisibility(View.VISIBLE);
            mSelectAllCheckBox.setVisibility(View.VISIBLE);
        }
    }

    // update checkboxes, e.g. re-enable checkboxes onClick button Prev
    private void updateCheckBoxes () {
        Iterator<Item> iterator = mBill.getListOfAllItems().iterator();
        int numOfBillSplits = mBill.getNumOfBillSplits();
        int id = 0;

        while (iterator.hasNext()) {
            Item item = iterator.next();
            CheckBox checkBox = (CheckBox) mItemizedLayout.findViewById(id);
            if (item.getGuestIndex() == item.fNOT_SELECTED) {
                checkBox.setEnabled(true);
                checkBox.setChecked(false);
            }
            else if (item.getGuestIndex() >= numOfBillSplits) {
                checkBox.setEnabled(true);
                // in case it was selected, clear highlight
                checkBox.setBackgroundColor(getResources().getColor(R.color.background_material_light));
            }
            else
                checkBox.setEnabled(false);
            id++;
        }
    }

    // for when user select New button
    public void newBill() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    // User swap split type on actionbar menu.
    private void swapSplitType(BillSplit.BillSplitType splitType){
        updateMenuButtons(splitType);
        setInstructionsText(splitType);
        updateSummary();
    }

    // When shifting from one type of BillSplit type to another.
    // Should be done after updateButtons()
    private void updateMenuButtons(BillSplit.BillSplitType splitType) {
        // IF updateButtons has set mButtonDone to new, no more bill splitting
        if (getString(R.string.new_).contentEquals(mButtonDone.getText())) {
            mMenuItemDutch.setEnabled(false);
            mMenuItemTreat.setEnabled(false);
            mMenuItemShare.setEnabled(false);
            return;
        }

            // Update type if active TREAT type Change Split type accordingly.
        if (mSummaryFragment.getRemainingNumOfPplTreating() > 0) {
            if(splitType == BillSplit.BillSplitType.SHARE_TYPE )
                mBillSplitType = BillSplit.BillSplitType.TREAT_SHARE_TYPE;
            else
                mBillSplitType = BillSplit.BillSplitType.TREAT_DUTCH_TYPE;
        }
        else {
            // otherwise, change back.
            switch (splitType) {
                case TREAT_DUTCH_TYPE:
                    mBillSplitType = BillSplit.BillSplitType.DUTCH_TYPE;
                    break;
                case TREAT_SHARE_TYPE:
                    mBillSplitType = BillSplit.BillSplitType.SHARE_TYPE;
                    break;
                default:
                    mBillSplitType = splitType;
                    break;
            }
        }

        // Get string
        String billSplitString = mSummaryFragment.getBillSplitString(mBillSplitType);

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
            case TREAT_DUTCH_TYPE:
                mMenuItemDutch.setEnabled(false);
                mMenuItemTreat.setEnabled(false);
                mMenuItemShare.setEnabled(true);
                mShareSpinner.setVisibility(View.INVISIBLE);
                mBill.setNoOfPplSharing(0);
                mSplitTypeTextView.setText(billSplitString);
                break;
            case TREAT_SHARE_TYPE:
                mMenuItemDutch.setEnabled(true);
                mMenuItemTreat.setEnabled(false);
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
        BigDecimal amount = mBill.getGuestTotal(mBill.getNumOfBillSplits());
        int remaingNumOfPplSharing = mSummaryFragment.getRemainingNumOfPplTreating();
        String summaryText = "";

        switch (mBillSplitType) {
            case TREAT_DUTCH_TYPE:
            case DUTCH_TYPE:
                if (remaingNumOfPplSharing > 0) {
                    amount = amount.add(mSummaryFragment.getTreatAmountPax());
                }
                summaryText = getString(R.string.amount) +": $" + amount.toString();
                break;
            case TREAT_SHARE_TYPE:
            case SHARE_TYPE:
                if (remaingNumOfPplSharing > 0) {
                    amount = amount.add(mSummaryFragment.getTreatAmountPax().multiply(BigDecimal.valueOf(remaingNumOfPplSharing)));
                }
            case TREAT_TYPE:
                summaryText = "pax. " + getString(R.string.amount) + ": $" + amount.toString();
                break;
            default:
                break;
        }

        mSummaryTextView.setText(summaryText);
    }

    private int getNumOfSharing(){
        return Integer.parseInt((String) mShareSpinner.getSelectedItem());
    }

    private void setSummaryScrollViewWeight(Boolean isDone) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mSummaryScrollView.getLayoutParams();
        float weight = 12; // default

        if (isDone) {
            //gravity = 20;
            LinearLayout.LayoutParams doneParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
            params = doneParams;
        }
        else
            params.height = 0;

        if (mBill.getNumOfBillSplits() < 5) {
            // add 3 for each split type
            weight = mBill.getNumOfBillSplits() * 3.0f;
        }

        params.weight = weight;
        mSummaryScrollView.setLayoutParams(params);
    }

    private void setInstructionsText(BillSplit.BillSplitType splitType) {
        boolean displayHelper = mSharedPrefs.getBoolean(getString(R.string.pref_show_help_messages), true);
        if (displayHelper && (mBill.getNumOfBillSplits() == 0))
            mInstructionsTextView.setVisibility(View.VISIBLE);
        else {
            mInstructionsTextView.setVisibility(View.GONE);
            return;
        }

        switch (splitType) {
            case DUTCH_TYPE:
            case TREAT_DUTCH_TYPE:
                mInstructionsTextView.setText(getString(R.string.dutch_split_instructions));
                break;
            case SHARE_TYPE:
            case TREAT_SHARE_TYPE:
                mInstructionsTextView.setText(getString(R.string.share_split_instructions));
                break;
            case TREAT_TYPE:
                mInstructionsTextView.setText(getString(R.string.treat_split_instructions));
                break;
            default:
                mInstructionsTextView.setText(getString(R.string.bill_split_instructions));
                break;
        }
    }

    // ************************ EDIT FRAGMENT CALLBACKS ********************
    private void startEditFragment() {
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        EditFragment fragment = new EditFragment();

        fragmentTransaction.replace(android.R.id.content, fragment);
        fragmentTransaction.addToBackStack(getString(R.string.edit));
        fragmentTransaction.commit();
    }

    public Bill getActivityBill() {
        return mBill;
    }

    public void setActivityBill(Bill bill) {
        mBill = bill;
    }

    // This should only ever be called by dynamically added fragments
    public void fragmentSuicide() {
        mFragmentManager.popBackStack();
        updateItems();
        updateTotals();
    }

    private void updateItems() {
        if (mItemizedLayout.findViewById(0) == null) {
            drawItemizedLayout();
        }
        else {
            // check what we have against mBill.ListOfItems
            Iterator<Item> iterator = mBill.getListOfAllItems().iterator();
            int numOfBillSplits = mBill.getNumOfBillSplits();
            int id = 0;
            String checkBoxText;

            while (iterator.hasNext()) {
                Item item = iterator.next();
                CheckBox checkBox = (CheckBox) mItemizedLayout.findViewById(id);
                if (checkBox != null) {
                    // update text
                    checkBoxText = item.getDescription() + "\t" + getString(R.string.symbol_currency) + item.getPrice().toString();
                    checkBox.setText(checkBoxText);

                    // update is selected checked
                    if (item.getGuestIndex() == item.fNOT_SELECTED) {
                        checkBox.setEnabled(true);
                        checkBox.setChecked(false);
                    } else if (item.getGuestIndex() >= numOfBillSplits) {
                        checkBox.setEnabled(true);
                    } else
                        checkBox.setEnabled(false);
                }
                else {
                    // added new item
                    checkBox = new CheckBox(this);
                    checkBox.setId(id);
                    checkBoxText = item.getDescription() + "\t" + getString(R.string.symbol_currency) + item.getPrice().toString();
                    checkBox.setText(checkBoxText);
                    checkBox.setOnClickListener(onItemCheckBoxClick(checkBox));
                    mItemizedLayout.addView(checkBox);
                }
                id++;
            }

            // update local variable
            mNumOfItems = mBill.getListOfAllItems().size();

            // check to reomve items that have been deleted.
            while (mItemizedLayout.getChildCount() > mNumOfItems+1) {
                mItemizedLayout.removeViewAt(mItemizedLayout.getChildCount()-1);
            }
        }
    }
    // ********************** END EDIT FRAGMENT CALLBACKS ******************

    public void displayToast(String toastString, Boolean isHelper) {
        if (isHelper) {
            boolean displayHelper = mSharedPrefs.getBoolean(getString(R.string.pref_show_help_messages), true);

            if (!displayHelper)
                return;
        }

        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast,
                (ViewGroup) findViewById(R.id.toast_layout_root));

        TextView text = (TextView) layout.findViewById(R.id.text);
        text.setText(toastString);

        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }

    private void shareBill() {
        String subject = getResources().getString(R.string.app_name);
        String body = mShareText;
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        shareIntent.putExtra(Intent.EXTRA_TEXT, body);
        startActivity(Intent.createChooser(shareIntent, getResources().getString(R.string.share_bill)));
    }

    public void highlightSplitItems(int index, Boolean setSelected) {
        Iterator<Item> iterator = mBill.getListOfAllItems().iterator();
        int id = 0;

        while (iterator.hasNext()) {
            Item item = iterator.next();

            CheckBox checkBox = (CheckBox) mItemizedLayout.findViewById(id);
            if (checkBox != null) {
                if (item.getGuestIndex() == index) {
                    if (setSelected)
                        checkBox.setBackgroundColor(getResources().getColor(R.color.primary_light));
                    else
                        checkBox.setBackgroundColor(getResources().getColor(R.color.background_material_light));
                }
                else if (item.getGuestIndex() < 0) // in case user clicked prev.
                    checkBox.setBackgroundColor(getResources().getColor(R.color.background_material_light));
            }
            id++;
        }
    }

    public String printSplitItems(int index) {
        Iterator<Item> iterator = mBill.getListOfAllItems().iterator();
        int id = 0;
        StringBuilder items = new StringBuilder();

        while (iterator.hasNext()) {
            Item item = iterator.next();

            CheckBox checkBox = (CheckBox) mItemizedLayout.findViewById(id);
            if (checkBox != null && item.getGuestIndex() == index) {
                items.append("\n").append("- ");
                items.append(checkBox.getText().toString());
            }
            id++;
        }
        return items.toString();
    }
}
