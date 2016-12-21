package iop.org.iop_contributors_app.core.iop_sdk.governance.vote;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Created by mati on 21/12/16.
 */

public class Vote implements Serializable {



    public enum VoteType{
        NO,
        NEUTRAL,
        YES
    }

    /** contract wich the vote is pointing */
    private byte[] genesisHash;
    /** Vote -> yes/no */
    private VoteType vote;
    /** freeze outputs -> is the amount of votes that the user is giving as yes or no */
    private long votingPower;
    /** locked values */
    private String lockedOutputHashHex;
    private int lockedOutputIndex;

    public Vote(byte[] genesisHash, VoteType vote, long votingPower) {
        this.genesisHash = genesisHash;
        this.vote = vote;
        this.votingPower = votingPower;
    }

    /**
     *
     * @return
     */
    public boolean isYesVote() {
        return vote == VoteType.YES;
    }

    public byte[] getGenesisHash() {
        return genesisHash;
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
                "genesisHash=" + Arrays.toString(genesisHash) +
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
}
