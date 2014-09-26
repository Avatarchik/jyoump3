package com.jytmp3.extraction;

import com.jytmp3.MemoryStream;
import com.jytmp3.SeekOrigin;

import java.io.*;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by Sp0x on 9/26/2014.
 */
public class MP3Extractor implements IAudioExtractor {
    public static final int MAX_CHUNK = 1024 * 1000 * 12;
    MemoryStream videoStream;
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


    //region "Static variables"
    public static int[] mpeg1BitRate = new int[]{0, 32, 40, 48, 56, 64,
            80, 96, 112, 128, 160, 192,
            224, 256, 320, 0};
    public static int[] mpeg2XBitRate = new int[]{0, 8, 16, 24, 32, 40,
            48, 56, 64, 80, 96, 112,
            128, 144, 160, 0};
    public static int[] mpeg1SampleRate = new int[]{44100, 48000, 32000, 0};
    public static int[] mpeg20SampleRate = new int[]{22050, 24000, 16000, 0};
    public static int[] mpeg25SampleRate = new int[]{11025, 12000, 8000, 0};
//endregion

    //region Construction
    public MP3Extractor(String path) throws FileNotFoundException {
        FileOutputStream fout = new FileOutputStream(path, false);
        videoStream = new MemoryStream(fout);
        ls_warnings = new ArrayList<String>();
        chunkBuffer = ByteBuffer.allocateDirect(MAX_CHUNK);// allocate 35 megs
        frameOffsets = new ArrayList<Long>();
        delayWrite = true;
    }
    //endregion

    //region Props

    @Override
    public com.jytmp3.MemoryStream getVideoStream() {
        return videoStream;
    }

    @Override
    public void setVideoStream(MemoryStream stream) {
        videoStream = stream;
    }

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

    private static int getFrameLength(int mpegVersion, int bitRate, int sampleRate, int padding) {
        int vFlag = (mpegVersion == 3 ? 144 : 72);
        bitRate = vFlag * bitRate;
        return (int) Math.floor(bitRate / sampleRate) + padding;
    }

