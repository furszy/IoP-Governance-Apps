package iop.org.iop_contributors_app.intents.constants;

import iop.org.iop_contributors_app.ui.base.BaseActivity;

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
    public static final String INTENTE_BROADCAST_DIALOG_TYPE = BaseActivity.class.getPackage().toString() + "_intent_broadcast_dialog_type";

    /**
     * Dialog
     */

    /** Mensaje a mostrar en el dialog */
    public static final String INTENTE_EXTRA_MESSAGE = BaseActivity.class.getPackage().toString() + "_intent_extra_message";


    /**
     *  Posibles elecciones de dialogs
     */
    public static final String RESTORE_SUCCED_DIALOG = BaseActivity.class.getPackage().toString() + "_restore_succed_dialog";


    /**
     * Data
     */

    public static final String INTENT_BROADCAST_DATA_TRANSACTION_SUCCED = BaseActivity.class.getPackage().toString() + "_broadcast_transaction_succed";

}
