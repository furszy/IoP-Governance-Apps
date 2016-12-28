package org.iop;

import java.io.File;

import iop_sdk.global.ContextWrapper;

/**
 * Created by mati on 27/12/16.
 */
public interface AppController extends ContextWrapper {

    boolean isVotingApp();

    WalletModule getModule();

    boolean isMemoryLow();

    String[] databaseList();

    File getFilesDir();

    long getTimeCreateApplication();
}
