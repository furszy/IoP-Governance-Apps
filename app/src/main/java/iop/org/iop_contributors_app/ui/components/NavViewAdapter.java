package iop.org.iop_contributors_app.ui.components;

import android.content.Context;
import android.graphics.Color;
import android.view.View;

import com.squareup.picasso.Picasso;

import java.util.List;

import iop.org.furszy_lib.nav_view.NavData;
import iop.org.furszy_lib.nav_view.NavMenuItem;
import iop.org.iop_contributors_app.R;
import iop.org.furszy_lib.adapter.FermatAdapterImproved;

/**
 * Created by mati on 18/11/16.
 */
public class NavViewAdapter extends FermatAdapterImproved<NavMenuItem,NavMenuHolder> {


    public NavViewAdapter(Context context, List<NavMenuItem> strings) {
        super(context,strings);
    }

    @Override
    protected NavMenuHolder createHolder(View itemView, int type) {
        return new NavMenuHolder(itemView,type);
    }

    @Override
    protected int getCardViewResource(int type) {
        return R.layout.nav_view_item_holder;
    }

    @Override
    protected void bindHolder(NavMenuHolder holder, NavMenuItem data, int position) {

        holder.txt_title.setText(data.getText());
        if (position== NavData.navSelection){
            holder.itemView.setBackgroundColor(Color.parseColor("#66000000"));
        }

        Picasso.with(context).load(data.getIcResOff()).into(holder.icon);

    }
}
