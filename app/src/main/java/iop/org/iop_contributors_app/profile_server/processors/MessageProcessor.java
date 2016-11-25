package iop.org.iop_contributors_app.profile_server.processors;

/**
 * Created by mati on 09/11/16.
 */

public interface MessageProcessor<M> {

    void execute(M message);

}
