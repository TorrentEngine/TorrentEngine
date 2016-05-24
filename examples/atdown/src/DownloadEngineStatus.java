

import torrentlib.Formatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;

import connect.peer.PEPeer;

import torrentlib.AzureusCoreException;
import torrentlib.AzureusCoreFactory;
import xfer.download.DownloadManager;
import torrentlib.TorrentEngineCore;
import stdlib.util.Print;

public class DownloadEngineStatus implements Runnable {

    @Override
    public void run() {
        try {
            //Print.line("Thread Running");
            boolean downloadCompleted = false;
            while (!downloadCompleted) {
                TorrentEngineCore core = AzureusCoreFactory.getSingleton();
                List<DownloadManager> managers = core.getGlobalManager().getDownloadManagers();

                if (managers.size() < 1) {
                    Print.line("Download Halted!");
                    downloadCompleted = true;

                    try {
                        core.requestStop();
                    } catch (AzureusCoreException aze) {
                        Print.line("Could not end session gracefully - forcing exit.....");
                        core.stop();
                    }

                    break;
                }

                // maybe in the future we allways try ourself?
//					core.getGlobalManager().getDownloadManagers().get(0).getPeerManager()
//					.addPeer("127.0.0.1", 6801, 6801, false, null);
                String peers = getPeerString(core);

                long totalReceivedRate = 0;
                long totalSize = 0;
                long totalRemaining = 0;

                for (DownloadManager man : managers) {

                    try {
                        totalRemaining += man.getDiskManager().getRemainingExcludingDND();

                        //totalRemaining += man.getDiskManager().getRemaining();

                        totalReceivedRate += man.getStats().getDataReceiveRate();

                        totalSize += man.getSize();

                    } catch (Exception e) {
                        System.out.println("Error with stats list " + man.getDisplayName());
                    }
                }

                int terminalWidth = stdlib.jline.TerminalFactory.get().getWidth();

                for (int i = 0; i < terminalWidth; i++) {
                    Print.string("\b");
                }

                for (int i = 0; i < terminalWidth; i++) {
                    Print.string(" ");
                }

                Print.string("\r");

                // There is only one in the queue.
                Print.string(String.format("%." + (terminalWidth - 1) + "s", Formatter.humanReadableByteCount(totalReceivedRate, true) + "/s "
                        + Formatter.humanReadableByteCountRatio(totalSize - totalRemaining, totalSize, true) + "/"
                        + +((int) ((totalSize - totalRemaining) / (totalSize * 1.0) * 100)) + "%, "
                        + peers));

                // There is only one in the queue.
//					DownloadManager man = managers.get(0);
//					Print.string(Main.humanReadableByteCount(man.getStats().getDataReceiveRate(), true) + "/s " +
//							Main.humanReadableByteCountRatio(man.getSize() - man.getDiskManager().getRemainingExcludingDND(), man.getSize(),true) + "/" +
//							+ (man.getStats().getCompleted() / 10.0) + "%, "
//							+ man.getNbSeeds() + " Mirrors " + peers.toString());
//					downloadCompleted = man.isDownloadComplete(true);
//					Main.print("\r");
                // Check every 1 seconds on the progress
                Thread.sleep(1500);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


    public static String getPeerString(TorrentEngineCore core) throws NamingException {

        List<String> peers = new ArrayList<>();
        List<DownloadManager> managers = core.getGlobalManager().getDownloadManagers();

        final Map<String, Long> rawPeers = new HashMap<>();
        final Map<String, String> peerType = new HashMap<>();

        for (DownloadManager m : managers) {
            try {
                for (PEPeer p : m.getPeerManager().getPeers()) {

                    Long speed = rawPeers.get(p.getIPHostName());

                    Long speedLocal = p.getStats().getDataReceiveRate();

                    if (speed != null) {
                        speed = speed + speedLocal;
                    } else {
                        speed = speedLocal;
                    }

                    String iphostname = p.getIPHostName();
                    rawPeers.put(iphostname, speed);

                    String prot = p.getProtocol();
//						if (prot.contains("HTTP")){
//							prot = "http";
//						}else if (prot.contains("FTP")){
//							prot = "ftp";
//						}else{
//							prot = "";
//						}

                    if (prot.contains("TCP")) {
                        prot = "";
                    }

                    peerType.put(iphostname, prot);
                }
            } catch (Exception e) {
                System.out.println("Error with peer list " + m.getDisplayName());
            }
        }

        List<String> tosort = new ArrayList<>(rawPeers.keySet());
        Collections.sort(tosort, new Comparator<String>() {

            @Override
            public int compare(String o1, String o2) {

                long o1r = rawPeers.get(o1);
                long o2r = rawPeers.get(o2);

                if (o2r > o1r) {
                    return 1;
                } else if (o2r == o1r) {
                    return 0;
                } else {
                    return -1;
                }
            }
        });

        int count = 0;
        for (String opstring : tosort) {

            long dlrate = rawPeers.get(opstring);

            //if (dlrate != 0){
            count++;

            String pstring = tryForDNSName(opstring);

            // only show some peers but show all edu
            if (!(count > 3) || pstring.contains(".edu")) {

                String type = peerType.get(opstring);

                // add space
                if (!"".equals(type)) {
                    type = " " + type;
                }

                pstring = pstring + " " + Formatter.humanReadableByteCount(dlrate, true) + "/s" + type;

                peers.add(pstring);

            }

        }

//			for (int i = 0; i < peers.size() ; i++){
//
//				peers.set(i, peers.get(i) + " " + peerType.get);
//			}
        return tosort.size() + " Mirrors " + peers.toString();

    }
    public static String tryForDNSName(String pstring) throws NamingException {

        if (!hasAlpha(pstring)) {
            pstring = getRevName(pstring);

        }

        //check if dns resolved
        if (hasAlpha(pstring)) {
            // get rid of last .
            if (pstring.length() == pstring.lastIndexOf('.') + 1) {
                pstring = pstring.substring(0, pstring.length() - 1);
            }

            // get end of dns
            if (pstring.contains(".com.")) {
                pstring = pstring.substring(pstring.lastIndexOf('.', pstring.lastIndexOf(".com.") - 1) + 1);
            } else if (pstring.contains(".edu.")) {
                pstring = pstring.substring(pstring.lastIndexOf('.', pstring.lastIndexOf(".edu.") - 1) + 1);
            } else if (pstring.contains(".org.")) {
                pstring = pstring.substring(pstring.lastIndexOf('.', pstring.lastIndexOf(".org.") - 1) + 1);
            } else {
                pstring = pstring.substring(pstring.lastIndexOf('.', pstring.lastIndexOf('.') - 1) + 1);
            }
        }

        return pstring;
    }

    public static String getRevName(String oipAddr) throws NamingException {

        String ipAddr = oipAddr;
        try {
            Properties env = new Properties();
            env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.dns.DnsContextFactory");
            InitialDirContext idc = new InitialDirContext(env);

            String revName = null;
            String[] quads = ipAddr.split("\\.");

            //StringBuilder would be better, I know.
            ipAddr = "";

            for (int i = quads.length - 1; i >= 0; i--) {
                ipAddr += quads[i] + ".";
            }

            ipAddr += "in-addr.arpa.";
            Attributes attrs = idc.getAttributes(ipAddr, new String[]{"PTR"});
            Attribute attr = attrs.get("PTR");

            if (attr != null) {
                revName = (String) attr.get(0);
            }

            return revName;
        } catch (Exception e) {

            return oipAddr;
        }

    }

    public static boolean hasAlpha(String name) {
        char[] chars = name.toCharArray();

        for (char c : chars) {
            if (Character.isLetter(c)) {
                return true;
            }
        }

        return false;
    }

}