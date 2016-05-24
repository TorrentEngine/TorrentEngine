

import java.io.File;
import java.io.IOException;

import controller.GlobalManager;
import torrentlib.AzureusCoreFactory;
import torrentlib.TorrentEngineCore;
import torrentlib.disk.DiskManagerFileInfo;
import xfer.download.DownloadManager;
import xfer.download.DownloadManagerListener;

public class Download {

	public static void main(String[] args) throws IOException {
		
		
		TorrentEngineCore core = AzureusCoreFactory.create();
		core.start();
		
		File downloadDirectory = new File("."); //Destination directory
		
		File torFile = new File("cb1655a57dd24345c9ea7a43c5ec09e03c7a0979.torrent");
		
        //Start the download of the torrent
        GlobalManager globalManager = core.getGlobalManager();

        //FileUtils.copyInputStreamToFile(new ByteArrayInputStream(entry.getTorrentFile()), downloadedTorrentFile);
        DownloadManager manager = globalManager.addDownloadManager(
        							torFile.getCanonicalPath(), 
        							downloadDirectory.getAbsolutePath());

        DownloadManagerListener listener = new DownloadStateListener();
        manager.addListener(listener);

        manager.resume();
        globalManager.startAllDownloads();
		
		
	}
	
}
