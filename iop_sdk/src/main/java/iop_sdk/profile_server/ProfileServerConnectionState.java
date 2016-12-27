package iop_sdk.profile_server;

/**
 * Created by mati on 09/11/16.
 */
public enum  ProfileServerConnectionState {


    NO_SERVER,
    GETTING_ROLE_LIST,
    HAS_ROLE_LIST,
    WAITING_START_NON_CL,
    START_CONVERSATION_NON_CL,
    WAITING_HOME_NODE_REQUEST,
    HOME_NODE_REQUEST,
    WAITING_START_CL,
    START_CONVERSATION_CL,
    WAITING_CHECK_IN,
    CHECK_IN


}