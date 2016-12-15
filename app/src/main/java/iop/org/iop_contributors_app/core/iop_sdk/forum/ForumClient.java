package iop.org.iop_contributors_app.core.iop_sdk.forum;

import iop.org.iop_contributors_app.ConnectionRefusedException;
import iop.org.iop_contributors_app.core.iop_sdk.governance.Proposal;
import iop.org.iop_contributors_app.utils.exceptions.NotValidParametersException;
import iop.org.iop_contributors_app.wallet.db.CantUpdateProposalException;

/**
 * Created by mati on 28/11/16.
 */
public interface ForumClient {



    ForumProfile getForumProfile();

    boolean isRegistered();

    boolean registerUser(String username, String password, String email) throws InvalidUserParametersException;

    boolean connect(String username, String password) throws InvalidUserParametersException, ConnectionRefusedException;

    int createTopic(String title,String category,String raw) throws CantCreateTopicException;

    boolean updatePost(String title, int forumId, String category, String toForumBody) throws CantUpdateProposalException;

    Proposal getProposal(int forumId);

    void getAndCheckValid(Proposal proposal) throws NotValidParametersException;
}
