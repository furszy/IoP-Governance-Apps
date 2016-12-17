package iop.org.iop_contributors_app.core.iop_sdk.utils;

import android.text.Html;
import android.text.Spanned;

/**
 * Created by mati on 17/12/16.
 */

public class TextUtils {

    /**
     * Transform the text to html text with color
     * @param text
     * @param color
     * @return
     */
    public static String transformToHtmlWithColor(String text, String color){
        return "<font color='"+color+"'>"+text+"</font>";

    }


}
