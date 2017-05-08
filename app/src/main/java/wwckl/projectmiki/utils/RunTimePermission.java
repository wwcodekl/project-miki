package wwckl.projectmiki.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;

/**
 * Created by renu.yadav on 10/4/17.
 */

public class RunTimePermission {
    public static boolean checkHasPermission(Context context , String permission){
        return (ContextCompat.checkSelfPermission(context,permission) == PackageManager.PERMISSION_GRANTED);
    }
}
