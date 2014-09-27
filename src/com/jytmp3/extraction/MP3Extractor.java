package com.jytmp3.extraction;

import com.jytmp3.BitHelper;
import com.jytmp3.MemoryStream;
import com.jytmp3.SeekOrigin;

import java.io.*;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static com.jytmp3.BitHelper.*;


/**
 * Created by Sp0x on 9/26/2014.
 */
public class MP3Extractor extends AudioExtractor{
    //region Variables
    private ByteBuffer chunkBuffer;// As MemoryStream ' List(Of Byte());// As FileStream
    private ByteBuffer bf;
    private List<Long> frameOffsets;
    private List<String> ls_warnings;

    private boolean hasVbrHeader;
    private boolean isVbr;
    private MP3Frame frameInfo;
    private long totalFrameLength;
    private boolean doWriteVbrHeader;
    private boolean delayWrite;
    //endregion


       //region Construction
    public MP3Extractor(String path) throws FileNotFoundException {
        super(path);
        ls_warnings = new ArrayList<String>();
        chunkBuffer = ByteBuffer.allocateDirect(MAX_CHUNK);// allocate 35 megs
        frameOffsets = new ArrayList<Long>();
        delayWrite = true;
    }
    //endregion

    //region Props

   public List<String> getLs_warnings() {
        return ls_warnings;
    }

    public void setLs_warnings(List<String> ls_warnings) {
        this.ls_warnings = ls_warnings;
    }

    //endregion

    //region "Freame writer"
            /*''' <summary>
            ''' Writes the FLV Buffer, by extracting it to a MP3 Frame.
            ''' </summary>
            ''' <param name="chunk">The buffer to write</param>
            ''' <param name="timeStamp">Not used</param>
            ''' <remarks></remarks>*/
    @Override
    public void writeChunk(byte[] chunk, long stamp) throws IOException {
        chunkBuffer.put(chunk, 0, chunk.length);
        parseMp3Frame(chunk);
        if (delayWrite && totalFrameLength >= 65536)
            delayWrite = false;

        if (!delayWrite)
            flush();
    }


    private void flush() throws IOException {
        byte[] tmpBuff = chunkBuffer.array();
        videoStream.write(tmpBuff);
        chunkBuffer.clear();
        chunkBuffer = ByteBuffer.allocate(MAX_CHUNK);
        //chunkBuffer = New MemoryStream() 'Me.chunkBuffer.Clear()
    }
    //endregion

    //region "Disposition"
    @Override
    public void dispose() throws IOException {
        flush();
        if (doWriteVbrHeader) {
            videoStream.mark(0, SeekOrigin.Begin);
            writeVbrHeader(false);
        }
        videoStream.dispose();
    }
    //endregion

    //region "Helpers"
    private static int getFrameDataOffset(int mpegVersion, int channelMode) {
        return 4 + (mpegVersion == 3 ? (channelMode == 3 ? 17 : 32) : (channelMode == 3 ? 9 : 17));
    }

     /* Parses the main information from the frame */
    private BigInteger getMp3FrameInfo(BigInteger header, MP3Frame frame) {
        AtomicReference<BigInteger> hRef = new AtomicReference<BigInteger>(header);
        frame.setMpegVersion((BitHelper.read(hRef, 2))).
            setLayerCount(BitHelper.read(hRef, 2));
        BitHelper.read(hRef, 1);

        frame.setBitRate(BitHelper.read(hRef, 4)).
                setSampleRate(BitHelper.read(hRef, 2)).
                setPadding(BitHelper.read(hRef, 1));
        BitHelper.read(hRef, 1);
        frame.setChannelMode(BitHelper.read(hRef, 2));
        return header;
    }


