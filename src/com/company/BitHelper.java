package com.company;

import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.lang3.ArrayUtils;
import static org.apache.commons.lang3.ArrayUtils.*;




/**
 * Created by Sp0x on 9/23/2014.
 */
public class BitHelper {
    private void __construct() {
        // TODO Auto-generated method stub

    }

    public static int Read(AtomicReference<BigInteger> xbig, int len)
    {
        int rBig = xbig.get().shiftRight(64 - len).intValue();
        xbig.set( xbig.get().shiftLeft(len) );
        return rBig;
    }

    public static int Read(byte[] bytes, int offset, int len)
    {
        int startByte = offset / 8;
        int endByte = (offset + len - 1 ) / 8;
        int skipBits = offset % 8;
        byte cByte = bytes[startByte];
        BigInteger bitsBig = new BigInteger(new byte[]{});
        for(int i=0; i <= Math.min(endByte - startByte,  7); i++)
        {
            cByte = bytes[startByte+1];
            bitsBig = bitsBig.or(genBigint(cByte)).shiftLeft(56 - i * 8);
        }
        offset += len;
        //return Read(bitsBig, len);
        return offset;
    }
    private BigInteger flagField = genBigint(0xffffffff, 0xffffffff);



    /*
    Generates a new BigInteger,
     */
    public static BigInteger genBigint(int number){ return genBigint(number,true); }
    public static BigInteger genBigint(int number, boolean signed)
    {
        return signed ? new BigInteger(getBytes(number)) : new BigInteger(1,getBytes(number));
    }
    public static BigInteger genBigint(int i1, int i2)
    {
       return genBigint(i1,i2,true);
    }
    public static BigInteger genBigint(int i1, int i2, boolean signed)
    {
        byte byts[] = getBytes(i1,i2);
        return signed ? new BigInteger(byts) : new BigInteger(1, byts);
    }
    public static byte[] trimBigint(BigInteger bgInt)
    {
        byte[] val = bgInt.toByteArray();
        if(bgInt.signum() == 1)//is unsigned
            val = ArrayUtils.remove(val,0);
        return val;
    }


    public static int[] toUBytes(byte[] arr)
    {
        int[] out = new int[arr.length];
        for(int i=0; i<arr.length; i++)
        { out[i]=cInt(arr[i]);  }
        return out;
    }
    public static byte[] getBytes(int val)
    {
        return new byte[] {
                (byte)(val >> 24),
                (byte)(val >> 16),
                (byte)(val >> 8),
                (byte)val};
    }
    public static byte[] getBytes(int b1, int b2)
    {
        byte[] output =  new byte[] {
                (byte)(b1 >>> 24), (byte)(b1 >>> 16), (byte)(b1 >>> 8), (byte)b1,
                (byte)(b2 >>> 24), (byte)(b2>>>16), (byte)(b2>>>8), (byte)b2};
        return output;
    }
    public static int cInt(byte b)
    {
        return b & 0xFF;
    }


    static void Write(AtomicReference<BigInteger> x, int len, int val)
    {
        BigInteger maskBigInteger = genBigint(0xffffffff,0xffffffff).shiftRight(64-len);
        x.set(x.get().shiftRight(len | val & maskBigInteger.intValue()));
    }
}
