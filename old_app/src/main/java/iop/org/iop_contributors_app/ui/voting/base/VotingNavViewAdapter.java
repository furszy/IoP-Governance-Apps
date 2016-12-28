package iop.org.iop_contributors_app.ui.voting.base;

import android.content.Context;
import android.graphics.Color;
import android.view.View;

import com.squareup.picasso.Picasso;

import java.util.List;

import iop.org.furszy_lib.adapter.FermatAdapterImproved;
import iop.org.furszy_lib.nav_view.NavData;
import iop.org.furszy_lib.nav_view.NavMenuItem;
import iop.org.iop_contributors_app.R;

/**
 * Created by mati on 18/11/16.
 */
public class VotingNavViewAdapter extends FermatAdapterImproved<NavMenuItem,NavMenuHolder> {


    public VotingNavViewAdapter(Context context, List<NavMenuItem> strings) {
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

        if (position == NavData.navSelection){
            holder.itemView.setBackgroundColor(Color.parseColor("#666666"));
            Picasso.with(context).load(data.getIcResOn()).into(holder.icon);
            holder.txt_title.setTextColor(Color.parseColor("#ffffff"));
        }else {
            Picasso.with(context).load(data.getIcResOff()).into(holder.icon);
            holder.txt_title.setTextColor(Color.parseColor("#4D4D4D"));
        }

    }
}