    /*
    ''' <summary>
            ''' Checks if the given frame is a VBR Header frame.
            ''' </summary>
            ''' <param name="buffer">The buffer for the frame</param>
            ''' <param name="frameOffsets">The list of frame offsets</param>
            ''' <param name="offset">The current offset</param>
            ''' <param name="mpgVersion">MPEG Version</param>
            ''' <param name="chanMode">Channel count</param>
            ''' <param name="mp3Extractor">The extractor to modify</param>
            ''' <returns></returns>
            ''' <remarks></remarks>*/
    private static boolean checkForVBRHeader(byte[] buffer, int offset, MP3Frame frame, MP3Extractor mp3Extractor) {
        boolean isVbrHeaderFrame = false;
        if (mp3Extractor.getFrameOffsets().size() == 0) { //'No frames have been found
            // Check for an existing VBR header just to be safe (I haven't seen any in FLVs)
            int hdrOffset = offset + getFrameDataOffset(frame.getMpegVersion(), frame.getChannelMode());
            if (BigEndianBitConverter.ToUInt32(buffer, hdrOffset) == 0x58696E67) {
                isVbrHeaderFrame = true;
                mp3Extractor.delayWrite = false;
                mp3Extractor.hasVbrHeader = true;
            }
        }
        return isVbrHeaderFrame;
    }
    //endregion

    //region "Parsing"

   private void parseMp3Frame(byte[] buffer) throws IOException {
       int offset = 0;
       int length = buffer.length;

       while(length>=4) {

           BigInteger header = genBigint(BigEndianBitConverter.ToUInt32(buffer, offset)).shiftLeft(32);
           AtomicReference<BigInteger> hRef= new AtomicReference<BigInteger>(header);
           MP3Frame newFrame=null;

           if(BitHelper.read(hRef, 11) != 0x7FF)
                break;

            getMp3FrameInfo(header, newFrame);
           if(newFrame.isInvalidFrame())
               break;

           newFrame.parseBitRate();
           newFrame.parseSampleRate();

           int frameLenght = newFrame.getFrameLength();
           if(frameLenght>length)
               break;

           parseHeaderInformation(this, offset, buffer, newFrame);

           frameOffsets.add(totalFrameLength + offset);
           offset += frameLenght;
           length -= frameLenght;
       }

       totalFrameLength += buffer.length;
   }

    private static void parseHeaderInformation(MP3Extractor mp3Extractor, int offset, byte[] buffer, MP3Frame frame) throws IOException {

        boolean isVbrHeaderFrame = checkForVBRHeader(buffer, offset, frame, mp3Extractor);
        if (!isVbrHeaderFrame) {
            if (mp3Extractor.frameInfo.getBitRate() == 0) {
                mp3Extractor.frameInfo = frame;
                mp3Extractor.frameInfo.setFirstFrameHeader(BigEndianBitConverter.ToUInt32(buffer, offset));

            } else if (!mp3Extractor.isVbr) {
                    mp3Extractor.isVbr = true;
                if (!mp3Extractor.hasVbrHeader) {
                    if (mp3Extractor.delayWrite) {
                        mp3Extractor.writeVbrHeader(true);
                        mp3Extractor.doWriteVbrHeader = true;
                        mp3Extractor.delayWrite = false;
                    } else {
                        mp3Extractor.ls_warnings.add("Detected VBR too late, cannot add VBR header.");
                    }
                }
            }
        }
    }

    private void writeVbrHeader(boolean isPlaceholder) throws IOException {
        byte[] buffer = new byte[(MP3Frame.getFrameLength(frameInfo.getMpegVersion(), 64000, frameInfo.getSampleRate(), 0))];
        if (!isPlaceholder) {
            BigInteger header = genBigint(frameInfo.getFirstFrameHeader(), false);
            int dataOffset = getFrameDataOffset(frameInfo.getMpegVersion(), frameInfo.getChannelMode());
            header = bgintAnd(header, 0xFFFE0DFFL);
            header = bgintOr(header ,((frameInfo.getMpegVersion()==3 ? 5 : 8)) << 12);
            BitHelper.copyBytes(buffer, 0, getBytes(header));
            BitHelper.copyBytes(buffer, dataOffset, getBytes(0x58696E67));
            BitHelper.copyBytes(buffer, dataOffset + 4, getBytes(0x7));
            BitHelper.copyBytes(buffer, dataOffset + 8, getBytes(frameOffsets.size()));
            BitHelper.copyBytes(buffer, dataOffset + 12, getBytes(totalFrameLength));

            for (int i = 0; i <= 99; i++) {
                int frameIndex = ((i / 100) * this.frameOffsets.size());
                buffer[dataOffset + 16 + i] = (byte)(this.frameOffsets.get(frameIndex) / this.totalFrameLength * 256.0);
            }
        }
        videoStream.write(buffer);
    }

    public List<Long> getFrameOffsets() {
        return frameOffsets;
    }


    //Region







}
