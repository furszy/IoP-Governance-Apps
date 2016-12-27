package iop.org.iop_sdk_android.core.wrappers;

import iop_sdk.global.IntentWrapper;

/**
 * Created by mati on 26/12/16.
 */

public class IntentWrapperAndroid implements IntentWrapper {

    String action;
    String packageName;

    public IntentWrapperAndroid(String action) {
        this.action = action;
    }

    @Override
    public void setPackage(String packageName) {
        this.packageName = packageName;
    }

    @Override
    public String getAction() {
        return action;
    }

    @Override
    public String getPackageName() {
        return packageName;
    }
}
