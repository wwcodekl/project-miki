package wwckl.projectmiki.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import wwckl.projectmiki.R;
import wwckl.projectmiki.fragment.SettingsFragment;
import wwckl.projectmiki.models.Bill;
import wwckl.projectmiki.models.Receipt;

/**
 * Created by Aryn on 5/17/15.
 * To accomodate user preferences.
 */
public class BillSplitterActivity extends AppCompatActivity {
    final int DUTCH_TYPE = 0;
    final int TREAT_TYPE = 1;
    final int SHARE_TYPE = 2;

    private TextView mTextView;
    private int BillSplitType = DUTCH_TYPE;
    private Bill bill;

    private Menu mMenu;
    private Button mButtonPrev;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill_splitter);

        // Get the various layout objects

        // testing, display recognised text
        mTextView = (TextView) findViewById(R.id.tvRecognisedText);
        String receiptText = Receipt.getRecognizedText();
        if(!receiptText.isEmpty()) {
            // Testing
            mTextView.setText(receiptText);
            bill = new Bill(receiptText);
        }
        else
            Log.d("BillSplitter","empty receiptText");

        // Prev button should be disabled for 1st Guest
        mButtonPrev = (Button)findViewById(R.id.button_prev);
        mButtonPrev.setVisibility(View.INVISIBLE);
        mButtonPrev.setEnabled(false);
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // When shifting from one type of BillSplit type to another.
    private void updateMenuButtons() {
        MenuItem itemDutch = mMenu.findItem(R.id.dutch);
        MenuItem itemTreat = mMenu.findItem(R.id.treat);
        MenuItem itemShare = mMenu.findItem(R.id.share);

        switch(BillSplitType){
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
}
