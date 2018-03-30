package bases;

/**
 * Created by HP on 2018-03-15.
 */

public class Constants {

    public interface PREFERENCE{
        String IS_ALARM_SET = "kr.co.picklecode.crossmedia.isAlarmSet";
        String ALARM_TIME = "kr.co.picklecode.crossmedia.alarmTime";
        String ALARM_TIME_FOR_ADAPTER = "kr.co.picklecode.crossmedia.alarmTime.adapter";
    }

    public interface DATABASE{
        String DB_NAME = "kr.co.picklecode.crossmedia.pickDB";
    }

    public interface INTENT_NOTIFICATION{
        String REP_FILTER = "kr.co.picklecode.crossmedia.action.notification";
        String ACTION_PLAY = "kr.co.picklecode.crossmedia.action.notification.play";
        int REQ_CODE_ACTION_PLAY = 111;
        String ACTION_STOP = "kr.co.picklecode.crossmedia.action.notification.stop";
        int REQ_CODE_ACTION_STOP = 121;
        String ACTION_CLOSE = "kr.co.picklecode.crossmedia.action.notification.close";
        int REQ_CODE_ACTION_CLOSE = 131;
    }

    public static final String ACTIVITY_INTENT_FILTER = "kr.co.picklecode.crossmedia.intent.activity.common";

    public static final String BASE_YOUTUBE_URL = "http://zacchaeus151.cafe24.com/youtube.php?vid=";

    public static final String NOTIFICATION_CHANNEL_ID = "kr.co.picklecode.crossmedia.channel001";
    public static final String NOTIFICATION_CHANNEL_NAME = "kr.co.picklecode.crossmedia.channel001.name";

    public static String getYoutubeSrc(String filtered){
        final String source = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "  <body>\n" +
                "    <!-- 1. The <iframe> (and video player) will replace this <div> tag. -->\n" +
                "    <div id=\"player\"></div>\n" +
                "\n" +
                "    <script>\n" +
                "      // 2. This code loads the IFrame Player API code asynchronously.\n" +
                "      var tag = document.createElement('script');\n" +
                "\n" +
                "      tag.src = \"https://www.youtube.com/iframe_api\";\n" +
                "      var firstScriptTag = document.getElementsByTagName('script')[0];\n" +
                "      firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);\n" +
                "\n" +
                "      // 3. This function creates an <iframe> (and YouTube player)\n" +
                "      //    after the API code downloads.\n" +
                "      var player;\n" +
                "      function onYouTubeIframeAPIReady() {\n" +
                "        player = new YT.Player('player', {\n" +
                "          height: '360',\n" +
                "          width: '640',\n" +
                "          videoId: '" + filtered + "',\n" +
                "          events: {\n" +
                "            'onReady': onPlayerReady,\n" +
                "            'onStateChange': onPlayerStateChange\n" +
                "          }\n" +
                "        });\n" +
                "      }\n" +
                "\n" +
                "      // 4. The API will call this function when the video player is ready.\n" +
                "      function onPlayerReady(event) {\n" +
                "        event.target.playVideo();\n" +
                "      }\n" +
                "\n" +
                "      // 5. The API calls this function when the player's state changes.\n" +
                "      //    The function indicates that when playing a video (state=1),\n" +
                "      //    the player should play for six seconds and then stop.\n" +
                "      var done = false;\n" +
                "      function onPlayerStateChange(event) {\n" +
                "        if (event.data == YT.PlayerState.PLAYING && !done) {\n" +
                "          //setTimeout(stopVideo, 6000);\n" +
                "          done = true;\n" +
                "        }\n" +
                "      }\n" +
                "      function stopVideo() {\n" +
                "        player.stopVideo();\n" +
                "      }\n" +
                "    </script>\n" +
                "  </body>\n" +
                "</html>";

//        function getStarted(){
//            if(player.getPlayerState() === -1 || player.getPlayerState() === 0 || player.getPlayerState() === 5){
//                play();
//                window.setTimeout(
//                        getStarted, 1000
//                )
//            }
//            else
//                return;
//        }

        return source;
    }

}
