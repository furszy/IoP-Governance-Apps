package iop.org.iop_contributors_app.wallet;

import android.content.Context;

import com.google.common.base.Stopwatch;

import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.crypto.MnemonicCode;
import org.bitcoinj.wallet.Protos;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.WalletProtobufSerializer;
import org.bitcoinj.wallet.listeners.WalletCoinsReceivedEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import iop.org.iop_contributors_app.services.BlockchainService;
import iop.org.iop_contributors_app.services.ServicesCodes;
import iop.org.iop_contributors_app.configurations.WalletPreferencesConfiguration;
import iop.org.iop_contributors_app.utils.Io;

import static iop.org.iop_contributors_app.wallet.WalletConstants.Files.BIP39_WORDLIST_FILENAME;

/**
 * Created by mati on 07/11/16.
 *
 */

public class WalletManager {

    private static final Logger LOG = LoggerFactory.getLogger(WalletManager.class);

    private Wallet wallet;

    private File walletFile;

    private WalletPreferencesConfiguration walletConfiguration;

    // android,  por ahora puedo dejarlo con esto en duro pero despues le voy a hacer un wrapper.
    private ContextWrapper context;

    public WalletManager(ContextWrapper context,WalletPreferencesConfiguration walletConfiguration) {
        this.walletConfiguration = walletConfiguration;
        this.context = context;
        init();
    }

    public void init(){

        initMnemonicCode();

        restoreWallet();


    }

    private void initMnemonicCode() {
        try {
            final Stopwatch watch = Stopwatch.createStarted();
            MnemonicCode.INSTANCE = new MnemonicCode(context.openAssestsStream(BIP39_WORDLIST_FILENAME),null);
            watch.stop();
            LOG.info("BIP39 wordlist loaded from: '{}', took {}", BIP39_WORDLIST_FILENAME, watch);
        }
        catch (final IOException x) {
            throw new Error(x);
        }
    }


    private void restoreWallet(){

        walletFile = context.getFileStreamPath(WalletConstants.Files.WALLET_FILENAME_PROTOBUF);

        loadWalletFromProtobuf(walletFile);


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

                if (!wallet.getParams().equals(WalletConstants.NETWORK_PARAMETERS))
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

            if (!wallet.getParams().equals(WalletConstants.NETWORK_PARAMETERS))
                throw new Error("bad wallet network parameters: " + wallet.getParams().getId());

        }else {
            wallet = new Wallet(WalletConstants.CONTEXT);

            saveWallet();
            backupWallet();

//            config.armBackupReminder();
            LOG.info("new wallet created");
        }


        wallet.addCoinsReceivedEventListener(new WalletCoinsReceivedEventListener() {
            @Override
            public void onCoinsReceived(Wallet wallet, Transaction transaction, Coin coin, Coin coin1) {
                org.bitcoinj.core.Context.propagate(WalletConstants.CONTEXT);
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
        if (WalletConstants.TEST)
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
            os = context.openFileOutput(WalletConstants.Files.WALLET_KEY_BACKUP_PROTOBUF,Context.MODE_PRIVATE);
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
            is = context.openFileInput(WalletConstants.Files.WALLET_KEY_BACKUP_PROTOBUF);
            final Wallet wallet = new WalletProtobufSerializer().readWallet(is,true,null);
            if (!wallet.isConsistent())
                throw new Error("Inconsistent backup");
            resetBlockchain();
            context.toast("Your wallet was reset!\\\\nIt will take some time to recover.");
            LOG.info("wallet restored from backup: '" + WalletConstants.Files.WALLET_KEY_BACKUP_PROTOBUF + "'");
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



    /**
     * Launch an intent to the blockchain service to reset the blockchain
     */
    public void resetBlockchain() {
        // implicitly stops blockchain service
        context.startService(ServicesCodes.BLOCKCHAIN_SERVICE,BlockchainService.ACTION_RESET_BLOCKCHAIN);
    }

    public Wallet getWallet() {
        return wallet;
    }
}
