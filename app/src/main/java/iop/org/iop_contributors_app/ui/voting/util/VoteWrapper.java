package iop.org.iop_contributors_app.ui.voting.util;

import java.io.Serializable;

import iop.org.iop_contributors_app.core.iop_sdk.governance.propose.Proposal;
import iop.org.iop_contributors_app.core.iop_sdk.governance.vote.Vote;

/**
 * Created by mati on 23/12/16.
 */

public class VoteWrapper implements Serializable {

    private Vote vote;
    private Proposal proposal;

    public VoteWrapper(Vote vote, Proposal proposal) {
        this.vote = vote;
        this.proposal = proposal;
    }

    public Vote getVote() {
        return vote;
    }

    public Proposal getProposal() {
        return proposal;
    }
}
