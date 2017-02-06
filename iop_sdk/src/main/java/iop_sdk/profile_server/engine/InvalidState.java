package iop_sdk.profile_server.engine;

import static iop_sdk.profile_server.engine.ProfSerConnectionState.NO_SERVER;

/**
 * Created by mati on 05/02/17.
 */
public class InvalidState extends Exception {

    public InvalidState(String s) {
        super(s);
    }

    public InvalidState(String state, String validState) {
        super("State: "+state+" must be "+NO_SERVER.toString());
    }
}
