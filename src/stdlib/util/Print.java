package stdlib.util;

import java.io.PrintStream;

public class Print
{
   private static final PrintStream stdout = System.out;

   public static void line(String s)
   {
      stdout.println(s);
   }

   public static void string(String s)
   {
      stdout.print(s);
   }
}
