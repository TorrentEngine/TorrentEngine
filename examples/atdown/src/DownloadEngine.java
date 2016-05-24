import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import controller.GlobalManager;
import controller.GlobalManagerDownloadRemovalVetoException;
import controller.config.ConfigurationManager;
import stdlib.util.Print;
import torrentlib.AzureusCoreException;
import torrentlib.AzureusCoreFactory;
import torrentlib.Formatter;
import torrentlib.TorrentEngineCore;
import torrentlib.disk.DiskManagerFileInfo;
import torrentlib.models.Entry;
import xfer.download.DownloadManager;
import xfer.download.DownloadManagerListener;

public class DownloadEngine {

    TorrentEngineCore core;
    final static Thread PROGRESS_CHECKER = new Thread(new DownloadEngineStatus());

    public DownloadEngine() throws Exception {

        try {
            core = AzureusCoreFactory.create();
        } catch (Throwable re) {
            Print.line("Error starting core: " + re.getLocalizedMessage());
        }

        if (!core.isStarted()) {
            core.start();
        }

        // Remove any previous download managers.
        try {
            GlobalManager globalManager = core.getGlobalManager();

            for (DownloadManager d : globalManager.getDownloadManagers()) {
                System.out.println("Removed: " + d.getDisplayName());
                globalManager.removeDownloadManager(d);
            }
        } catch (GlobalManagerDownloadRemovalVetoException e) {
            e.printStackTrace();
            throw new Exception("Error setting up engine");
        }

        //setup status checker
        PROGRESS_CHECKER.setDaemon(true);
    }

    public void shutdown() {
        // Print.line("Shutting down...");
        try {
            core.requestStop();
        } catch (AzureusCoreException aze) {
            Print.line("Could not end session gracefully - forcing exit.....");
            core.stop();
        }
    }

    public void downloadMulti(Entry entry, String[] specificFile)
            throws InterruptedException,
            GlobalManagerDownloadRemovalVetoException,
            IOException,
            Exception {

        List<Integer> intList = new LinkedList<>();
        for (String s : specificFile) {
            int i = Integer.valueOf(s) - 1;
            intList.add(i);
        }
        Collections.sort(intList);

        boolean check = true;

        File downloadDirectory = new File("."); //Destination directory

        //Start the download of the torrent
        GlobalManager globalManager = core.getGlobalManager();

        DownloadManager manager
                = globalManager.addDownloadManager(ConfigurationManager.TorrentDirectory + entry.getInfohash() + ".torrent",
                        downloadDirectory.getAbsolutePath());
        manager.pause();

        //Print.line(""+manager.getDiskManagerFileInfoSet().getFiles().length);
        DiskManagerFileInfo[] files = manager.getDiskManagerFileInfoSet().getFiles();
        if (check) {
            for (int y : intList) {
                if (y > files.length - 1) {
                    check = false;
                    Print.line("Specified file number "+(y+1)+" doesn't exists. Please try again.....");
                }
            }
        }
        if (check) {
            Print.line("Downloading: " + entry.getName());
            //DiskManagerFileInfoListener listen = null;

            for (int j = 0; j < files.length; j++) {

                //DiskManagerFileInfo fileInfo = files[j];
                //Print.line(""+ fileInfo);
                if (!intList.contains(j)) {

                    files[j].setSkipped(true);
                    files[j].setStorageType(2);

                    //files[j].close();
                    // files[j].flushCache();
                    //files[j].close();
                } else {
                    files[j].setStorageType(1);
                }
                //Print.line(""+ files[j].getPriority()); // to check priority

            }

            DownloadManagerListener listener = new DownloadStateListener();
            manager.addListener(listener);
            manager.resume();
            globalManager.startAllDownloads();

        }
        else{
        core.requestStop();
    }
    }

