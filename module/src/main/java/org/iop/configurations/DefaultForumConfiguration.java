package org.iop.configurations;

import android.content.SharedPreferences;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import iop_sdk.forum.ForumConfigurations;
import iop_sdk.forum.ForumProfile;
import iop_sdk.forum.discourge.com.wareninja.opensource.discourse.DiscouseApiConstants;
import iop_sdk.global.HardCodedConstans;


/**
 * Created by mati on 22/11/16.
 */

public class DefaultForumConfiguration extends Configurations implements ForumConfigurations {


    public static final String PREFS_NAME = "MyPrefsFile";

    public static final String PREFS_USER_IS_REGISTERED_IN_FORUM = "isRegInForum";
    public static final String PREFS_USERNAME = "forumUsername";
    public static final String PREFS_PASSWORD = "forumPassword";
    public static final String PREFS_EMAIL = "forumEmail";
    public static final String PREFS_API_KEY = "apiKey";
    public static final String PREFS_URL = "forumUrl";
    private static final String PREFS_WRAPPER_URL = "wrapperUrl";

    private String privateDirUrl;

    public DefaultForumConfiguration(SharedPreferences prefs,String privateDirUrl) {
        super(prefs);
        this.privateDirUrl = privateDirUrl;
    }

    @Override
    public void setIsRegistered(boolean isRegistered) {
        save(PREFS_USER_IS_REGISTERED_IN_FORUM,isRegistered);
    }

    @Override
    public boolean isRegistered() {
        return getBoolean(PREFS_USER_IS_REGISTERED_IN_FORUM,false);
    }

    @Override
    public void setForumUser(String name, String password, String mail) {
        save(PREFS_USERNAME,name);
        save(PREFS_PASSWORD,password);
        if (mail!=null)save(PREFS_EMAIL,mail);
    }

    @Override
    public void setMail(String email) {
        save(PREFS_EMAIL,email);
    }

    @Override
    public void setApiKey(String apiKey) {
        save(PREFS_API_KEY,apiKey);
    }

    @Override
    public void setUrl(String url) {
        save(PREFS_URL,url);
    }

    @Override
    public void setWrapperUrl(String url) {
        save(PREFS_WRAPPER_URL,url);
    }

    @Override
    public ForumProfile getForumUser() {
        String username = getString(PREFS_USERNAME,null);
        if (username==null) return null;
        return new ForumProfile(
                username,
                getString(PREFS_PASSWORD,null),
                getString(PREFS_EMAIL,null)
        );
    }

    @Override
    public String getApiKey() {
        return getString(PREFS_API_KEY,null);
    }

    @Override
    public String getUrl() {
        return getString(PREFS_URL, DiscouseApiConstants.FORUM_URL);
    }

    @Override
    public String getWrapperUrl() {
        return getString(PREFS_WRAPPER_URL,"http://"+ HardCodedConstans.HOST);
    }

    @Override
    public void setUserImg(byte[] profImgData) {
        if (profImgData==null || profImgData.length<1) throw new IllegalArgumentException("Invalid img data array");
        File file = new File(privateDirUrl+"img.png");
        if (file.exists()){
            file.delete();
        }
        try {
            file.createNewFile();

            FileOutputStream fileOutputStream = new FileOutputStream(file);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(profImgData.length);
            byteArrayOutputStream.write(profImgData);
            byteArrayOutputStream.writeTo(fileOutputStream);

            fileOutputStream.close();
            byteArrayOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public File getUserImgFile() {
        return new File(privateDirUrl+"img.png");
    }

    public void removeProfileImg(){
        File img = getUserImgFile();
        if (img.exists()){
            img.delete();
        }
    }

    @Override
    public void remove() {
        super.remove();
        removeProfileImg();
    }
}
