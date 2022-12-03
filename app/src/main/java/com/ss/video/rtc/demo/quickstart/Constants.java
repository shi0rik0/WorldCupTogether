package com.ss.video.rtc.demo.quickstart;

/**
 * VolcEngineRTC 常量定义
 */
public class Constants {

    //APPID 使用SDK前需要为自己的应用申请一个AppId，详情参见{https://www.volcengine.com/docs/6348/69865}
    public static final String APPID = "634aa0872adebe01979a1fc0";

    public static final String APP_KEY = "966a733ecfa0463eaec5e9697c05fd23";

    //TOKEN 加入房间的时候需要使用token完成鉴权，详情参见{https://www.volcengine.com/docs/6348/70121}
    public static final String TOKEN_1918 =
            "001634aa0872adebe01979a1fc0PgCOgQsEWWSEY9mejWMEADExNDUEADE5MTgGAAAA2Z6NYwEA2Z6NYwIA2Z6NYwMA2Z6NYwQA2Z6NYwUA2Z6NYyAAAtPYQM8Z7e68frf+28dgg/H0XHZxTWICTY8FQyv3o8w=";

    public static final String TOKEN_1919 = "001634aa0872adebe01979a1fc0PgDHuEYBd2SEY/eejWMEADExNDUEADE5MTkGAAAA956NYwEA956NYwIA956NYwMA956NYwQA956NYwUA956NYyAArbqxAGejFpdm4IT4PiNkGjT2GlPGokBIvmz2d1oXGzw=";
    //INPUT_REGEX SDK 对房间名、用户名的限制是：非空且最大长度不超过128位的数字、大小写字母、@ . _ -
    public static final String INPUT_REGEX = "^[a-zA-Z0-9@._-]{1,128}$";

    public static final String ROOM_ID_EXTRA = "extra_room_id";

    public static final String USER_ID_EXTRA = "extra_user_id";

    public static final int VIDEO_WIDTH = 360;
    public static final int VIDEO_HEIGHT = 640;
    public static final int VIDEO_FRAME_RATE = 15;
    public static final int VIDEO_MAX_BITRATE = 800;
}
