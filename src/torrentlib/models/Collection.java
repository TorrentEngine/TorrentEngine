package torrentlib.models;

/**
 * Created by nogueira on 6/19/14.
 */

import java.util.HashMap;

/**
 *
 */
public class Collection {

    private String name;
    private String urlname;
    private int torrent_count;
    private long total_size_bytes;
    private boolean mirrored = false;
    private HashMap<String, Entry> torrents;

    // needed for hibernate
    /**
     *
     */
    public Collection() {
    }

    /**
     *
     * @param collection_name
     * @param urlname
     * @param torrent_count
     * @param total_size
     */
    public Collection(String collection_name, String urlname, int torrent_count, long total_size) {
        this.name = collection_name;
        this.urlname = urlname;
        this.torrent_count = torrent_count;
        this.total_size_bytes = total_size;
    }

    /**
     *
     * @param urlname
     */
    public Collection(String urlname){

    }

    /**
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @return
     */
    public String getUrlname() {
        return urlname;
    }

    /**
     *
     * @return
     */
    public int getTorrent_count() {
        return torrent_count;
    }

    /**
     *
     * @return
     */
    public long getTotal_size_bytes() {
        return total_size_bytes;
    }

    /**
     *
     * @return
     */
    public boolean isMirrored() { return mirrored; }

    /**
     * @param mirrored
     * @return
     */
    public void setMirrored(boolean mirrored) { this.mirrored = mirrored; }

    /**
     *
     * @return
     */
    public HashMap<String, Entry> getTorrents() { return torrents; }

    /**
     *
     * @param torrents
     */
    public void setTorrents(HashMap<String, Entry> torrents) {
        this.torrents = torrents;
    }
}
