package com.ss.video.rtc.demo.quickstart;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;


import android.content.Context;
import android.content.Intent;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;

import android.widget.EditText;

import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.ss.bytertc.engine.RTCEngine;
import com.ss.bytertc.engine.RTCRoom;
import com.ss.bytertc.engine.RTCRoomConfig;
import com.ss.bytertc.engine.RTCVideo;
import com.ss.bytertc.engine.UserInfo;
import com.ss.bytertc.engine.VideoCanvas;
import com.ss.bytertc.engine.VideoEncoderConfig;
import com.ss.bytertc.engine.data.AudioRoute;
import com.ss.bytertc.engine.data.CameraId;
import com.ss.bytertc.engine.data.RemoteStreamKey;
import com.ss.bytertc.engine.data.ScreenMediaType;
import com.ss.bytertc.engine.data.StreamIndex;
import com.ss.bytertc.engine.data.VideoFrameInfo;
import com.ss.bytertc.engine.data.VideoSourceType;
import com.ss.bytertc.engine.handler.IRTCVideoEventHandler;
import com.ss.bytertc.engine.type.ChannelProfile;
import com.ss.bytertc.engine.type.MediaDeviceState;
import com.ss.bytertc.engine.type.MediaStreamType;
import com.ss.bytertc.engine.type.StreamRemoveReason;
import com.ss.bytertc.engine.type.UserMessageSendResult;
import com.ss.bytertc.engine.type.VideoDeviceType;
import com.ss.video.rtc.demo.quickstart.token.Utils;

//<<<<<<< HEAD
import java.text.SimpleDateFormat;
//=======
import java.util.ArrayList;
//>>>>>>> 9597204 (????????????????????????????????????)
import org.webrtc.RXScreenCaptureService;

import java.util.Locale;
import java.util.stream.Stream;

import me.rosuh.filepicker.bean.FileItemBeanImpl;
import me.rosuh.filepicker.config.AbstractFileFilter;
import me.rosuh.filepicker.config.FilePickerManager;
import me.rosuh.filepicker.filetype.VideoFileType;

/**
 * VolcEngineRTC ????????????????????????
 * ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
 *
 * ???????????????????????????
 * - ????????????
 * - ????????????????????????
 * - ???????????????????????????
 * - ????????????
 * - ???????????????????????????
 * - ??????/???????????????
 * - ??????/???????????????
 * - ?????????????????????????????????
 * - ????????????
 * - ????????????
 *
 * ??????????????????????????????????????????????????????
 * 1.?????? IRTCVideo ?????????
 *   RTCVideo.createRTCVideo(Context context, String appId, IRTCVideoEventHandler handler,
 *     Object eglContext, JSONObject parameters)
 * 2.????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
 *   RTCVideo.setVideoEncoderConfig(VideoEncoderConfig maxSolution)
 * 3.??????????????????????????? RTCVideo.startVideoCapture()
 * 4.????????????????????????????????????????????????????????????????????????
 *   RTCVideo.setLocalVideoCanvas(StreamIndex streamIndex, VideoCanvas videoCanvas)
 * 5.???????????????RTCVideo.createRTCRoom(String roomId)
 * 6.??????????????????????????????
 *   RTCRoom.joinRoom(String token, UserInfo userInfo, RTCRoomConfig roomConfig)
 * 7.SDK ??????????????????????????????????????????????????????????????????????????????
 *   RTCVideo.setRemoteVideoCanvas(String userId, StreamIndex streamIndex, VideoCanvas videoCanvas)
 * 8.???????????????????????????????????????????????????????????????
 * 9.??????????????????????????????RTCRoom.leaveRoom()
 * 10.?????? RTCRoom.destroy() ?????????????????????
 * 11.?????? RTCVideo.destroyRTCVideo() ???????????????
 *
 * ?????????API????????????{https://www.volcengine.com/docs/6348/70080}
 */
public class RTCRoomActivity extends AppCompatActivity {
    private StringBuilder mVideoPath = new StringBuilder();
    private int mRemoteUsers = 0;
    private int mLineCount = 0;
    private String mRoomID;
    private String mUserID;
    private Handler handler;
    private Runnable r;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private boolean screenCastAvailable = true;

