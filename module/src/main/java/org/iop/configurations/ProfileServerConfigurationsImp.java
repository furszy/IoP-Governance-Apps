package org.iop.configurations;

import android.content.Context;
import android.content.SharedPreferences;

import org.iop.CryptoUtil;
import org.iop.PrivateStorage;

import iop.org.iop_sdk_android.core.crypto.KeyEd25519;
import iop_sdk.crypto.CryptoBytes;
import iop_sdk.global.HardCodedConstans;
import iop_sdk.profile_server.*;

/**
 * Created by mati on 09/11/16.
 * //todo: falta guardar la priv key del user y del cliente en un archivo encriptado..
 */
public class ProfileServerConfigurationsImp extends Configurations implements iop_sdk.profile_server.ProfileServerConfigurations {

    public static final String PREFS_NAME = "MyPrefsFile";

    public static final String PREFS_NON_CUSTOMER_PORT = "nonClPort";
    public static final String PREFS_CUSTOMER = "clPort";
    public static final String PREFS_PRIMARY = "primPort";
    public static final String PREFS_HOST = "host";

    public static final String PREFS_USER_VERSION = "userVersion";
    public static final String PREFS_USER_NAME = "username";
    public static final String PREFS_USER_PK = "userPk";
    public static final String PREFS_USER_PRIV_KEY = "userPrivKey";
    public static final String PREFS_USER_IMAGE = "userImg";

    public static final String PREFS_USER_IS_REGISTERED_IN_SERVER = "isRegistered";
    public static final String PREFS_USER_IS_CREATED = "isCreated";

    public static final String PREF_PROTOCOL_VERSION = "version";
    // static version for now
    public static final byte[] version = HardCodedConstans.PROTOCOL_VERSION;
    
    private PrivateStorage privateStorage;


    public ProfileServerConfigurationsImp(Context context, SharedPreferences sharedPreferences) {
        super(sharedPreferences);
        privateStorage = new PrivateStorage(context);
    }


    public String getHost() {
        return prefs.getString(PREFS_HOST,HardCodedConstans.HOME_HOST);
    }

    public int getPrimaryPort() {
        return prefs.getInt(PREFS_PRIMARY,HardCodedConstans.PRIMARY_PORT);
    }

    public int getClPort() {
        return prefs.getInt(PREFS_CUSTOMER,0);
    }

    public int getNonClPort() {
        return prefs.getInt(PREFS_NON_CUSTOMER_PORT,0);
    }

    public String getUsername() {
        return prefs.getString(PREFS_USER_NAME,null);
    }


    public byte[] getUserPubKey() {
        return CryptoUtil.hexStringToByte(prefs.getString(PREFS_USER_PK,null));
    }


    public byte[] getProfileVersion() {
        try {
            byte[] bytes = CryptoBytes.fromHexToBytes(prefs.getString(PREFS_USER_VERSION, null));
            return bytes;
        }catch (Exception e){
            return version;
        }
    }

    public boolean isRegisteredInServer() {
        return prefs.getBoolean(PREFS_USER_IS_REGISTERED_IN_SERVER,false);
    }

    public boolean isIdentityCreated() {
        return prefs.getBoolean(PREFS_USER_IS_CREATED,false);
    }

    public byte[] getProtocolVersion(){
        return version;
    }

    public void setHost(String host) {
        final SharedPreferences.Editor edit = prefs.edit();
        edit.putString(PREFS_HOST, host);
        edit.apply();
    }


    public Object getPrivObject(String name){
        return privateStorage.getPrivObj(name);
    }

    public void savePrivObject(String name, Object obj){
        privateStorage.savePrivObj(name,obj);
    }

    public void saveUserKeys(Object obj){
        privateStorage.savePrivObj(PREFS_USER_PRIV_KEY,obj);
    }

    public KeyEd25519 getUserKeys(){
        return (KeyEd25519) privateStorage.getPrivObj(PREFS_USER_PRIV_KEY);
    }

    public byte[] getUserPrivKey() {
        byte[] privKey = new byte[32];
        privateStorage.getFile(PREFS_USER_PRIV_KEY,privKey);
        return privKey;
    }

    public void setPrivKey(byte[] privKey){
        privateStorage.saveFile(PREFS_USER_PRIV_KEY,privKey);
    }

    public void setPrimaryPort(int primaryPort){
        final SharedPreferences.Editor edit = prefs.edit();
        edit.putInt(PREFS_PRIMARY, primaryPort);
        edit.apply();
    }

    public void setClPort(int clPort){
        final SharedPreferences.Editor edit = prefs.edit();
        edit.putInt(PREFS_CUSTOMER, clPort);
        edit.apply();
    }

    public void setNonClPort(int nonClPort){
        final SharedPreferences.Editor edit = prefs.edit();
        edit.putInt(PREFS_NON_CUSTOMER_PORT, nonClPort);
        edit.apply();
    }

    public void setUsername(String username){
        final SharedPreferences.Editor edit = prefs.edit();
        edit.putString(PREFS_USER_NAME, username);
        edit.apply();
    }

    public void setUserPubKey(String userPubKeyHex){
        final SharedPreferences.Editor edit = prefs.edit();
        edit.putString(PREFS_USER_PK, userPubKeyHex);
        edit.apply();
    }

    public void setUserPubKey(byte[] userPubKeyHex){
        final SharedPreferences.Editor edit = prefs.edit();
        edit.putString(PREFS_USER_PK, CryptoBytes.toHexString(userPubKeyHex));
        edit.apply();
    }

    public void setProfileVersion(byte[] version){
        final SharedPreferences.Editor edit = prefs.edit();
        edit.putString(PREFS_USER_VERSION, CryptoBytes.toHexString(version));
        edit.apply();
    }


    public void setIsRegistered(boolean isRegistered){
        final SharedPreferences.Editor edit = prefs.edit();
        edit.putBoolean(PREFS_USER_IS_REGISTERED_IN_SERVER, isRegistered);
        edit.apply();
    }

    public void setIsCreated(boolean isCreated){
        final SharedPreferences.Editor edit = prefs.edit();
        edit.putBoolean(PREFS_USER_IS_CREATED, isCreated);
        edit.apply();
    }


    public void registerOnSharedPreferenceChangeListener(final SharedPreferences.OnSharedPreferenceChangeListener listener) {
        prefs.registerOnSharedPreferenceChangeListener(listener);
    }

    public void unregisterOnSharedPreferenceChangeListener(final SharedPreferences.OnSharedPreferenceChangeListener listener) {
        prefs.unregisterOnSharedPreferenceChangeListener(listener);
    }


}

