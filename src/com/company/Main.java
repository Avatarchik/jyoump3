package com.company;


import org.apache.commons.compress.archivers.zip.ZipEightByteInteger;

import java.math.BigInteger;

import static com.company.BitHelper.*;

// 10, -69
public class Main {


     public static void main(String[] args) {
         byte input[] = {(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff}; // 64 bits, signed long = -1
         byte inputAlt[] = getBytes(0xffffffff, 0xffffffff);
         byte a[], b[], c[];
         BigInteger signed = new BigInteger(input);
         BigInteger unsigned = new BigInteger(1, input);
         BigInteger alt0 = genBigint(0xffffffff, 0xffffffff);
         BigInteger altu = genBigint(0xffffffff, 0xffffffff, false);
         a = alt0.toByteArray();
         b = altu.toByteArray();
         alt0 = alt0.shiftRight(32);
         altu = altu.shiftRight(32);
         a = alt0.toByteArray();
         b = altu.toByteArray();
         b = trimBigint(altu);

         BigInteger alt = unsigned.add(signed);
         input = alt.toByteArray();
         byte[] bUnsigned = unsigned.toByteArray();


        byte[] bits;
        int[] ints;
        bits = getBytes(0xFFFFFEFF, 0xFFFFFFFF);
        BigInteger bz = ZipEightByteInteger.getValue(bits, 0);
        byte[] bibi = bz.toByteArray();
        BigInteger bigi = new BigInteger(bits);
        bits = bigi.toByteArray();
        bigi = bigi.shiftRight(32);
        bits = bigi.toByteArray();
        ints = toUBytes(bits);

        System.out.println(bits[0]);
    }


}