    /* ''' <summary>
             ''' Parses the main information from the frame
             ''' </summary>
             ''' <param name="header">The header flag</param>
             ''' <param name="mpegVersion"></param>
             ''' <param name="layer"></param>
             ''' <param name="bitrate"></param>
             ''' <param name="padding"></param>
             ''' <param name="channelMode"></param>
             ''' <param name="sampleRate"></param>
             ''' <remarks></remarks>*/
    private void getMp3FrameInfo(AtomicReference<BigInteger> header, AtomicReference<Integer> mpegVersion, AtomicReference<Integer> layer,
                                 AtomicReference<Integer> bitrate, AtomicReference<Integer> padding,
            AtomicReference<Integer> channelMode, AtomicReference<Integer> sampleRate) {
        mpegVersion.set(BitHelper.read(header, 2));
        layer.set(BitHelper.read(header, 2));
        BitHelper.read(header, 1));
        bitrate.set(BitHelper.read(header, 4));
        sampleRate.set(BitHelper.read(header, 2));
        padding.set(BitHelper.read(header, 1));
        BitHelper.read(header, 1);
        channelMode.set(BitHelper.read(header, 2));
    }

    /* ''' <summary>
             ''' Gets ssamplerate, based on the version of MPEG
             ''' </summary>
             ''' <param name="mpgVersion"></param>
             ''' <param name="sampleRate"></param>
             ''' <remarks></remarks>*/
    private static int calcSampleRate(int mpgVersion, int sampleRate) {
        switch (mpgVersion) {
            case 2:
                sampleRate = mpeg20SampleRate[sampleRate]; break;
            case 3:
                sampleRate = mpeg1SampleRate[sampleRate]; break;
            default:
                sampleRate = mpeg25SampleRate[sampleRate];
        }
        return sampleRate;
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
    private static boolean checkForVBRHeader(byte[] buffer, List<Long> frameOffsets , int offset, int mpgVersion, int chanMode
    , AtomicReference<MP3Extractor> mp3Extractor)
    {
    boolean bx = false;
    if(frameOffsets.size()==0){ //'No frames have been found
        // Check for an existing VBR header just to be safe (I haven't seen any in FLVs)
        int hdrOffset = offset + GetFrameDataOffset(mpgVersion, chanMode);
        if(BigEndianBitConverter.ToUInt32(buffer, hdrOffset)==0x58696E67)
        {
       // ' "Xing"
            isVbrHeaderFrame = true;
            mp3Extractor.delayWrite = false;
            mp3Extractor.hasVbrHeader = true;
        }
    }
    Return bx;
    }
    //endregion

    //region "Parsing"
   private void parseMp3Frame(byte[] buffer) {
       Dim offset As Integer = 0
       Dim length As Integer = buffer.Length

       While length>=4
       Dim mpegVersion, sampleRate, channelMode As Integer
       Dim layer, bitRate, padding As Integer
       Dim header As ULong = CULng(BigEndianBitConverter.ToUInt32(buffer, offset)) << 32

       If BitHelper.Read(header, 11) <>&H7FF Then
       Exit While
       End If

       getMp3FrameInfo(header, mpegVersion, layer, bitRate, padding, channelMode, sampleRate)

       If mpegVersion = 1 OrElse layer<>1 OrElse bitRate = 0 OrElse bitRate = 15 OrElse sampleRate = 3 Then
       Exit While
       End If

       bitRate = (If(mpegVersion = 3, mpeg1BitRate(bitRate), mpeg2XBitRate(bitRate))) * 1000
       calcSampleRate(mpegVersion, sampleRate)
       Dim frameLenght As Integer = GetFrameLength(mpegVersion, bitRate, sampleRate, padding)
       If frameLenght>length Then
       Exit While
       End If

       ParseHeaderInformation(Me, offset, buffer, bitRate, mpegVersion, sampleRate, channelMode)


       Me.frameOffsets.Add(Me.totalFrameLength + offset)
       offset += frameLenght
       length -= frameLenght
       End While

       Me.totalFrameLength += buffer.Length
   }

    Private Shared Sub ParseHeaderInformation(ByRef mp3Extractor As Mp3AudioExtractor, _
            offset As Int32, buffer As Byte(), bitrate As Int32, mpegVer As Int32, sampleRate As Int32, channelMode As Int32)
    Dim isVbrHeaderFrame As Boolean = checkForVBRHeader(buffer, mp3Extractor.frameOffsets, offset, mpegVer, channelMode, mp3Extractor)
    If Not isVbrHeaderFrame Then
    With mp3Extractor.FrameInfo
    If .BitRate = 0 Then
            .BitRate = bitrate
            .MpegVersion = mpegVer
            .SampleRate = sampleRate
            .ChannelMode = channelMode
            .FirstFrameHeader = BigEndianBitConverter.ToUInt32(buffer, offset)
    ElseIf Not mp3Extractor.isVbr AndAlso bitrate <> .BitRate Then
    mp3Extractor.isVbr = True

    If Not mp3Extractor.hasVbrHeader Then
    If mp3Extractor.delayWrite Then
    mp3Extractor.WriteVbrHeader(True)
    mp3Extractor.doWriteVbrHeader = True
    mp3Extractor.delayWrite = False
            Else
    mp3Extractor.ls_warnings.Add("Detected VBR too late, cannot add VBR header.")
    End If
    End If
    End If
    End With
    End If
    End Sub

    Private Sub WriteVbrHeader(isPlaceholder As Boolean)
    Dim buffer As Byte() = New Byte(GetFrameLength(FrameInfo.MpegVersion, 64000, FrameInfo.SampleRate, 0)) {}

    If Not isPlaceholder Then
    Dim header As UInteger = FrameInfo.FirstFrameHeader
    Dim dataOffset As Integer = GetFrameDataOffset(FrameInfo.MpegVersion, FrameInfo.ChannelMode)
    header = header And &HFFFE0DFFUI
            header = header Or (If(FrameInfo.MpegVersion = 3, 5, 8)) << 12
            BitHelper.CopyBytes(buffer, 0, BigEndianBitConverter.GetBytes(header))
            BitHelper.CopyBytes(buffer, dataOffset, BigEndianBitConverter.GetBytes(&H58696E67))
            BitHelper.CopyBytes(buffer, dataOffset + 4, BigEndianBitConverter.GetBytes(&H7))
            BitHelper.CopyBytes(buffer, dataOffset + 8, BigEndianBitConverter.GetBytes(frameOffsets.Count))
            BitHelper.CopyBytes(buffer, dataOffset + 12, BigEndianBitConverter.GetBytes(totalFrameLength))

    For i As Int32 = 0 To 99
    Dim frameIndex As Integer = ((i / 100.0) * Me.frameOffsets.Count)
    buffer(dataOffset + 16 + i) = (Me.frameOffsets(frameIndex) / Me.totalFrameLength * 256.0)
    Next
    End If

    Me.p_videoStream.Write(buffer, 0, buffer.Length)
    End Sub
    //Region







}
