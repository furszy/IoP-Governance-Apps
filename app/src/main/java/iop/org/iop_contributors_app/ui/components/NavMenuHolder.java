package iop.org.iop_contributors_app.ui.components;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import iop.org.iop_contributors_app.R;
import iop.org.furszy_lib.adapter.FermatViewHolder;

/**
 * Created by mati on 18/11/16.
 */
public class NavMenuHolder extends FermatViewHolder {

    public ImageView icon;
    public TextView txt_title;

    protected NavMenuHolder(View itemView, int holderType) {
        super(itemView, holderType);
        icon = (ImageView) itemView.findViewById(R.id.img_icon);
        txt_title = (TextView) itemView.findViewById(R.id.txt_title);
    }
}
