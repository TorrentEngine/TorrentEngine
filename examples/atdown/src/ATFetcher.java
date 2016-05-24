

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;

import au.com.bytecode.opencsv.CSVReader;
import controller.config.ConfigurationManager;
import torrentlib.models.Collection;
import torrentlib.models.Entry;

/**
 * Created by nogueira on 7/2/14.
 */
public class ATFetcher {

    private Logger logger;

    /**
     *
     * @param logger
     */
   public ATFetcher() {
      logger = new Logger(ConfigurationManager.TorrentDirectory + "log.atlogger", Logger.LogLevel.Error);
   }

    public ArrayList<Collection> getCollections() {

        ArrayList<Collection> collections = new ArrayList<>();

        try {
            // create connection to AT
            logger.log("local string BAD!!", Logger.LogLevel.Debug);
            logger.log("Opening connection to AT Getting collections", Logger.LogLevel.Info);

            String uri = "http://www.academictorrents.com/collections.php?format=.csv";
            logger.log("uri: " + uri, Logger.LogLevel.Debug);
            URI collections_uri = new URI(uri);
            URLConnection collections_con = collections_uri.toURL().openConnection();

            // reader content from connection and create collection
            CSVReader reader = new CSVReader(new InputStreamReader(collections_con.getInputStream()));
            //skip csv header
            String[] line = reader.readNext();
            while ((line = reader.readNext()) != null) {
                Collection collection = new Collection(line[0], line[1], Integer.parseInt(line[2]), Long.parseLong(line[3]));
                collection.setTorrents(getCollectionEntries(line[1]));
                collections.add(collection);
                logger.log("Added collection to collections", Logger.LogLevel.Debug);
            }
            reader.close();

        } catch (URISyntaxException e) {
            logger.log(e.getMessage() + e.getInput() + e.getReason() + e.getIndex(), Logger.LogLevel.Error);
        } catch (MalformedURLException e) {
            logger.log(e.getMessage(), Logger.LogLevel.Error);
        } catch (IOException e) {
            logger.log(e.getMessage(), Logger.LogLevel.Error);
            // try { Thread.sleep(500); } catch(Exception ex) {}
        } catch (NumberFormatException e) {
            logger.log("Parse Error" + e.getMessage(), Logger.LogLevel.Error);
        } catch (Exception e) {
            logger.log("Unknown Exception in fetcher getting collections", Logger.LogLevel.Error);
        }

        return collections;
    }

    public HashMap<String, Entry> getCollectionEntries(String urlname) {

        HashMap<String, Entry> entries = new HashMap<>();

        try {
            // create connection to AT
            logger.log("local string BAD!!", Logger.LogLevel.Debug);
            logger.log("Opening connection to AT Getting collection entries", Logger.LogLevel.Info);

            String uri = "http://www.academictorrents.com/collection/" + urlname + ".csv";
            logger.log("uri: " + uri, Logger.LogLevel.Debug);
            URI collections_uri = new URI(uri);
            URLConnection collections_con = collections_uri.toURL().openConnection();

            // reader content from connection and create collection
            CSVReader reader = new CSVReader(new InputStreamReader(collections_con.getInputStream()));
            //skip csv header
            String[] line = reader.readNext();
            while ((line = reader.readNext()) != null) {
                Entry entry = new Entry();

                entry = new Entry(line[0], line[1], line[2], Long.parseLong(line[3]), Integer.parseInt(line[4]),
                        Integer.parseInt(line[5]), Integer.parseInt(line[6]), Long.parseLong(line[7]), Long.parseLong(line[8]));

                entries.put(line[2], entry);
                logger.log("Added entry to collection", Logger.LogLevel.Debug);
            }
            
            reader.close();
        } catch (URISyntaxException e) {
            logger.log(e.getMessage() + e.getInput() + e.getReason() + e.getIndex(), Logger.LogLevel.Error);
        } catch (MalformedURLException e) {
            logger.log(e.getMessage(), Logger.LogLevel.Error);
        } catch (IOException e) {
            logger.log(e.getMessage(), Logger.LogLevel.Error);
        } catch (NumberFormatException e) {
            logger.log("Parse Error" + e.getMessage(), Logger.LogLevel.Error);
        } catch (Exception e) {
            logger.log("Unknown Exception in fetcher getting collections", Logger.LogLevel.Error);
        }

        return entries;
    }
}