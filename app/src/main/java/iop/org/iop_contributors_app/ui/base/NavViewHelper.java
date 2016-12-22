package iop.org.iop_contributors_app.ui.base;

import android.content.Context;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;
import java.util.List;

import iop.org.iop_contributors_app.furszy_sdk.android.adapter.FermatListItemListeners;
import iop.org.iop_contributors_app.furszy_sdk.android.nav_view.NavData;
import iop.org.iop_contributors_app.furszy_sdk.android.nav_view.NavMenuItem;
import iop.org.iop_contributors_app.furszy_sdk.android.nav_view.NavViewAdapter;

/**
 * Created by mati on 22/12/16.
 */

public class NavViewHelper {

    private static final String TAG = "NavViewHelper";

    private WeakReference<BaseActivity> context;

    private WeakReference<DrawerLayout> drawerLayout;
    private WeakReference<NavigationView> navigationView;

    private WeakReference<View> headerView;
    private WeakReference<RecyclerView> navViewRecyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private NavViewAdapter navViewAdapter;
    private List<NavMenuItem> itemsList;

    private FermatListItemListeners<NavMenuItem> navMenuListener;

    public NavViewHelper(BaseActivity context,DrawerLayout drawerLayout,NavigationView navigationView,RecyclerView navViewRecyclerView) {
        this.navViewRecyclerView = new WeakReference<RecyclerView>(navViewRecyclerView);
        this.context = new WeakReference<BaseActivity>(context);
        this.drawerLayout = new WeakReference<DrawerLayout>(drawerLayout);
        this.navigationView = new WeakReference<NavigationView>(navigationView);
    }

    public void setItemsList(List<NavMenuItem> itemsList) {
        this.itemsList = itemsList;
    }

    public void setHeaderView(View headerView){
        this.headerView = new WeakReference<View>(headerView);
    }

    public void init() {

        RecyclerView navViewRecyclerView = this.navViewRecyclerView.get();

        navViewRecyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(context.get(),LinearLayoutManager.VERTICAL,false);
        navViewRecyclerView.setLayoutManager(layoutManager);


        navViewAdapter = new NavViewAdapter(context.get(),itemsList);
        navViewAdapter.setFermatListEventListener(new FermatListItemListeners<NavMenuItem>() {
            @Override
            public void onItemClickListener(NavMenuItem data, int position) {
                int id = data.getId();

                // position selected
                saveNavSelection(position);

                if (navMenuListener!=null)
                    navMenuListener.onItemClickListener(data,position);
                else
                    Log.d(TAG,"Te estas olvidando de setear el navMenuListener..");

                drawerLayout.get().closeDrawers();
            }

            @Override
            public void onLongItemClickListener(NavMenuItem data, int position) {

            }
        });

        navViewRecyclerView.setAdapter(navViewAdapter);
    }

    private void saveNavSelection(int position) {
        NavData.navSelection = position;
    }

    public void setNavMenuListener(FermatListItemListeners<NavMenuItem> listener){
        navMenuListener = listener;
    }

    public void setHeaderViewBackground(int resource){
        headerView.get().setBackgroundResource(resource);
    }

    public void setNavViewBackgroundColor(int navViewBackgroundColor) {
        this.navigationView.get().setBackgroundColor(navViewBackgroundColor);
    }

    public void onDestroy() {
        this.navigationView.clear();
        this.headerView.clear();
        this.drawerLayout.clear();
        this.navViewRecyclerView.clear();
    }
}
