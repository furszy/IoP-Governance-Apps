package iop.org.iop_contributors_app.core.iop_sdk.governance.vote;

import java.io.Serializable;
import java.util.Arrays;

import iop.org.iop_contributors_app.core.iop_sdk.crypto.CryptoBytes;

/**
 * Created by mati on 21/12/16.
 */

public class Vote implements Serializable {

    public enum VoteType{
        NO,
        NEUTRAL,
        YES
    }

    /** Application id */
    private long voteId;
    /** contract wich the vote is pointing hex */
    private String genesisHashHex;
    /** Vote -> yes/no */
    private VoteType vote;
    /** freeze outputs -> is the amount of votes that the user is giving as yes or no */
    private long votingPower;
    /** locked values */
    private String lockedOutputHashHex;
    private int lockedOutputIndex;

    public Vote(String genesisHash, VoteType vote, long votingPower) {
        this.genesisHashHex = genesisHash;
        this.vote = vote;
        this.votingPower = votingPower;
    }

    public Vote(long voteId,String genesisHashHex, VoteType vote, long votingPower, String lockedOutputHashHex, int lockedOutputIndex) {
        this.voteId = voteId;
        this.genesisHashHex = genesisHashHex;
        this.vote = vote;
        this.votingPower = votingPower;
        this.lockedOutputHashHex = lockedOutputHashHex;
        this.lockedOutputIndex = lockedOutputIndex;
    }

    /**
     *
     * @return
     */
    public boolean isYesVote() {
        return vote == VoteType.YES;
    }

    public byte[] getGenesisHash() {
        return CryptoBytes.fromHexToBytes(genesisHashHex);
    }


    public String getGenesisHashHex() {
        return genesisHashHex;
    }

    public void setVoteId(long voteId) {
        this.voteId = voteId;
    }

    public VoteType getVote() {
        return vote;
    }

    public long getVotingPower() {
        return votingPower;
    }

    @Override
    public String toString() {
        return "Vote{" +
                "genesisHash=" + genesisHashHex +
                ", vote=" + vote +
                ", votingPower=" + votingPower +
                '}';
    }


    public void setLockedOutputHashHex(String lockedOutputHashHex) {
        this.lockedOutputHashHex = lockedOutputHashHex;
    }

    public void setLockedOutputIndex(int lockedOutputIndex) {
        this.lockedOutputIndex = lockedOutputIndex;
    }

    public String getLockedOutputHex() {
        return lockedOutputHashHex;
    }

    public int getLockedOutputIndex() {
        return lockedOutputIndex;
    }

    public long getVoteId() {
        return voteId;
    }
}
