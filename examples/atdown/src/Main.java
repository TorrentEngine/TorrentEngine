import controller.config.ConfigurationManager;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import stdlib.util.Print;

public class Main {

    final public static int TIMEOUT = 3000;
    public static boolean keepsharing = false;

    public static void main(String[] args) throws Exception {

        Print.line("Welcome to the Academic Torrents Download tool!");

        try {

            main2(logging_sharing(args).toArray(new String[]{}));
        } catch (Exception e) {

            Print.line("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main2(String[] args) throws Exception {

        // args = new String[]{"82c64b111b07ff855b8966701a13a25512687521", "ls"};
        // Mars Images
        // args = new String[]{"059ed25558b4587143db637ac3ca94bebb57d88d","ls"};
        // args = new String[]{"059ed25558b4587143db637ac3ca94bebb57d88d"};
        // args = new String[]{"059ed25558b4587143db637ac3ca94bebb57d88d","-M","1,6"};
        // args = new String[]{"059ed25558b4587143db637ac3ca94bebb57d88d", "-S", "6"};
        // args = new String[]{"059ed25558b4587143db637ac3ca94bebb57d88d", "-M", "9, 10"};

        // args = new String[]{"7858fdf307d9fe94aeaaeaeadfc554988b80a3ce","ls"};
        // Crater PDF
        // args = new String[]{"8ae530c0c1466ba8feee9914236cc900ad2f708e"};
        // Wiki, huge
        // args = new String[]{"30ac2ef27829b1b5a7d0644097f55f335ca5241b","ls"};
        // big files with lot of images and specific file download testing
        // args = new String[]{"06f73b5ca501194ba1cd3aa918bd801b84ea7050"};
        // args = new String[]{"06f73b5ca501194ba1cd3aa918bd801b84ea7050","ls"};
        // args = new String[]{"06f73b5ca501194ba1cd3aa918bd801b84ea7050","-M","20,30,40"};
         //args = new String[]{"06f73b5ca501194ba1cd3aa918bd801b84ea7050","-S","20"};
        // video files download e046bca3bc837053d1609ef33d623ee5c5af7300
        // args = new String[]{"e046bca3bc837053d1609ef33d623ee5c5af7300"};
        // args = new String[]{"e046bca3bc837053d1609ef33d623ee5c5af7300","ls"};
        // args = new String[]{"e046bsca3bc837053d1609ef33d623ee5c5af7300","-M","20,23,24,140"};
        // args = new String[]{"e046bca3bc837053d1609ef33d623ee5c5af7300","-S","20"};
        // args = new String[]{"joseph-paul-cohen-publications"};
        // args = new String[]{"0f97ce1fa054ad5269bd675e3ad9ad599cd67e66.torrent"};

        // One item and small
        //args = new String[]{"551952d08103200cf5034fb74adf71643aa0c643", "ls"};
        // args = new String[]{"551952d08103200cf5034fb74adf71643aa0c643"};
        // args = new String[]{"551952d08103200cf5034fb74adf71643aa0c643", "-S", "1"};

        // Tiny but may files and no peers available
        // args = new String[]{"95d245e3413f4bb8923b04b277749f041f443f6d","ls"};
        // args = new String[]{"95d245e3413f4bb8923b04b277749f041f443f6d"};
        // args = new String[]{"95d245e3413f4bb8923b04b277749f041f443f6d", "-S", "3"};

        // args = new String[]{"tex"};
        // mactex, one item
        // args = new String[]{"fe3fda68caa7c6a3821121c9f9b35c581b7af913","ls"};
        // args = new String[]{"e54c73099d291605e7579b90838c2cd86a8e9575", "ls"};
        // args = new String[]{"massgis-datasets", "ls"};
        // One item
        // args = new String[]{"d3f859ec025cc730a7e7a0214eaaa15e66db9a24","ls"};
        // args = new String[]{"7fafb101f9c7961f9b840daeb4af43039107ddef","ls"};
        // huge
        // args = new String[]{"journal-of-machine-learning-research","ls"};

        // args = new String[]{"noaa-datasets", "ls"};
        // args = new String[]{"ls"};

        if (checkArgs(args)) {

            if (args[0].equals("ls")) {
               TorrentEngine.listAcademicTorrentsCollections();
            } else {
                downloadImpl(args);
            }
        }
    }

    static boolean checkArgs(String args[]) {

        boolean bStatus = args.length > 0;

        if (!bStatus) {
            throwErr();
        }

        return bStatus;
    }

    static void throwErr() {
        Print.line("Usage: atdown ls // list connections");
        Print.line("Usage: atdown INFOHASH // download entry");
        Print.line("Usage: atdown INFOHASH ls // list contents of entry");
        Print.line("Usage: atdown INFOHASH -S <File_num>// Download specific file using number shown in ls");
        Print.line("Usage: atdown INFOHASH -M <File_num,File_num,....>// Download multiple files ");
        Print.line("Usage: atdown ... -v // verbose");
        Print.line("Usage: atdown ... -s // keep sharing");
        Print.line("Usage: atdown ... -h // Help");

    }

    static void downloadImpl(String args[]) throws Exception {

        if (args.length >= 2) {

            if (null != args[1]) // special op
            {
                //converted if to switch statement
                switch (args[1]) {

                   case "ls":

                      // just list files
                      TorrentEngine.list(args[0]);
                      break;

                    case "-M":

                        if (args.length > 3) {

                            Print.line("Don't use space between numbers. Try again without spaces");
                            throwErr();
                        } 
                        else if(!args[2].contains(",")){
                            Print.line("Use ',' between two numbers. Use '-S' option if you want to download single file");
                            throwErr();
                        }
                        else {
                          // Print.line(Arrays.toString(args[2].split("\\s*,\\s*")));
                           TorrentEngine.downloadFiles(args[0], args[2].split("\\s*,\\s*"));
                        }

                        break;

                    case "-S":

                        if (args[2].contains(",")) {
                            Print.line("There was an error, please check the description");
                            throwErr();

                        } else {
                            //Print.line(args[2]);
                           TorrentEngine.downloadFiles(args[0], new String[] {args[2]});
                        }

                        break;

                    default:
                        // by default download all files under torrent
                        TorrentEngine.download(args[0]);
                        break;
                }
            }
        } else if(args[0].contains(".torrent")){
            
                TorrentEngine.downloadFiles(args[0], new String[] {});
        }
         else {
            // just resume or start download it
            TorrentEngine.download(args[0]);
        }

    }

    static List<String> logging_sharing(String args[]) throws Exception {

        new File(ConfigurationManager.TorrentDirectory).mkdirs();

        List<String> argsl = new ArrayList<>(Arrays.asList(args));

//        if (!argsl.remove("-v")) {
//            hardLogging();
//        }

        if (argsl.remove("-s")) {
            keepsharing = true;
        }
        return argsl;
    }

//    private static void hardLogging() throws FileNotFoundException {
//
//        //Logger.allowLoggingToStdErr(true);
//        System.setOut(new PrintStream(new File(ConfigurationManager.TorrentDirectory + "log.out")));
//        System.setErr(new PrintStream(new File(ConfigurationManager.TorrentDirectory + "log.err")));
//    }
}