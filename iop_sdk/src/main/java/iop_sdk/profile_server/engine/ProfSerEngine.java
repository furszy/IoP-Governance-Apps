package iop_sdk.profile_server.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import iop_sdk.profile_server.client.ProfileServer;

import static iop_sdk.profile_server.engine.ProfSerConnectionState.NO_SERVER;

/**
 * Created by mati on 05/02/17.
 *
 *
 * Esta clase va a ser el engine de conexión con el profile server, abstrayendo a los usuarios de su conexión.
 *
 */

public class ProfSerEngine {

    private Logger LOG = LoggerFactory.getLogger(ProfSerEngine.class);
    /** Host */
    private String host;
    /** Connection state */
    private ProfSerConnectionState profSerConnectionState;
    /**  Profile server */
    private ProfileServer profileServer;
    /** Server configuration data */
    private ProfServerConfData profServerConfData;

    private ExecutorService executor;

    public ProfSerEngine(String host) {
        this.host = host;
        this.profSerConnectionState=NO_SERVER;
    }

    public void start(){

        executor = Executors.newFixedThreadPool(3);



    }

    public void stop(){

        executor.shutdown();

    }

    private void engine(){

        try {

            if (profSerConnectionState == NO_SERVER) {
                // get the availables roles..
                requestRoleList();

            }





        } catch (InvalidState e) {
            e.printStackTrace();
        }

    }

    /**
     * Request roles list to the server
     */
    private void requestRoleList() throws InvalidState {
        LOG.info("requestRoleList for host: "+host);
        if (profSerConnectionState == NO_SERVER) throw new InvalidState(profSerConnectionState.toString(),NO_SERVER.toString());
            profSerConnectionState = ProfSerConnectionState.GETTING_ROLE_LIST;
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        profileServer.listRolesRequest();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

    }

}
