package wwckl.projectmiki.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
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
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;

import wwckl.projectmiki.R;
import wwckl.projectmiki.activity.BillSplitterActivity;
import wwckl.projectmiki.activity.EditActivity;
import wwckl.projectmiki.activity.SettingsActivity;
import wwckl.projectmiki.adapter.EditItemAdapter;
import wwckl.projectmiki.models.Bill;
import wwckl.projectmiki.models.Item;

/**
 * Created by Aryn on 7/12/15.
 */
public class EditFragment extends Fragment {
    final String PARCEL_BILL = "PARCEL_BILL";

    private View mView;
    private BillSplitterActivity mBillSplitterActivity;
    private EditActivity mEditActivity;
    private Bill mBill;
    private TextView mSubtotal;
    private EditText mGstPercent;
    private EditText mSvcPercent;
    private TextView mTotal;
    private CheckBox mUseSubtotalsCheckBox;
    private EditItemAdapter mItemAdapter;

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

        RelativeLayout layoutEditTotals = (RelativeLayout) mView.findViewById(R.id.layoutEditTotals);
        mGstPercent = (EditText) layoutEditTotals.findViewById(R.id.etGstPercent);
        mSvcPercent = (EditText) layoutEditTotals.findViewById(R.id.etSvcPercent);
        mSubtotal = (TextView) layoutEditTotals.findViewById(R.id.tvSubTotalCalc);
        mTotal = (TextView) layoutEditTotals.findViewById(R.id.tvTotalCalc);

        //get bottom sheet behavior from bottom sheet view
        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(layoutEditTotals);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        FloatingActionButton fabEditDone = (FloatingActionButton) mView.findViewById(R.id.fabEditDone);
        fabEditDone.setOnClickListener(new View.OnClickListener() {
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

        setupListView();
        updateTotalsText();

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
                //addNewItem();
                displayEditDialog(-1);
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

    private void setupListView() {
        ArrayList<Item> arrayOfItems = (ArrayList<Item>) mBill.getListOfAllItems();
        mItemAdapter = new EditItemAdapter(this.getActivity(), arrayOfItems);
        ListView lvEditItems = (ListView) mView.findViewById(R.id.listEditItems);
        lvEditItems.setAdapter(mItemAdapter);

        lvEditItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View
                    view, int position, long id) {
                    displayEditDialog(position);
                }
        });
    }

    private void displayEditDialog(final int index) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this.getActivity());
        final View dialogView = this.getActivity().getLayoutInflater().inflate(
                R.layout.dialog_edit_item, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setTitle(R.string.add_item);

        final EditText etItemDesc = (EditText) dialogView.findViewById(R.id.etDialogItemDesc);
        final EditText etItemAmt = (EditText) dialogView.findViewById(R.id.etDialogItemAmt);

        if (index >= 0) {
            dialogBuilder.setTitle("Edit Item");
            Item selectedItem  = mBill.getListOfAllItems().get(index);
            DecimalFormat df = new DecimalFormat("0.00");
            etItemDesc.setText(selectedItem.getDescription());
            etItemAmt.setText(df.format(selectedItem.getPrice()));
        }

        dialogBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                upsertItem(index, etItemDesc.getText().toString(), etItemAmt.getText().toString());
            }
        });

        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //pass
            }
        });

        if (index >= 0) {
            dialogBuilder.setNeutralButton("Delete", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    deleteItem(index);
                }
            });
        }
        AlertDialog b = dialogBuilder.create();
        b.show();
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
            case R.id.etSvcPercent:
                if (!mBill.updateSvcPercent(Integer.parseInt(text)))
                    return;
            default:
                break;
        }
        // update Totals UI
        updateTotalsText();
    }

    private void updateTotalsText() {
        DecimalFormat df = new DecimalFormat("0.00");
        mGstPercent.setText(Integer.toString(mBill.getGstPercent()));
        mSvcPercent.setText(Integer.toString(mBill.getSvcPercent()));
        mSubtotal.setText(df.format(mBill.getSubTotal()));
        mTotal.setText(df.format(mBill.getTotal()));
    }

    public void upsertItem(int itemIndex, String description, String amount) {
        if (amount.isEmpty()) return;
        if (itemIndex >= 0) {
            mBill.updateItem(description, itemIndex);
            mBill.updateItem(new BigDecimal(amount), itemIndex);
        } else {
            mBill.addItem(description, amount);
        }
        mItemAdapter.notifyDataSetChanged();
        updateTotalsText();
    }

    public void deleteItem(int itemIndex) {
        Log.d("deleteItem", Integer.toString(itemIndex));
        mBill.deleteItem(itemIndex);
        mItemAdapter.notifyDataSetChanged();
        updateTotalsText();
    }

    // Hide keyboard if present
    private void hideKeyboard() {
        View view = getActivity().getCurrentFocus();
        if(view != null) {
            InputMethodManager inputMethodManager =
                    (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
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
