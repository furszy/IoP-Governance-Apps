package iop.org.contributors_app;

import android.app.ActivityManager;
import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import org.bitcoinj.crypto.LinuxSecureRandom;
import org.bitcoinj.utils.Threading;
import org.iop.AppController;
import org.iop.PrivateStorage;
import org.iop.WalletConstants;
import org.iop.WalletModule;
import org.iop.configurations.DefaultForumConfiguration;
import org.iop.configurations.WalletPreferencesConfiguration;
import org.iop.intents.constants.IntentsConstants;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.android.LogcatAppender;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import iop.org.furszy_lib.dialogs.DialogBuilder;
import iop.org.iop_contributors_app.core.iop_sdk.blockchain.explorer.android.TransactionStorageSQlite;
import iop.org.iop_contributors_app.services.BlockchainService;
import iop.org.iop_contributors_app.services.BlockchainServiceImpl;
import iop.org.iop_contributors_app.services.ServicesCodes;
import iop.org.iop_contributors_app.ui.ProfileActivity;
import iop.org.iop_contributors_app.ui.base.BaseActivity;
import iop.org.iop_contributors_app.utils.AppUtils;
import iop.org.iop_contributors_app.utils.CrashReporter;
import iop.org.iop_sdk_android.core.wrappers.PackageInfoAndroid;
import iop_sdk.forum.ForumConfigurations;
import iop_sdk.global.IntentWrapper;
import iop_sdk.global.PackageInfoWrapper;

import static org.iop.WalletConstants.SHOW_BLOCKCHAIN_OFF_DIALOG;
import static org.iop.WalletConstants.SHOW_IMPORT_EXPORT_KEYS_DIALOG_FAILURE;

/**
 * Created by mati on 07/11/16.
 */

public class ApplicationController extends Application implements AppController {

    private final String TAG = "ApplicationController";
    public static final long TIME_CREATE_APPLICATION = System.currentTimeMillis();

    private static ApplicationController instance;

    // profile server
    //private ModuleProfileServer profileServerService;
    //private ProfileServerConfigurationsImp profileServerPref;
    // wallet
    private WalletModule module;
    private WalletPreferencesConfiguration walletConfigurations;
    // Forum
    private ForumConfigurations forumConfigurations;
    // application
    private PackageInfo packageInfo;
    private ActivityManager activityManager;
    // android services
    private LocalBroadcastManager localBroadcastManager;

    private static String APP_TYPE ;

    @Override
    public void onCreate() {

        Log.d(TAG,"initializing app");

        new LinuxSecureRandom(); // init proper random number generator

        super.onCreate();

        packageInfo = AppUtils.packageInfoFromContext(this);
        activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        localBroadcastManager = LocalBroadcastManager.getInstance(this);

        initLogging();

        instance = this;

        String packageName = packageInfo.packageName;
        Intent launchIntent = getPackageManager().getLaunchIntentForPackage(packageName);
        String className = launchIntent.getComponent().getClassName();
        APP_TYPE = className;

        Threading.uncaughtExceptionHandler = new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(final Thread thread, final Throwable throwable)
            {
                Log.d(TAG,"bitcoinj uncaught exception", throwable);
//                CrashReporter.saveBackgroundTrace(throwable, packageInfoWrapper);
            }
        };

        // initialize preferences
        walletConfigurations = new WalletPreferencesConfiguration(getSharedPreferences(WalletPreferencesConfiguration.PREFS_NAME,0));
        //profileServerPref = new ProfileServerConfigurationsImp(this,getSharedPreferences(ProfileServerConfigurationsImp.PREFS_NAME,0));
        forumConfigurations = new DefaultForumConfiguration(getSharedPreferences(DefaultForumConfiguration.PREFS_NAME,0),getFilesDir().getAbsolutePath());

        // Crash reporter
        CrashReporter.init(getCacheDir());

        Threading.uncaughtExceptionHandler = new Thread.UncaughtExceptionHandler()
        {
            @Override
            public void uncaughtException(final Thread thread, final Throwable throwable)
            {
                Log.i(TAG,"bitcoinj uncaught exception", throwable);
                CrashReporter.saveBackgroundTrace(throwable, packageInfo);
            }
        };



        // Module initialization
        Log.d(TAG,"initializing module");
        org.bitcoinj.core.Context.enableStrictMode();
        org.bitcoinj.core.Context.propagate(WalletConstants.CONTEXT);
        module = new WalletModule(this, walletConfigurations, forumConfigurations);
        module.setTransactionStorage(new TransactionStorageSQlite(new PrivateStorage(this)));
        module.start();

