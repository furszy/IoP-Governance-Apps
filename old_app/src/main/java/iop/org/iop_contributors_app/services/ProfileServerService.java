package iop.org.iop_contributors_app.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import org.iop.AppController;
import org.iop.WalletModule;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import iop.org.iop_contributors_app.profile_server.util.SslContextFactory;
import iop.org.iop_sdk_android.core.crypto.CryptoWrapperAndroid;
import iop.org.iop_sdk_android.core.crypto.KeyEd25519Android;
import iop.org.iop_sdk_android.core.crypto.KeyEd25519;
import iop.org.iop_sdk_android.core.profile_server.Profile;
import iop_sdk.crypto.Crypto;
import iop_sdk.crypto.CryptoWrapper;
import iop_sdk.global.HardCodedConstans;
import iop_sdk.profile_server.ModuleProfileServer;
import iop_sdk.profile_server.ProfileServerConfigurations;
import iop_sdk.profile_server.Signer;
import iop_sdk.profile_server.engine.ProfSerEngine;
import iop_sdk.profile_server.model.ProfServerData;


/**
 * Created by mati on 09/11/16.
 */

public class ProfileServerService extends Service implements ModuleProfileServer {

    private static final String TAG = "ProfileServerService";

    private WalletModule module;

    private ProfileServerConfigurations configurationsPreferences;

    //private ProfileServerHanlder profileServerHanlder;

    private ExecutorService executor;
    
    private AppController application;

    private ProfSerEngine profSerEngine;


    public class ProfServerBinder extends Binder {
        public ProfileServerService getService() {
            return ProfileServerService.this;
        }
    }

    private final IBinder mBinder = new ProfileServerService.ProfServerBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG,".onBind()");
        return mBinder;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG,"onCreate");

        try {

            application = (AppController) getApplication();
            // configurations
            module = application.getModule();
            configurationsPreferences = module.getProfileServerConfigurations();

            executor = Executors.newFixedThreadPool(3);
            // init client data
            initClientData();
            // init profile server
//            profileServer = ApplicationController.getInstance().getProfileServerManager();
            //profileServerHanlder = new ProfileServerHanlder();
            //profileServer.addHandler(profileServerHanlder);

            //init
            initProfileServer();

            profSerEngine.start();

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Initialize the profile server
     * @throws Exception
     */
    private void initProfileServer() throws Exception {

        String host = configurationsPreferences.getHost();
        if (host==null){
            host = HardCodedConstans.HOST;
            configurationsPreferences.setHost(host);
        }

        CryptoWrapper cryptoWrapper = new CryptoWrapperAndroid();

        ProfServerData profServerData = new ProfServerData(HardCodedConstans.HOME_HOST);
        iop_sdk.profile_server.model.Profile profile = new iop_sdk.profile_server.model.Profile(
                new byte[]{1,0,0},
                "Mati",
                new KeyEd25519Android()
        );

        profSerEngine = new ProfSerEngine(
                application,
                profServerData,
                profile,
                cryptoWrapper,
                new SslContextFactory()
                );


        // primary port
//        ProfileServerConfigurationsImp profileServerConfigurations = new ProfileServerConfigurationsImp(host,primaryPort);
//        profileServerConfigurations.setClPort(clPort);
//        profileServerConfigurations.setNonClPort(nonClPort);

        //profileServer = new ProfSerImp(application,configurationsPreferences,null);
    }

    private void initClientData() {
        //todo: esto lo tengo que hacer cuando guarde la privkey encriptada.., por ahora lo dejo asI. Este es el profile que va a crear el usuario, está acá de ejemplo.

        if (configurationsPreferences.isRegisteredInServer()) {

            // load profile

//            byte[] publicKey = configurationsPreferences.getUserPubKey();
//            byte[] privKey = configurationsPreferences.getUserPrivKey();

            KeyEd25519 keyEd25519 = (KeyEd25519) configurationsPreferences.getUserKeys();

            Profile profile = new Profile(
                    configurationsPreferences.getProtocolVersion(),
                    configurationsPreferences.getUsername(),
                    keyEd25519
            );

            module.setProfile(profile);

        } else {

            // save

            Profile profile = new Profile(configurationsPreferences.getProfileVersion(), configurationsPreferences.getUsername());
            module.setProfile(profile);
            // save
            configurationsPreferences.saveUserKeys(profile.getKey());
        }


    }


    @Override
    public int registerReqeust(Signer signer, String name, byte[] img, int latitude, int longitude, String extraData) throws Exception {
        int res = updateProfileRequest(signer,new byte[]{1,0,0},name,img,latitude,longitude,extraData);
        configurationsPreferences.setIsCreated(true);
        return res;
    }

    @Override
    public int updateProfileRequest(Signer signer, byte[] version, String name, byte[] img, int latitude, int longitude, String extraData) throws Exception {
//        Log.d(TAG,"updateProfileRequest, state: "+state);
//        try{
//            configurationsPreferences.setUsername(name);
//
//            return profileServer.updateProfileRequest(
//                    signer,
//                    version,
//                    name,
//                    img,
//                    latitude,
//                    longitude,
//                    extraData
//            );
//        }catch (Exception e){
//            e.printStackTrace();
//            throw e;
//        }

        return 0;

    }

    @Override
    public int updateExtraData(Signer signer, String extraData) throws Exception {
        return 0;//profileServer.updateExtraData(signer,extraData);
    }

    @Override
    public boolean isIdentityCreated() {
        return configurationsPreferences.isRegisteredInServer();
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG,"onStartCommand");


        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy() {
        Log.d(TAG,"onDestroy");

        executor.shutdown();

        super.onDestroy();
    }




}
