package wwckl.projectmiki.models;

import android.graphics.Bitmap;

/**
 * Created by cheeyim on 2/2/2015.
 */
public class Receipt {
    // Receipt image
    private static Bitmap mReceiptBitmap = null;
    private static String mRecognizedText;

    public static Bitmap getReceiptBitmap() {
        return mReceiptBitmap;
    }

    public static void setReceiptBitmap(Bitmap receiptBitmap){
        mReceiptBitmap = receiptBitmap;
    }

    public static String getRecognizedText(){
        return mRecognizedText;
    }

    public static void setRecognizedText(String recognizedText) {
        mRecognizedText = recognizedText;
    }
}
