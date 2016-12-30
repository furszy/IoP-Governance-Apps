package iop_sdk.wallet;

import com.google.common.base.Charsets;
import com.google.common.base.Stopwatch;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.crypto.MnemonicCode;
import org.bitcoinj.wallet.Protos;
import org.bitcoinj.wallet.SendRequest;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.WalletFiles;
import org.bitcoinj.wallet.WalletProtobufSerializer;
import org.bitcoinj.wallet.listeners.WalletCoinsReceivedEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import iop_sdk.crypto.Crypto;
import iop_sdk.global.ContextWrapper;
import iop_sdk.global.utils.Io;
import iop_sdk.wallet.utils.WalletUtils;


/**
 * Created by mati on 07/11/16.
 *
 */

public class WalletManager {

    private static final Logger LOG = LoggerFactory.getLogger(WalletManager.class);

    private Wallet wallet;

    private File walletFile;

    private WalletPreferenceConfigurations walletConfiguration;

    private ContextWrapper context;

    private WalletManagerListener listener;

    public WalletManager(ContextWrapper context,WalletPreferenceConfigurations walletConfiguration,WalletManagerListener listener) {
        this.walletConfiguration = walletConfiguration;
        this.context = context;
        this.listener = listener;
        init();
    }

    public void init(){

        initMnemonicCode();

        restoreWallet();

        System.out.println("Seed: "+Arrays.toString(wallet.getActiveKeyChain().getSeed().getSeedBytes()));


    }

    public void setListener(WalletManagerListener listener) {
        this.listener = listener;
    }

    public void removeListener(WalletManagerListener listener){
        this.listener = null;
    }

    private void initMnemonicCode() {
        try {
            final Stopwatch watch = Stopwatch.createStarted();
            MnemonicCode.INSTANCE = new MnemonicCode(context.openAssestsStream(walletConfiguration.getMnemonicFilename()),null);
            watch.stop();
            LOG.info("BIP39 wordlist loaded from: '{}', took {}", walletConfiguration.getMnemonicFilename(), watch);
        }
        catch (final IOException x) {
            throw new Error(x);
        }
    }


    private void restoreWallet(){

        walletFile = context.getFileStreamPath(walletConfiguration.getWalletProtobufFilename());

        loadWalletFromProtobuf(walletFile);
    }

    private void restoreWallet(final Wallet wallet) throws IOException {

        replaceWallet(wallet);

        //config.disarmBackupReminder();
        if (listener!=null)
            listener.onWalletRestored();

    }

    /**
     * Load the wallet from a wallet file or create,save and backup one in that file if not exist
     *
     * @param walletFile
     */
    private void loadWalletFromProtobuf(File walletFile) {

        if (walletFile.exists()) {

            FileInputStream walletStream = null;

            try {
                walletStream = new FileInputStream(walletFile);
                wallet = new WalletProtobufSerializer().readWallet(walletStream);

                if (!wallet.getParams().equals(walletConfiguration.getNetworkParams()))
                    throw new UnreadableWalletException("bad wallet network parameters: " + wallet.getParams().getId());

            } catch (UnreadableWalletException e) {
                LOG.error("problem loading wallet", e);

                wallet = restoreWalletFromBackup();
            } catch (FileNotFoundException e) {
                LOG.error("problem loading wallet", e);
                context.toast(e.getClass().getName());
                wallet = restoreWalletFromBackup();
            } finally {
                if (walletStream != null)
                    try {
                        walletStream.close();
                    } catch (IOException e) {
                        //nothing
                    }
            }

            //todo: ver que es esto..
            if (!wallet.isConsistent()) {
                context.toast("inconsistent wallet: " + walletFile);
                LOG.error("inconsistent wallet "+walletFile);
                wallet = restoreWalletFromBackup();
            }

            if (!wallet.getParams().equals(walletConfiguration.getNetworkParams()))
                throw new Error("bad wallet network parameters: " + wallet.getParams().getId());

            afterLoadWallet();

        }else {
            wallet = new Wallet(walletConfiguration.getWalletContext());

            saveWallet();
            backupWallet();

//            config.armBackupReminder();
            LOG.info("new wallet created");
        }


        wallet.addCoinsReceivedEventListener(new WalletCoinsReceivedEventListener() {
            @Override
            public void onCoinsReceived(Wallet wallet, Transaction transaction, Coin coin, Coin coin1) {
                org.bitcoinj.core.Context.propagate(walletConfiguration.getWalletContext());
                saveWallet();

            }
        });
    }

