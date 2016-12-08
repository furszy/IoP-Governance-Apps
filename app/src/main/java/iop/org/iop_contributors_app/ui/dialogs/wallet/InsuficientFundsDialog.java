package iop.org.iop_contributors_app.ui.dialogs.wallet;

import android.app.DialogFragment;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.WriterException;

import iop.org.iop_contributors_app.R;
import iop.org.iop_contributors_app.utils.Cache;
import iop.org.iop_contributors_app.wallet.WalletModule;

import static android.graphics.Color.WHITE;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static iop.org.iop_contributors_app.utils.mine.QrUtils.encodeAsBitmap;
import static iop.org.iop_contributors_app.utils.mine.SizeUtils.convertDpToPx;

/**
 * Created by mati on 08/12/16.
 */

public class InsuficientFundsDialog extends DialogFragment {

    private WalletModule module;

    private View root;
    private ImageView img_qr;

    public static final InsuficientFundsDialog newInstance(WalletModule module) {
        InsuficientFundsDialog dialog = new InsuficientFundsDialog();
        dialog.setModule(module);
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        int width = getResources().getDimensionPixelSize(R.dimen.popup_width);
//        int height = getResources().getDimensionPixelSize(R.dimen.popup_height);
        getDialog().getWindow().setLayout(width, WRAP_CONTENT);

        root = inflater.inflate(R.layout.insuficient_funds_dialog,null);
        img_qr = (ImageView) root.findViewById(R.id.img_qr);

        // set the custom dialog components - text, image and button
        TextView text = (TextView) root.findViewById(R.id.txt_share);
        text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        ImageView image = (ImageView) root.findViewById(R.id.img_qr);

        String address = Cache.getCacheAddress();
        if (address==null) {
            address = module.getNewAddress();
            Cache.setCacheAddress(address);
        }
        // qr
        Bitmap qrBitmap = Cache.getQrBigBitmapCache();
        try {
            if (qrBitmap == null) {
                Resources r = getResources();
                int px = convertDpToPx(getResources(), 175);
                qrBitmap = encodeAsBitmap(address, px, px, Color.parseColor("#1A1A1A"), WHITE);
                Cache.setQrBigBitmapCache(qrBitmap);
            }
        } catch (WriterException e) {
            e.printStackTrace();
        }
        image.setImageBitmap(qrBitmap);


        return root;
    }

    public void setModule(WalletModule module) {
        this.module = module;
    }
}
