.. title:: Torrents Engine

******************
Torrents Engine
******************

============
Introduction
============

The Torrents Engine is a software library that provides internet torrent download and peer functionality. It is designed to provide an
easy to use API for donwloading and listing torrents.

The project was founded by Dr. Joseph Cohen (The University of Massachusetts, Boston)

|  Contributors:
|      Henry Z. Lo (cofounder)
|      Jonathan Nogueira (SmartNode and Architecture)
|      Alpesh Kothari (Refactoring to Engine and Specific File Download)
|      Greg McPherran (Refactoring to Engine, Module/Code Structure, and API Exposure Layer)

==================
Using the Software
==================

The software is developed in Java and is available at 
https://github.com/CompCoder/Torrents-Engine/tree/master

The software can be used by any Java application and the available features of the software are available via the TorrentEngine class. This class provides an "API" (see below) that provides various download and torrent directory listing capabilities including advanced features such as downloading only specific files from a torrent.

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
