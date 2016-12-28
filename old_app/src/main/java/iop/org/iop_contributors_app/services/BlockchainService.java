package iop.org.iop_contributors_app.services;

import android.support.annotation.Nullable;

import org.bitcoinj.core.Peer;
import org.bitcoinj.core.StoredBlock;

import java.util.List;

import iop_sdk.wallet.utils.BlockchainState;

/**
 * Created by mati on 11/11/16.
 */

public interface BlockchainService {

    public static final String ACTION_PEER_STATE = BlockchainService.class.getPackage().getName() + ".peer_state";
    public static final String ACTION_PEER_STATE_NUM_PEERS = "num_peers";

    public static final String ACTION_BLOCKCHAIN_STATE = BlockchainService.class.getPackage().getName() + ".blockchain_state";

    public static final String ACTION_CANCEL_COINS_RECEIVED = BlockchainService.class.getPackage().getName() + ".cancel_coins_received";
    public static final String ACTION_RESET_BLOCKCHAIN = BlockchainService.class.getPackage().getName() + ".reset_blockchain";
    public static final String ACTION_BROADCAST_TRANSACTION = BlockchainService.class.getPackage().getName() + ".broadcast_transaction";
    public static final String ACTION_BROADCAST_PROPOSAL_TRANSACTION = BlockchainService.class.getPackage().getName() + ".broadcast_proposal_transaction";
    public static final String ACTION_BROADCAST_TRANSACTION_HASH = "hash";

    public static final String INTENT_EXTRA_PROPOSAL = "proposal";

    // voting
    public static final String ACTION_BROADCAST_VOTE_PROPOSAL_TRANSACTION = BlockchainService.class.getPackage().getName() + ".broadcast_vote_proposal_transaction";
    public static final String INTENT_EXTRA_PROPOSAL_VOTE = "proposal_vote";



    BlockchainState getBlockchainState();

    @Nullable
    List<Peer> getConnectedPeers();

    List<StoredBlock> getRecentBlocks(int maxBlocks);


}