    public void download(Entry entry, String specificFile) throws InterruptedException,
            GlobalManagerDownloadRemovalVetoException,
            IOException,
            Exception {

        int i = Integer.parseInt(specificFile);
        i--;
        // Print.line("to be downloaded inside single file");

        File downloadDirectory = new File("."); //Destination directory

        //Start the download of the torrent
        GlobalManager globalManager = core.getGlobalManager();

        //FileUtils.copyInputStreamToFile(new ByteArrayInputStream(entry.getTorrentFile()), downloadedTorrentFile);
        DownloadManager manager
                = globalManager.addDownloadManager(ConfigurationManager.TorrentDirectory + entry.getInfohash() + ".torrent",
                        downloadDirectory.getAbsolutePath());
        manager.pause();

        DiskManagerFileInfo[] files = manager.getDiskManagerFileInfoSet().getFiles();

        //Print.line(""+manager.getDiskManagerFileInfoSet().getFiles().length);
        if (i > files.length - 1) {
            // Print.line(files.length+"");

            //Print.line(i+"");
            Print.line("Specified file number doesn't exists. Please try again.....");
            core.requestStop();
        } else {
            Print.line("Downloading: " + entry.getName());

            for (int j = 0; j < files.length; j++) {

                //DiskManagerFileInfo fileInfo = files[j];
                //Print.line(""+ fileInfo);
                if (j != i) {
                    files[j].setSkipped(true);
                    //files[j].flushCache();
                    files[j].setStorageType(2);
                    // files[j].close();
                    // files[j].flushCache();

                } else {
                    files[j].setStorageType(1);
                }
                //Print.line(""+ files[j].getPriority()); // to check priority

            }
            DownloadManagerListener listener = new DownloadStateListener();
            manager.addListener(listener);

            manager.resume();
            globalManager.startAllDownloads();

        }
    }

    public void download(Entry entry, String specificFile, File torFile) throws InterruptedException,
            GlobalManagerDownloadRemovalVetoException,
            IOException,
            Exception {

        int i = Integer.parseInt(specificFile);
        i--;
        // Print.line("to be downloaded inside single file");

        File downloadDirectory = new File("."); //Destination directory

        //Start the download of the torrent
        GlobalManager globalManager = core.getGlobalManager();

        //FileUtils.copyInputStreamToFile(new ByteArrayInputStream(entry.getTorrentFile()), downloadedTorrentFile);
        DownloadManager manager
                = globalManager.addDownloadManager(torFile.getCanonicalPath(),
                        downloadDirectory.getAbsolutePath());
        manager.pause();

        DiskManagerFileInfo[] files = manager.getDiskManagerFileInfoSet().getFiles();

        //Print.line(""+manager.getDiskManagerFileInfoSet().getFiles().length);
        if (i > files.length - 1) {
            // Print.line(files.length+"");

            // Print.line(i+"");
            Print.line("Specified file number doesn't exists. Please try again.....");
            core.requestStop();
        } else {
            Print.line("Downloading: " + entry.getName());

            for (int j = 0; j < files.length; j++) {

                //DiskManagerFileInfo fileInfo = files[j];
                //Print.line(""+ fileInfo);
                if (j != i) {
                    files[j].setSkipped(true);
                    //files[j].flushCache();
                    files[j].setStorageType(2);
                    // files[j].close();
                    // files[j].flushCache();

                } else {
                    files[j].setStorageType(1);
                }
                //Print.line(""+ files[j].getPriority()); // to check priority

            }
            DownloadManagerListener listener = new DownloadStateListener();
            manager.addListener(listener);

            manager.resume();
            globalManager.startAllDownloads();

        }
    }

