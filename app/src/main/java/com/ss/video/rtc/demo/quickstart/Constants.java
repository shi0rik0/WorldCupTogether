package com.ss.video.rtc.demo.quickstart;

/**
 * VolcEngineRTC 常量定义
 */
public class Constants {

    //APPID 使用SDK前需要为自己的应用申请一个AppId，详情参见{https://www.volcengine.com/docs/6348/69865}
    public static final String APPID = "634aa0872adebe01979a1fc0";

    public static final String APP_KEY = "966a733ecfa0463eaec5e9697c05fd23";

    //INPUT_REGEX SDK 对房间名、用户名的限制是：非空且最大长度不超过128位的数字、大小写字母、@ . _ -
    public static final String INPUT_REGEX = "^[a-zA-Z0-9@._-]{1,128}$";

    public static final String ROOM_ID_EXTRA = "extra_room_id";

    public static final String USER_ID_EXTRA = "extra_user_id";

    public static final int VIDEO_WIDTH = 360;
    public static final int VIDEO_HEIGHT = 640;
    public static final int VIDEO_FRAME_RATE = 15;
    public static final int VIDEO_MAX_BITRATE = 800;
}
