package com.chess.ui.core;

import android.app.backup.BackupAgent;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataOutput;
import android.os.ParcelFileDescriptor;

import java.io.IOException;

/**
 * @author Alexey Schekin (schekin@azoft.com)
 * @created 02.04.12
 * @modified 02.04.12
 */
public class ChessBackupAgent extends BackupAgent {
    @Override
    public void onBackup(ParcelFileDescriptor parcelFileDescriptor, BackupDataOutput backupDataOutput, ParcelFileDescriptor parcelFileDescriptor1) throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onRestore(BackupDataInput backupDataInput, int i, ParcelFileDescriptor parcelFileDescriptor) throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