    private ImageView mSpeakerIv;
    private ImageView mAudioIv;
    private ImageView mVideoIv;
    private ImageView mMessageIv;
    private Button mMessageSendBt;
    private EditText mMessageEt;
    private TextView mMessageTv;

    private boolean mIsSpeakerPhone = true;
    private boolean mIsMuteAudio = false;
    private boolean mIsMuteVideo = false;
    private boolean mIsMessage = false;
    private CameraId mCameraID = CameraId.CAMERA_ID_FRONT;

    private FrameLayout mSelfContainer;
    private ConstraintLayout mMessageViewLayout;

    private static final int MAX_REMOTE_USERS = 7;
    private final String[] mRemoteRoomIDs = new String[MAX_REMOTE_USERS];
    private final FrameLayout[] mRemoteContainerArray = new FrameLayout[MAX_REMOTE_USERS];
    private final TextView[] mUserIdTvArray = new TextView[MAX_REMOTE_USERS];
    private final String[] mShowUidArray = new String[MAX_REMOTE_USERS];
    private final StreamIndex[] mRemoteStreamIndex = new StreamIndex[MAX_REMOTE_USERS];

    private RTCVideo mRTCVideo;
    private RTCRoom mRTCRoom;
    // ?????????????????????????????????????????????????????????????????????
    private RTCRoomEventHandlerAdapter mIRtcRoomEventHandler = new RTCRoomEventHandlerAdapter() {

        /**
         * ?????????????????????????????????????????????
         */
        @Override
        public void onUserJoined(UserInfo userInfo, int elapsed) {
            super.onUserJoined(userInfo, elapsed);
            Log.d("IRTCRoomEventHandler", "onUserJoined: " + userInfo.getUid());
        }

        /**
         * ?????????????????????????????????
         */
        @Override
        public void onUserLeave(String uid, int reason) {
            super.onUserLeave(uid, reason);
            Log.d("IRTCRoomEventHandler", "onUserLeave: " + uid);
            runOnUiThread(() -> removeRemoteView(uid));
        }

        @Override
        public void onRoomStateChanged(String roomId, String uid, int state, String extraInfo) {
            super.onRoomStateChanged(roomId, uid, state, extraInfo);
            Log.d("RoomStateChanged", "roomid:"+roomId+"state:"+state+extraInfo+"uid:"+uid);
        }

        /**
         * ???token??????????????????????????????token???
         */
        @Override
        public void onTokenWillExpire() {
            super.onTokenWillExpire();
            mRTCRoom.updateToken(Utils.generateToken(mRoomID, mUserID));
            Log.d("onTokenWillExpire", "token updated");
        }

        @Override
        public void onRoomMessageSendResult(long msgid, int error) {
            super.onRoomMessageSendResult(msgid, error);
            if (error != UserMessageSendResult.USER_MESSAGE_SEND_RESULT_SUCCESS) {
                Log.e("onRoomMessageSendResult", "sending message failed");
            }
        }

        @Override
        public void onRoomMessageReceived(String uid, String message) {
            super.onRoomMessageReceived(uid, message);
            runOnUiThread(() -> onMessageReceived(uid, message));
        }

        // ?????????????????????????????????????????????
        @Override
        public void onUserUnpublishScreen(String uid, MediaStreamType type, StreamRemoveReason reason) {
            super.onUserUnpublishScreen(uid, type, reason);
            // Implementation Version 1
//            Log.d("onUserUnpublishScreen", "some user just unpublished screen");
//            int i;
//            for (i = 0; i < mShowUidArray.length; ++i) {
//                if (mShowUidArray[i].equals(uid)) {
//                    break;
//                }
//            }
//            mRemoteStreamIndex[i] = StreamIndex.STREAM_INDEX_MAIN;
//            int j = i;
//            String roomID = mRemoteRoomIDs[i];
//            String userID = mShowUidArray[i];
//            runOnUiThread(()->setRemoteViewByIndex(j, roomID, userID, StreamIndex.STREAM_INDEX_MAIN));

            // Implementation Version 2:

            FrameLayout videoCast = findViewById(R.id.video_cast);
            ConstraintLayout videoCastHolder = findViewById(R.id.video_cast_holder);
            ViewGroup.LayoutParams currParam = videoCastHolder.getLayoutParams();
            currParam.height = 0;
            runOnUiThread(()-> videoCastHolder.setLayoutParams(currParam));
            runOnUiThread(()->setRemoteView(mRoomID, uid, StreamIndex.STREAM_INDEX_MAIN));
            screenCastAvailable = true;

        }
    };

