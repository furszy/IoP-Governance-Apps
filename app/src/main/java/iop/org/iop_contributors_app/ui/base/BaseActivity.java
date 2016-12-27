package iop.org.iop_contributors_app.ui.base;

import android.app.Activity;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.bitcoinj.core.Context;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import iop.org.furszy_lib.base.NavViewHelper;
import iop.org.iop_contributors_app.ApplicationController;
import iop.org.iop_contributors_app.R;
import iop.org.furszy_lib.nav_view.NavMenuItem;
import iop.org.iop_contributors_app.intents.DialogIntentsBuilder;
import iop.org.iop_contributors_app.intents.constants.IntentsConstants;
import iop.org.iop_contributors_app.services.BlockchainServiceImpl;
import iop.org.iop_contributors_app.ui.ProfileActivity;
import iop.org.furszy_lib.adapter.FermatListItemListeners;
import iop.org.iop_contributors_app.utils.Cache;
import org.iop.WalletConstants;
import iop.org.iop_contributors_app.module.WalletModule;

import static android.graphics.Color.WHITE;
import static iop.org.iop_contributors_app.intents.constants.IntentsConstants.INTENTE_BROADCAST_DIALOG_TYPE;
import static iop.org.iop_contributors_app.intents.constants.IntentsConstants.INTENT_BROADCAST_DATA_ON_COIN_RECEIVED;
import static iop.org.iop_contributors_app.intents.constants.IntentsConstants.INTENT_BROADCAST_DATA_TYPE;
import static iop.org.iop_contributors_app.intents.constants.IntentsConstants.INTENT_DATA;
import static iop.org.iop_contributors_app.intents.constants.IntentsConstants.INTENT_DIALOG;
import static iop.org.iop_contributors_app.intents.constants.IntentsConstants.INTENT_NOTIFICATION;
import static iop.org.iop_contributors_app.intents.constants.IntentsConstants.RESTORE_SUCCED_DIALOG;
import static iop.org.furszy_lib.utils.QrUtils.encodeAsBitmap;
import static iop.org.furszy_lib.utils.SizeUtils.convertDpToPx;

/**
 * Created by mati on 07/11/16.
 */

public abstract class BaseActivity extends AppCompatActivity {

    private static final String TAG = "BaseActivity";

    public static final String ACTION_NOTIFICATION = BaseActivity.class.getPackage().toString() + "_action_notification";


    protected NotificationManager notificationManager;
    protected LocalBroadcastManager localBroadcastManager;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ViewGroup container;
    protected Toolbar toolbar;

    private NavViewHelper navViewHelper;

    private TextView txt_available_balance;
    private TextView txt_lock_balance;
    private TextView txt_drawer_name;
    private ImageView imgQr;
    private ImageView img_photo;

    private NotificationReceiver notificationReceiver = new NotificationReceiver();

    protected ApplicationController application;
    protected WalletModule module;

    protected ExecutorService executor;


    @Override
    public final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {

            notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            localBroadcastManager = LocalBroadcastManager.getInstance(this);

            application = ApplicationController.getInstance();
            module = application.getWalletModule();

            executor = Executors.newFixedThreadPool(3);

            beforeCreate(savedInstanceState);

            setContentView(R.layout.base_main);
            initToolbar();
            if (hasDrawer()) setupDrawerLayout();
            container = (ViewGroup) findViewById(R.id.container);
            onCreateView(container, savedInstanceState);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (hasDrawer())
            updateBasicValues();
    }

    private void updateBasicValues(){
        try {
            File imgFile = module.getUserImageFile();
            if (imgFile.exists())
                Picasso.with(this).load(imgFile).into(img_photo);
        }catch (Exception e){
            e.printStackTrace();
        }

        updateBalances();
    }

    private void initToolbar() {
        toolbar  = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.icon_back);
            actionBar.setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }

    }

    private void setupDrawerLayout() {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        // toolbar button
        toolbar.setNavigationIcon(R.drawable.menu_hamburguesa);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        navigationView = (NavigationView) findViewById(R.id.navigation_view);

        View headerView = navigationView.getHeaderView(0);

        headerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(BaseActivity.this, ProfileActivity.class));
            }
        });

        navViewHelper = new NavViewHelper(
                this,
                drawerLayout,
                navigationView,
                (RecyclerView) navigationView.findViewById(R.id.recycler_nav_view)
        );
        // init navView.
