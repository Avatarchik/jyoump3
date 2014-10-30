package com.jytmp3;

import jdk.nashorn.internal.runtime.regexp.joni.Regex;
import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Sp0x on 10/16/2014.
 */
public class Utils {

    public static String matchGroup(Pattern rx, String input, Integer group){
        Matcher rMtc = rx.matcher(input);
        if(rMtc.matches()) {
            return rMtc.group(group).toString();// rMtc.Groups(group).Value
        }
        return null;
    }

    public static String dldUrlTxt(String url) throws IOException {
        InputStream input = new URL(url).openConnection().getInputStream();
        return IOUtils.toString(input);
    }
    public static byte[] dldUrlData(String url) throws IOException{
        InputStream input = new URL(url).openConnection().getInputStream();
        return IOUtils.toByteArray(input);
    }
    public static String sprintf(String pattern, String strFnName) {
        return String.format(pattern, strFnName);
    }
    public static String[] removeNulls(String strs[])
    {
        List<String> ops = new ArrayList<String>();
        for(String op:strs){
            if(op!=null && op.length()>0) ops.add(op);
        }
        return (String[])ops.toArray();
    }
    public static <T> T[] aggregate(T[] elements, TransformerAction tx){
      for(int i=0; i<elements.length; i++){
          tx.aggregate(elements[i]);
      }
        return elements;
    }
    public static <T> String aggregateJoin(T[] elements, TransformerAction tx){
        StringBuilder output = new StringBuilder();
        for(int i=0; i<elements.length; i++){
            output.append(tx.aggregate(elements[i]));
        }
        return output.toString();
    }
}