    /**
     * Este metodo puede tener varias implementaciones de guardado distintas.
     */
    public void saveWallet() {
        try {
            protobufSerializeWallet(wallet);
        } catch (final IOException x) {
            throw new RuntimeException(x);
        }
    }

    /**
     * Save wallet file
     *
     * @param wallet
     * @throws IOException
     */
    private void protobufSerializeWallet(final Wallet wallet) throws IOException {

        final Stopwatch watch = Stopwatch.createStarted();
        wallet.saveToFile(walletFile);
        watch.stop();

        // make wallets world accessible in test mode
        if (walletConfiguration.isTest())
            Io.chmod(walletFile, 0777);

        LOG.info("wallet saved to: '{}', took {}", walletFile, watch);
    }

    /**
     * Backup wallet
     */
    private void backupWallet() {

        final Protos.Wallet.Builder builder = new WalletProtobufSerializer().walletToProto(wallet).toBuilder();

        // strip redundant
        builder.clearTransaction();
        builder.clearLastSeenBlockHash();
        builder.setLastSeenBlockHeight(-1);
        builder.clearLastSeenBlockTimeSecs();
        final Protos.Wallet walletProto = builder.build();

        OutputStream os = null;

        try{
            os = context.openFileOutputPrivateMode(walletConfiguration.getKeyBackupProtobuf());
            walletProto.writeTo(os);
        } catch (FileNotFoundException e) {
            LOG.error("problem writing wallet backup", e);
        } catch (IOException e) {
            LOG.error("problem writing wallet backup", e);
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                // nothing
            }
        }
    }

    /**
     * Restore wallet from backup
     * @return
     */
    private Wallet restoreWalletFromBackup(){

        InputStream is = null;
        try {
            is = context.openFileInput(walletConfiguration.getKeyBackupProtobuf());
            final Wallet wallet = new WalletProtobufSerializer().readWallet(is,true,null);
            if (!wallet.isConsistent())
                throw new Error("Inconsistent backup");
            resetBlockchain();
            context.toast("Your wallet was reset!\\\\nIt will take some time to recover.");
            LOG.info("wallet restored from backup: '" + walletConfiguration.getKeyBackupProtobuf() + "'");
            return wallet;
        }catch (final IOException e){
            throw new Error("cannot read backup",e);
        }catch (UnreadableWalletException e){
            throw new Error("cannot read backup",e);
        }finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                // nothing
            }
        }
    }


    public void backupWallet(File file,final String password) throws IOException {

        final Protos.Wallet walletProto = new WalletProtobufSerializer().walletToProto(wallet);

        Writer cipherOut = null;

        try {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            walletProto.writeTo(baos);
            baos.close();
            final byte[] plainBytes = baos.toByteArray();

            cipherOut = new OutputStreamWriter(new FileOutputStream(file), Charsets.UTF_8);
            cipherOut.write(Crypto.encrypt(plainBytes, password.toCharArray()));
            cipherOut.flush();

            LOG.info("backed up wallet to: '" + file + "'");

            //ArchiveBackupDialogFragment.show(getFragmentManager(), file);
        }finally {
            if (cipherOut != null)
            {
                try {
                    cipherOut.close();
                }
                catch (final IOException x) {
                    // swallow
                }
            }
        }


    }

    /**
     * Launch an intent to the blockchain service to reset the blockchain
     */
    public void resetBlockchain() {
        // implicitly stops blockchain service
//        context.startService(ServicesCodes.BLOCKCHAIN_SERVICE,BlockchainService.ACTION_RESET_BLOCKCHAIN);
        context.stopBlockchainService();
    }

    public Wallet getWallet() {
        return wallet;
    }

    public void replaceWallet(final Wallet newWallet) {
        resetBlockchain();
        wallet.shutdownAutosaveAndWait();

        wallet = newWallet;
        walletConfiguration.maybeIncrementBestChainHeightEver(newWallet.getLastBlockSeenHeight());
        afterLoadWallet();

        // todo: Nadie estaba escuchando esto.. Tengo que ver que deberia hacer despues
//        final IntentWrapper intentWrapper = new IntentWrapperAndroid(WalletConstants.ACTION_WALLET_REFERENCE_CHANGED);
//        intentWrapper.setPackage(context.getPackageName());
//
//        context.sendLocalBroadcast(intentWrapper);

    }

    private void afterLoadWallet()
    {
        wallet.autosaveToFile(walletFile, walletConfiguration.getWalletAutosaveDelayMs(), TimeUnit.MILLISECONDS, new WalletAutosaveEventListener(walletConfiguration));

        // clean up spam
        wallet.cleanup();

        // make sure there is at least one recent backup
        if (!context.getFileStreamPath(walletConfiguration.getKeyBackupProtobuf()).exists())
            backupWallet();
    }

    public void restoreWalletFromProtobuf(final File file) throws IOException {
        FileInputStream is = null;
        try {
            is = new FileInputStream(file);
            restoreWallet(WalletUtils.restoreWalletFromProtobuf(is, walletConfiguration.getNetworkParams()));

            LOG.info("successfully restored unencrypted wallet: {}", file);
        } finally {
            if (is != null) {
                try {
                    is.close();
                }
                catch (final IOException x2) {
                    // swallow
                }
            }
        }
    }

    public void restorePrivateKeysFromBase58(File file) throws IOException {
        FileInputStream is = null;
        try {
            is = new FileInputStream(file);
            restoreWallet(WalletUtils.restorePrivateKeysFromBase58(is,walletConfiguration.getNetworkParams(),walletConfiguration.getBackupMaxChars()));

            LOG.info("successfully restored unencrypted private keys: {}", file);
        } finally {
            if (is != null)
            {
                try
                {
                    is.close();
                }
                catch (final IOException x2)
                {
                    // swallow
                }
            }
        }
    }

    public void restoreWalletFromEncrypted(File file, String password) throws IOException {
        final BufferedReader cipherIn = new BufferedReader(new InputStreamReader(new FileInputStream(file), Charsets.UTF_8));
        final StringBuilder cipherText = new StringBuilder();
        Io.copy(cipherIn, cipherText, walletConfiguration.getBackupMaxChars());
        cipherIn.close();

        final byte[] plainText = Crypto.decryptBytes(cipherText.toString(), password.toCharArray());
        final InputStream is = new ByteArrayInputStream(plainText);

        restoreWallet(WalletUtils.restoreWalletFromProtobufOrBase58(is, walletConfiguration.getNetworkParams(),walletConfiguration.getBackupMaxChars()));

        LOG.info("successfully restored encrypted wallet: {}", file);
    }


    public Transaction createAndLockTransaction(String address, long amount) throws InsufficientMoneyException {

        Address to = Address.fromBase58(walletConfiguration.getNetworkParams(),address);
        Coin value = Coin.valueOf(amount);

        SendRequest sendRequest = SendRequest.to(to,value);

        sendRequest.signInputs = true;

        wallet.completeTx(sendRequest);

        wallet.commitTx(sendRequest.tx);

        return sendRequest.tx;
    }

    public WalletPreferenceConfigurations getConfigurations() {
        return walletConfiguration;
    }


    private static final class WalletAutosaveEventListener implements WalletFiles.Listener {

        WalletPreferenceConfigurations conf;

        public WalletAutosaveEventListener(WalletPreferenceConfigurations walletConfiguration) {
            conf = walletConfiguration;
        }

        @Override
        public void onBeforeAutoSave(final File file)
        {
        }

        @Override
        public void onAfterAutoSave(final File file)
        {
            // make wallets world accessible in test mode
            if (conf.isTest())
                Io.chmod(file, 0777);
        }
    }
}
