/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package torrentlib;

import controller.config.ConfigurationManager;
import torrentlib.data.torrent.Metafile;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author Alpesh
 */
public class Cache {
    public static byte[] getFromCacheOrDownload(String infohash) throws MalformedURLException, IOException {

        infohash = infohash.toLowerCase();

        try {

            byte[] torrent = IOUtils.toByteArray(new FileInputStream(new File(ConfigurationManager.TorrentDirectory + infohash + ".torrent")));

            // verify it works
            Metafile meta = new Metafile(new ByteArrayInputStream(torrent));

            return torrent;

        } catch (Exception e) {

            byte[] torrent = IOUtils.toByteArray(new URL("http://academictorrents.com/download/" + infohash));

            FileOutputStream fw = new FileOutputStream(ConfigurationManager.TorrentDirectory + infohash + ".torrent");
            IOUtils.copy(new ByteArrayInputStream(torrent), fw);
            fw.flush();
            fw.close();

            return torrent;

        }

    }

}
