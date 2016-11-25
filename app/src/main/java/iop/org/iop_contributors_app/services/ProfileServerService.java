package iop.org.iop_contributors_app.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import iop.org.iop_contributors_app.ApplicationController;
import iop.org.iop_contributors_app.HardCodedConstans;
import iop.org.iop_contributors_app.Profile;
import iop.org.iop_contributors_app.configurations.ProfileServerConfigurations;
import iop.org.iop_contributors_app.ProfileServerConnectionState;
import iop.org.iop_contributors_app.Signer;
import iop.org.iop_contributors_app.core.iop_sdk.IoHandler;
import iop.org.iop_contributors_app.core.iop_sdk.IoSession;
import iop.org.iop_contributors_app.core.iop_sdk.crypto.CryptoBytes;
import iop.org.iop_contributors_app.core.iop_sdk.crypto.KeyEd25519;
import iop.org.iop_contributors_app.profile_server.ModuleProfileServer;
import iop.org.iop_contributors_app.profile_server.PortType;
import iop.org.iop_contributors_app.profile_server.ProfileServer;
import iop.org.iop_contributors_app.profile_server.ProfileServerImp;
import iop.org.iop_contributors_app.profile_server.processors.MessageProcessor;
import iop.org.iop_contributors_app.profile_server.protocol.IopHomeNodeProto3;
import iop.org.iop_contributors_app.wallet.WalletModule;

import static iop.org.iop_contributors_app.ProfileServerConnectionState.CHECK_IN;
import static iop.org.iop_contributors_app.ProfileServerConnectionState.HAS_ROLE_LIST;
import static iop.org.iop_contributors_app.ProfileServerConnectionState.HOME_NODE_REQUEST;
import static iop.org.iop_contributors_app.ProfileServerConnectionState.NO_SERVER;
import static iop.org.iop_contributors_app.ProfileServerConnectionState.START_CONVERSATION_CL;
import static iop.org.iop_contributors_app.ProfileServerConnectionState.START_CONVERSATION_NON_CL;
import static iop.org.iop_contributors_app.ProfileServerConnectionState.WAITING_HOME_NODE_REQUEST;
import static iop.org.iop_contributors_app.ProfileServerConnectionState.WAITING_START_CL;
import static iop.org.iop_contributors_app.ProfileServerConnectionState.WAITING_START_NON_CL;

/**
 * Created by mati on 09/11/16.
 */

public class ProfileServerService extends Service implements ModuleProfileServer {

    private static final String TAG = "ProfileServerService";

    private WalletModule module;

    private ProfileServer profileServer;
    private ProfileServerConfigurations configurationsPreferences;

    private ProfileServerConnectionState state = NO_SERVER;

    private ProfileServerHanlder profileServerHanlder;

    private ExecutorService executor;



    public class ProfServerBinder extends Binder {
        public ProfileServerService getService() {
            return ProfileServerService.this;
        }
    }

