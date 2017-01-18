package iop.org.iop_contributors_app.ui;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.WriterException;
import com.squareup.picasso.Picasso;

import java.io.File;

import de.hdodenhof.circleimageview.CircleImageView;
import iop.org.iop_contributors_app.R;
import iop.org.iop_contributors_app.ui.base.BaseActivity;

import static android.graphics.Color.WHITE;
import static iop.org.furszy_lib.utils.QrUtils.encodeAsBitmap;
import static iop.org.furszy_lib.utils.SizeUtils.convertDpToPx;

/**
 * Created by mati on 17/01/17.
 */

public class BalanceActivity extends BaseActivity {

    private View root;

    private View container_unnavailable;

    private TextView txt_available;
    private TextView txt_unnavailable;
    private TextView txt_locked;
    private ImageView img_qr;
    private CircleImageView img_photo;

    @Override
    protected void onCreateView(ViewGroup container, Bundle savedInstance) {

        root = getLayoutInflater().inflate(R.layout.balance_activity_main,container);

        txt_available = (TextView) root.findViewById(R.id.txt_available);
        txt_unnavailable = (TextView) root.findViewById(R.id.txt_unnavailable);
        txt_locked = (TextView) findViewById(R.id.txt_locked);
        img_qr = (ImageView) findViewById(R.id.img_qr);
        img_photo = (CircleImageView) findViewById(R.id.img_photo);

        container_unnavailable = findViewById(R.id.container_unnavailable);

        try {
            loadBalance();
            loadQr();
            loadProfile();

        } catch (WriterException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }

        container_unnavailable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(v.getContext(),TransactionsActivity.class));
            }
        });

        super.onCreateView(container, savedInstance);
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
        txt_available.setText(module.getAvailableBalanceStr());
        txt_unnavailable.setText(module.getUnnavailableBalanceStr());
        txt_locked.setText(module.getLockedBalance());
    }

    @Override
    protected boolean onBroadcastReceive(Bundle data) {
        return false;
    }

    @Override
    protected boolean hasDrawer() {
        return false;
    }
}
