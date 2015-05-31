package wwckl.projectmiki.activity;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.os.Handler;
import android.widget.TextView;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import wwckl.projectmiki.R;
import wwckl.projectmiki.models.Receipt;

/**
 * Created by Aryn on 5/24/15.
 */
public class LoadingActivity extends AppCompatActivity {
    private Bitmap mReceiptPicture = null;
    private String mRecognizedText = "start";

    private ImageView mImageView;
    private ProgressBar mProgressBar;

    private int mProgressStatus = 0;
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        // Set receipt image in background.
        mReceiptPicture = Receipt.receiptBitmap;

        mImageView = (ImageView) findViewById(R.id.imageViewLoading);
        mImageView.setImageBitmap(mReceiptPicture);

        // Progress bar
        startProgressBar();

        // START OCR operation
        TesseractDetectText();

        //Receipt.recognizedText = mRecognizedText;
        // TODO: CLEAN UP DATA
        if(!mRecognizedText.isEmpty()) {
            TextView t = (TextView) findViewById(R.id.tvRecognisedText);
            t.setText(mRecognizedText);

            mImageView.setVisibility(View.GONE);
        }

        // completed job. stop thread & hide progress bar
        stopProgressBar();
        //startBillSplitting();
    }

    // set value for progress bar.
    public int incrementSpinner(){
        switch (mProgressStatus) {
            case 99:
                return 0;
            case 100:
                return mProgressStatus;
            default:
                return mProgressStatus+1;
        }
    }

    // Start thread to run progress bar
    public void startProgressBar(){
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        // start progress bar thread.
        new Thread(new Runnable() {
            public void run() {
                while (mProgressStatus < 100) {
                    mProgressStatus = incrementSpinner();

                    // sleep 0.5 second
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    // Update the progress bar
                    mHandler.post(new Runnable() {
                        public void run() {
                            mProgressBar.setProgress(mProgressStatus);
                        }
                    });
                }
            }
        }).start();
    }

    public void stopProgressBar(){
        mProgressStatus = 100;

        mProgressBar.setVisibility(View.GONE);
    }

    public void startBillSplitting(){
        // TODO: STORE RECEIPT RESULT?
        Intent intent = new Intent(this, BillSplitterActivity.class);
        startActivity(intent);
    }

    public void TesseractDetectText() {
        // create tessdata directory
        File tessDir = new File(Environment.getExternalStorageDirectory().getPath() + "/tessdata");
        if (!tessDir.exists()) {
            tessDir.mkdir();
        }

        // get data path
        String path = Environment.getExternalStorageDirectory().getPath();
        String lang = "eng";

        File tessData = new File(path + "/tessdata/" + lang + ".traineddata");

        // Copy tessdata language file
        if (!tessData.exists()) {
            try {
                AssetManager assetManager = getAssets();
                InputStream in = assetManager.open(lang + ".traineddata");
                OutputStream out = new FileOutputStream(path
                        + "/tessdata/" + lang + ".traineddata");

                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                out.close();
            } catch (IOException e) {}
        }

        TessBaseAPI tessBaseAPI = new TessBaseAPI();
        tessBaseAPI.setDebug(true);
        tessBaseAPI.init(path, lang); //Init the Tess with the trained data file, with english language

        // Set the Receipt image
        tessBaseAPI.setImage(mReceiptPicture);
        // Retrieve text detected
        mRecognizedText = tessBaseAPI.getUTF8Text();

        tessBaseAPI.end();
    }
}