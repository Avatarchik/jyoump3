package com.jytmp3.extraction;

import com.jytmp3.TransformerAction;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import static com.jytmp3.Utils.*;
import static org.apache.commons.lang3.ArrayUtils.*;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Sp0x on 10/16/2014.
 */
public class YtSignitureDecoder {
    private static Pattern rxFnName = Pattern.compile("\\.sig\\s*\\|\\|(\\w+)\\(");
    private static Pattern rxFn2PStrInt = Pattern.compile("\\(\\w+,(?<index>\\d+)\\)");
    private static String fnBody = "(?<brace>{([^{}]| ?(brace))*})";
    private static String fnReverse = "{0}:\\bfunction\\b\\(\\w+\\)";
    private static String fnSubstring = "{0}:\\bfunction\\b\\([a],b\\).(\\breturn\\b)?.?\\w+\\.";
    private static String fnSwap = "{0}:\\bfunction\\b\\(\\w+\\,\\w\\).\\bvar\\b.\\bc=a\\b";
    private String PlayerSrc = "";
    Object PlayerVersion;

    public YtSignitureDecoder(String html5ver) throws IOException {
        this.PlayerVersion=html5ver;
        String jsUrl = String.format("http://s.ytimg.com/yts/jsbin/html5player-%s.js", PlayerVersion);
        this.PlayerSrc= dldUrlTxt(jsUrl);
    }



    public String DecipherWithVersion(String cipher) throws InvalidOperationException {
        if(PlayerSrc==null) {
            throw new InvalidOperationException("Player source not found!");
        }
        //'Find "C" in this: var A = B.sig||C (B.s)
        String funcName = rxFnName.matcher(PlayerSrc).group(1).toString();
        String funcPattern = String.format("%s\\(\\w+\\)%s", funcName, fnBody.toString());
        String funcBody = Pattern.compile(funcPattern).matcher(PlayerSrc).group("brace").toString();
        String lines[]= funcBody.split(";");

        String idReverse= "", idSlice = "";
        String idCharSwap = "";
        String strFnName= "";
        String opCodes= "";

        for(String line : ArrayUtils.subarray(lines, 1, lines.length - 2)) {
            if(idReverse!=null && idSlice!=null && idCharSwap!=null) {
                break;
            }
            strFnName = jsGetFunctionFromLine(line);
            String rxSwap = String.format("", strFnName);

            if(Pattern.compile(sprintf(fnReverse, strFnName)).matcher(PlayerSrc).matches()) idReverse = strFnName;
            if(Pattern.compile(sprintf(fnSubstring, strFnName)).matcher(PlayerSrc).matches()) idSlice = strFnName;
            if(Pattern.compile( sprintf(fnSwap, strFnName)).matcher(PlayerSrc).matches()) idCharSwap = strFnName;
        }

        opCodes = getDecipherOps(lines, idReverse, idSlice, idCharSwap);
        return DecipherWithOperations(cipher, opCodes);
    }



    private static String getDecipherOps(String[] dcLines , String fnReverse, String fnSubstr, String fnSwap) {
        String fnName ="";
        String opcodes ="";
        for(String line : ArrayUtils.subarray(dcLines, 1, dcLines.length- 2)) {
            Matcher mFn2PStrInt = rxFn2PStrInt.matcher(line);
            fnName = jsGetFunctionFromLine(line);

            if(mFn2PStrInt.matches() && fnName.equals(fnSwap)) {
                opcodes += "w" + mFn2PStrInt.group("index").toString() + " ";
                continue;
            }

            if(mFn2PStrInt.matches() && fnName.equals(fnSubstr)) {
                opcodes += "s" + mFn2PStrInt.group("index").toString() + " ";
                continue;
            }

            if(fnName.equals(fnReverse)){
                opcodes += "r ";
                continue;
            }
        }
        opcodes = opcodes.trim();
        return opcodes;
    }

    private static String DecipherWithOperations(String cipher,String operations) {
        if (operations == null) return null;
        List<String> ops = new ArrayList<String>();
        for(String op:ops){
            if(op!=null && op.length()>0) ops.add(op);
        }
        String[] output = removeNulls(operations.split(" "));
        return aggregateJoin(output, new TransformerAction<String>() {
            @Override
            public String aggregate(String arg, String next) {
                return ApplyOperation(arg,next);
            }
        });
    }

        private static String ApplyOperation(String cipher , String op){
            switch(op.charAt(0)){
                case 'r':
                    return StringUtils.reverse(cipher);
                case 'w':
                    return SwapFirstChar(cipher, GetOpIndex(op));
                case 's':
                    return cipher.substring(GetOpIndex(op));
                default:
                    throw new NotImplementedException("Couldn't'find the cipher op.");
            }
        }

    private static String jsGetFunctionFromLine(String currentLine  ){
        Pattern matchFunctionReg = Pattern.compile("\\w+\\.(?<functionID>\\w+)\\(");
        Matcher rgMatch = matchFunctionReg.matcher(currentLine);
        String matchedFunction = rgMatch.group("functionID").toString();
        return matchedFunction;
    }

    private static Integer GetOpIndex(String op)
    {
        String parsed  = Pattern.compile(".(\\d+)").matcher(op).group();
        int index= Integer.parseInt(parsed);
        return index;
    }

    private static String SwapFirstChar(String cipher, Integer index ){
        StringBuilder builder = new StringBuilder(cipher);
        builder.setCharAt(0, cipher.charAt(index));
        builder.setCharAt(index, cipher.charAt(0));
        return builder.toString();
    }
}
