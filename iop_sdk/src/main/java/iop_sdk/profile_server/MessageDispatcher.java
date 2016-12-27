package iop_sdk.profile_server;


import iop_sdk.profile_server.protocol.IopHomeNodeProto3;

/**
 * Created by mati on 08/11/16.
 */
public class MessageDispatcher {

    private static final String TAG = "MessageDispatcher";


    public void dispatch(IopHomeNodeProto3.Message message) throws Exception {

        switch (message.getMessageTypeCase()){


            case REQUEST:

                break;

            case RESPONSE:
                dispatchResponse(message.getResponse());
                break;

        }
    }


    private void dispatchResponse(IopHomeNodeProto3.Response response) throws Exception {
        switch (response.getConversationTypeCase()){

            case CONVERSATIONRESPONSE:

                break;

            case SINGLERESPONSE:
                dispatchSingleResponse(response.getSingleResponse());
                break;

            case CONVERSATIONTYPE_NOT_SET:
                throw new Exception("response with CONVERSATIONTYPE_NOT_SET");


        }
    }

    private void dispatchSingleResponse(IopHomeNodeProto3.SingleResponse singleResponse){
        switch (singleResponse.getResponseTypeCase()){

            case LISTROLES:
//                Log.d(TAG,"ListRoles received");

//                IopHomeNodeProto3.ListRolesResponse listRoles = singleResponse.getListRoles();

                break;


        }

    }

}