//        navViewHelper.setItemsList(loadNavMenuItems());
        navViewHelper.setHeaderView(headerView);
        navViewHelper.init();

        // Method to initialize navView in childs
        onNavViewCreated(navViewHelper);
        // init data
        navViewHelper.initAdapter();

        // todo: pasar esto al navViewHelper, no lo aún ya que es lo mismo en las apps voting y en contributors
        imgQr = (ImageView) navigationView.findViewById(R.id.img_qr);
        int appColor = appColor();
        if (appColor!=-1){
            ((TextView)navigationView.findViewById(R.id.txt_increase_voting_power)).setTextColor(appColor);
        }
        txt_available_balance = (TextView) headerView.findViewById(R.id.txt_available_balance);
        txt_lock_balance = (TextView) headerView.findViewById(R.id.txt_lock_balance);
        txt_drawer_name = (TextView) headerView.findViewById(R.id.txt_drawer_name);
        img_photo = (ImageView) headerView.findViewById(R.id.img_photo);

        txt_drawer_name.setText(module.getForumProfile().getUsername());

        imgQr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showQrDialog(BaseActivity.this);
            }
        });

        executor.submit(new Runnable() {
            @Override
            public void run() {

                Context.propagate(WalletConstants.CONTEXT);

                try {

                    // qr
                    Bitmap qrBitmap = Cache.getQrLittleBitmapCache();
                    if (qrBitmap==null){
                        qrBitmap = encodeAsBitmap(module.getReceiveAddress(),imgQr.getWidth(),imgQr.getHeight(),WHITE,Color.parseColor("#1A1A1A"));
                        Cache.setQrLittleBitmapCache(qrBitmap);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        imgQr.setImageBitmap(Cache.getQrLittleBitmapCache());
                    }
                });
            }
        });

    }

    private void updateBalances(){
        try {
            if (hasDrawer()) {
                txt_available_balance.setText(module.getAvailableBalanceStr() + " IoPs");
                txt_lock_balance.setText(module.getLockedBalance() + " IoPs");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /***************************** Nav View region  ************************************/

    protected void onNavViewCreated(NavViewHelper navViewHelper){

    }

    protected List<NavMenuItem> loadNavMenuItems(){
        return null;
    }

    protected void setNavViewHeaderBackground(int resource){
        navViewHelper.setHeaderViewBackground(resource);
    }

    protected void setNavViewBackgroundColor(int color){
        navViewHelper.setNavViewBackgroundColor(color);
    }

    public void setNavMenuListener(FermatListItemListeners<NavMenuItem> navMenuListener) {
        navViewHelper.setNavMenuListener(navMenuListener);
    }

    /***************************** end Nav View region  ************************************/

    /**
     * Method to change the app base color
     * @return
     */
    protected int appColor(){
        return -1;
    }


    protected void sendWorkToProfileService(Bundle data){
//        Intent intent = new Intent(this, ProfileServerService.class);
//        intent.putExtras(data);
//        startService(intent);
//        ApplicationController.getInstance().getProfileServerManager().updateProfileRequest();

    }

    public void sendWorkToBlockchainService(String action, Bundle data){
        Intent intent = new Intent(action,null,this, BlockchainServiceImpl.class);
        intent.putExtras(data);
        startService(intent);
    }

    protected ViewGroup getContainer() {
        return container;
    }

    /**
     *
     * @param savedInstance
     */
    protected  void onCreateView(ViewGroup container, Bundle savedInstance){}

    protected void beforeCreate(Bundle savedInstanceState){
        // nothing
    }

    protected boolean hasOptionMenu(){
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        localBroadcastManager.registerReceiver(notificationReceiver,new IntentFilter(ACTION_NOTIFICATION));

    }

    @Override
    protected void onStop() {
        localBroadcastManager.unregisterReceiver(notificationReceiver);
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (navViewHelper!=null)
            navViewHelper.onDestroy();
    }

    private void showQrDialog(Activity activity){

        try {
            final Dialog dialog = new Dialog(activity);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.qr_dialog);


            // set the custom dialog components - text, image and button
            TextView text = (TextView) dialog.findViewById(R.id.txt_share);

            dialog.findViewById(R.id.txt_exit).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            ImageView image = (ImageView) dialog.findViewById(R.id.img_qr);

            final String address = module.getReceiveAddress();
            // qr
            Bitmap qrBitmap = Cache.getQrBigBitmapCache();
            if (qrBitmap == null) {
                Resources r = getResources();
                int px = convertDpToPx(getResources(),175);
                qrBitmap = encodeAsBitmap(address, px, px, Color.parseColor("#1A1A1A"), WHITE );
                Cache.setQrBigBitmapCache(qrBitmap);
            }
            image.setImageBitmap(qrBitmap);

            // cache address
            TextView txt_qr = (TextView)dialog.findViewById(R.id.txt_qr);
            txt_qr.setText(address);
//            txt_qr.setOnLongClickListener();

            View.OnClickListener clickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    textToClipboard(address);
                    Toast.makeText(BaseActivity.this,"Copied",Toast.LENGTH_LONG).show();
                }
            };

            dialog.findViewById(R.id.txt_copy).setOnClickListener(clickListener);

            text.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    shareText(BaseActivity.this,"Qr",address);
                    dialog.dismiss();
                }
            });



            dialog.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * metodo llamado cuando un permiso es otorgado
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
//        switch (requestCode) {
//            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
//                // If request is cancelled, the result arrays are empty.
//                if (grantResults.length > 0
//                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//
//                    // permission was granted, yay! Do the
//                    // contacts-related task you need to do.
//
//                } else {
//
//                    // permission denied, boo! Disable the
//                    // functionality that depends on this permission.
//                }
//                return;
//            }
//
//            // other 'case' lines to check for other
//            // permissions this app might request
//        }
    }


    /**
     *
     * @return false if the activity don't know how to do with that broadcast and this activity send a notification
     */
    protected abstract boolean onBroadcastReceive(Bundle data);

    /**
     * Enable drawer
     *
     * @return
     */
    protected abstract boolean hasDrawer();

    private class NotificationReceiver extends BroadcastReceiver{


        @Override
        public void onReceive(android.content.Context context, Intent intent) {

            Bundle bundle = intent.getExtras();

            Log.i(TAG,"broadcast data received");

            if (intent.getAction().equals(ACTION_NOTIFICATION)){
                // tipo de broadcast
                String type = bundle.getString(IntentsConstants.INTENT_BROADCAST_TYPE);

                if (type.equals(INTENT_DIALOG)){
                    // tipo de dialog
                    int dialogType = bundle.getInt(INTENTE_BROADCAST_DIALOG_TYPE);

                    if (dialogType == RESTORE_SUCCED_DIALOG){
                        DialogIntentsBuilder.buildSuccedRestoreDialog(BaseActivity.this,module.getWalletManager(),intent)
                                .show();

                    }else {
                        onBroadcastReceive(bundle);
                    }
                }else
                    if (type.equals(INTENT_DATA)){
                        Log.e(TAG,"llegó algo al intent_data inesperado!");

                    }
                else
                    if (type.equals(INTENT_NOTIFICATION)){

                        if (!onBroadcastReceive(bundle)) {
                            android.support.v4.app.NotificationCompat.Builder mBuilder =
                                    new NotificationCompat.Builder(getApplicationContext())
                                            .setSmallIcon(R.drawable.ic__launcher)
                                            .setContentTitle("Transaction broadcast succed!")
                                            .setContentText(intent.getStringExtra("title"));

                            notificationManager.notify(0, mBuilder.build());

                        }
                    }
                else if (type.equals(INTENT_DATA+INTENT_NOTIFICATION)){



                        if (bundle.containsKey(INTENT_BROADCAST_DATA_TYPE)){
                            String dataType = bundle.getString(INTENT_BROADCAST_DATA_TYPE);

                            if (dataType.equals(INTENT_BROADCAST_DATA_ON_COIN_RECEIVED)){
                                updateBalances();
                            }else {

                                if (!onBroadcastReceive(bundle)) {
                                    android.support.v4.app.NotificationCompat.Builder mBuilder =
                                            new NotificationCompat.Builder(getApplicationContext())
                                                    .setSmallIcon(R.drawable.ic__launcher)
                                                    .setContentTitle("Proposal broadcast succed!")
                                                    .setContentText(intent.getStringExtra("title"));

                                    notificationManager.notify(3, mBuilder.build());
                                }
                            }


                        }else {

                            android.support.v4.app.NotificationCompat.Builder mBuilder =
                                    new NotificationCompat.Builder(getApplicationContext())
                                            .setSmallIcon(R.drawable.ic__launcher)
                                            .setContentTitle("Proposal broadcast succed!")
                                            .setContentText(intent.getStringExtra("title"));

                            notificationManager.notify(0, mBuilder.build());
                        }
                    }
            }
        }
    }

    private void textToClipboard(String text) {
        if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(android.content.Context.CLIPBOARD_SERVICE);
            clipboard.setText(text);
        } else {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(android.content.Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", text);
            clipboard.setPrimaryClip(clip);
        }
    }


    private void shareText(Activity activity,String title,String text){
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, text);
        sendIntent.setType("text/plain");
        activity.startActivity(Intent.createChooser(sendIntent, title));
    }
}
