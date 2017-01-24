package iop.org.contributors_app;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.iop.WalletConstants;
import org.iop.WalletModule;
import org.iop.db.CantSaveProposalException;
import org.iop.exceptions.CantSendProposalException;
import org.iop.exceptions.InvalidProposalException;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import iop_sdk.blockchain.NotConnectedPeersException;
import iop_sdk.governance.propose.CantCompleteProposalException;
import iop_sdk.governance.propose.Proposal;
import iop_sdk.wallet.exceptions.InsuficientBalanceException;

import static org.junit.Assert.assertEquals;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    private static final String TAG = "ExampleInstrumentedTest";

    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("iop.org.contributors_app", appContext.getPackageName());
    }


}
