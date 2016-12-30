package org.iop.intents.constants;


/**
 * Created by mati on 08/12/16.
 *
 * Basicamente el funcionamiento es así:
 *
 * INTENT_BROADCAST_TYPE -> se cheque el tipo de broadcasteo
 *    |
 *    |--> INTENT_DIALOG
 *    |         | (chequea el tipo de dialog)
 *    |         |-->INTENTE_BROADCAST_DIALOG_TYPE
 *    |                         |
 *    |                         |--> RESTORE_SUCCED_DIALOG
 *    |--> INTENT_NOTIFICATION
 *    |
 *    |
 *    |--> INTENT_DATA
 *              |
 *              |--> INTENTE_BROADCAST_DIALOG_TYPE
 *                              |
 *                              |--> INTENT_BROADCAST_DATA_TRANSACTION_SUCCED
 *
 */

public class IntentsConstants {


    /** tipo de broadcast del intent -> dialog,notification,data */
    public static final String INTENT_BROADCAST_TYPE = IntentsConstants.class.getPackage().toString() + "_intentet_notification_type";


    /**
     * tipos de broadcast del intent
     * Se usan para decir que la notificacion es de un determinado tipo
     */
    public static final String INTENT_DIALOG = IntentsConstants.class.getPackage().toString()+"_intent_dialog";
    public static final String INTENT_NOTIFICATION = IntentsConstants.class.getPackage().toString()+"_intent_notification";
    public static final String INTENT_DATA = IntentsConstants.class.getPackage().toString()+"_intent_data";

    /**
     * Tipos
     */

    public static final String INTENT_BROADCAST_DATA_TYPE = IntentsConstants.class.getPackage().toString() + "_intent_broadcast_data_type";
    public static final String INTENTE_BROADCAST_DIALOG_TYPE = IntentsConstants.class.getPackage().toString() + "_intent_broadcast_dialog_type";

    /**
     * Dialog
     */

    /** Mensaje a mostrar en el dialog */
    public static final String INTENTE_EXTRA_MESSAGE = IntentsConstants.class.getPackage().toString() + "_intent_extra_message";


    /**
     *  Posibles elecciones de dialogs
     */
    public static final int RESTORE_SUCCED_DIALOG = 1000;//BaseActivity.class.getPackage().toString() + "_restore_succed_dialog";

    public static final int UNKNOWN_ERROR_DIALOG = 0;
    public static final int INSUFICIENTS_FUNDS_DIALOG = 1;
    public static final int CANT_SAVE_PROPOSAL_DIALOG = 2;
    public static final int COMMON_ERROR_DIALOG = 3;
    public static final int INVALID_PROPOSAL_DIALOG = 4;

    /**
     * Data
     */

    public static final String INTENT_BROADCAST_DATA_TRANSACTION_SUCCED = IntentsConstants.class.getPackage().toString() + "_broadcast_transaction_succed";
    public static final String INTENT_BROADCAST_DATA_VOTE_TRANSACTION_SUCCED = IntentsConstants.class.getPackage().toString() + "_broadcast_vote_transaction_succed";
    public static final String INTENT_BROADCAST_DATA_ON_PROPOSAL_TRANSACTION_RECEIVED = IntentsConstants.class.getPackage().toString() + "_broadcast_proposal_transaction_received";

    public static final String INTENT_BROADCAST_DATA_PROPOSAL_TRANSACTION_ARRIVED = IntentsConstants.class.getPackage().toString() + "_broadcast_proposal_transaction_arrived";

    public static final String INTENT_BROADCAST_DATA_ON_COIN_RECEIVED = IntentsConstants.class.getPackage().toString() + "_broadcast_coin_received";
    public static final String INTENT_BROADCAST_DATA_ON_COIN_RECEIVED_IS_TRANSACTION_MINE = IntentsConstants.class.getPackage().toString()+"_bradcast_coin_received_is_transaction_mine";

}
