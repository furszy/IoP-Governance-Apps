package iop.org.iop_contributors_app;

import android.app.ActivityManager;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.util.Log;

import org.bitcoinj.crypto.LinuxSecureRandom;
import org.bitcoinj.utils.Threading;
import org.slf4j.LoggerFactory;

import java.io.File;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.android.LogcatAppender;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import iop.org.iop_contributors_app.configurations.DefaultForumConfiguration;
import iop.org.iop_contributors_app.configurations.ProfileServerConfigurations;
import iop.org.iop_contributors_app.core.iop_sdk.forum.ForumConfigurations;
import iop.org.iop_contributors_app.profile_server.ModuleProfileServer;

import iop.org.iop_contributors_app.services.BlockchainServiceImpl;
import iop.org.iop_contributors_app.services.ProfileServerService;
import iop.org.iop_contributors_app.configurations.WalletPreferencesConfiguration;
import iop.org.iop_contributors_app.wallet.BlockchainManager;
import iop.org.iop_contributors_app.wallet.WalletConstants;
import iop.org.iop_contributors_app.wallet.WalletManager;
import iop.org.iop_contributors_app.wallet.WalletModule;

/**
 * Created by mati on 07/11/16.
 */

public class ApplicationController extends Application {

    private final String TAG = "ApplicationController";

    private static ApplicationController instance;

    // profile server
    private ModuleProfileServer profileServerService;
    private ProfileServerConfigurations profileServerPref;
    // wallet
    private WalletModule module;
    private WalletPreferencesConfiguration walletConfigurations;
    // Forum
    private ForumConfigurations forumConfigurations;
    // application
    private PackageInfo packageInfo;
    private ActivityManager activityManager;


    @Override
    public void onCreate() {

        Log.d(TAG,"initializing app");

        new LinuxSecureRandom(); // init proper random number generator

        super.onCreate();

        packageInfo = packageInfoFromContext(this);
        activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

        initLogging();

        instance = this;

        Threading.uncaughtExceptionHandler = new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(final Thread thread, final Throwable throwable)
            {
                Log.d(TAG,"bitcoinj uncaught exception", throwable);
//                CrashReporter.saveBackgroundTrace(throwable, packageInfo);
            }
        };

        // initialize preferences
        walletConfigurations = new WalletPreferencesConfiguration(getSharedPreferences(WalletPreferencesConfiguration.PREFS_NAME,0));
        profileServerPref = new ProfileServerConfigurations(this,getSharedPreferences(ProfileServerConfigurations.PREFS_NAME,0));
        forumConfigurations = new DefaultForumConfiguration(getSharedPreferences(DefaultForumConfiguration.PREFS_NAME,0));


        // Module initialization
        Log.d(TAG,"initializing module");
        org.bitcoinj.core.Context.enableStrictMode();
        org.bitcoinj.core.Context.propagate(WalletConstants.CONTEXT);
        module = new WalletModule(this,walletConfigurations,forumConfigurations);
        module.start();

        startBlockchainService();



//        try {
//
//            // We need an Editor object to make preference changes.
//            // All objects are from android.context.Context
//            SharedPreferences settings = getSharedPreferences(ProfileServerConfigurations.PREFS_NAME, 0);
//            profileServerPref = new ProfileServerConfigurations(this,settings);
//
//            startProfileServerService();
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

    }


    public ServiceConnection profServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder binder) {
            Log.d(TAG,"profile service connected");
            profileServerService = ((ProfileServerService.ProfServerBinder)binder).getService();
        }
        //binder comes from server to communicate with method's of

        public void onServiceDisconnected(ComponentName className) {
            Log.d("ServiceConnection","disconnected");
            profServiceConnection = null;
        }
    };

    private void startProfileServerService() {
        Intent intent = new Intent(this,ProfileServerService.class);
        bindService(intent,profServiceConnection,Context.BIND_AUTO_CREATE);
    }

    private void startBlockchainService() {
        Intent blockchainServiceIntent = new Intent(this, BlockchainServiceImpl.class);
        startService(blockchainServiceIntent);
    }


    public static ApplicationController getInstance() {
        return instance;
    }


    public ModuleProfileServer getProfileServerManager(){
        if (profileServerService ==null) throw new IllegalStateException("Profile server is not connected");
        return profileServerService;
    }


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

    public WalletManager getWalletManager() {
        return module.getWalletManager();
    }

    public WalletPreferencesConfiguration getWalletConfigurations() {
        return walletConfigurations;
    }

    public static PackageInfo packageInfoFromContext(final Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        }
        catch (final PackageManager.NameNotFoundException x) {
            throw new RuntimeException(x);
        }
    }

    public PackageInfo packageInfo()
    {
        return packageInfo;
    }

    public boolean isMemoryLow(){
        final int memoryClass = activityManager.getMemoryClass();
        return memoryClass<= WalletConstants.MEMORY_CLASS_LOWEND;
    }

    public BlockchainManager getBlockchainManager() {
        return module.getBlockchainManager();
    }

    public ProfileServerConfigurations getProfileServerPreferences() {
        return profileServerPref;
    }

    public WalletModule getWalletModule() {
        return module;
    }

}
