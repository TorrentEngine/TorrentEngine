.. title:: Torrents Engine

******************
Torrents Engine
******************

============
Introduction
============

The Torrents Engine is a software library that provides internet torrent download and peer functionality.


|  Contributors:
|      Greg McPherran 
|      Alpesh Kothari 


==================
Using the Software
==================

The software is developed in Java and is available at 
https://github.com/wiperz1789/Torrent-Engine/tree/master

The software can be used by any Java application and the available features of the software are provided below.

"""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""
The GlobalManager "API" class provides the following methods:
"""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
DownloadManager addDownloadManager(String fileName, String savePath)
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
|  **Description:** Add torrent file to download manager.
|  **Parameters:** A filename and path where needs to be saved.


^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
void addListener(GlobalManagerListener listener)
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
|  **Description:** Add listener to download manager, which initiates the download process.
|  **Parameters:** GlobalManagerListener.


^^^^^^^^^^^^^^^^^^^^^^^^^^^^
boolean  canPauseDownloads()
^^^^^^^^^^^^^^^^^^^^^^^^^^^^
|  **Description:** To check whether downlaod can be paused.


^^^^^^^^^^^^^^^^^^^^^^^^^^^^
boolean canResumeDownloads()
^^^^^^^^^^^^^^^^^^^^^^^^^^^^
|  **Description:** To check whether downlaod can be resumed.


^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
void pauseDownloadsForPeriod(int seconds)
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
|  **Description:** Pause download for specific period of time.
|  **Parameters:** int.



^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
void removeDownloadManager(DownloadManager manager)
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
|  **Description:** Remove download manager.
|  **Parameters:** DownloadManager.



^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
DownloadManager getDownloadManager(TOTorrent torrent)
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
|  **Description:** Get download manager for particular torrent.
|  **Parameters:** TOTorrent.


^^^^^^^^^^^^^^^^^^^^^^
void resumeDownloads()
^^^^^^^^^^^^^^^^^^^^^^
|  **Description:** To resume download of torrent.


^^^^^^^^^^^^^^^^
void saveState()
^^^^^^^^^^^^^^^^
|  **Description:** Save current state of download, which can be used later to resume.


^^^^^^^^^^^^^^^^^^^^^^^^
void startAllDownloads()
^^^^^^^^^^^^^^^^^^^^^^^^
|  **Description:** Starts download from all download manager


^^^^^^^^^^^^^^^^^^^^^^^
void stopAllDownloads()
^^^^^^^^^^^^^^^^^^^^^^^
|  **Description:** Stops download from all download manager


^^^^^^^^^^^^^^^^^^^^^^^^
void stopGlobalManager()
^^^^^^^^^^^^^^^^^^^^^^^^
|  **Description:** Stops global manager.


^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
void addListener(GlobalManagerListener listener)
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
|  **Description:** Add listener to download manager, which initiates the download process.
|  **Parameters:** GlobalManagerListener.


"""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""
The DownloadManager "API" class provides the following methods:
"""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""

^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
void addDiskListener(DownloadManagerDiskListener listener)
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
|  **Description:** DiskListener monitors the disk operations.
|  **Parameters:** DownloadManagerDiskListener.


^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
void addListener(DownloadManagerListener listener)
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
|  **Description:** Add listener to specific download manager, which initiates the download process.
|  **Parameters:** DownloadManagerListener.


^^^^^^^^^^^^^^^^^^^^^^^^^
void addPeer(PEPeer peer)
^^^^^^^^^^^^^^^^^^^^^^^^^
|  **Description:** Adds peers to current DownloadManager.
|  **Parameters:** PEPeer.



^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
void addPeerListener(DownloadManagerPeerListener listener)
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
|  **Description:** Add listener to peers to current DownloadManager.


^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
File getSaveLocation()
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
|  **Description:** Returns the location where file is saved.


^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
void saveResumeData()  
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
|  **Description:** Save the data after resume is initiated.
|  **Parameters:** TOTorrent.


^^^^^^^^^^^^^^^^^^^^^^
void startDownload() 
^^^^^^^^^^^^^^^^^^^^^^
|  **Description:** Starts the download for loaded download manager.


""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""
The DiskManager "API" class provides the following methods (static):
""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""

^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
DiskManagerFileInfo[] getFiles()
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
|  **Description:** Returns array all the files described in torrent meta-data.
	


^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
DiskManagerFileInfoSet getFileSet()
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
|  **Description:** Returns set all the files in torrents meta-data.


^^^^^^^^^^^^^^^^^^^^^^^^^^
long getSizeExcludingDND()
^^^^^^^^^^^^^^^^^^^^^^^^^^
|  **Description:** Returns the overall size of files, excluding the size of the files which won't be downloaded.


^^^^^^^^^^^^
void start()
^^^^^^^^^^^^
|  **Description:** Turns on the downloading process.
 

^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
boolean stop(boolean closing)
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
|  **Description:** Stops downloading of files.
|  **Parameters:** boolean.
 

^^^^^^^^^^^^^^^^^^^^
boolean filesExist()
^^^^^^^^^^^^^^^^^^^^
|  **Description:** Returns true if file exists, otherwise false
 

 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 

""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""
The TorrentEngine "API" class provides the following methods (static):
""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""

^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
void download(String item)
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
|  **Description:** Download the specified torrent or torrent collection.
|  **Parameters:**
|  		**item:** A file, url, or hash of a torrent or the name of a torrent collection.

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

Example::

	download("Crater_Analysis_2015.torrent", new String[] {"5", "12", "27"});

   
^^^^^^^^^^^^^^^^^^^^^^
void list(String item)
^^^^^^^^^^^^^^^^^^^^^^
|  **Description:** List the files of a torrent or the torrents of a collection.
|  **Parameter:**
|  		**item:** A file, url, or hash of a torrent or the name of a torrent collection.

Example::

	list("noaa datasets");
	list("551952d08103200cf5034fb74adf71643aa0c643");
