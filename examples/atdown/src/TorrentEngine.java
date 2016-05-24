import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import javax.xml.bind.DatatypeConverter;
import org.apache.commons.io.IOUtils;
import stdlib.util.Print;
import torrentlib.Cache;
import torrentlib.Formatter;
import torrentlib.data.torrent.Metafile;
import torrentlib.data.util.Utils;
import torrentlib.models.Collection;
import torrentlib.models.Entry;

public class TorrentEngine
{
   private static DownloadEngine downloadEngine = null;

   // download: item can be a file, url, torrent hash, or collection.
   public static void download(String item) throws Exception
   {
      downloadFiles(item, null);
   }

   // downloadFiles: Download specified files in torrent (item).
   // item can be a file, url, torrent hash, or collection.
   // fileNumbers is comma separated list of numbers, null means all files
   public static void downloadFiles(String item, String[] fileNumbers) throws Exception
   {
      if (Utils.isUrl(item))
      {
         downloadUrl(new URL(item), fileNumbers);
      }
      else if (Utils.isHash(item))
      {
         downloadHash(item, fileNumbers);
      }
      else
      {
         File file = null;

         if (Utils.isFile(item, file))
         {
             file= new File(item);
             downloadFile(file, fileNumbers);
               

         }
         else
         {
            Map<String, Entry> collection = getCollection(item);

            if (collection != null && !collection.isEmpty()) {
               downloadCollection(collection, fileNumbers);
            }
            else {
               Print.line("Error fetching collection");
            }
         }
      }
   }

   // list: List contents of item. item can be a file, url, torrent hash, or collection.
   public static void list(String item) throws Exception
   {
      // DownloadEngine downloader;

      if (Utils.isUrl(item))
      {
         listUrl(new URL(item));
      }
      else if (Utils.isHash(item))
      {
         listHash(item);
      }
      else
      {
         File file = null;

         if (Utils.isFile(item, file))
         {
            file= new File(item);

            listFile(file);
         }
         else
         {
            Map<String, Entry> collection = getCollection(item);

            if (collection != null && !collection.isEmpty()) {
               listCollection(collection);
            }
            else {
               Print.line("Error fetching collection");
            }
         }
      }
   }

   public static void listAcademicTorrentsCollections() {
       Print.line("Fetching all collections..");
       ArrayList<Collection> collections = new ATFetcher().getCollections();

       Print.line(String.format("|%-65s|%-25.25s|%6s|%9s|", "url-name", "Name", "count", "total size"));

       for (Collection c : collections) {
           Print.line(String.format("|%-65s|%-25.25s|%6s|%9s|",
                   c.getUrlname(),
                   c.getName(),
                   c.getTorrent_count(),
                   Formatter.humanReadableByteCount(c.getTotal_size_bytes(), true)));
       }
   }

   // <editor-fold desc="API Support" defaultstate="collapsed">

   private static void downloadUrl(URL url, String[] fileNumbers) throws Exception
   {
      downloadTorrent(IOUtils.toByteArray(url), fileNumbers);
   }

   private static void downloadHash(String hash, String[] fileNumbers) throws Exception
   {
      downloadTorrent(Cache.getFromCacheOrDownload(hash), fileNumbers);
   }

   private static void downloadFile(File file, String[] fileNumbers) throws Exception
   {
      if (file.isDirectory())
      {
         Print.line("Specified file is a directory, invalid.");
      }
      else
      {
         if(fileNumbers.length==1)
                getDownloadEngine().download(Utils.getEntry(IOUtils.toByteArray(new FileInputStream(file))), fileNumbers[0],file);
             else if(fileNumbers.length>1)
                getDownloadEngine().downloadMulti(Utils.getEntry(IOUtils.toByteArray(new FileInputStream(file))), fileNumbers,file);
             else if(fileNumbers.length<1)
                getDownloadEngine().download(Utils.getEntry(IOUtils.toByteArray(new FileInputStream(file))),file);
      }
   }

   private static void downloadCollection(Map<String, Entry> collection, String[] fileNumbers) throws Exception
   {
      List<Entry> entries = getCollectionEntries(collection);

      if (fileNumbers == null)
      {
         for (Entry entry : entries) {
            getDownloadEngine().download(entry);
         }
      }
      else if(fileNumbers.length==1)
      {
          for (Entry entry : entries) {
            
                //Print.line(fileNumbers[0] +"to be downloaded");
                    getDownloadEngine().download(entry, fileNumbers[0]);
         }
      }
      else if(fileNumbers.length>1)

      {
         for (Entry entry : entries) {
             
                getDownloadEngine().downloadMulti(entry, fileNumbers);
         }
      }
   }

   private static void downloadTorrent(byte[] torrent, String[] fileNumbers) throws Exception
   {
      if (fileNumbers == null)
      {
         getDownloadEngine().download(Utils.getEntry(torrent));
      }
      else if(fileNumbers.length==1)
      {          
              //  Print.line(fileNumbers[0] +"to be downloaded");
                  getDownloadEngine().download(Utils.getEntry(torrent), fileNumbers[0]);
         
      }
      else if(fileNumbers.length>1)
      {
         getDownloadEngine().downloadMulti(Utils.getEntry(torrent), fileNumbers);
      }
   }

   private static void listUrl(URL url) throws Exception
   {
      listTorrent(IOUtils.toByteArray(url));
   }

   private static void listHash(String hash) throws Exception
   {
      listTorrent(Cache.getFromCacheOrDownload(hash));
   }

   private static void listFile(File file) throws Exception
   {
      if (file.isDirectory())
      {
         Print.line("Specified file is a directory, invalid.");
      }
      else
      {
         listTorrent(IOUtils.toByteArray(new FileInputStream(file)));
      }
   }

   private static void listCollection(Map<String, Entry> collection) throws Exception
   {
      List<Entry> entries = getCollectionEntries(collection);

      for (Entry entry : entries) {
         getDownloadEngine().list(entry);
      }

      getDownloadEngine().shutdown();
   }

   private static void listTorrent(byte[] torrent) throws Exception
   {
      getDownloadEngine().list(Utils.getEntry(torrent));
      getDownloadEngine().shutdown();
   }

   private static Map<String, Entry> getCollection(String str) throws Exception
   {
      return (new ATFetcher()).getCollectionEntries(str);
   }

   private static List<Entry> getCollectionEntries(Map<String, Entry> collection) throws Exception
   {
      List<Entry> collectionEntries = new ArrayList<>();

      int count = 0;
      Print.string("Fetching collection " + count + "/" + collection.size());

      for (Entry entry : collection.values()) {

         count++;

         try {
             // System.out.println(entry.getInfohash());
             byte[] torrent = Cache.getFromCacheOrDownload(entry.getInfohash());

             Metafile meta = new Metafile(new ByteArrayInputStream(torrent));
             String infohash = DatatypeConverter.printHexBinary(meta.getInfoSha1());
             entry.setTorrentFile(torrent);

             if (!entry.getInfohash().equalsIgnoreCase(infohash)) {
                 Print.line(entry.getInfohash());
                 Print.line(infohash);
                 throw new Exception("Collection-Entry Consistancy Error");
             }

             collectionEntries.add(entry);

         } catch (Exception ex) {
             Print.line("Error with entry: " + entry.getInfohash());
         }

         Print.string("\rFetching collection " + count + "/" + collection.size());
      }

      Print.line("");
      return collectionEntries;
   }

   public static DownloadEngine getDownloadEngine() throws Exception
   {
      if (downloadEngine == null)
      {
         downloadEngine = new DownloadEngine();
      }

      return downloadEngine;
   }

   // </editor-fold>
}