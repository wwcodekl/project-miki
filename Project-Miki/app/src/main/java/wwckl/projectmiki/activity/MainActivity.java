package wwckl.projectmiki.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import wwckl.projectmiki.R;
import wwckl.projectmiki.models.Receipt;


public class MainActivity extends AppCompatActivity {
    final int REQUEST_INPUT_METHOD = 1;  // for checking of requestCode onActivityResult
    final int REQUEST_PICTURE_MEDIASTORE = 2;
    private String mInputMethod = ""; // whether to start Gallery or Camera
    private String mPicturePath = ""; // path of where the picture is saved.
    private ActionMode mActionMode = null; // for Context Action Bar
    private Bitmap mReceiptPicture = null; // bitmap image of the receipt

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up listener for longClick menu for Image
        // to allow user to rotate and crop image
        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setOnLongClickListener(new View.OnLongClickListener() {
            // Called when the user long-clicks on someView
            public boolean onLongClick(View view) {
                if (mActionMode != null) {
                    return false;
                }

                // Start the CAB using the ActionMode.Callback defined above
                mActionMode = MainActivity.this.startActionMode(mActionModeCallback);
                view.setSelected(true);
                return true;
            }
        });

        // if this is the first time loading this activity
        if (savedInstanceState == null) {
            // Check to run Welcome Activity
            // or retrieve default input method
            getDefaultInputMethod();
        }
    }

    // on returning to activity from another activity.
    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first

        TextView t = (TextView)findViewById(R.id.textView);

        if(mPicturePath.isEmpty()){
            // Prompt user to Get image of receipt
            t.setText(getString(R.string.take_a_photo_receipt)
                    +"\n or \n"
                    +getString(R.string.select_image_from_gallery));
        }
        else{ // image will be displayed, hide text.
            t.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Action bar menu; perform activity based on menu item selected.
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            case R.id.action_gallery:
                startGallery();
                return true;
            case R.id.action_camera:
                startCamera();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode) {
            // Retrieve Result from Welcome Screen
            case REQUEST_INPUT_METHOD:
                if (resultCode == RESULT_OK) {
                    mInputMethod = data.getStringExtra("result_input_method");
                }
                else {
                    SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
                    mInputMethod = sharedPrefs.getString("pref_input_method", getString(R.string.gallery));
                }
                // Get receipt image based on selected/default input method.
                getReceiptPicture();
                break;

            // Retrieve Image from Gallery
            case REQUEST_PICTURE_MEDIASTORE:
                if (resultCode == RESULT_OK && data != null) {
                    Uri selectedImage = data.getData();
                    String[] filePathColumn = { MediaStore.Images.Media.DATA };

                    Cursor cursor = getContentResolver().query(selectedImage,
                            filePathColumn, null, null, null);
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    mPicturePath = cursor.getString(columnIndex);
                    cursor.close();

                    mReceiptPicture = BitmapFactory.decodeFile(mPicturePath);
                    ImageView imageView = (ImageView) findViewById(R.id.imageView);
                    imageView.setImageBitmap(mReceiptPicture);
                }
                break;

            default:
                // Not the intended intent
                break;
        }
    }

    // retrieves the selected or default input method
    private void getDefaultInputMethod() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean displayWelcome = sharedPrefs.getBoolean("pref_display_welcome", true);

        if (displayWelcome) {
            startWelcomeActivity();
        }
        else {
            mInputMethod = sharedPrefs.getString("pref_input_method", getString(R.string.gallery));
            // Get receipt image based on selected/default input method.
            getReceiptPicture();
        }
    }

    // retrieves the receipt image
    private void getReceiptPicture() {
        // Retrieve image
        if (mInputMethod.equalsIgnoreCase(getString(R.string.gallery))) {
            startGallery();
        }
        else if (mInputMethod.equalsIgnoreCase(getString(R.string.camera))) {
            startCamera();
        }
        else {
            Log.d("getReceiptImage", "NOT gallery or camera.");
        }
    }

    // display welcome activity and returns with result
    public void startWelcomeActivity() {
        Intent intentInputMethod = new Intent(MainActivity.this, WelcomeActivity.class);
        startActivityForResult(intentInputMethod, REQUEST_INPUT_METHOD);
    }

    // start gallery
    private void startGallery() {
        Intent intentGallery = new Intent(
                Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        startActivityForResult(intentGallery, REQUEST_PICTURE_MEDIASTORE);
    }

    // Start Camera
    private void startCamera() {
        Intent intentCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // start the image capture Intent
        startActivityForResult(intentCamera, REQUEST_PICTURE_MEDIASTORE);
    }

    // onClick of next button
    public void startLoadingAcitivty(View view){
        Receipt.receiptBitmap = mReceiptPicture;
        Intent intent = new Intent(this, LoadingActivity.class);
        startActivity(intent);
    }

    private static Bitmap RotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    // Setting up call backs for Action Bar that will
    // overlay existing when long click on image
    // for editing of image. rotate/crop
    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        // Called when the action mode is created; startActionMode() was called
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.menu_image, menu);
            return true;
        }

        // Called each time the action mode is shown. Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false; // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            ImageView imageView = (ImageView) findViewById(R.id.imageView);

            switch (item.getItemId()) {
                case R.id.rotate_left:
                    mReceiptPicture = RotateBitmap(mReceiptPicture, 270);
                    imageView.setImageBitmap(mReceiptPicture);
                    mode.finish(); // Action picked, so close the CAB
                    return true;
                case R.id.rotate_right:
                    mReceiptPicture = RotateBitmap(mReceiptPicture, 90);
                    imageView.setImageBitmap(mReceiptPicture);
                    mode.finish(); // Action picked, so close the CAB
                    return true;
                default:
                    return false;
            }
        }

        // Called when the user exits the action mode
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
        }
    };
}