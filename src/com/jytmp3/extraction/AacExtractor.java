package com.jytmp3.extraction;

import com.jytmp3.BitHelper;
import com.jytmp3.MemoryStream;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicReference;

import static com.jytmp3.BitHelper.*;


/**
 * Created by Sp0x on 9/27/2014.
 */
public class AacExtractor extends AudioExtractor {

    int aacProfile;
    int sampleRateIndex;

    public AacExtractor(String path) throws FileNotFoundException {
        super(path);
    }

    @Override
    public void dispose() throws IOException {
        videoStream.dispose();
    }

    @Override
    public void writeChunk(byte[] chunk, long timeStamp) throws AudioExtractionException, IOException {
        if(chunk.length < 1)
                return;

        if(chunk[0]==0) {
            if (chunk.length < 3)
                return;
            BigInteger bits = genBigint(BigEndianBitConverter.ToUInt16(chunk, 1)).shiftLeft(48);
            AtomicReference<BigInteger> hBits = new AtomicReference<BigInteger>(bits);
            aacProfile = BitHelper.read(hBits, 5) - 1;
            sampleRateIndex = BitHelper.read(hBits, 4);
            channelConfig = BitHelper.read(hBits, 4);

            if (aacProfile < 0 || aacProfile > 3)
                throw new AudioExtractionException("Unsupported AAC profile.");
            if (sampleRateIndex > 12)
                throw new AudioExtractionException("Invalid AAC sample rate index.");
            if (channelConfig > 6)
                throw new AudioExtractionException("Invalid AAC channel configuration.");
        }
        else {
            //' Audio data
            int dataSize = chunk.length - 1;
            BigInteger bits = null;
            AtomicReference<BigInteger> hBits = new AtomicReference<BigInteger>(bits);
            //Reference: WriteADTSHeader from FAAC's bitstream.c
            BitHelper.write(hBits, 12, 0xFFF);
            BitHelper.write(hBits, 1, 0);
            BitHelper.write(hBits, 2, 0);
            BitHelper.write(hBits, 1, 1);
            BitHelper.write(hBits, 2, aacProfile);
            BitHelper.write(hBits, 4, sampleRateIndex);
            BitHelper.write(hBits, 1, 0);
            BitHelper.write(hBits, 3, channelConfig);
            BitHelper.write(hBits, 1, 0);
            BitHelper.write(hBits, 1, 0);
            BitHelper.write(hBits, 1, 0);
            BitHelper.write(hBits, 1, 0);
            BitHelper.write(hBits, 13, 7 + dataSize);
            BitHelper.write(hBits, 11, 0x7FF);
            BitHelper.write(hBits, 2, 0);
            videoStream.write(getBytes(bits), 1, 7);
            videoStream.write(chunk, 1, dataSize);
        }
    }


}
