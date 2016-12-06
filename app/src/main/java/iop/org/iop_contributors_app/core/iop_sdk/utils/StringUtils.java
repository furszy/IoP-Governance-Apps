package iop.org.iop_contributors_app.core.iop_sdk.utils;

/**
 * Created by mati on 01/12/16.
 */

public class StringUtils {

    public static String cleanString(String s){
        StringBuilder stringBuilder = new StringBuilder();
        for(int i=0;i<s.length();i++){
            char c = s.charAt(i);
            if (c != '\"' && c!='[' && c!=']' && c!='\\')
                stringBuilder.append(c);
        }
        return stringBuilder.toString();
    }

}
