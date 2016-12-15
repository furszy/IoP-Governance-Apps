package iop.org.iop_contributors_app.furszy_sdk.android.mine;

import android.content.res.Resources;
import android.util.DisplayMetrics;

/**
 * Created by mati on 08/12/16.
 */

public class SizeUtils {

    public static int convertDpToPx(Resources resources, int dp){
        return Math.round(dp*(resources.getDisplayMetrics().xdpi/ DisplayMetrics.DENSITY_DEFAULT));

    }


}
