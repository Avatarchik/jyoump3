package com.jytmp3.extraction;
import java.io.IOException;
import java.net.URL;
import java.util.*;
;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.jndi.toolkit.url.Uri;
import org.json.*;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.*;

import static com.jytmp3.Utils.sprintf;


/**
 * Created by Sp0x on 10/15/2014.
 */
public class YtUrlDecoder {
    private static int CorrectSignatureLength = 81;
    private static String SignatureQuery = "signature";
    private YtSignitureDecoder ytDecoder;

    public VideoCodecInfo DecryptDownloadUrl(VideoCodecInfo videoInfo) throws YoutubeParseException, InvalidOperationException {
        Dictionary<String, String> queries = URLHelper.ParseQueryString(videoInfo.downloadUrl);
        if (containsKey(queries, SignatureQuery)) {
            String encryptedSignature = queries.get(SignatureQuery);
            String decrypted = "";
            try {
                decrypted = getDecipheredSignature(videoInfo.htmlPlayerVersion, encryptedSignature);
            } catch (Exception ex) {
                throw new YoutubeParseException("Could not decipher signature", ex);
            }
            videoInfo.downloadUrl = URLHelper.ReplaceQueryStringParameter(videoInfo.downloadUrl, SignatureQuery, decrypted);
            videoInfo.requiresDecryption = false;
        }
        return videoInfo;
    }

    public YtVideo GetVideo(String videoUrl, Boolean decryptSignature) throws Exception, ArgumentException, YoutubeParseException, NullReferenceException {
        if (videoUrl == null) throw new ArgumentNullException("videoUrl");
        videoUrl = TryNormalizeYoutubeUrl(videoUrl);//handle invalid urls
        String id = YtVideo.getVideoId(videoUrl);
        if (id == null) {
            throw new ArgumentException("You must specify a valid url containing the youtube video id.", "URL is not a valid youtube URL!");
        }

        try {
            JSONObject json = LoadYTPlayerJson(videoUrl,"");
            String videoTitle = GetVideoTitle(json);
            List<ExtractionInfo> downloadUrls = ExtractDownloadUrls(json);
            List<VideoCodecInfo> infos = GetVideoInfos(downloadUrls, videoTitle);
            String htmlPlayerVersion = getHtml5PlayerVersion(json);
            for (VideoCodecInfo info : infos) {
                info.htmlPlayerVersion = htmlPlayerVersion;
                if (decryptSignature && info.requiresDecryption) {
                    try {
                        DecryptDownloadUrl(info);
                    } catch (InvalidOperationException e) {
                        e.printStackTrace();
                    }
                }
            }
            YtVideo result = new YtVideo(id);
            result.codecs = infos;
        } catch (Exception ex) {
            if (ex instanceof VideoNotAvailableException) {
                throw new Exception();
            }
            throwYoutubeParseException(ex, videoUrl);
        }
        return null;
    }

    public YtVideo ParseVideoPage(String videoPage, String videoId, Boolean decryptSignature) throws VideoNotAvailableException, YoutubeParseException, NullReferenceException {
        try {
            JSONObject json = LoadYTPlayerJson("", videoPage);
            String videoTitle = GetVideoTitle(json);
            List<ExtractionInfo> downloadUrls = ExtractDownloadUrls(json);
            List<VideoCodecInfo> infos = GetVideoInfos(downloadUrls, videoTitle);
            String htmlPlayerVersion = getHtml5PlayerVersion(json);
            for (VideoCodecInfo info : infos) {
                info.htmlPlayerVersion = htmlPlayerVersion;
                if (decryptSignature && info.requiresDecryption) {
                    try {
                        info = DecryptDownloadUrl(info);
                    } catch (InvalidOperationException e) {
                        e.printStackTrace();
                    }
                }
            }
            YtVideo result = new YtVideo(videoId);
            result.codecs = infos;
            return result;
        } catch (Exception ex) {
            if (ex instanceof VideoNotAvailableException) {
                throwYoutubeParseException(ex, "YtPlaylist");
            }
        }
        return null;
    }

    public static Runnable GetDownloadUrlsAsync(String videoUrl, Boolean decryptSignature) {
        throw new NotImplementedException("");
        //Return System.Threading.Tasks.Task.Run(Function() GetDownloadUrls(videoUrl, decryptSignature))
    }


    //#End If
    public static String TryNormalizeYoutubeUrl(String url){
        url = url.trim();
        url = url.replace("youtu.be/", "youtube.com/watch?v=");
        url = url.replace("www.youtube", "youtube");
        url = url.replace("youtube.com/embed/", "youtube.com/watch?v=");
        if(url.contains("/v/")) {
            url = sprintf("http://youtube.com%s", new Uri(url).AbsolutePath.Replace("/v/", "/watch?v="));
        }
        url = url.replace("/watch#", "/watch?");
        Dictionary<String, String> query = URLHelper.ParseQueryString(url);
        String v = query.get("v");
        if(v==null){
            return v;
        }
        url = sprintf("http://youtube.com/watch?v=%s", v);
        if(containsKey(query,"list")){
            url = String.format("%s&%s=%s", url, "list", query.get("list"));
        }
        return url;
    }

