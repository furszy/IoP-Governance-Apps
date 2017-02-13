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
import android.net.Uri;
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
import org.bitcoinj.utils.BtcFormat;
import org.iop.AppController;
import org.iop.WalletConstants;
import org.iop.WalletModule;
import org.iop.intents.constants.IntentsConstants;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import iop.org.furszy_lib.adapter.FermatListItemListeners;
import iop.org.furszy_lib.base.NavViewHelper;
import iop.org.furszy_lib.dialogs.SimpleTwoButtonsDialog;
import iop.org.furszy_lib.nav_view.NavMenuItem;
import iop.org.iop_contributors_app.R;
import iop.org.iop_contributors_app.intents.DialogIntentsBuilder;
import iop.org.iop_contributors_app.services.BlockchainServiceImpl;
import iop.org.iop_contributors_app.ui.BalanceActivity;
import iop.org.iop_contributors_app.ui.dialogs.SimpleDialogs;
import iop.org.iop_contributors_app.utils.Cache;

import static android.graphics.Color.WHITE;
import static iop.org.furszy_lib.utils.QrUtils.encodeAsBitmap;
import static iop.org.furszy_lib.utils.SizeUtils.convertDpToPx;
import static iop.org.iop_contributors_app.ui.dialogs.SimpleDialogs.showQrDialog;
import static org.bitcoinj.utils.BtcFormat.COIN_SCALE;
import static org.iop.intents.constants.IntentsConstants.ACTION_NOTIFICATION;
import static org.iop.intents.constants.IntentsConstants.ADMIN_NOTIFICATION_DIALOG;
import static org.iop.intents.constants.IntentsConstants.INTENTE_BROADCAST_DIALOG_TYPE;
import static org.iop.intents.constants.IntentsConstants.INTENT_BROADCAST_DATA_BLOCKCHAIN_STATE;
import static org.iop.intents.constants.IntentsConstants.INTENT_BROADCAST_DATA_ON_COIN_RECEIVED;
import static org.iop.intents.constants.IntentsConstants.INTENT_BROADCAST_DATA_TYPE;
import static org.iop.intents.constants.IntentsConstants.INTENT_DATA;
import static org.iop.intents.constants.IntentsConstants.INTENT_DIALOG;
import static org.iop.intents.constants.IntentsConstants.INTENT_NOTIFICATION;
import static org.iop.intents.constants.IntentsConstants.RESTORE_SUCCED_DIALOG;

/**
 * Created by mati on 07/11/16.
 */

public abstract class BaseActivity extends AppCompatActivity {

    private static final String TAG = "BaseActivity";


    protected NotificationManager notificationManager;
    protected LocalBroadcastManager localBroadcastManager;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ViewGroup container;
    protected Toolbar toolbar;

    private NavViewHelper navViewHelper;

    private ViewGroup container_balance;
    private TextView txt_available_balance;
    private TextView txt_lock_balance;
    private ImageView img_unspendable_tx;
    private TextView txt_drawer_name;
    private ImageView imgQr;
    private ImageView img_photo;

    private NotificationReceiver notificationReceiver = new NotificationReceiver();

    protected AppController application;
    protected WalletModule module;

    protected ExecutorService executor;

