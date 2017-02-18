package org.iop;

import iop_sdk.governance.propose.Proposal;

/**
 * Created by mati on 30/01/17.
 */

public class ForumHelper {

    public static String getForumUrl(WalletModule module, Proposal data){
        return module.getForumUrl()+"/t/"+data.getForumId();
    }

}
