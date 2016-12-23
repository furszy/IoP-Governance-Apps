package iop.org.iop_contributors_app.ui.voting.db;

import android.content.Context;

import java.util.List;

import iop.org.iop_contributors_app.core.iop_sdk.governance.vote.Vote;

/**
 * Created by mati on 21/12/16.
 */

public class VotesDao {

    private VotesDatabaseHandler handler;


    public VotesDao(Context context) {
        handler = new VotesDatabaseHandler(context);
    }

    /**
     * Lockeo outputs
     * // todo: esto está hecho de una forma que solo deje pasar los que no existen, cuando tenga ganas tengo que hacerlo bien..
     * @param genesisHash
     * @param index
     * @return
     */
    public boolean isLockedOutput(String genesisHash, long index) {
        return handler.getVote(genesisHash)!=null;
    }

    /**
     * Acá chequeo si el voto existe -> id == genesis transaction hash, si existe chequeo el tipo de voto (yes,no,neutral) si es igual devuelvo true, si existe pero tiene otro voto lanzo una excepcion o veo que carajo hago..
     * //todo: lazy lazy implementation
     *  @param vote
     * @return
     */
    public boolean exist(Vote vote) {
        return handler.getVote(vote.getGenesisHashHex())!=null;
    }

    public boolean lockOutput(String lockedOutputHex, int lockedOutputIndex) {
        return false;
    }

    public void addVote(Vote vote){
        handler.addVote(vote);
    }

    /**
¿     *
¿     * @return
     */
    public List<Vote> listVotes() {
        return handler.getAllVotes();
    }
}