    private void stopScreenShare() {
        // ????????????????????????
        mRTCVideo.stopScreenCapture();
    }

    private IRTCVideoEventHandler mIRtcVideoEventHandler = new IRTCVideoEventHandler() {

        /**
         * SDK?????????????????????????????????????????????????????????????????????
         * ??????????????????????????????????????????
         */
        @Override
        public void onFirstRemoteVideoFrameDecoded(RemoteStreamKey remoteStreamKey, VideoFrameInfo frameInfo) {
            super.onFirstRemoteVideoFrameDecoded(remoteStreamKey, frameInfo);
//            Log.d("onFirstRemoteVideoFrame",
//                    remoteStreamKey.getUserId() + ":" + remoteStreamKey.getStreamIndex());
            runOnUiThread(() -> setRemoteView(remoteStreamKey.getRoomId(),
                    remoteStreamKey.getUserId(), remoteStreamKey.getStreamIndex()));

            if (remoteStreamKey.getStreamIndex() == StreamIndex.STREAM_INDEX_SCREEN)
            {
                screenCastAvailable = false; // ??????????????????????????????????????????
                Log.d("screenCast", "enter main logic");
                // ?????????????????????
                String userId = remoteStreamKey.getUserId();
                Log.d("screenCast", "userid: " +userId);
                String roomId = remoteStreamKey.getRoomId();
                Log.d("screenCast", "roomId: "+ roomId);

                runOnUiThread(() -> removeRemoteView(userId));
                // ??????????????????
                Point point = new Point();
                Display display = getWindowManager().getDefaultDisplay();
                display.getRealSize(point);
                ConstraintLayout screenCastLayout = findViewById(R.id.video_cast_holder);
                ViewGroup.LayoutParams currParam = screenCastLayout.getLayoutParams();
                currParam.height = point.y - 270;
                Log.d("screenCast", "height:"+currParam.height);

                runOnUiThread(()->screenCastLayout.setLayoutParams(currParam));
                // ?????????????????????????????????????????????
                FrameLayout videoCast = findViewById(R.id.video_cast);
                runOnUiThread(()->setRemoteRenderView(roomId, userId, videoCast, StreamIndex.STREAM_INDEX_SCREEN));
                TextView castUsername = findViewById(R.id.video_cast_user_id_tv);
                runOnUiThread(() -> castUsername.setText(userId+"???????????????"));
                // toast??????
                runOnUiThread(() -> Toast.makeText(RTCRoomActivity.this, userId+"?????????????????????", Toast.LENGTH_SHORT).show());
            }
        }

        /**
         * ?????????????????????????????? {https://www.volcengine.com/docs/6348/70082#warncode}
         */
        @Override
        public void onWarning(int warn) {
            super.onWarning(warn);
            Log.d("IRTCVideoEventHandler", "onWarning: " + warn);
        }

        /**
         * ?????????????????????????????? {https://www.volcengine.com/docs/6348/70082#errorcode}
         */
        @Override
        public void onError(int err) {
            super.onError(err);
            Log.d("IRTCVideoEventHandler", "onError: " + err);
            showAlertDialog(String.format(Locale.US, "error: %d", err));
        }


        @Override
        public void onVideoDeviceStateChanged(String deviceId, VideoDeviceType deviceType, int deviceState, int deviceError) {
            if (deviceType == VideoDeviceType.VIDEO_DEVICE_TYPE_SCREEN_CAPTURE_DEVICE) {
                if (deviceState == MediaDeviceState.MEDIA_DEVICE_STATE_STARTED) {
                    Log.d("onVideo***StateChanged", "publish screen");
                    mRTCRoom.publishScreen(MediaStreamType.RTC_MEDIA_STREAM_TYPE_BOTH);
                    mRTCVideo.setVideoSourceType(StreamIndex.STREAM_INDEX_SCREEN, VideoSourceType.VIDEO_SOURCE_TYPE_INTERNAL);
                } else if (deviceState == MediaDeviceState.MEDIA_DEVICE_STATE_STOPPED
                        || deviceState == MediaDeviceState.MEDIA_DEVICE_STATE_RUNTIMEERROR) {
                    mRTCRoom.unpublishScreen(MediaStreamType.RTC_MEDIA_STREAM_TYPE_BOTH);
                }
            }
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);

        // ??????????????????
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // ??????????????????
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(android.R.color.black));
        }

        Intent intent = getIntent();
        mRoomID = intent.getStringExtra(Constants.ROOM_ID_EXTRA);
        mUserID = intent.getStringExtra(Constants.USER_ID_EXTRA);

        // ?????????ui??????
        initUI(mRoomID, mUserID);
        // ?????????????????????????????????????????????
        // createRTCVideo
        // setVideoEncoderConfig
        // startVideoCapture
        // startAudioCapture
        // createRTCRoom
        // setRoomEventHandler
        // joinRoom
        initEngineAndJoinRoom(mRoomID, mUserID);

        // ????????????????????????????????? handler
        handler = new Handler();
        r = new Runnable() {
            @Override
            public void run() {
                int lineCountTemp = mMessageTv.getLineCount();
                Log.d(TAG, "run: " + lineCountTemp);
                if(mLineCount == lineCountTemp) {
//                    hideChatView();
                    updateLocalMessageStatus();
                } else {
                    mLineCount = lineCountTemp;
                    handler.postDelayed(this, Constants.CHAT_SHOW_TIME);
                }
            }
        };
        mMessageTv.setMovementMethod(new ScrollingMovementMethod());
    }

    private void initUI(String roomId, String userId) {
        mSelfContainer = findViewById(R.id.self_video_container);
        mRemoteContainerArray[0] = findViewById(R.id.remote_video_0_container);
        mRemoteContainerArray[1] = findViewById(R.id.remote_video_1_container);
        mRemoteContainerArray[2] = findViewById(R.id.remote_video_2_container);
        mRemoteContainerArray[3] = findViewById(R.id.remote_video_3_container);
        mRemoteContainerArray[4] = findViewById(R.id.remote_video_4_container);
        mRemoteContainerArray[5] = findViewById(R.id.remote_video_5_container);
        mRemoteContainerArray[6] = findViewById(R.id.remote_video_5_container);
        mUserIdTvArray[0] = findViewById(R.id.remote_video_0_user_id_tv);
        mUserIdTvArray[1] = findViewById(R.id.remote_video_1_user_id_tv);
        mUserIdTvArray[2] = findViewById(R.id.remote_video_2_user_id_tv);
        mUserIdTvArray[3] = findViewById(R.id.remote_video_0_user_id_tv);
        mUserIdTvArray[4] = findViewById(R.id.remote_video_1_user_id_tv);
        mUserIdTvArray[5] = findViewById(R.id.remote_video_2_user_id_tv);
        mUserIdTvArray[6] = findViewById(R.id.remote_video_0_user_id_tv);
        findViewById(R.id.switch_camera).setOnClickListener((v) -> onSwitchCameraClick());
        mSpeakerIv = findViewById(R.id.switch_audio_router);
        mAudioIv = findViewById(R.id.switch_local_audio);
        mVideoIv = findViewById(R.id.switch_local_video);
        mMessageIv = findViewById(R.id.message);
        mMessageSendBt = findViewById(R.id.message_bt);
        mMessageEt = findViewById(R.id.message_et);
        mMessageTv = findViewById(R.id.message_tv);

        mMessageViewLayout = findViewById(R.id.message_view_layout);

        findViewById(R.id.hang_up).setOnClickListener((v) -> onBackPressed());
        mSpeakerIv.setOnClickListener((v) -> updateSpeakerStatus());
        mAudioIv.setOnClickListener((v) -> updateLocalAudioStatus());
        mVideoIv.setOnClickListener((v) -> updateLocalVideoStatus());
        mMessageIv.setOnClickListener((v) -> updateLocalMessageStatus());
        mMessageSendBt.setOnClickListener((v) -> sendMessage());
        mMessageEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    // ???????????????
                    Log.d(TAG, "onFocusChange: get focus");
                    handler.removeCallbacksAndMessages(null);
                } else {
                    // ???????????????
                    Log.d(TAG, "onFocusChange: lose focus");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        if (!handler.hasCallbacks(r)) {
                         handler.postDelayed(r, Constants.CHAT_SHOW_TIME);
                        }
                    }
                }
            }
        });
        TextView roomIDTV = findViewById(R.id.room_id_text);
        TextView userIDTV = findViewById(R.id.self_video_user_id_tv);
        roomIDTV.setText(String.format("????????????%s", roomId));
        userIDTV.setText(String.format("%s", userId));
    }

    private void initEngineAndJoinRoom(String roomId, String userId) {
        // ????????????
        mRTCVideo = RTCVideo.createRTCVideo(getApplicationContext(), Constants.APPID, mIRtcVideoEventHandler, null, null);
        // ????????????????????????
        VideoEncoderConfig videoEncoderConfig = new VideoEncoderConfig(Constants.VIDEO_WIDTH,
                Constants.VIDEO_HEIGHT, Constants.VIDEO_FRAME_RATE, Constants.VIDEO_MAX_BITRATE);
        mRTCVideo.setVideoEncoderConfig(videoEncoderConfig);
        setLocalRenderView(userId, StreamIndex.STREAM_INDEX_MAIN);
        // ????????????????????????
        mRTCVideo.startVideoCapture();
        // ????????????????????????
        mRTCVideo.startAudioCapture();
        // ????????????
        mRTCRoom = mRTCVideo.createRTCRoom(roomId);
        mRTCRoom.setRTCRoomEventHandler(mIRtcRoomEventHandler);
        RTCRoomConfig roomConfig = new RTCRoomConfig(ChannelProfile.CHANNEL_PROFILE_COMMUNICATION,
                true, true, true);
        int joinRoomRes = mRTCRoom.joinRoom(Utils.generateToken(roomId, userId),
            UserInfo.create(userId, ""), roomConfig);
        Log.i("TAG", "initEngineAndJoinRoom: " + joinRoomRes);
    }

    private void setLocalRenderView(String uid, StreamIndex streamIndex) {
        VideoCanvas videoCanvas = new VideoCanvas();
        TextureView renderView = new TextureView(this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        mSelfContainer.removeAllViews();
        mSelfContainer.addView(renderView, params);
        videoCanvas.renderView = renderView;
        videoCanvas.uid = uid;
        videoCanvas.isScreen = false;
        videoCanvas.renderMode = VideoCanvas.RENDER_MODE_HIDDEN;
        // ??????????????????????????????
        mRTCVideo.setLocalVideoCanvas(streamIndex, videoCanvas);
    }

    private void setRemoteRenderView(String roomId, String uid, FrameLayout container, StreamIndex streamIndex) {
        TextureView renderView = new TextureView(this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        container.removeAllViews();
        container.addView(renderView, params);

        VideoCanvas videoCanvas = new VideoCanvas();
        videoCanvas.renderView = renderView;
        videoCanvas.roomId = roomId;
        videoCanvas.uid = uid;
        videoCanvas.isScreen = false;
        videoCanvas.renderMode = VideoCanvas.RENDER_MODE_HIDDEN;
        // ????????????????????????????????????
        mRTCVideo.setRemoteVideoCanvas(uid, streamIndex, videoCanvas);
    }

    private void setRemoteView(String roomId, String uid, StreamIndex streamIndex) {
        if (streamIndex == StreamIndex.STREAM_INDEX_SCREEN) {
            return;
        }
        int j = -1;
        for (int i = 0; i < mShowUidArray.length; i++) {
            if (TextUtils.isEmpty(mShowUidArray[i]) && j == -1) {
                j = i;
            } else if (TextUtils.equals(uid, mShowUidArray[i])) {
                return;
            }
        }
        if (j < 0) {
            return;
        }
        setRemoteViewByIndex(j, roomId, uid, streamIndex);
        mRemoteUsers++;
    }

    private void setRemoteViewByIndex(int i, String roomID, String userID, StreamIndex streamIndex) {
        mRemoteRoomIDs[i] = roomID;
        mShowUidArray[i] = userID;
        mUserIdTvArray[i].setText(userID);
        mRemoteStreamIndex[i] = streamIndex;
        setRemoteRenderView(roomID, userID, mRemoteContainerArray[i], streamIndex);
    }



    private void removeRemoteView(String uid) {
        int i;
        for (i = 0; i < mShowUidArray.length; i++) {
            if (TextUtils.equals(uid, mShowUidArray[i])) {
                break;
            }
        }
        for (int j = i; j < mRemoteUsers - 1; j++) {
            setRemoteViewByIndex(j, mRemoteRoomIDs[j+1], mShowUidArray[j+1], mRemoteStreamIndex[j+1]);
        }
        removeRemoteViewByIndex(mRemoteUsers - 1);
        mRemoteUsers--;
    }

    private void removeRemoteViewByIndex(int i) {
        mShowUidArray[i] = null;
        mUserIdTvArray[i].setText(null);
        mRemoteContainerArray[i].removeAllViews();
    }

    private void onSwitchCameraClick() {
        // ????????????/????????????????????????????????????????????????
        if (mCameraID.equals(CameraId.CAMERA_ID_FRONT)) {
            mCameraID = CameraId.CAMERA_ID_BACK;
        } else {
            mCameraID = CameraId.CAMERA_ID_FRONT;
        }
        mRTCVideo.switchCamera(mCameraID);
    }

    private void updateSpeakerStatus() {
        mIsSpeakerPhone = !mIsSpeakerPhone;
        // ??????????????????????????????????????????
        mRTCVideo.setAudioRoute(mIsSpeakerPhone ? AudioRoute.AUDIO_ROUTE_SPEAKERPHONE
                : AudioRoute.AUDIO_ROUTE_EARPIECE);
        mSpeakerIv.setImageResource(mIsSpeakerPhone ? R.drawable.speaker_on : R.drawable.speaker_off);
    }

    private void updateLocalAudioStatus() {
        mIsMuteAudio = !mIsMuteAudio;
        // ??????/????????????????????????
        if (mIsMuteAudio) {
            mRTCRoom.unpublishStream(MediaStreamType.RTC_MEDIA_STREAM_TYPE_AUDIO);
        } else {
            mRTCRoom.publishStream(MediaStreamType.RTC_MEDIA_STREAM_TYPE_AUDIO);
        }
        mAudioIv.setImageResource(mIsMuteAudio ? R.drawable.mute_audio : R.drawable.normal_audio);
    }

    private void updateLocalVideoStatus() {
        mIsMuteVideo = !mIsMuteVideo;
        if (mIsMuteVideo) {
            // ??????????????????
            mRTCVideo.stopVideoCapture();
        } else {
            // ??????????????????
            mRTCVideo.startVideoCapture();
        }
        mVideoIv.setImageResource(mIsMuteVideo ? R.drawable.mute_video : R.drawable.normal_video);
    }

    private void updateLocalMessageStatus() {
        mIsMessage = !mIsMessage;
        if (mIsMessage) {
            // ??????????????????
            showChatView();
            handler.postDelayed(r, Constants.CHAT_SHOW_TIME);
        } else {
            // ??????????????????
            hideChatView();
            handler.removeCallbacksAndMessages(null);
        }
        mMessageIv.setImageResource(mIsMessage ? R.drawable.message_square_off: R.drawable.message_square_on);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent e) {
        //???????????????????????????????????????view?????????????????????????????????
        if(e.getAction() == MotionEvent.ACTION_DOWN) {
            //???????????????????????????view
            View v = getCurrentFocus();
            Log.d(TAG, "Clicked: " + v);
            //????????????????????????????????????
            if (isShouldHideKeyboard(v, e)) {
                mMessageEt.clearFocus();
                hideKeyboard(v.getWindowToken());
            }
        }
        //?????????activity???????????????????????????????????????????????????????????????????????????
        return super.dispatchTouchEvent(e);
    }

    //???????????????????????????
    private boolean isShouldHideKeyboard(View v, MotionEvent event) {
        //?????????????????????????????????view???editText?????????????????????????????????
        if (v instanceof EditText) {
            int[] l = {0, 0};
            v.getLocationInWindow(l);
            int left = l[0],
                    top = l[1],
                    bottom = top + v.getHeight(),
                    right = left + v.getWidth();
            // ??????EditText????????????????????????
            return !(event.getX() > left) || !(event.getX() < right)
                    || !(event.getY() > top) || !(event.getY() < bottom);
        }
        // ??????????????????EditText?????????????????????????????????????????????????????????????????????EditText???
        return false;
    }

    //?????????????????????editText????????????
    private void hideKeyboard(IBinder token) {
        if (token != null) {
            //???????????????InputMethodManager???????????????????????????????????????
            //InputMethodManager?????????????????????????????????manager
            InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (im != null) {
                im.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }
    private void showChatView() {
        //??????????????????????????????????????????????????????????????????????????????????????????500ms
        final TranslateAnimation ctrlAnimation = new TranslateAnimation(
                TranslateAnimation.RELATIVE_TO_SELF, 0, TranslateAnimation.RELATIVE_TO_SELF, 0,
                TranslateAnimation.RELATIVE_TO_SELF, 1, TranslateAnimation.RELATIVE_TO_SELF, 0);
        ctrlAnimation.setDuration(400l);     //???????????????????????????
        mMessageViewLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                mMessageViewLayout.setVisibility(View.VISIBLE);
                mMessageViewLayout.startAnimation(ctrlAnimation);
            }
        }, 500);
    }
    private void hideChatView() {
        //??????????????????????????????500ms
        final TranslateAnimation ctrlAnimation = new TranslateAnimation(
                TranslateAnimation.RELATIVE_TO_SELF, 0, TranslateAnimation.RELATIVE_TO_SELF, 0,
                TranslateAnimation.RELATIVE_TO_SELF, 0, TranslateAnimation.RELATIVE_TO_SELF, 1);
        ctrlAnimation.setDuration(400l);     //???????????????????????????
        mMessageViewLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                mMessageViewLayout.setVisibility(View.GONE);
                mMessageViewLayout.startAnimation(ctrlAnimation);
            }
        }, 500);
    }

    private void sendMessage() {
        String input = mMessageEt.getText().toString();
        mMessageEt.setText("");
        if(!input.equals("")) {
            // local ???????????????Username ?????????
            mMessageTv.append(Html.fromHtml("<b>"+ mUserID + ": " + input + "</b>"));
            mMessageTv.append("\n");
            mRTCRoom.sendRoomMessage(input);

            ViewTreeObserver vto = mMessageTv.getViewTreeObserver();
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    int scrollAmount = mMessageTv.getLayout().getLineTop(mMessageTv.getLineCount())
                            - mMessageTv.getHeight();
                    if (scrollAmount > 0) {
                        mMessageTv.scrollTo(0, scrollAmount);
                    }else {
                        mMessageTv.scrollTo(0, 0);
                    }
                }
            });
        }
    }

    private void showAlertDialog(String message) {
        runOnUiThread(() -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(message);
            builder.setPositiveButton("?????????", (dialog, which) -> dialog.dismiss());
            builder.create().show();
        });
    }

    @Override
    public void finish() {
        super.finish();
        // ????????????
        if (mRTCRoom != null) {
            mRTCRoom.leaveRoom();
            mRTCRoom.destroy();
        }
        mRTCRoom = null;
        // ????????????
        RTCVideo.destroyRTCVideo();
        mIRtcVideoEventHandler = null;
        mIRtcRoomEventHandler = null;
        mRTCVideo = null;
    }

    private void onMessageReceived(String userID, String msg) {
        if(!mIsMessage) updateLocalMessageStatus();
        mMessageTv.append(userID + ": " + " " + msg + "        " +"\n");
        // ???????????? ??? simpleDateFormat.format(System.currentTimeMillis())
        ViewTreeObserver vto = mMessageTv.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int scrollAmount = mMessageTv.getLayout().getLineTop(mMessageTv.getLineCount())
                        - mMessageTv.getHeight();
                if (scrollAmount > 0) {
                    mMessageTv.scrollTo(0, scrollAmount);
                }else {
                    mMessageTv.scrollTo(0, 0);
                }
            }
        });
    }

    public void startScreenRecord(View view) {
        if (!screenCastAvailable) return;

        FilePickerManager.INSTANCE.from(this).maxSelectable(1).filter(new AbstractFileFilter() {
            @Override
            public ArrayList<FileItemBeanImpl> doFilter(ArrayList<FileItemBeanImpl> arrayList) {
                ArrayList<FileItemBeanImpl> res = new ArrayList<FileItemBeanImpl>();
                for (FileItemBeanImpl item : arrayList) {
                    if (item.isDir() || item.getFileType() instanceof VideoFileType)
                        res.add(item);
                }
                return res;
            }
        }).forResult(Constants.VIDEO_SELECTION);
    }
    private static final int REQUEST_CODE_OF_SCREEN_SHARING = 101;

    // ??????????????????????????????????????????
    public void requestForScreenSharing() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Log.e("ShareScreen","???????????????????????????????????????????????????");
            return;
        }
        MediaProjectionManager projectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        if (projectionManager != null) {
            startActivityForResult(projectionManager.createScreenCaptureIntent(), REQUEST_CODE_OF_SCREEN_SHARING);
        } else {
            Log.e("ShareScreen","???????????????????????????????????????????????????");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case (Constants.VIDEO_SELECTION):
                if (resultCode == RESULT_OK) {
                    mVideoPath = new StringBuilder();
                    for (String part : FilePickerManager.obtainData()) mVideoPath.append(part);
                    FilePickerManager.release();
                    requestForScreenSharing();
                }
                break;
            case(Constants.VIDEO_PLAY):
                // ??????????????????????????????????????????????????????????????????????????????
                stopScreenShare();
                break;
            default:
                break;
        }

        if (requestCode == REQUEST_CODE_OF_SCREEN_SHARING && resultCode == Activity.RESULT_OK) {
            Intent startVideo = new Intent(this, VideoPlayActivity.class);
            startVideo.putExtra("path", mVideoPath.toString());
            startActivityForResult(startVideo, Constants.VIDEO_PLAY);
            startScreenShare(data);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void startScreenShare(Intent data) {
        startRXScreenCaptureService(data);
        //????????????
        VideoEncoderConfig config = new VideoEncoderConfig();
        config.width = 1280;
        config.height = 720;
        config.frameRate = 15;
        config.maxBitrate = 1600;
        mRTCVideo.setScreenVideoEncoderConfig(config);
        // ??????????????????????????????
        mRTCVideo.startScreenCapture(ScreenMediaType.SCREEN_MEDIA_TYPE_VIDEO_AND_AUDIO, data);
    }

    private void startRXScreenCaptureService(@NonNull Intent data) {
        Context context = getApplicationContext();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Intent intent = new Intent();
            intent.putExtra(RXScreenCaptureService.KEY_LARGE_ICON, R.drawable.launcher_quick_start);
            intent.putExtra(RXScreenCaptureService.KEY_SMALL_ICON, R.drawable.launcher_quick_start);
            intent.putExtra(RXScreenCaptureService.KEY_LAUNCH_ACTIVITY, getClass().getCanonicalName());
            intent.putExtra(RXScreenCaptureService.KEY_CONTENT_TEXT, "????????????/??????????????????");
            intent.putExtra(RXScreenCaptureService.KEY_RESULT_DATA, data);
            context.startForegroundService(RXScreenCaptureService.getServiceIntent(context, RXScreenCaptureService.COMMAND_LAUNCH, intent));
        }
    }
}