    @Override
    public final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {

            notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            localBroadcastManager = LocalBroadcastManager.getInstance(this);

            application = ((AppController)getApplication());
            module = application.getModule();

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
        if (module.hasAdminNotification()){

        }
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
                startActivity(new Intent(BaseActivity.this, application.getProfileActivity()));
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
        container_balance = (ViewGroup) headerView.findViewById(R.id.container_balance);
        txt_available_balance = (TextView) headerView.findViewById(R.id.txt_available_balance);
        //txt_lock_balance = (TextView) headerView.findViewById(R.id.txt_lock_balance);
        img_unspendable_tx = (ImageView) headerView.findViewById(R.id.img_unspendable_tx);
        txt_drawer_name = (TextView) headerView.findViewById(R.id.txt_drawer_name);
        img_photo = (ImageView) headerView.findViewById(R.id.img_photo);

        txt_drawer_name.setText(module.getForumProfile().getUsername());

        imgQr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showQrDialog(BaseActivity.this,module);
            }
        });

        container_balance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(v.getContext(), BalanceActivity.class));
            }
        });

        executor.submit(new Runnable() {
            @Override
            public void run() {

                Context.propagate(WalletConstants.CONTEXT);
                Bitmap qrBitmap = null;//Cache.getQrLittleBitmapCache();
                try {

                    // qr
                    if (qrBitmap==null){
                        qrBitmap = encodeAsBitmap(module.getReceiveAddress(),imgQr.getWidth(),imgQr.getHeight(),WHITE,Color.parseColor("#1A1A1A"));
                        //Cache.setQrLittleBitmapCache(qrBitmap);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                final Bitmap finalQrBitmap = qrBitmap;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        imgQr.setImageBitmap(finalQrBitmap);
                    }
                });
            }
        });

    }

    protected void updateBalances(){
        try {
            if (hasDrawer()) {
                // voy a tener un problema mostrando los satoshis acá.., no se ven si el monto es muy bajo..
                BtcFormat btcFormat = BtcFormat.getInstance(COIN_SCALE, Locale.GERMANY);
                long spendableValuelong = module.getAvailableBalance();
                String spendableValue = (spendableValuelong>0)?btcFormat.format(spendableValuelong,2,1)+" IoPs":"0";
                txt_available_balance.setText(spendableValue);
                if(module.hasTxUnspendable()){
                    img_unspendable_tx.setVisibility(View.VISIBLE);
                }else {
                    img_unspendable_tx.setVisibility(View.INVISIBLE);
                }
                //txt_lock_balance.setText(module.getLockedBalanceStr() + " IoPs");
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
    public void onBackPressed() {
        super.onBackPressed();
//        if (!backButtonTouched)
//            backButtonTouched = true;
//        else
//            finishAffinity();
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

    protected void cleanUserData(){
        img_photo.setImageResource(0);
    }

    public void cleanData() {
        cleanUserData();
    }

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

                    if (dialogType == ADMIN_NOTIFICATION_DIALOG){

                        SimpleDialogs.buildSimpleTwoBtnsDialogForContributors(BaseActivity.this, "Notification", "App need an update from the play store", new SimpleTwoButtonsDialog.SimpleTwoBtnsDialogListener() {
                            @Override
                            public void onRightBtnClicked(SimpleTwoButtonsDialog dialog) {
                                Intent intent = new Intent(Intent.ACTION_VIEW , Uri.parse("market://search?q="+getPackageName()));
                                startActivity(intent);
                            }

                            @Override
                            public void onLeftBtnClicked(SimpleTwoButtonsDialog dialog) {
                                dialog.dismiss();
                            }
                        });

                    }else
                    if (dialogType == RESTORE_SUCCED_DIALOG){
                        DialogIntentsBuilder.buildSuccedRestoreDialog(BaseActivity.this,module.getWalletManager(),intent)
                                .show();

                    } else {
                        onBroadcastReceive(bundle);
                    }
                }else
                    if (type.equals(INTENT_DATA)){
                        Log.e(TAG,"llegó algo al intent_data inesperado!");
                        if(bundle.containsKey(INTENT_BROADCAST_DATA_BLOCKCHAIN_STATE)){
                            updateBalances();
                        }
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
                            }

                            if (!onBroadcastReceive(bundle)) {
                                // nothing yet.
                            }
                        }else {

                            android.support.v4.app.NotificationCompat.Builder mBuilder =
                                    new NotificationCompat.Builder(getApplicationContext())
                                            .setSmallIcon(R.drawable.ic__launcher)
                                            .setContentTitle("Algo raro pasó.., chequear..")
                                            .setContentText(intent.getStringExtra("title"));

                            notificationManager.notify(0, mBuilder.build());

                            Log.e(TAG,"Broadcast error, something bad arrived: "+bundle);
                        }
                    }
            }
        }
    }


}
