package iop_sdk.global;

/**
 * Created by mati on 25/12/16.
 */

public interface IntentWrapper {

    void setPackage(String packageName);

    String getAction();

    String getPackageName();
}