        startBlockchainService();



//        try {
//
//            // We need an Editor object to make preference changes.
//            // All objects are from android.context.Context
//            SharedPreferences settings = getSharedPreferences(ProfileServerConfigurationsImp.PREFS_NAME, 0);
//            profileServerPref = new ProfileServerConfigurationsImp(this,settings);
//
//            startProfileServerService();
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

    }


//    public ServiceConnection profServiceConnection = new ServiceConnection() {
//        public void onServiceConnected(ComponentName className, IBinder binder) {
//            Log.d(TAG,"profile service connected");
//            profileServerService = ((ProfileServerService.ProfServerBinder)binder).getService();
//        }
//        //binder comes from server to communicate with method's of
//
//        public void onServiceDisconnected(ComponentName className) {
//            Log.d("ServiceConnection","disconnected");
//            profServiceConnection = null;
//        }
//    };
//
//    private void startProfileServerService() {
//        Intent intent = new Intent(this,ProfileServerService.class);
//        bindService(intent,profServiceConnection,Context.BIND_AUTO_CREATE);
//    }

    private void startBlockchainService() {
        Intent blockchainServiceIntent = new Intent(this, BlockchainServiceImpl.class);
        startService(blockchainServiceIntent);
    }


    public static ApplicationController getInstance() {
        return instance;
    }


//    public ModuleProfileServer getProfileServerManager(){
//        if (profileServerService ==null) throw new IllegalStateException("Profile server is not connected");
//        return profileServerService;
//    }


    private void initLogging() {

        final File logDir = getDir("log", Context.MODE_WORLD_READABLE );//Constants.TEST ? Context.MODE_WORLD_READABLE : MODE_PRIVATE);
        final File logFile = new File(logDir, "wallet.log");

        final LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        final PatternLayoutEncoder filePattern = new PatternLayoutEncoder();
        filePattern.setContext(context);
        filePattern.setPattern("%d{HH:mm:ss,UTC} [%thread] %logger{0} - %msg%n");
        filePattern.start();

        final RollingFileAppender<ILoggingEvent> fileAppender = new RollingFileAppender<ILoggingEvent>();
        fileAppender.setContext(context);
        fileAppender.setFile(logFile.getAbsolutePath());

        final TimeBasedRollingPolicy<ILoggingEvent> rollingPolicy = new TimeBasedRollingPolicy<ILoggingEvent>();
        rollingPolicy.setContext(context);
        rollingPolicy.setParent(fileAppender);
        rollingPolicy.setFileNamePattern(logDir.getAbsolutePath() + "/wallet.%d{yyyy-MM-dd,UTC}.log.gz");
        rollingPolicy.setMaxHistory(7);
        rollingPolicy.start();

        fileAppender.setEncoder(filePattern);
        fileAppender.setRollingPolicy(rollingPolicy);
        fileAppender.start();

        final PatternLayoutEncoder logcatTagPattern = new PatternLayoutEncoder();
        logcatTagPattern.setContext(context);
        logcatTagPattern.setPattern("%logger{0}");
        logcatTagPattern.start();

        final PatternLayoutEncoder logcatPattern = new PatternLayoutEncoder();
        logcatPattern.setContext(context);
        logcatPattern.setPattern("[%thread] %msg%n");
        logcatPattern.start();

        final LogcatAppender logcatAppender = new LogcatAppender();
        logcatAppender.setContext(context);
        logcatAppender.setTagEncoder(logcatTagPattern);
        logcatAppender.setEncoder(logcatPattern);
        logcatAppender.start();

        final ch.qos.logback.classic.Logger log = context.getLogger(Logger.ROOT_LOGGER_NAME);
        log.addAppender(fileAppender);
        log.addAppender(logcatAppender);
        log.setLevel(Level.INFO);
    }


    public PackageInfo packageInfo() {
        return packageInfo;
    }
