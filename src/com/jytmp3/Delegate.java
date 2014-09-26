package com.jytmp3;
import java.lang.reflect.*;
import java.io.*;


/**
 * Created by Sp0x on 9/25/2014.
 */
public class Delegate
{
    public static void runa() throws Exception
    {
        String[] list= {"to","be","or","not","to","be"};
        Method m1 = Delegate.class.getMethod("toConsole",
                new Class[] {String.class});
        display(m1, list);
        Method m2 = Delegate.class.getMethod("toFile",
                new Class[] {String.class});
        display(m2, list);
    }

    public static void toConsole (String str)
    {
        System.out.print(str+" ");
    }

    public static void toFile (String s)
    {
        File f = new File("delegate.txt");
        try{
            PrintWriter fileOut =
                    new PrintWriter(new FileOutputStream(f));
            fileOut.write(s);
            fileOut.flush();
            fileOut.close();
        }catch(IOException ioe) {}
    }

    public static void display(Method m, String[] list)
    {
        for(int k = 0; k < list.length; k++) {
            try {
                Object[] args = {new String(list[k])};
                m.invoke(null, args);
            }catch(Exception e) {}
        }
    }
}