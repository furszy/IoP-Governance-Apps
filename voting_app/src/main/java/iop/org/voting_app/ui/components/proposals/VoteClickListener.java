package iop.org.voting_app.ui.components.proposals;

import iop_sdk.governance.propose.Proposal;
import iop_sdk.governance.vote.VoteWrapper;

/**
 * Created by mati on 25/01/17.
 */
public interface VoteClickListener {


    void goVote(Proposal proposal,int pos);

}
