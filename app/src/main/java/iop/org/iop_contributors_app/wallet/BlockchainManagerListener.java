package iop.org.iop_contributors_app.wallet;

import org.bitcoinj.core.PeerGroup;

/**
 * Created by mati on 19/12/16.
 */
public interface BlockchainManagerListener {

    void peerGroupInitialized(PeerGroup peerGroup);

}
