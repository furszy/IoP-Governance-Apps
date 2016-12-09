package iop.org.iop_contributors_app.wallet;

import android.content.Intent;
import android.content.pm.PackageInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by mati on 12/11/16.
 */

public interface ContextWrapper {

    FileOutputStream openFileOutput(String name,int mode) throws FileNotFoundException;

    FileInputStream openFileInput(String name) throws FileNotFoundException;

    File getFileStreamPath(String name);

    File getDir(String name, int mode);

    void startService(int service,String command,Object... args);

    void toast(String text);


    PackageInfo packageInfo();

    boolean isMemoryLow();

    InputStream openAssestsStream(String name) throws IOException;

    String getPackageName();


    void sendLocalBroadcast(Intent broadcast);

    void showDialog(String id);

    String[] fileList();
}
