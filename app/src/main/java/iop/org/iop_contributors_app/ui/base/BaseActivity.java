package iop.org.iop_contributors_app.ui.base;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import iop.org.iop_contributors_app.ApplicationController;
import iop.org.iop_contributors_app.services.BlockchainServiceImpl;
import iop.org.iop_contributors_app.R;
import iop.org.iop_contributors_app.ui.CreateProposalActivity;
import iop.org.iop_contributors_app.ui.ForumActivity;
import iop.org.iop_contributors_app.ui.MainActivity;
import iop.org.iop_contributors_app.ui.ProfileActivity;
import iop.org.iop_contributors_app.ui.ProposalsActivity;
import iop.org.iop_contributors_app.ui.SettingsActivity;
import iop.org.iop_contributors_app.ui.components.sdk.FermatListItemListeners;
import iop.org.iop_contributors_app.ui.dialogs.BackupDialog;
import iop.org.iop_contributors_app.utils.Cache;
import iop.org.iop_contributors_app.wallet.WalletModule;

import static android.graphics.Color.WHITE;

/**
 * Created by mati on 07/11/16.
 */

public class BaseActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ViewGroup container;
    protected Toolbar toolbar;

    private RecyclerView navViewRecyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private NavViewAdapter navViewAdapter;

    private TextView txt_available_balance;
    private TextView txt_lock_balance;
    private ImageView imgQr;

    protected WalletModule module;

    protected ExecutorService executor;

    protected boolean isStarted = true;

    @Override
    public final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        module = ApplicationController.getInstance().getWalletModule();

        executor = Executors.newFixedThreadPool(3);

        beforeCreate(savedInstanceState);

        if (isStarted) {
            setContentView(R.layout.base_main);
            initToolbar();
            setupDrawerLayout();
            container = (ViewGroup) findViewById(R.id.container);
            onCreateView(container,savedInstanceState);
        }else {
            setContentView(R.layout.start_main);
            findViewById(R.id.btn_sigin).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(BaseActivity.this,ProfileActivity.class));
                }
            });
            findViewById(R.id.btn_login).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(BaseActivity.this,ProfileActivity.class);
                    intent.putExtra(ProfileActivity.INTENT_LOGIN,true);
                    startActivity(intent);
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        if (hasOptionMenu()) {

            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.menu, menu);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_backup:
                BackupDialog backupDialog = BackupDialog.factory(this);
                backupDialog.show(getFragmentManager(),"backup_dialog");
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    private void initToolbar() {
        toolbar  = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_home_black_24dp);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationIcon(R.drawable.menu_hamburguesa);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

    }

    private void setupDrawerLayout() {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        navigationView = (NavigationView) findViewById(R.id.navigation_view);

        navigationView.getHeaderView(0).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(BaseActivity.this, ProfileActivity.class));
            }
        });


        navViewRecyclerView = (RecyclerView) navigationView.findViewById(R.id.recycler_nav_view);

        navViewRecyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        navViewRecyclerView.setLayoutManager(layoutManager);


        navViewAdapter = new NavViewAdapter(this,loadNavMenuItems());
        navViewAdapter.setFermatListEventListener(new FermatListItemListeners<NavMenuItem>() {
            @Override
            public void onItemClickListener(NavMenuItem data, int position) {
                int id = data.getId();

                Intent intent = null;

                switch (id){
                    case MENU_DRAWER_HOME:
                        intent = new Intent(BaseActivity.this, MainActivity.class);
                        break;
                    case MENU_DRAWER_FORUM:
                        intent = new Intent(BaseActivity.this, ForumActivity.class);
                        break;
                    case MENU_DRAWER_CREATE_PROPOSAL:
                        intent = new Intent(BaseActivity.this, CreateProposalActivity.class);
                        break;
                    case MENU_DRAWER_PROPOSALS:
                        intent = new Intent(BaseActivity.this, ProposalsActivity.class);
                        break;
                    case MENU_DRAWER_SETTINGS:
                        intent = new Intent(BaseActivity.this, SettingsActivity.class);
                        break;
                }

                startActivity(intent);

                data.setClicked(true);
                drawerLayout.closeDrawers();
            }

            @Override
            public void onLongItemClickListener(NavMenuItem data, int position) {

            }
        });

        navViewRecyclerView.setAdapter(navViewAdapter);

        imgQr = (ImageView) navigationView.findViewById(R.id.img_qr);
        txt_available_balance = (TextView) drawerLayout.findViewById(R.id.txt_available_balance);
        txt_lock_balance = (TextView) drawerLayout.findViewById(R.id.txt_lock_balance);


        imgQr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showQrDialog();
                    }
                });
            }
        });

        executor.submit(new Runnable() {
            @Override
            public void run() {

                try {

                    // qr
                    Bitmap qrBitmap = Cache.getQrLittleBitmapCache();
                    if (qrBitmap==null){
                        qrBitmap = encodeAsBitmap(module.getNewAddress(),imgQr.getWidth(),imgQr.getHeight(),WHITE,Color.parseColor("#1A1A1A"));
                        Cache.setQrLittleBitmapCache(qrBitmap);
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            imgQr.setImageBitmap(Cache.getQrLittleBitmapCache());

                        }
                    });


                } catch (WriterException e) {
                    e.printStackTrace();
                }

                // balance
                try {
                    txt_available_balance.setText(String.valueOf(module.getAvailableBalance() + " IoPs"));
                    txt_lock_balance.setText(String.valueOf(module.getLockedBalance()) + " IoPs");
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        });






    }

    private final static int MENU_DRAWER_HOME = 0;
    private final static int MENU_DRAWER_FORUM = 1;
    private final static int MENU_DRAWER_CREATE_PROPOSAL = 2;
    private final static int MENU_DRAWER_PROPOSALS = 3;
    private final static int MENU_DRAWER_SETTINGS = 4;

    private List<NavMenuItem> loadNavMenuItems() {
        List<NavMenuItem> items = new ArrayList<>();
        items.add(new NavMenuItem(MENU_DRAWER_HOME,true,"Home",R.drawable.icon_home_on));
        items.add(new NavMenuItem(MENU_DRAWER_FORUM,false,"Forum",R.drawable.icon_forum_off));
        items.add(new NavMenuItem(MENU_DRAWER_CREATE_PROPOSAL,false,"Create Proposal",R.drawable.icon_createcontributioncontract_off_drawer));
        items.add(new NavMenuItem(MENU_DRAWER_PROPOSALS,false,"Proposals",R.drawable.icon_mycontracts_off_drawer));
        items.add(new NavMenuItem(MENU_DRAWER_SETTINGS,false,"Settings",R.drawable.icon_settings_off));
        return items;
    }

    protected void sendWorkToProfileService(Bundle data){
//        Intent intent = new Intent(this, ProfileServerService.class);
//        intent.putExtras(data);
//        startService(intent);
//        ApplicationController.getInstance().getProfileServerManager().updateProfileRequest();

    }

    protected void sendWorkToBlockchainService(String action,Bundle data){
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


    private void showQrDialog(){

        try {
            final Dialog dialog = new Dialog(this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.qr_dialog);


            // set the custom dialog components - text, image and button
            TextView text = (TextView) dialog.findViewById(R.id.txt_share);
            text.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    Toast.makeText(v.getContext(), "Share", Toast.LENGTH_SHORT).show();
                }
            });
            ImageView image = (ImageView) dialog.findViewById(R.id.img_qr);

            // qr
            Bitmap qrBitmap = Cache.getQrBigBitmapCache();
            if (qrBitmap == null) {
                Resources r = getResources();
                int px = convertDpToPx(175);
                qrBitmap = encodeAsBitmap(module.getNewAddress(), px, px, Color.parseColor("#1A1A1A"), WHITE );
                Cache.setQrBigBitmapCache(qrBitmap);
            }

            image.setImageBitmap(qrBitmap);

            dialog.show();

        } catch (WriterException e) {
            e.printStackTrace();
        }catch ( Exception e){
            e.printStackTrace();
        }
    }

    private int convertDpToPx(int dp){
        return Math.round(dp*(getResources().getDisplayMetrics().xdpi/ DisplayMetrics.DENSITY_DEFAULT));

    }

    protected Bitmap encodeAsBitmap(String str,int widht,int height,int qrColor,int backgroundColor) throws WriterException {
        BitMatrix result;
        try {
           result = new MultiFormatWriter().encode(str,
                    BarcodeFormat.QR_CODE, widht, height, null);
        } catch (IllegalArgumentException iae) {
            // Unsupported format
            return null;
        }
        int w = result.getWidth();
        int h = result.getHeight();
        int[] pixels = new int[w * h];
        for (int y = 0; y < h; y++) {
            int offset = y * w;
            for (int x = 0; x < w; x++) {
                pixels[offset + x] = result.get(x, y) ? qrColor : backgroundColor;
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, w, 0, 0, w, h);
        return bitmap;
    }
}
