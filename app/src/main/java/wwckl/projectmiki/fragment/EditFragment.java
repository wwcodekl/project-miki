package wwckl.projectmiki.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.RelativeLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import wwckl.projectmiki.R;
import wwckl.projectmiki.activity.BillSplitterActivity;
import wwckl.projectmiki.activity.EditActivity;
import wwckl.projectmiki.activity.SettingsActivity;
import wwckl.projectmiki.models.Bill;
import wwckl.projectmiki.models.Item;
import wwckl.projectmiki.models.Receipt;

/**
 * Created by Aryn on 7/12/15.
 */
public class EditFragment extends Fragment {
    final String PARCEL_BILL = "PARCEL_BILL";

    private View mView;
    private TableLayout mLayoutEditItems;
    private RelativeLayout mLayoutEditTotals;
    private BillSplitterActivity mBillSplitterActivity;
    private EditActivity mEditActivity;
    private Bill mBill;
    private TextView mSubtotal;
    private EditText mGstPercent;
    private EditText mSvcPercent;
    private TextView mTotal;
    private CheckBox mUseSubtotalsCheckBox;
    private ImageView mReceiptImageView;
    private ScrollView mReceiptScrollView;
    private FloatingActionButton mFabEditDone;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_edit, container, false);

        // get views
        mReceiptImageView = (ImageView) mView.findViewById(R.id.imgvReceipt);
        mReceiptScrollView = (ScrollView) mView.findViewById(R.id.scrollImage);
        // get edit fields
        mLayoutEditItems = (TableLayout) mView.findViewById(R.id.layoutEditItems);
        mLayoutEditTotals = (RelativeLayout) mView.findViewById(R.id.layoutEditTotals);
        mGstPercent = (EditText) mLayoutEditTotals.findViewById(R.id.etGstPercent);
        mSvcPercent = (EditText) mLayoutEditTotals.findViewById(R.id.etSvcPercent);
        mSubtotal = (TextView) mLayoutEditTotals.findViewById(R.id.tvSubTotalCalc);
        mTotal = (TextView) mLayoutEditTotals.findViewById(R.id.tvTotalCalc);

        //get bottom sheet behavior from bottom sheet view
        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(mLayoutEditTotals);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        mFabEditDone = (FloatingActionButton) mView.findViewById(R.id.fabEditDone);
        mFabEditDone.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startBillSplitter();
            }
        });

        // Set up UseSubtotals checkbox
        mUseSubtotalsCheckBox = (CheckBox) mView.findViewById(R.id.cbUseSubtotals);
        mUseSubtotalsCheckBox.setChecked(mBill.getUseSubtotals());
        mUseSubtotalsCheckBox.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mBill.setUseSubtotals(mUseSubtotalsCheckBox.isChecked());
                updateTotalsText();
            }
        });

        // setup onFocusChangeListener
        setOnFocusChangeListener(mGstPercent);
        setOnFocusChangeListener(mSvcPercent);

        createItems();
        updateTotalsText();

        // add Image if present
        if (Receipt.getReceiptBitmap() != null) {
            mReceiptImageView.setImageBitmap(Receipt.getReceiptBitmap());
            mReceiptScrollView.setVisibility(View.VISIBLE);
            Log.d("Receipt bitmap", "not null");
        }
        else {
            mReceiptScrollView.setVisibility(View.GONE);
            Log.d("Receipt bitmap", "null");
        }
        return mView;
    }

    @Override
    public void onCreateOptionsMenu (Menu menu, MenuInflater inflater) {
        menu.clear();
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_edit, menu);
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item) {
        // Action bar menu.
        switch (item.getItemId()) {
            case R.id.action_add:
                addNewItem();
                return true;
            case R.id.action_settings:
                Intent settingsIntent = new Intent(this.getActivity(), SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            case R.id.action_help:
                Intent myWebLink = new Intent(android.content.Intent.ACTION_VIEW);
                myWebLink.setData(Uri.parse("https://github.com/WomenWhoCode/KL-network/wiki/Project-Miki-Help-File"));
                startActivity(myWebLink);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof BillSplitterActivity) {
            mBillSplitterActivity = (BillSplitterActivity) activity;
            mBill = mBillSplitterActivity.getActivityBill();

            mBillSplitterActivity.displayToast(getString(R.string.click_done_when_complete), true);
        }
        else if(activity instanceof EditActivity) {
            mEditActivity = (EditActivity) activity;
            mBill = mEditActivity.getActivityBill();
        }
        else {
            mBill = new Bill();
        }
        // do not show keyboard on start of Edit. Allow user to check first.
        hideKeyboard();
    }

    @Override
    public void onResume() {
        super.onResume();
        // do not show keyboard on start of Edit. Allow user to check first.
        hideKeyboard();
    }

    private void setOnFocusChangeListener(EditText editText){

        editText.setOnFocusChangeListener(new EditText.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) setValueOfEditText(((EditText) v).getText().toString(), v.getId());
            }
        });

        // also set onEditorActionListener
        editText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    setValueOfEditText(((EditText) v).getText().toString(), v.getId());
                }
                return false;
            }
        });
    }

    public void setValueOfEditText(String text, int viewId)
    {
        Log.d("onFocusChange", viewId + ":" + text);
        if(text.isEmpty()) {
            text = "0";
        }

        switch (viewId) {
            case R.id.etGstPercent:
                if (!mBill.updateGstPercent(Integer.parseInt(text)))
                    return;
                break;
            case R.id.etSvcPercent:
                if (!mBill.updateSvcPercent(Integer.parseInt(text)))
                    return;
                break;
            default:
                // check for editItems
                int index = viewId;
                Log.d("index edit", Integer.toString(index));

                EditText editText;
                TableRow row = (TableRow) mLayoutEditItems.getChildAt(index + 1);
                editText = (EditText) row.getChildAt(0);
                Log.d("editText 0", editText.getText().toString());
                mBill.updateItem(editText.getText().toString(), index);

                editText = (EditText) row.getChildAt(1);
                Log.d("editText 1", editText.getText().toString());
                if (!mBill.updateItem(new BigDecimal(editText.getText().toString()), index))
                    return;
                break;
        }
        // update Totals UI
        updateTotalsText();
    }

    // OnCreate
    private void createItems() {
        Item item;
        for (int i = 0; i < mBill.getListOfAllItems().size(); i++) {
            item = mBill.getListOfAllItems().get(i);
            addItemRow(item, i);
        }
    }

    private void addItemRow(Item item, int index) {
        EditText editTextDesc;
        EditText editTextAmt;
        ImageButton imageButtonDelete;

        TableRow row = new TableRow(this.getActivity());
        TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
        row.setLayoutParams(lp);

        editTextDesc = new EditText(this.getActivity());
        editTextDesc.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        editTextDesc.setText(item.getDescription());
        editTextDesc.setId(index);
        editTextDesc.setSelectAllOnFocus(true);
        setOnFocusChangeListener(editTextDesc);
        row.addView(editTextDesc);

        editTextAmt = new EditText(this.getActivity());
        editTextAmt.setText(item.getPrice().toString());
        editTextAmt.setInputType(InputType.TYPE_CLASS_NUMBER |
                InputType.TYPE_NUMBER_FLAG_SIGNED | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        editTextAmt.setId(index);
        editTextAmt.setSelectAllOnFocus(true);
        setOnFocusChangeListener(editTextAmt);
        row.addView(editTextAmt);

        imageButtonDelete = new ImageButton(this.getActivity());
        imageButtonDelete.setImageResource(R.drawable.ic_delete_black_18dp);
        imageButtonDelete.setClickable(true);
        imageButtonDelete.setId(index);
        imageButtonDelete.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                deleteItem(v.getId());
            }
        });
        row.addView(imageButtonDelete);

        mLayoutEditItems.addView(row, index+1);
    }

    // check if there are any new items
    private void updateItems() {
        int numOfItems = mBill.getListOfAllItems().size();
        if (mLayoutEditItems.getChildCount() == (numOfItems + 1))
            return;
        Log.d("updateItems", Integer.toString(numOfItems));

        Item item = mBill.getListOfAllItems().get(numOfItems-1);
        addItemRow(item, numOfItems - 1);
    }

    private void updateTotalsText() {
        DecimalFormat df = new DecimalFormat("##.00");
        mGstPercent.setText(Integer.toString(mBill.getGstPercent()));
        mSvcPercent.setText(Integer.toString(mBill.getSvcPercent()));
        mSubtotal.setText(df.format(mBill.getSubTotal()));
        mTotal.setText(df.format(mBill.getTotal()));
    }

    // User clicked add new item button
    public void addNewItem() {
        mBill.addItem();
        // populate new item in mEditItemLayout
        int index = mBill.getListOfAllItems().size()-1;
        Item item = mBill.getListOfAllItems().get(index);
        addItemRow(item, index);

        // Set focus to new row
        TableRow row = (TableRow) mLayoutEditItems.getChildAt(index + 1);
        if (row != null) {
            EditText editText = (EditText) row.getChildAt(0);
            if (editText != null)
                editText.requestFocus();
        }
    }

    public void deleteItem(int itemIndex) {
        Log.d("deleteItem", Integer.toString(itemIndex));

        try {
            mLayoutEditItems.removeViewAt(itemIndex + 1);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        // delete item at location itemIndex
        // needs to be done only after we have finished with the view
        mBill.deleteItem(itemIndex);

        // and update the rest
        for (int i = (itemIndex+1); i < (mLayoutEditItems.getChildCount()); i++) {
            TableRow row = (TableRow) mLayoutEditItems.getChildAt(i);
            row.getChildAt(0).setId(i-1);
            row.getChildAt(1).setId(i-1);
            row.getChildAt(2).setId(i-1);
        }

        updateTotalsText();
    }

    // Hide keyboard if present
    private void hideKeyboard() {
        View view = getActivity().getCurrentFocus();
        if(view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void startBillSplitter() {
        hideKeyboard();
        if (mBillSplitterActivity != null) {
            mBillSplitterActivity.setActivityBill(mBill);
            mBillSplitterActivity.fragmentSuicide();
        }
        else if (mEditActivity != null) {
            mEditActivity.startBillSplitting();
        }
        else {
            // Start bill splitter activity
            Intent intent = new Intent(this.getActivity(), BillSplitterActivity.class);
            startActivity(intent);
        }
    }
}