    public void downloadMulti(Entry entry, String[] specificFile, File torFile)
            throws InterruptedException,
            GlobalManagerDownloadRemovalVetoException,
            IOException,
            Exception {

        List<Integer> intList = new LinkedList<>();
        for (String s : specificFile) {
            int i = Integer.valueOf(s) - 1;
            intList.add(i);
        }
        Collections.sort(intList);

        boolean check = true;

        File downloadDirectory = new File("."); //Destination directory

        //Start the download of the torrent
        GlobalManager globalManager = core.getGlobalManager();

        DownloadManager manager
                = globalManager.addDownloadManager(torFile.getCanonicalPath(),
                        downloadDirectory.getAbsolutePath());
        manager.pause();

        //Print.line(""+manager.getDiskManagerFileInfoSet().getFiles().length);
        DiskManagerFileInfo[] files = manager.getDiskManagerFileInfoSet().getFiles();
        if (check) {
            for (int y : intList) {
                if (y > files.length - 1) {
                    check = false;
                    Print.line("Specified file number "+(y+1)+" doesn't exists. Please try again.....");
                }
            }
        }
        if (check) {
            Print.line("Downloading: " + entry.getName());
            //DiskManagerFileInfoListener listen = null;

            for (int j = 0; j < files.length; j++) {

                //DiskManagerFileInfo fileInfo = files[j];
                //Print.line(""+ fileInfo);
                if (!intList.contains(j)) {

                    files[j].setSkipped(true);
                    files[j].setStorageType(2);

                    //files[j].close();
                    // files[j].flushCache();
                    //files[j].close();
                } else {
                    files[j].setStorageType(1);
                }
                //Print.line(""+ files[j].getPriority()); // to check priority

            }

            DownloadManagerListener listener = new DownloadStateListener();
            manager.addListener(listener);
            manager.resume();
            globalManager.startAllDownloads();

        }
        else{
            core.requestStop();
        }
    }

    public void list(Entry entry) throws Exception {

        // final Metafile metafile = new Metafile(new ByteArrayInputStream(entry.getTorrentFile()));
        entry.setName(Formatter.clean(entry.getName()));
        int i = 0;
        //System.out.println(metafile.getAnnounceList());

        if (entry.getFiles().isEmpty()) {
            Print.line(entry.getInfohash() + "/" + entry.getName());
        } else {
            for (Object elem : entry.getFiles()) {
                i++;
                Map file = (Map) elem;
                List path = (List) file.get(ByteBuffer.wrap("path".getBytes()));
                String pathName = entry.getName();

                Iterator pathIterator = path.iterator();
                while (pathIterator.hasNext()) {
                    byte[] pathElem = ((ByteBuffer) pathIterator.next()).array();
                    pathName += "/" + new String(pathElem);
                }
                //Print.line(entry.getInfohash() + "/" + Formatter.clean(pathName));
                Print.line(i + " " + Formatter.clean(pathName));

            }
        }

        // to show files length
        // Print.line(""+manager.getDiskManagerFileInfoSet().getFiles().length);
    }

    public void download(Entry entry) throws InterruptedException,
            GlobalManagerDownloadRemovalVetoException,
            IOException {

        Print.line("Downloading: " + entry.getName());
        download(entry.getInfohash());
    }

    private void download(String infoHash) throws InterruptedException,
            GlobalManagerDownloadRemovalVetoException,
            IOException {
        File downloadDirectory = new File("."); //Destination directory

        //Start the download of the torrent
        GlobalManager globalManager = core.getGlobalManager();

        DownloadManager manager
                = globalManager.addDownloadManager(ConfigurationManager.TorrentDirectory + infoHash + ".torrent",
                        downloadDirectory.getAbsolutePath());

        DownloadManagerListener listener = new DownloadStateListener();
        manager.addListener(listener);
        globalManager.startAllDownloads();
    }

    public void download(Entry entry, File torFile) throws InterruptedException,
            GlobalManagerDownloadRemovalVetoException,
            IOException {

        Print.line("Downloading: " + entry.getName());
        File downloadDirectory = new File("."); //Destination directory

        //Start the download of the torrent
        GlobalManager globalManager = core.getGlobalManager();

        DownloadManager manager
                = globalManager.addDownloadManager(torFile.getCanonicalPath(),
                        downloadDirectory.getAbsolutePath());

        DownloadManagerListener listener = new DownloadStateListener();
        manager.addListener(listener);
        globalManager.startAllDownloads();

    }
}