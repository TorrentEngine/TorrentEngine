


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
    	System.out.println("Download Completed\n");
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