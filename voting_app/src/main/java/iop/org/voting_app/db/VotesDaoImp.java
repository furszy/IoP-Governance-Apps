package iop.org.voting_app.db;

import android.content.Context;

import java.util.List;

import iop_sdk.governance.vote.Vote;
import iop_sdk.governance.vote.VotesDao;


/**
 * Created by mati on 21/12/16.
 */

public class VotesDaoImp implements VotesDao{

    private VotesDatabaseHandler handler;


    public VotesDaoImp(Context context) {
        handler = new VotesDatabaseHandler(context);
    }

    /**
     * Lockeo outputs
     * // todo: esto está hecho de una forma que solo deje pasar los que no existen, cuando tenga ganas tengo que hacerlo bien..
     * @param parentVoteTransactionHash
     * @param index
     * @return
     */
    public boolean isLockedOutput(String parentVoteTransactionHash, long index) {
        return handler.isLockedOutput(parentVoteTransactionHash,index);
    }

    /**
     * Acá chequeo si el voto existe -> id == genesis transaction hash, si existe chequeo el tipo de voto (yes,no,neutral) si es igual devuelvo true, si existe pero tiene otro voto lanzo una excepcion o veo que carajo hago..
     * //todo: lazy lazy implementation
     *  @param vote
     * @return
     */
    public boolean exist(Vote vote) {
        return handler.exist(vote.getGenesisHashHex());
    }

    public boolean lockOutput(long voteId,String lockedOutputHex, int lockedOutputIndex) {
        return handler.updateVote(voteId,lockedOutputHex,lockedOutputIndex);
    }

    /**
     *
     * @param vote
     * @return  vote ID
     */
    public long addVote(Vote vote){
        long voteId = handler.addVote(vote);
        vote.setVoteId(voteId);
        return voteId;
    }

    /**
¿     *
¿     * @return
     */
    public List<Vote> listVotes() {
        return handler.getAllVotes();
    }

    @Override
    public void removeIfExist(Vote vote) {
        handler.delete(vote);
    }

    @Override
    public void clean() {
        handler.deleteDb();
    }
}
