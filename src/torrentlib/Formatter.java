/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package torrentlib;

/**
 *
 * @author Alpesh
 */
public class Formatter {

    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) {
            return bytes + "B";
        }
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f%sB", bytes / Math.pow(unit, exp), pre);
    }

    public static String clean(String s) {
        return s.replaceAll("[^\\x00-\\x7f]", "");
    }

    public static String humanReadableByteCountRatio(long bytes, long totbytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (totbytes < unit) {
            return bytes + "/" + totbytes + "B";
        }
        int exp = (int) (Math.log(totbytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f/%.1f%sB", bytes / Math.pow(unit, exp), totbytes / Math.pow(unit, exp), pre);
    }
    
    
}