    private final IBinder mBinder = new ProfileServerService.ProfServerBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG,".onBind()");
        return mBinder;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG,"onCreate");

        try {
            // configurations
            configurationsPreferences = ApplicationController.getInstance().getProfileServerPreferences();
            module = ApplicationController.getInstance().getWalletModule();
            //init
            initProfileServer();
            executor = Executors.newFixedThreadPool(3);
            // init client data
            initClientData();
            // init profile server
//            profileServer = ApplicationController.getInstance().getProfileServerManager();
            profileServerHanlder = new ProfileServerHanlder();
            profileServer.addHandler(profileServerHanlder);

            // Engine to do what ever is necessary to be checked in in the server
            engineServerConnect();

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Initialize the profile server
     * @throws Exception
     */
    private void initProfileServer() throws Exception {

        String host = configurationsPreferences.getHost();
        if (host==null){
            host = HardCodedConstans.HOST;
            configurationsPreferences.setHost(host);
        }
        // primary port
//        ProfileServerConfigurations profileServerConfigurations = new ProfileServerConfigurations(host,primaryPort);
//        profileServerConfigurations.setClPort(clPort);
//        profileServerConfigurations.setNonClPort(nonClPort);

        profileServer = new ProfileServerImp(this,configurationsPreferences);
    }

    private void initClientData() {
        //todo: esto lo tengo que hacer cuando guarde la privkey encriptada.., por ahora lo dejo asI. Este es el profile que va a crear el usuario, está acá de ejemplo.

        if (configurationsPreferences.isRegisteredInServer()) {

            // load profile

//            byte[] publicKey = configurationsPreferences.getUserPubKey();
//            byte[] privKey = configurationsPreferences.getUserPrivKey();

            KeyEd25519 keyEd25519 = configurationsPreferences.getUserKeys();

            Profile profile = new Profile(
                    configurationsPreferences.getProtocolVersion(),
                    configurationsPreferences.getUsername(),
                    keyEd25519
            );

            module.setProfile(profile);

        } else {

            // save

            Profile profile = new Profile(configurationsPreferences.getProfileVersion(), configurationsPreferences.getUsername());
            module.setProfile(profile);
            // save
            configurationsPreferences.saveUserKeys(profile.getKey());
        }


    }


    @Override
    public int registerReqeust(Signer signer, String name, byte[] img, int latitude, int longitude, String extraData) throws Exception {
        int res = updateProfileRequest(signer,new byte[]{1,0,0},name,img,latitude,longitude,extraData);
        configurationsPreferences.setIsCreated(true);
        return res;
    }

    @Override
    public int updateProfileRequest(Signer signer, byte[] version, String name, byte[] img, int latitude, int longitude, String extraData) throws Exception {
        Log.d(TAG,"updateProfileRequest, state: "+state);
        if (state == CHECK_IN){
            try{
                configurationsPreferences.setUsername(name);

                return profileServer.updateProfileRequest(
                        signer,
                        version,
                        name,
                        img,
                        latitude,
                        longitude,
                        extraData
                );
            }catch (Exception e){
                e.printStackTrace();
                throw e;
            }
        }else
            throw new Exception("Not checked in");
    }

    @Override
    public int updateExtraData(Signer signer, String extraData) throws Exception {
        return profileServer.updateExtraData(signer,extraData);
    }

    @Override
    public boolean isIdentityCreated() {
        return configurationsPreferences.isRegisteredInServer();
    }


    /**
     *  Engine
     */

    private void engineServerConnect(){
        // get the availables roles..
        requestRoleList();
        boolean isRegistered = configurationsPreferences.isRegisteredInServer();

        if (!isRegistered) {
            // start conversation to request a home request (if it not logged)
            startConverNonClPort();
            // Request home node request
            requestHomeNodeRequest();
        }
        // Start conversation with customer port
        if (isRegistered || configurationsPreferences.getClPort()!=configurationsPreferences.getNonClPort())
            startConverClPort();
        else if (state == HOME_NODE_REQUEST){
            state = START_CONVERSATION_CL;
        }
        // Client connected, now the identity have to do the check in
        requestCheckin();


    }

    /**
     * Request roles list to the server
     */
    private void requestRoleList(){
        if (configurationsPreferences.getClPort()==0){
            Log.d(TAG,"requestRoleList");
            if (state == NO_SERVER) {
                state = ProfileServerConnectionState.GETTING_ROLE_LIST;
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
        }else if (state==NO_SERVER){
            state = HAS_ROLE_LIST;
        }
    }

    /**
     * Start conversation with nonCustomerPort to make a HomeNodeRequest
     */
    private void startConverNonClPort(){
        if (state == HAS_ROLE_LIST && !configurationsPreferences.isRegisteredInServer()) {
            Log.d(TAG,"startConverNonClPort");
            if (configurationsPreferences.getNonClPort()==0) throw new RuntimeException("Non customer port == 0!!");
            state = WAITING_START_NON_CL;
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        profileServer.startConversationNonCl(
                                    module.getProfile().getPublicKey(),
                                    module.getProfile().getConnectionChallenge()
                        );
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

        }
    }

    /**
     * Request a home node request to the server
     */
    private void requestHomeNodeRequest(){
        if (state == ProfileServerConnectionState.START_CONVERSATION_NON_CL){
            Log.d(TAG,"requestHomeNodeRequest");
            state = WAITING_HOME_NODE_REQUEST;
            byte[] idetityPubKey = configurationsPreferences.getUserPubKey();
            final String identityType = "Contributor";
            if(idetityPubKey==null){
                Log.d(TAG,"Creating identity");
                // acá saco el dialogo para que se cree una identidad
                idetityPubKey = module.getProfile().getPublicKey(); //KeyEd25519.generateKeys().getPublicKey();
                configurationsPreferences.setUserPubKey(idetityPubKey);
                final byte[] finalIdetityPubKey = idetityPubKey;
                try {
                    profileServer.homeNodeRequest(finalIdetityPubKey, identityType);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }else {
                try {
                    profileServer.homeNodeRequest(idetityPubKey,identityType);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Start a conversation with the customer port to make the check-in
     */
    private void startConverClPort(){
        if (state == ProfileServerConnectionState.HOME_NODE_REQUEST || state == HAS_ROLE_LIST) {
            Log.d(TAG,"startConverClPort");
            state = WAITING_START_CL;
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        profileServer.startConversationCl(
                                    module.getProfile().getPublicKey(),
                                    module.getProfile().getConnectionChallenge()
                        );
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    /**
     * Do the check in to the server
     */
    private void requestCheckin(){
        if (state==START_CONVERSATION_CL){
            try {
                // se le manda el challenge del nodo + la firma de dicho challenge en el campo de signature
                profileServer.checkIn(module.getProfile().getNodeChallenge(), module.getProfile());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Update the existing profile in the server
     */
    public void updateProfileRequest(byte[] version,String name,byte[] img,int lat,int lon,String extraData){
        Log.d(TAG,"updateProfileRequest, state: "+state);
        if (state == CHECK_IN){
            try{
                profileServer.updateProfileRequest(
                        module.getProfile(),
                        version,
                        name,
                        img,
                        lat,
                        lon,
                        extraData
                );
            }catch (Exception e){
                e.printStackTrace();
            }
        }


    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG,"onStartCommand");


        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy() {
        Log.d(TAG,"onDestroy");

        executor.shutdown();

        super.onDestroy();
    }

    public class ProfileServerHanlder implements IoHandler<IopHomeNodeProto3.Message>{

        private static final String TAG = "ProfileServerHanlder";

        public static final int LIST_ROLES_PROCESSOR = 1;
        public static final int START_CONVERSATION_NON_CL_PROCESSOR = 2;
        private static final int HOME_NODE_REQUEST_PROCESSOR = 3;
        private static final int HOME_START_CONVERSATION_CL_PROCESSOR = 4;
        private static final int HOME_CHECK_IN_PROCESSOR = 5;
        private static final int HOME_UPDATE_PROFILE_PROCESSOR = 6;

        private Map<Integer,MessageProcessor> processors;

        public ProfileServerHanlder() {
            processors = new HashMap<>();
            processors.put(LIST_ROLES_PROCESSOR,new ListRolesProcessor());
            processors.put(START_CONVERSATION_NON_CL_PROCESSOR,new StartConversationNonClProcessor());
            processors.put(HOME_NODE_REQUEST_PROCESSOR,new HomeNodeRequestProcessor());
            processors.put(HOME_START_CONVERSATION_CL_PROCESSOR,new StartConversationClProcessor());
            processors.put(HOME_CHECK_IN_PROCESSOR,new CheckinConversationProcessor());
            processors.put(HOME_UPDATE_PROFILE_PROCESSOR,new UpdateProfileConversationProcessor());
        }

        @Override
        public void sessionCreated(IoSession session) throws Exception {

        }

        @Override
        public void sessionOpened(IoSession session) throws Exception {

        }

        @Override
        public void sessionClosed(IoSession session) throws Exception {

        }

        @Override
        public void exceptionCaught(IoSession session, Throwable cause) throws Exception {

        }

        @Override
        public void messageReceived(IoSession session, IopHomeNodeProto3.Message message) throws Exception {
            switch (message.getMessageTypeCase()) {
                case REQUEST:

                    break;

                case RESPONSE:
                    try {
                        dispatchResponse(session, message.getResponse());
                    }catch (Exception e){
                        Log.e(TAG,"Message id fail: "+message.getId());
                        e.printStackTrace();
                    }
                    break;
            }
        }

        @Override
        public void messageSent(IoSession session, IopHomeNodeProto3.Message message) throws Exception {

        }

        @Override
        public void inputClosed(IoSession session) throws Exception {

        }

        private void dispatchResponse(IoSession session, IopHomeNodeProto3.Response response) throws Exception {
            switch (response.getConversationTypeCase()){

                case CONVERSATIONRESPONSE:
                    dispatchConversationResponse(session,response.getConversationResponse());
                    break;
                case SINGLERESPONSE:
                    dispatchSingleResponse(session,response.getSingleResponse());
                    break;

                case CONVERSATIONTYPE_NOT_SET:
                    IopHomeNodeProto3.Status status = response.getStatus();
                    switch (status){
                        // this happen when the connection is active and i send a startConversation or something else that i have to see..
                        case ERROR_BAD_CONVERSATION_STATUS:
                            Log.e(TAG,"response: "+response.toString());
                            state = START_CONVERSATION_NON_CL;
                            break;
                        // this happen whe the identity already exist or when the cl and non-cl port are the same in the StartConversation message
                        case ERROR_ALREADY_EXISTS:
                            Log.d(TAG,"response: "+response.toString());
                            if (state == WAITING_START_CL)
                                state = START_CONVERSATION_CL;
                            else state = HOME_NODE_REQUEST;
                            break;

                        default:
                            throw new Exception("response with CONVERSATIONTYPE_NOT_SET, response: "+response.toString());
                    }
                    // engine
                    engineServerConnect();
                    break;

            }
        }

        private void dispatchSingleResponse(IoSession session, IopHomeNodeProto3.SingleResponse singleResponse){
            switch (singleResponse.getResponseTypeCase()){

                case LISTROLES:
                    Log.d(TAG,"ListRoles received");
                    processors.get(LIST_ROLES_PROCESSOR).execute(singleResponse.getListRoles());
                    break;

                default:
                    Log.d(TAG,"algo llegó y no lo estoy controlando..");
                    break;
            }

        }

        private void dispatchConversationResponse(IoSession session, IopHomeNodeProto3.ConversationResponse conversationResponse) throws Exception {
            switch (conversationResponse.getResponseTypeCase()){

                case START:
                    Log.d(TAG,"start conversation received in port: "+session.getPortType());
                    // saving the challenge signed..
                    byte[] signedChallenge = conversationResponse.getSignature().toByteArray();
                    module.getProfile().setSignedConnectionChallenge(signedChallenge);
                    Log.d(TAG,"challenge signed: "+ CryptoBytes.toHexString(signedChallenge));

                    if (state==WAITING_START_NON_CL) processors.get(START_CONVERSATION_NON_CL_PROCESSOR).execute(conversationResponse.getStart());
                    else processors.get(HOME_START_CONVERSATION_CL_PROCESSOR).execute(conversationResponse.getStart());
                    break;
                case HOMENODEREQUEST:
                    Log.d(TAG,"home node response received in port: "+session.getPortType());
                    processors.get(HOME_NODE_REQUEST_PROCESSOR).execute(conversationResponse.getHomeNodeRequest());
                    break;
                case CHECKIN:
                    Log.d(TAG,"check in response ");
                    processors.get(HOME_CHECK_IN_PROCESSOR).execute(conversationResponse.getCheckIn());
                    break;
                case UPDATEPROFILE:
                    if (verifyIdentity(conversationResponse.getSignature().toByteArray(),conversationResponse.toByteArray())) {
                        processors.get(HOME_UPDATE_PROFILE_PROCESSOR).execute(conversationResponse.getUpdateProfile());
                    }else {
                        throw new Exception("El nodo no es quien dice, acá tengo que desconectar todo");
                    }
                    break;
                default:
                    Log.e(TAG,"algo llegó y no lo estoy controlando..");
                    break;
            }

        }

        private boolean verifyIdentity(byte[] signature,byte[] message) {
            //KeyEd25519.verify(signature,message,profile.getNodePubKey());
            return true;
        }
    }

    /**
     *
     */
    private class ListRolesProcessor implements MessageProcessor<IopHomeNodeProto3.ListRolesResponse> {

        private static final String TAG = "ListRolesProcessor";

        @Override
        public void execute(IopHomeNodeProto3.ListRolesResponse message) {
            Log.d(TAG,"execute..");
            for (IopHomeNodeProto3.ServerRole serverRole : message.getRolesList()) {
                switch (serverRole.getRole()){
                    case CL_NON_CUSTOMER:
                        configurationsPreferences.setNonClPort(serverRole.getPort());
                        break;
                    case CL_CUSTOMER:
                        configurationsPreferences.setClPort(serverRole.getPort());
                        break;
                    case PRIMARY:
                        configurationsPreferences.setPrimaryPort(serverRole.getPort());
                        break;
                    default:
                        //nothing
                        break;
                }
            }
            state = HAS_ROLE_LIST;
            try {
                profileServer.closePort(PortType.PRIMARY);
            }catch (Exception e){
                e.printStackTrace();
            }
            Log.d(TAG,"ListRolesProcessor no cl port: "+configurationsPreferences.getNonClPort());
            engineServerConnect();
        }
    }

    /**
     *
     */
    private class StartConversationNonClProcessor implements MessageProcessor<IopHomeNodeProto3.StartConversationResponse>{

        private static final String TAG = "StartNonClProcessor";

        @Override
        public void execute(IopHomeNodeProto3.StartConversationResponse message) {
            Log.d(TAG,"execute..");
            //todo: ver todos los get que tiene esto, el challenge y demás...
            state = START_CONVERSATION_NON_CL;
            // set the node challenge
            module.getProfile().setNodeChallenge(message.getChallenge().toByteArray());
            engineServerConnect();
        }
    }

    private class HomeNodeRequestProcessor implements MessageProcessor<IopHomeNodeProto3.HomeNodeRequestResponse>{

        @Override
        public void execute(IopHomeNodeProto3.HomeNodeRequestResponse message) {
            Log.d(TAG,"execute..");
            //todo: ver que parametros utilizar de este homeNodeResponse..
            state = HOME_NODE_REQUEST;
            configurationsPreferences.setIsRegistered(true);
            engineServerConnect();
        }
    }

    private class StartConversationClProcessor implements MessageProcessor<IopHomeNodeProto3.StartConversationResponse>{

        private static final String TAG = "StartClProcessor";

        @Override
        public void execute(IopHomeNodeProto3.StartConversationResponse message) {
            Log.d(TAG,"execute..");
            //todo: ver todos los get que tiene esto, el challenge y demás...
            // set the node challenge
            module.getProfile().setNodeChallenge(message.getChallenge().toByteArray());
            state = START_CONVERSATION_CL;
            engineServerConnect();
        }
    }

    private class CheckinConversationProcessor implements MessageProcessor<IopHomeNodeProto3.CheckInResponse>{

        private static final String TAG = "CheckinProcessor";

        @Override
        public void execute(IopHomeNodeProto3.CheckInResponse message) {
            Log.d(TAG,"execute..");
            state = CHECK_IN;
            Log.d(TAG,"#### Check in completed!!  ####");

        }
    }

    private class UpdateProfileConversationProcessor implements MessageProcessor<IopHomeNodeProto3.UpdateProfileResponse>{

        private static final String TAG = "UpdateProfileProcessor";

        @Override
        public void execute(IopHomeNodeProto3.UpdateProfileResponse message) {
            Log.d(TAG,"execute..");
            Log.d(TAG,"update works..");

        }
    }


}