    public static <T,K> Boolean containsKey(Dictionary<T,K> dict, T key)
    {
        return Collections.list(dict.keys()).contains(key);
    }
    private static List<ExtractionInfo> ExtractDownloadUrls(JSONObject json) throws VideoNotAvailableException {
        List<ExtractionInfo> output = new ArrayList<ExtractionInfo>();
        String[] splitByUrls  = getStreamMap(json).split(",");
        String[] adaptiveFmtSplitByUrls = GetAdaptiveStreamMap(json).split(",");
        splitByUrls = ArrayUtils.addAll(splitByUrls, adaptiveFmtSplitByUrls);
        for(String s : splitByUrls)
        {
            Dictionary<String,String> queries = URLHelper.ParseQueryString(s);
            String url;
            Boolean requiresDecryption = false;
            Boolean x = Collections.list(queries.keys()).contains("s");
            if(containsKey(queries, "s") || containsKey(queries,"sig")){
                requiresDecryption = containsKey(queries,"s");
                String signature = requiresDecryption ? queries.get("s") : queries.get("sig");
                url = String.format("%s&%s=%s", queries.get("url"), YtUrlDecoder.SignatureQuery, signature);
                String fallbackHost = containsKey(queries,"fallback_host") ?   "&fallback_host=" + queries.get("fallback_host") : "";
                url += fallbackHost;
            }else{
                url = queries.get("url");
            }

            url = HtmlDecoder.Decode(url);
            url = urlDecode(url);
            ExtractionInfo ext = new ExtractionInfo(); ext.requiresDecryption=requiresDecryption;
            ext.Uri=url;
            output.add(ext);

        }
        return output;
    }

    private static String GetAdaptiveStreamMap(JSONObject json){
        JSONObject streamMap = json.getJSONObject("args").getJSONObject("adaptive_fmts");
        return streamMap.toString();
    }

    private String getDecipheredSignature(String htmlPlayerVersion, String signature) throws IOException, InvalidOperationException {
        if (signature.length()== CorrectSignatureLength) return signature;
        if(ytDecoder!=null){
            if (! ytDecoder.PlayerVersion.equals(htmlPlayerVersion)) ytDecoder = new YtSignitureDecoder(htmlPlayerVersion);
        }
        else{
            ytDecoder = new YtSignitureDecoder(htmlPlayerVersion);
        }
        return ytDecoder.DecipherWithVersion(signature);
    }

    private static String getHtml5PlayerVersion(JSONObject json){
        Pattern regex = Pattern.compile("html5player-(.+?)\\.js");
        String js = json.getJSONObject("assets").getString("js");
        return regex.matcher(js).toString();//($1)
    }

    private static String getStreamMap(JSONObject json ) throws VideoNotAvailableException {
        JSONObject streamMap = json.getJSONObject("args").getJSONObject("url_encoded_fmt_stream_map");
        String streamMapString = streamMap == null ? null : streamMap.toString(); //'' streamMap == null ? null : streamMap.ToString()
        if(streamMapString == null || streamMapString.contains("been+removed")){
            throw new VideoNotAvailableException("Video is removed or has an age restriction.");
        }
        return streamMapString;
    }

    private List<VideoCodecInfo> GetVideoInfos(List<ExtractionInfo> extractionInfos , String videoTitle){
        List<VideoCodecInfo> downLoadInfos = new ArrayList<VideoCodecInfo>();
        ListIterator<ExtractionInfo> iter = extractionInfos.listIterator();
        while(iter.hasNext())
        {
            ExtractionInfo extinfo = iter.next();
            VideoCodecInfo info=null;
            String itag = URLHelper.ParseQueryString(extinfo.Uri).get("itag");
            Integer formatCode = Integer.parseInt(itag);
            for(int iDef=0; iDef < VideoCodecInfo.defaults.length; iDef++)
            {
                if(VideoCodecInfo.defaults[iDef].formatCode==formatCode) {info=VideoCodecInfo.defaults[iDef]; break;}
            }
            if(info!=null){
                info = new VideoCodecInfo(formatCode);
                info.title = videoTitle;
                info.downloadUrl = extinfo.Uri;
                info.requiresDecryption=extinfo.requiresDecryption;
            }
            else{
                info = new VideoCodecInfo(formatCode);
                info.downloadUrl=extinfo.Uri;
            }
            info.downloadUrl = URLHelper.LinkSetArg(info.downloadUrl, "ratebypass", "yes");
            downLoadInfos.add(info);
        }
        return downLoadInfos;
    }

    private static String GetVideoTitle(JSONObject json){
        String title = json.getJSONObject("args").getString("title");
        return title== null ? "" : title.toString();
    }

    public static Boolean IsVideoUnavailable(String pageSource){
        String unavailableContainer = "<div id=\"\"watch-player-unavailable\"\">\"\"";
        return pageSource.contains(unavailableContainer);
    }


    private static JSONObject LoadYTPlayerJson(String url, String pageSource) throws NullReferenceException, IOException, VideoNotAvailableException, YoutubeParseException {
        if(pageSource == null) {
            IOUtils ios;
            pageSource = IOUtils.toString(new URL(url).openStream(), "UTF8");
        }
        if(pageSource==null){
            throw new NullReferenceException("Could not fetch URL's content!");
        }
        if(IsVideoUnavailable(pageSource)){
            throw new VideoNotAvailableException("");
        }
        Pattern dataRx = Pattern.compile("ytplayer\\.config\\s*=\\s*(\\{.+?\\});", Pattern.DOTALL);
        Matcher rxMatch = dataRx.matcher(pageSource);
        rxMatch.find();

        if(!rxMatch.find()) throwYoutubeParseException(new Exception("INVALID JSON!"), url);
        String extractedJson = rxMatch.toString();//("$1")
        return new JSONObject(extractedJson);
    }

    private static void throwYoutubeParseException(Exception innerException, String videoUrl) throws YoutubeParseException {
        throw new YoutubeParseException("Could not parse the Youtube page for URL "+ videoUrl + "\n" +
                "This may be due to a change of the Youtube page structure.\n" +
                "Please report this bug at www.github.com/flagbug/YoutubeExtractor/issues", innerException);
    }

}
