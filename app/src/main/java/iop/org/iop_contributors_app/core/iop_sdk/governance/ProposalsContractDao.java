package iop.org.iop_contributors_app.core.iop_sdk.governance;

/**
 * Created by mati on 05/12/16.
 */

public interface ProposalsContractDao {

    boolean isLockedOutput(String hashHex, long index);

}
