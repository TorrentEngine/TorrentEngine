package torrentlib.models;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by nogueira on 7/2/14.
 */
public class Entry {

 

    private enum EntryType {

        Paper, Dataset
    }

    private enum EntryStatus {

        download, seed, would_like, ban, unknown
    }

    private EntryType type;
    private String name;
    private String infohash;
    private long totalSizeBytes;
    private int mirrors;
    private int downloaders;
    private int timesCompleted;
    private long dateAdded;
    private long dateModified;
    private String path;
    private String filename;
    private EntryStatus status;
    private String bibtex;
    private byte[] torrentFile;
    private List files = new LinkedList();
    /**
     * needed for hibernate
     */
    public Entry() {
    }

    /**
     * Used for atdownloader
     * @param infohash
     * @param name
     * @param bibtext
     * @param torrentFile
     * @param files
     */
    public Entry(String infohash,String name,String bibtext, byte[] torrentFile, List files) {
        this.infohash = infohash.toLowerCase();
        this.name=name;
        this.bibtex=bibtext;
        this.torrentFile=torrentFile;
        this.files=files;
        
    }

    /**
     *
     * @param type
     * @param name
     * @param infohash
     * @param total_size_bytes
     * @param mirrors
     * @param downloaders
     * @param times_completed
     * @param date_added
     * @param date_modified
     */
    public Entry(String type, String name, String infohash, long total_size_bytes, int mirrors, int downloaders, int times_completed, long date_added, long date_modified) {

        this.type = EntryType.valueOf(type);
        this.setName(name);
        this.infohash = infohash;
        this.totalSizeBytes = total_size_bytes;
        this.mirrors = mirrors;
        this.downloaders = downloaders;
        this.timesCompleted = times_completed;
        this.dateAdded = date_added;
        this.dateModified = date_modified;
    }

    /**
     *
     * @return
     */
    public EntryType getType() {
        return type;
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
    public String getInfohash() {
        return infohash;
    }

    /**
     *
     * @return
     */
    public int getMirrors() {
        return mirrors;
    }

    /**
     *
     * @return
     */
    public long getTotalSizeByes() {
        return totalSizeBytes;
    }

    /**
     *
     * @return
     */
    public int getDownloaders() {
        return downloaders;
    }

    /**
     *
     * @return
     */
    public int getTimesCompleted() {
        return timesCompleted;
    }

    /**
     *
     * @return
     */
    public long getDateAdded() {
        return dateAdded;
    }

    /**
     *
     * @return
     */
    public long getDateModified() {
        return dateModified;
    }

    /**
     *
     * @return
     */
    public EntryStatus getStatus() {
        return status;
    }

    /**
     *
     * @param status
     */
    public void setStatus(EntryStatus status) {
        this.status = status;
    }

    /**
     *
     * @return
     */
    public String getFilename() {
        return filename;
    }

    /**
     *
     * @param filename
     */
    public void setFilename(String filename) {
        this.filename = filename;
    }

    /**
     *
     * @return
     */
    public String getPath() {
        return path;
    }

    /**
     *
     * @param path
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     *
     * @return
     */
    public byte[] getTorrentFile() {
        return torrentFile;
    }

    /**
     *
     * @param torrentFile
     */
    public void setTorrentFile(byte[] torrentFile) {
        this.torrentFile = torrentFile;
    }

    /**
     *
     * @return
     */
    public String getBibtex() {
        return bibtex;
    }

    /**
     *
     * @param bibtex
     */
    public void setBibtex(String bibtex) {
        this.bibtex = bibtex;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public void setFiles(List files) {
        this.files=files;
    }
    public List getFiles(){
        return files;
    }
}
