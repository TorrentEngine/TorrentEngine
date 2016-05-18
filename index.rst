.. title:: Torrents Engine

******************
Torrents Engine
******************

============
Introduction
============

The Torrents Engine is a software library that provides internet torrent download and peer functionality.


|  Contributors:
|      Dr. Joseph Paul Cohen
|      Henry Z. Lo (cofounder)
|      Greg McPherran (Refactoring to Engine, Module/Code Structure, and TorrentEngine API Exposure Layer)
|      Alpesh Kothari (Refactoring to Engine and Specific File Download)


==================
Using the Software
==================

The software is developed in Java and is available at 
https://github.com/wiperz1789/Torrent-Engine/tree/master

The software can be used by any Java application and the available features of the software are provided below.

- `TorrentEngineCore <https://github.com/wiperz1789/Torrent-Engine/blob/master/index.rst#the-torrentenginecore-api-class-provides-the-following-methods>`_

- `GlobalManager  <https://github.com/wiperz1789/Torrent-Engine/blob/master/index.rst#the-globalmanager-api-class-provides-the-following-methods>`_

- `DownloadManager <https://github.com/wiperz1789/Torrent-Engine/blob/master/index.rst#the-downloadmanager-api-class-provides-the-following-methods>`_

- `DiskManager <https://github.com/wiperz1789/Torrent-Engine/blob/master/index.rst#the-diskmanager-api-class-provides-the-following-methods-static>`_

- `TorrentEngine <https://github.com/wiperz1789/Torrent-Engine/blob/master/index.rst#the-torrentengine-api-class-provides-the-following-methods-static>`_



"""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""
**The TorrentEngineCore "API" class provides the following methods:**
"""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""


^^^^^^^^^^^^
void start()
^^^^^^^^^^^^
|  **Description:** Starts the main core of the engine
|

Examples::

         TorrentEngineCore core= AzureusCoreFactory.create();
         core.start();


^^^^^^^^^^^^^
void stop()
^^^^^^^^^^^^^
|  **Description:** If requestStop() fails to stop the core then, force stop is called.
|

Examples::

        TorrentEngineCore core= AzureusCoreFactory.create();
          core.stop();    


^^^^^^^^^^^^^^^^^^
boolean canStart()
^^^^^^^^^^^^^^^^^^
|  **Description:** Checks whether core can be started or not.
|


^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
static boolean isCoreAvailable()
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
|  **Description:** Returns true if core is available, false otherwise.


^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
static boolean isCoreRunning()
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
|  **Description:** Returns true if core is running, false otherwise.
|


^^^^^^^^^^^^^^^^^^^^^^
boolean isInitThread()
^^^^^^^^^^^^^^^^^^^^^^
|  **Description:** Returns true if thread is initialized already, else false.
|


^^^^^^^^^^^^^^^^^^
void requestStop()
^^^^^^^^^^^^^^^^^^
|  **Description:** Normal core stop request.
|




""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""
**The GlobalManager "API" class provides the following methods:**
""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""  
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
DownloadManager addDownloadManager(String fileName, String savePath)
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
|  **Description:** Add torrent file to download manager.
|  **Parameters:** A filename and path where it needs to be saved.
|

Examples::

	GlobalManager globalManager = core.getGlobalManager();
        globalManager.addDownloadManager(filename,pathToSave);


^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
void addListener(GlobalManagerListener listener)
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
|  **Description:** Add listener to download manager, which initiates the download process.
|  **Parameters:** GlobalManagerListener.
|

Examples::

	DownloadManagerListener listener = new DownloadStateListener();
            manager.addListener(listener);

^^^^^^^^^^^^^^^^^^^^^^^^^^^^
boolean  canPauseDownloads()
^^^^^^^^^^^^^^^^^^^^^^^^^^^^
|  **Description:** Returns true if download can be paused, false otherwise.
|


^^^^^^^^^^^^^^^^^^^^^^^^^^^^
boolean canResumeDownloads()
^^^^^^^^^^^^^^^^^^^^^^^^^^^^
|  **Description:** Returns true if download can be resumed, false otherwise.
|


^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
void pauseDownloadsForPeriod(int seconds)
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
|  **Description:** Pause download for specific period of time.
|  **Parameters:** int.
|



^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
void removeDownloadManager(DownloadManager manager)
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
|  **Description:** Remove download manager.
|  **Parameters:** DownloadManager.
|



^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
DownloadManager getDownloadManager(TOTorrent torrent)
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
|  **Description:** Get download manager for particular torrent.
|  **Parameters:** TOTorrent.
|


