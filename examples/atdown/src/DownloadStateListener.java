/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import torrentlib.AzureusCoreException;
import torrentlib.AzureusCoreFactory;
import torrentlib.disk.DiskManagerFileInfo;
import xfer.download.DownloadManager;
import xfer.download.DownloadManagerListener;
import controller.GlobalManagerDownloadRemovalVetoException;
import torrentlib.TorrentEngineCore;
import stdlib.util.Print;

/**
 *
 * @author Alpesh
 */
public class DownloadStateListener implements DownloadManagerListener {

    @Override
    public void stateChanged(DownloadManager manager, int state) {
        switch (state) {
            case DownloadManager.STATE_DOWNLOADING:
                //Print.line("Downloading....");
                // Start a new daemon thread periodically check
                // the progress of the upload and print it out
                // to the command line
                if (!DownloadEngine.PROGRESS_CHECKER.isAlive()) {
                    DownloadEngine.PROGRESS_CHECKER.start();
                }
                break;
            case DownloadManager.STATE_CHECKING:
                Print.line("Checking Existing Data.." + manager.getDisplayName());
                break;
            case DownloadManager.STATE_ERROR:
                System.out.println("Error : ( Check Log " + manager.getErrorDetails());
                break;
            case DownloadManager.STATE_STOPPED:
                //Print.line("\nStopped.." + manager.getDisplayName());
                break;
            case DownloadManager.STATE_ALLOCATING:
                Print.line("Allocating File Space.." + manager.getDisplayName());
                break;
            case DownloadManager.STATE_INITIALIZING:
                Print.line("Initializing.." + manager.getDisplayName());
                break;
            case DownloadManager.STATE_FINISHING:
                Print.line("Finishing.." + manager.getDisplayName());
                break;
            default:
            //Print.line("state:" + state);

        }
    }

    @Override
    public void downloadComplete(DownloadManager manager) {
        Print.line("\nDownload Completed\n");

        if (Main.keepsharing) {
            Print.line("\n-s Will keep sharing\n");
            return;
        }

        TorrentEngineCore core = AzureusCoreFactory.getSingleton();

        try {
            core.getGlobalManager().removeDownloadManager(manager, false, false);
        } catch (AzureusCoreException | GlobalManagerDownloadRemovalVetoException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // TODO Auto-generated catch block

        // if done
        if (core.getGlobalManager().isSeedingOnly()) {

            try {
                core.requestStop();
            } catch (AzureusCoreException aze) {
                Print.line("Could not end session gracefully - forcing exit.....");
                core.stop();
            }
        }
    }

    @Override
    public void completionChanged(DownloadManager manager, boolean bCompleted) {
        System.out.println("completionChanged");

    }

    @Override
    public void positionChanged(DownloadManager download, int oldPosition,
            int newPosition) {
        System.out.println("positionChanged");

    }

    @Override
    public void filePriorityChanged(DownloadManager download,
            DiskManagerFileInfo file) {
        System.out.println("filePriorityChanged");

    }

}