package iop_sdk.forum;


import iop_sdk.global.exceptions.ConnectionRefusedException;
import iop_sdk.global.exceptions.NotValidParametersException;
import iop_sdk.governance.propose.Proposal;

/**
 * Created by mati on 28/11/16.
 */
public interface ForumClient {



    ForumProfile getForumProfile();

    boolean isRegistered();

    boolean registerUser(String username, String password, String email) throws InvalidUserParametersException;

    boolean connect(String username, String password) throws InvalidUserParametersException, ConnectionRefusedException;

    int createTopic(String title, String category, String raw) throws CantCreateTopicException;

    boolean updatePost(String title, int forumId, String category, String toForumBody) throws CantUpdatePostException;

    Proposal getProposal(int forumId);

    Proposal getProposalFromWrapper(int forumId);

    void getAndCheckValid(Proposal proposal) throws NotValidParametersException;

    void clean();
}