//    public ProfileServerConfigurationsImp getProfileServerPreferences() {
//        return profileServerPref;
//    }

    public WalletModule getModule() {
        return module;
    }

    public void sendLocalBroadcast(Intent intent) {
        localBroadcastManager.sendBroadcast(intent);
    }

    public boolean isVotingApp(){
        return false;
    }


    /**  CONTEXT WRAPPER METHODS  */

    @Override
    public FileOutputStream openFileOutputPrivateMode(String name) throws FileNotFoundException {
        return openFileOutput(name,Context.MODE_PRIVATE);
    }

    @Override
    public File getDirPrivateMode(String name) {
        return getDir(name,Context.MODE_PRIVATE);
    }

    @Override
    public void startService(int service, String command, Object... args) {
        Class<? extends Service> serviceClass = switchServices(service);
        //todo: despues podria cachear los intents
        Intent intent = new Intent(command,null,this,serviceClass);
        super.startService(intent);
    }

    @Override
    public void toast(String text) {
        Toast.makeText(this,text,Toast.LENGTH_LONG).show();
    }

    @Override
    public PackageInfoWrapper packageInfoWrapper() {
        return new PackageInfoAndroid(packageInfo());
    }

    public boolean isMemoryLow(){
        final int memoryClass = activityManager.getMemoryClass();
        return memoryClass<= WalletConstants.MEMORY_CLASS_LOWEND;
    }

    @Override
    public long getTimeCreateApplication() {
        return TIME_CREATE_APPLICATION;
    }

    @Override
    public InputStream openAssestsStream(String name) throws IOException {
        return super.getAssets().open(name);
    }

    @Override
    public void sendLocalBroadcast(IntentWrapper broadcast) {
        final Intent intent = new Intent();
        if (broadcast.getAction()!=null){
            intent.setAction(broadcast.getAction());
        }
        if (broadcast.getPackageName()!=null) {
            broadcast.setPackage(getPackageName());
        }
        localBroadcastManager.sendBroadcast(intent);
    }


    @Override
    public void showDialog(String id) {
        showDialog(id,null);
    }
    @Override
    public void showDialog(String id, String dialogText){
        if (id.equals(WalletConstants.SHOW_RESTORE_SUCCED_DIALOG)){
            showRestoreSuccedDialog();
        }else if (id.equals(SHOW_BLOCKCHAIN_OFF_DIALOG)){
            showBlockchainOff(dialogText);
        }else if (id.equals(SHOW_IMPORT_EXPORT_KEYS_DIALOG_FAILURE)){
            showImportExportKeysDialogFailure(dialogText);
        }
    }

    private void showImportExportKeysDialogFailure(String message) {
        final DialogBuilder dialog = DialogBuilder.warn(this, iop.org.iop_contributors_app.R.string.import_export_keys_dialog_failure_title);
        dialog.setMessage(this.getString(iop.org.iop_contributors_app.R.string.import_keys_dialog_failure, message));
        dialog.setPositiveButton(iop.org.iop_contributors_app.R.string.button_dismiss, null);
        dialog.setNegativeButton(iop.org.iop_contributors_app.R.string.button_retry, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int id) {
                //showDialog(DIALOG_RESTORE_WALLET);
            }
        });
        dialog.show();
    }

    @Override
    /**
     * Hardcoded method.
     */

    public void stopBlockchainService() {
        startService(ServicesCodes.BLOCKCHAIN_SERVICE, BlockchainService.ACTION_RESET_BLOCKCHAIN);
    }

    private Class<? extends Service> switchServices(int service) {
        Class<? extends Service> clazz = null;
        switch (service){
            case ServicesCodes.BLOCKCHAIN_SERVICE:
                clazz = BlockchainServiceImpl.class;
                break;
//            case ServicesCodes.PROFILE_SERVER_SERVICE:
//                clazz = ProfileServerService.class;
//                break;
            default:
                throw new RuntimeException("Service unknown");
        }
        return clazz;
    }

    private void showRestoreSuccedDialog(){
        String message = this.getString(iop.org.iop_contributors_app.R.string.restore_wallet_dialog_success) +
                "\n\n" +
                this.getString(iop.org.iop_contributors_app.R.string.restore_wallet_dialog_success_replay);

        Intent intent = new Intent(BaseActivity.ACTION_NOTIFICATION);
        intent.putExtra(IntentsConstants.INTENT_BROADCAST_TYPE,IntentsConstants.INTENT_DIALOG);
        intent.putExtra(IntentsConstants.INTENTE_BROADCAST_DIALOG_TYPE,IntentsConstants.RESTORE_SUCCED_DIALOG);
        intent.putExtra(IntentsConstants.INTENTE_EXTRA_MESSAGE, message);
        localBroadcastManager.sendBroadcast(intent);
    }

    private void showBlockchainOff(String dialogText){
        Intent intent = new Intent(BaseActivity.ACTION_NOTIFICATION);
        intent.putExtra(IntentsConstants.INTENT_BROADCAST_TYPE,IntentsConstants.INTENT_DIALOG);
        intent.putExtra(IntentsConstants.INTENTE_BROADCAST_DIALOG_TYPE,IntentsConstants.COMMON_ERROR_DIALOG);
        intent.putExtra(IntentsConstants.INTENTE_EXTRA_MESSAGE, dialogText);
        localBroadcastManager.sendBroadcast(intent);
    }

    /**
     * Hardcoded method, i'm a lazy lazy person..
     * @return
     */
    @Override
    public Class<?> getProfileActivity() {
        return ProfileActivity.class;
    }

}
