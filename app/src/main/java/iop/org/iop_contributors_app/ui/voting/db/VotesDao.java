package iop.org.iop_contributors_app.ui.voting.db;

import android.content.Context;

import iop.org.iop_contributors_app.ApplicationController;
import iop.org.iop_contributors_app.core.iop_sdk.governance.vote.Vote;

/**
 * Created by mati on 21/12/16.
 */

public class VotesDao {


    public VotesDao(Context context) {

    }

    /**
     * Lockeo outputs
     * @param parentHash
     * @param index
     * @return
     */
    public boolean isLockedOutput(String parentHash, long index) {
        return false;
    }

    /**
     * Acá chequeo si el voto existe -> id == genesis transaction hash, si existe chequeo el tipo de voto (yes,no,neutral) si es igual devuelvo true, si existe pero tiene otro voto lanzo una excepcion o veo que carajo hago..
     * @param vote
     * @return
     */
    public boolean exist(Vote vote) {
        return false;
    }

    public boolean lockOutput(String lockedOutputHex, int lockedOutputIndex) {
        return false;
    }
}