^^^^^^^^^^^^^^^^^^^^^^
void resumeDownloads()
^^^^^^^^^^^^^^^^^^^^^^
|  **Description:** To resume downloads for current download manager.
|


^^^^^^^^^^^^^^^^
void saveState()
^^^^^^^^^^^^^^^^
|  **Description:** Save current state of download manager, which can be resumed later.
|


^^^^^^^^^^^^^^^^^^^^^^^^
void startAllDownloads()
^^^^^^^^^^^^^^^^^^^^^^^^
|  **Description:** Starts download from all download manager
|

Examples::

        globalManager.startAllDownloads();



^^^^^^^^^^^^^^^^^^^^^^^
void stopAllDownloads()
^^^^^^^^^^^^^^^^^^^^^^^
|  **Description:** Stops download process from all download manager
|

Examples::

        globalManager.stopAllDownloads();

^^^^^^^^^^^^^^^^^^^^^^^^
void stopGlobalManager()
^^^^^^^^^^^^^^^^^^^^^^^^
|  **Description:** Stops global manager.
|



""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""
**The DownloadManager "API" class provides the following methods:**
""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""

^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
void addDiskListener(DownloadManagerDiskListener listener)
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
|  **Description:** DiskListener monitors the disk operations.
|  **Parameters:** DownloadManagerDiskListener.
|


^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
void addListener(DownloadManagerListener listener)
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
|  **Description:** Add listener to specific download manager, which initiates the download process.
|  **Parameters:** DownloadManagerListener.
|


^^^^^^^^^^^^^^^^^^^^^^^^^
void addPeer(PEPeer peer)
^^^^^^^^^^^^^^^^^^^^^^^^^
|  **Description:** Adds peers to current DownloadManager.
|  **Parameters:** PEPeer.
|


^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
void addPeerListener(DownloadManagerPeerListener listener)
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
|  **Description:** Add listener to peers to current DownloadManager.
|


^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
File getSaveLocation()
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
|  **Description:** Returns the location where file is saved.
|


^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
void saveResumeData()  
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
|  **Description:** Save the data after resume is initiated.
|  **Parameters:** TOTorrent.
|


^^^^^^^^^^^^^^^^^^^^^^
void startDownload() 
^^^^^^^^^^^^^^^^^^^^^^
|  **Description:** Starts the download for loaded download manager.


"""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""
**The DiskManager "API" class provides the following methods:**
"""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""

^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
DiskManagerFileInfo[] getFiles()
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
|  **Description:** Returns array all the files described in torrent meta-data.
|
	

^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
DiskManagerFileInfoSet getFileSet()
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
|  **Description:** Returns set all the files in torrents meta-data.

|

^^^^^^^^^^^^^^^^^^^^^^^^^^
long getSizeExcludingDND()
^^^^^^^^^^^^^^^^^^^^^^^^^^
|  **Description:** Returns the overall size of files, excluding the size of the files which won't be downloaded.
|


^^^^^^^^^^^^
void start()
^^^^^^^^^^^^
|  **Description:** Turns on the downloading process.
|


^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
boolean stop(boolean closing)
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
|  **Description:** Stops downloading of files.
|  **Parameters:** boolean.
 

^^^^^^^^^^^^^^^^^^^^
boolean filesExist()
^^^^^^^^^^^^^^^^^^^^
|  **Description:** Returns true if file exists, otherwise false
|
 

""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""
The TorrentEngine "API" class provides the following methods (static):
""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""

^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
void download(String item)
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
|  **Description:** Download the specified torrent or torrent collection.
|  **Parameters:**
|  		**item:** A file, url, or hash of a torrent or the name of a torrent collection.
|

Examples::

	download("551952d08103200cf5034fb74adf71643aa0c643");
	download("http://umb.edu/Astronomy_Journal_2015.torrent");


^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
void downloadFiles(String item, String[ ] fileNumbers)
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
|  **Description:** Download the specified files of the torrent or torrent collection.
|  **Parameters:**
|  		**item:** A file, url, or hash of a torrent or the name of a torrent collection.
|  		**fileNumbers:** A string array of the numbers (1-based) of the files to download.
|

Example::

	download("Crater_Analysis_2015.torrent", new String[] {"5", "12", "27"});

   
^^^^^^^^^^^^^^^^^^^^^^
void list(String item)
^^^^^^^^^^^^^^^^^^^^^^
|  **Description:** List the files of a torrent or the torrents of a collection.
|  **Parameter:**
|  		**item:** A file, url, or hash of a torrent or the name of a torrent collection.
|

Example::

	list("noaa datasets");
	list("551952d08103200cf5034fb74adf71643aa0c643");
