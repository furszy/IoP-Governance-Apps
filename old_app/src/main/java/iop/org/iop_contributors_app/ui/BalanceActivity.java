package iop.org.iop_contributors_app.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.WriterException;
import com.squareup.picasso.Picasso;

import org.bitcoinj.utils.BtcFormat;

import java.io.File;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import iop.org.furszy_lib.TooltipWindow;
import iop.org.iop_contributors_app.R;
import iop.org.iop_contributors_app.ui.base.BaseActivity;

import static iop.org.furszy_lib.utils.QrUtils.encodeAsBitmap;
import static iop.org.furszy_lib.utils.SizeUtils.convertDpToPx;
import static iop.org.iop_contributors_app.ui.dialogs.SimpleDialogs.showQrDialog;
import static org.bitcoinj.utils.BtcFormat.COIN_SCALE;

/**
 * Created by mati on 17/01/17.
 */

public class BalanceActivity extends BaseActivity {

    private static final int HELP_ID = 1;
    private View root;

    private View container_unnavailable;

    private TextView txt_available;
    private TextView txt_unnavailable;
    private TextView txt_locked;
    private ImageView img_qr;
    private CircleImageView img_photo;

    private TooltipWindow tipWindow;

    @Override
    protected void onCreateView(ViewGroup container, Bundle savedInstance) {

        setTitle("Balance");

        root = getLayoutInflater().inflate(R.layout.balance_activity_main,container);

        txt_available = (TextView) root.findViewById(R.id.txt_available);
        txt_unnavailable = (TextView) root.findViewById(R.id.txt_unnavailable);
        txt_locked = (TextView) findViewById(R.id.txt_locked);
        img_qr = (ImageView) findViewById(R.id.img_qr);
        img_photo = (CircleImageView) findViewById(R.id.img_photo);

        container_unnavailable = findViewById(R.id.container_unnavailable);

        container_unnavailable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(v.getContext(),TransactionsActivity.class));
            }
        });

        img_qr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showQrDialog(BalanceActivity.this,module);
            }
        });

        tipWindow = TooltipWindow.newInstance(this);
        tipWindow.setText("Don't be afraid!\nIt's the balance that you have but you can not spend until it's confirmed by the network, it should happen when you receive/send a transaction");

        super.onCreateView(container, savedInstance);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuItem menuItem = menu.add(0,HELP_ID,0,"Help");
        menuItem.setIcon(R.drawable.help_icon);
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == HELP_ID){
            if (!tipWindow.isTooltipShown())
                tipWindow.showToolTip(container_unnavailable);
            return true;

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            loadBalance();
            loadQr();
            loadProfile();

        } catch (WriterException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void loadProfile() {
        try {
            File imgFile = module.getUserImageFile();
            if (imgFile.exists())
                Picasso.with(this).load(imgFile).into(img_photo);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void loadQr() throws WriterException {
        final String address = module.getReceiveAddress();
        // qr
        int px = convertDpToPx(getResources(),225);
        Bitmap qrBitmap = encodeAsBitmap(address, px, px, Color.parseColor("#CCCCCC"), Color.TRANSPARENT );
        img_qr.setImageBitmap(qrBitmap);
    }

    private void loadBalance() {
        BtcFormat btcFormat = BtcFormat.getInstance(COIN_SCALE,Locale.GERMANY);
        long spendableValuelong = module.getAvailableBalance();
        String spendableValue = (spendableValuelong>0)?btcFormat.format(spendableValuelong,4,1)+" IoPs":"0";
        txt_available.setText(spendableValue);
        long unnavailable = module.getUnnavailableBalance();
        String unnavailableBalance = (unnavailable>0)? btcFormat.format(unnavailable,4,1)+" IoPs":"0";
        txt_unnavailable.setText(unnavailableBalance);
        long locked = module.getLockedBalance();
        String lockedBalance = (locked>0)?  (btcFormat.format(locked,0,1)+" IoPs"):"0";
        txt_locked.setText(lockedBalance);
    }

    @Override
    protected boolean onBroadcastReceive(Bundle data) {
        return false;
    }

    @Override
    protected boolean hasDrawer() {
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tipWindow != null && tipWindow.isTooltipShown())
            tipWindow.dismissTooltip();
    }
}
