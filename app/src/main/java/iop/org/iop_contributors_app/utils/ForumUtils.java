package iop.org.iop_contributors_app.utils;

import android.content.Context;
import android.content.Intent;

import iop.org.iop_contributors_app.core.iop_sdk.governance.propose.Proposal;
import iop.org.iop_contributors_app.ui.ForumActivity;
import iop.org.iop_contributors_app.wallet.WalletModule;

/**
 * Created by mati on 21/12/16.
 */

public class ForumUtils {

    public static void goToFoum(Context context, WalletModule module, Proposal data){
        Intent intent1 = new Intent(context,ForumActivity.class);
        String url = module.getForumUrl()+"/t/"+data.getTitle().replace(" ","-")+"/"+data.getForumId();
        intent1.putExtra(ForumActivity.INTENT_URL,url);
        context.startActivity(intent1);
    }

}
