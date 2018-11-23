package com.fanap.podchat.chat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.CursorLoader;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.fanap.podasync.Async;
import com.fanap.podasync.AsyncAdapter;
import com.fanap.podasync.model.Device;
import com.fanap.podasync.model.DeviceResult;
import com.fanap.podasync.util.JsonUtil;
import com.fanap.podchat.ProgressHandler;
import com.fanap.podchat.cachemodel.ThreadVo;
import com.fanap.podchat.mainmodel.AddParticipant;
import com.fanap.podchat.mainmodel.BaseMessage;
import com.fanap.podchat.mainmodel.ChatMessage;
import com.fanap.podchat.mainmodel.ChatMessageContent;
import com.fanap.podchat.mainmodel.ChatThread;
import com.fanap.podchat.mainmodel.Contact;
import com.fanap.podchat.mainmodel.CreateThreadRequest;
import com.fanap.podchat.mainmodel.FileUpload;
import com.fanap.podchat.mainmodel.History;
import com.fanap.podchat.mainmodel.Invitee;
import com.fanap.podchat.mainmodel.MapNeshan;
import com.fanap.podchat.mainmodel.MapRout;
import com.fanap.podchat.mainmodel.NosqlListMessageCriteriaVO;
import com.fanap.podchat.mainmodel.Participant;
import com.fanap.podchat.mainmodel.RemoveParticipant;
import com.fanap.podchat.mainmodel.ResultDeleteMessage;
import com.fanap.podchat.mainmodel.SearchContact;
import com.fanap.podchat.mainmodel.SearchContactVO;
import com.fanap.podchat.mainmodel.Thread;
import com.fanap.podchat.mainmodel.ThreadInfoVO;
import com.fanap.podchat.mainmodel.UpdateContact;
import com.fanap.podchat.mainmodel.UserInfo;
import com.fanap.podchat.model.AddContacts;
import com.fanap.podchat.model.ChatMessageForward;
import com.fanap.podchat.model.ChatResponse;
import com.fanap.podchat.model.ContactRemove;
import com.fanap.podchat.model.Contacts;
import com.fanap.podchat.model.DeleteMessageContent;
import com.fanap.podchat.model.Error;
import com.fanap.podchat.model.ErrorOutPut;
import com.fanap.podchat.model.FileImageMetaData;
import com.fanap.podchat.model.FileImageUpload;
import com.fanap.podchat.model.FileMetaDataContent;
import com.fanap.podchat.model.MessageVO;
import com.fanap.podchat.model.MetaDataFile;
import com.fanap.podchat.model.MetaDataImageFile;
import com.fanap.podchat.model.OutPutAddContact;
import com.fanap.podchat.model.OutPutHistory;
import com.fanap.podchat.model.OutPutInfoThread;
import com.fanap.podchat.model.OutPutMapNeshan;
import com.fanap.podchat.model.OutPutMapRout;
import com.fanap.podchat.model.OutPutThread;
import com.fanap.podchat.model.OutPutUpdateContact;
import com.fanap.podchat.model.ResultAddParticipant;
import com.fanap.podchat.model.ResultBlock;
import com.fanap.podchat.model.ResultBlockList;
import com.fanap.podchat.model.ResultContact;
import com.fanap.podchat.model.ResultFile;
import com.fanap.podchat.model.ResultHistory;
import com.fanap.podchat.model.ResultImageFile;
import com.fanap.podchat.model.ResultLeaveThread;
import com.fanap.podchat.model.ResultMap;
import com.fanap.podchat.model.ResultMessage;
import com.fanap.podchat.model.ResultMute;
import com.fanap.podchat.model.ResultNewMessage;
import com.fanap.podchat.model.ResultParticipant;
import com.fanap.podchat.model.ResultThread;
import com.fanap.podchat.model.ResultThreads;
import com.fanap.podchat.model.ResultUpdateContact;
import com.fanap.podchat.model.ResultUserInfo;
import com.fanap.podchat.networking.ProgressRequestBody;
import com.fanap.podchat.networking.api.ContactApi;
import com.fanap.podchat.networking.api.FileApi;
import com.fanap.podchat.networking.api.MapApi;
import com.fanap.podchat.networking.api.TokenApi;
import com.fanap.podchat.networking.retrofithelper.RetrofitHelperFileServer;
import com.fanap.podchat.networking.retrofithelper.RetrofitHelperMap;
import com.fanap.podchat.networking.retrofithelper.RetrofitHelperPlatformHost;
import com.fanap.podchat.networking.retrofithelper.RetrofitHelperSsoHost;
import com.fanap.podchat.persistance.MessageDatabaseHelper;
import com.fanap.podchat.requestobject.RequestThread;
import com.fanap.podchat.util.Callback;
import com.fanap.podchat.util.ChatConstant;
import com.fanap.podchat.util.ChatMessageType.Constants;
import com.fanap.podchat.util.ChatStateType;
import com.fanap.podchat.util.FilePick;
import com.fanap.podchat.util.LogHelper;
import com.fanap.podchat.util.Permission;
import com.fanap.podchat.util.Util;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.orhanobut.logger.Logger;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Response;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static com.fanap.podchat.util.ChatStateType.ChatSateConstant.ASYNC_READY;
import static com.fanap.podchat.util.ChatStateType.ChatSateConstant.CHAT_READY;
import static com.fanap.podchat.util.ChatStateType.ChatSateConstant.CLOSED;
import static com.fanap.podchat.util.ChatStateType.ChatSateConstant.CLOSING;
import static com.fanap.podchat.util.ChatStateType.ChatSateConstant.CONNECTING;
import static com.fanap.podchat.util.ChatStateType.ChatSateConstant.OPEN;

public class Chat extends AsyncAdapter {
    private static Async async;
    private static Moshi moshi;
    private String token;
    private String typeCode;
    private static Chat instance;
    private String platformHost;
    private static ChatListenerManager listenerManager;
    private static MessageDatabaseHelper messageDatabaseHelper;
    private long userId;
    private ContactApi contactApi;
    private static HashMap<String, Callback> messageCallbacks;
    private static HashMap<Long, ArrayList<Callback>> threadCallbacks;
    private static HashMap<String, ChatHandler> handlerSend;
    private boolean syncContact = false;
    private boolean state = false;
    private long lastSentMessageTime;
    private boolean chatState = false;
    private boolean chatReady = false;
    private boolean rawLog = false;
    private boolean asyncReady = false;
    private boolean hasNextContact = false;
    private boolean ping = true;
    private boolean cache = false;
    private static final int TOKEN_ISSUER = 1;
    private long retryStepUserInfo = 1;
    private Handler pingHandler;
    private static final Handler sUIThreadHandler;
    private static final Handler getUserInfoHandler;
    private boolean currentDeviceExist;
    private String fileServer;
    private Context context;
    private boolean syncContacts = false;
    private long nextOffsetContact = 0;
    private boolean log;
    private Gson gson;
    private ArrayList<Contact> serverContacts;
    private boolean checkToken = false;
    private boolean userInfoResponse = false;

    /**
     * Initialize the Chat
     **/
    public static Chat init(Context context) {

        if (instance == null) {

            async = Async.getInstance(context);
            instance = new Chat();
            instance.setContext(context);
            moshi = new Moshi.Builder().build();
            listenerManager = new ChatListenerManager();
            messageDatabaseHelper = new MessageDatabaseHelper(context);
        }
        return instance;
    }

    public void isLoggable(boolean log) {
        this.log = log;
        LogHelper.init(log);
        async.isLoggable(log);

    }

    public void socketLog(boolean log) {
        async.isLoggable(log);
    }

    public void rawLog(boolean rawLog) {
        this.rawLog = rawLog;
    }

    /**
     * Connect to the Async .
     *
     * @param socketAddress {**REQUIRED**}
     * @param platformHost  {**REQUIRED**}
     * @param severName     {**REQUIRED**}
     * @param appId         {**REQUIRED**}
     * @param token         {**REQUIRED**}
     * @param fileServer    {**REQUIRED**}
     * @param ssoHost       {**REQUIRED**}
     */
    public void connect(String socketAddress, String appId, String severName, String token,
                        String ssoHost, String platformHost, String fileServer, String typeCode) {
//        Looper.prepare();
        if (platformHost.endsWith("/")) {
            pingHandler = new Handler();
            messageCallbacks = new HashMap<>();
            threadCallbacks = new HashMap<>();
            handlerSend = new HashMap<>();
            async.addListener(this);
            RetrofitHelperPlatformHost retrofitHelperPlatformHost = new RetrofitHelperPlatformHost(platformHost, getContext());
            contactApi = retrofitHelperPlatformHost.getService(ContactApi.class);
            setPlatformHost(platformHost);
            setToken(token);
            setTypeCode(typeCode);
            setFileServer(fileServer);
            gson = new GsonBuilder().create();
            async.connect(socketAddress, appId, severName, token, ssoHost, "");
//            deviceIdRequest(ssoHost, socketAddress, appId, severName);
            state = true;
        } else {
            String jsonError = getErrorOutPut("PlatformHost " + ChatConstant.ERROR_CHECK_URL, ChatConstant.ERROR_CODE_CHECK_URL, null);
            if (log) Logger.e(jsonError);
        }
    }

    /**
     * When state of the Async changed then the chat ping is stopped buy (chatState)flag
     */
    @Override
    public void onStateChanged(String state) throws IOException {
        super.onStateChanged(state);
        listenerManager.callOnChatState(state);
        if (log) {
            Logger.i("Chat State is" + " " + state);
        }
        @ChatStateType.ChatSateConstant String currentChatState = state;
        switch (currentChatState) {
            case OPEN:
                chatState = true;
                break;
            case ASYNC_READY:
                asyncReady = true;

                retryOnGetUserInfo();
                break;
            case CONNECTING:
                chatReady = false;
            case CLOSING:
                chatReady = false;
            case CLOSED:
                chatState = false;
                chatReady = false;
                break;
        }
    }

    /**
     * First we check the message type and then we set the
     * the  specific callback for that
     */
    @Override
    public void onReceivedMessage(String textMessage) throws IOException {
        super.onReceivedMessage(textMessage);
        int messageType = 0;
        ChatMessage chatMessage = gson.fromJson(textMessage, ChatMessage.class);

        if (rawLog) {
            Logger.i("RAW_LOG");
            Logger.json(textMessage);
        }

        String messageUniqueId = chatMessage != null ? chatMessage.getUniqueId() : null;
        long threadId = chatMessage != null ? chatMessage.getSubjectId() : 0;

        Callback callback = messageCallbacks.get(messageUniqueId);

        if (chatMessage != null) {
            messageType = chatMessage.getType();
        }
        @Constants int currentMessageType = messageType;
        switch (currentMessageType) {
            case Constants.ADD_PARTICIPANT:
                handleResponseMessage(callback, chatMessage, messageUniqueId);
                break;
            case Constants.UNBLOCK:
                handleResponseMessage(callback, chatMessage, messageUniqueId);
                break;
            case Constants.BLOCK:
                handleResponseMessage(callback, chatMessage, messageUniqueId);
                break;
            case Constants.CHANGE_TYPE:
                break;
            case Constants.SENT:
                handleSent(chatMessage, messageUniqueId, threadId);
                break;
            case Constants.DELIVERY:
                handleDelivery(chatMessage, messageUniqueId, threadId);
                break;
            case Constants.SEEN:
                handleSeen(chatMessage, messageUniqueId, threadId);
                break;
            case Constants.ERROR:
                handleError(chatMessage);
                break;
            case Constants.FORWARD_MESSAGE:
                handleForwardMessage(chatMessage);
                break;
            case Constants.GET_CONTACTS:
                handleResponseMessage(callback, chatMessage, messageUniqueId);
                break;
            case Constants.GET_HISTORY:
                handleResponseMessage(callback, chatMessage, messageUniqueId);
                break;
            case Constants.GET_STATUS:
                break;
            case Constants.GET_THREADS:
                handleResponseMessage(callback, chatMessage, messageUniqueId);
                break;
            case Constants.INVITATION:
                handleResponseMessage(callback, chatMessage, messageUniqueId);
                break;
            case Constants.REMOVED_FROM_THREAD:

                handleRemoveFromThread(chatMessage);
                break;
            case Constants.LEAVE_THREAD:
                handleResponseMessage(callback, chatMessage, messageUniqueId);
                break;
            case Constants.MESSAGE:
                handleNewMessage(chatMessage);
                break;
            case Constants.MUTE_THREAD:
                handleResponseMessage(callback, chatMessage, messageUniqueId);
                break;
            case Constants.PING:
                handleOnPing(chatMessage);
                break;
            case Constants.RELATION_INFO:
                break;
            case Constants.REMOVE_PARTICIPANT:
                handleResponseMessage(callback, chatMessage, messageUniqueId);
                break;
            case Constants.RENAME:
                handleResponseMessage(callback, chatMessage, messageUniqueId);
                break;
            case Constants.THREAD_PARTICIPANTS:
                handleResponseMessage(callback, chatMessage, messageUniqueId);
                break;
            case Constants.UN_MUTE_THREAD:
                handleResponseMessage(callback, chatMessage, messageUniqueId);
                break;
            case Constants.USER_INFO:
                handleResponseMessage(callback, chatMessage, messageUniqueId);
                break;
            case Constants.USER_STATUS:
                break;
            case Constants.GET_BLOCKED:
                handleResponseMessage(callback, chatMessage, messageUniqueId);
                break;
            case Constants.DELETE_MESSAGE:
                handleResponseMessage(callback, chatMessage, messageUniqueId);
                break;
            case Constants.EDIT_MESSAGE:
                handleResponseMessage(callback, chatMessage, messageUniqueId);
                break;
            case Constants.THREAD_INFO_UPDATED:

                handleThreadInfoUpdated(chatMessage);
                break;
            case Constants.LAST_SEEN_UPDATED:

                handleLastSeenUpdated(chatMessage);
                break;
            case Constants.UPDATE_THREAD_INFO:

                handleResponseMessage(callback, chatMessage, messageUniqueId);
                break;
            case Constants.SPAM_PV_THREAD:
                break;
        }
    }

    @Override
    public void onError(String textMessage) throws IOException {
        super.onError(textMessage);
        if (log) Logger.e(textMessage);
    }

    /**
     * Send text message to the thread
     *
     * @param textMessage  String that we want to sent to the thread
     * @param threadId     Id of the destination thread
     * @param JsonMetaData It should be Json,if you don't have metaData you can set it to "null"
     */
    public String sendTextMessage(String textMessage, long threadId, Integer messageType, String JsonMetaData, String typeCode, ChatHandler handler) {

        String asyncContent = null;
        String uniqueId = null;
        if (chatReady) {
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setContent(textMessage);
            chatMessage.setType(Constants.MESSAGE);
            chatMessage.setTokenIssuer("1");
            chatMessage.setToken(getToken());
            if (messageType != null) {
                chatMessage.setMessageType(messageType);
            }

            if (typeCode != null && !typeCode.isEmpty()) {
                chatMessage.setTypeCode(typeCode);
            } else {
                chatMessage.setTypeCode(getTypeCode());
            }

            if (JsonMetaData != null) {
                chatMessage.setSystemMetadata(JsonMetaData);
            }

            uniqueId = generateUniqueId();
            chatMessage.setUniqueId(uniqueId);
            chatMessage.setTime(1000);
            chatMessage.setSubjectId(threadId);

            JsonAdapter<ChatMessage> chatMessageJsonAdapter = moshi.adapter(ChatMessage.class);
            asyncContent = chatMessageJsonAdapter.toJson(chatMessage);
            setThreadCallbacks(threadId, uniqueId);

            if (handler != null) {

                handler.onSent(uniqueId, threadId);
                handler.onSentResult(null);
                handlerSend.put(uniqueId, handler);
            }

        }
        sendAsyncMessage(asyncContent, 4, "SEND_TEXT_MESSAGE");
        return uniqueId;
    }

    /**
     * First we get the contact from server then at the respond of that
     * {@link #handleSyncContact(ChatMessage, Callback)} we add all of the PhoneContact that get from
     * {@link #getPhoneContact(Context)} that's not in the list of serverContact.
     */
    public void syncContact(Context context, Activity activity) {
        if (Permission.Check_READ_CONTACTS(activity)) {
            syncContact = true;
            serverContacts = new ArrayList<>();
            getContacts(50, 0L, getTypeCode(), null);
            setContext(context);
        } else {
            String jsonError = getErrorOutPut(ChatConstant.ERROR_READ_CONTACT_PERMISSION, ChatConstant.ERROR_CODE_READ_CONTACT_PERMISSION, null);
            if (log) Logger.e(jsonError);
        }
    }

    /**
     * This method first check the type of the file and then choose the right
     * server and send that
     *
     * @param description Its the description that you want to send with file in the thread
     * @param fileUri     Uri of the file that you want to send to thread
     * @param threadId    Id of the thread that you want to send file
     * @param metaData    [optional]
     */
    public String sendFileMessage(Context context, Activity activity, String description, long threadId, Uri fileUri, String metaData, String typeCode, Integer messageType) {
        String uniqueId;
        metaData = metaData != null ? metaData : "";
        try {
            if (chatReady) {
                if (fileUri != null) {
                    uniqueId = generateUniqueId();
                    File file = new File(fileUri.getPath());
                    String mimeType = handleMimType(fileUri, file);
                    if (mimeType.equals("image/png") || mimeType.equals("image/jpeg")) {
                        uploadImageFileMessage(context, activity, description, threadId, fileUri, mimeType, metaData, uniqueId, typeCode, messageType);
                    } else {
                        String path = FilePick.getSmartFilePath(context, fileUri);
                        uploadFileMessage(activity, description, threadId, mimeType, path, metaData, uniqueId, typeCode, messageType);
                    }
                    return uniqueId;
                }
            }
        } catch (Exception e) {
            if (log) Logger.e(e.getCause().getMessage());
            return null;
        }

        return null;
    }


    //TODO error code
    public String uploadImageProgress(Context context, Activity activity, Uri fileUri, ProgressHandler.onProgress handler) {
        String uniqueId;
        if (fileServer != null) {
            if (Permission.Check_READ_STORAGE(activity)) {
                uniqueId = generateUniqueId();
                String mimeType = context.getContentResolver().getType(fileUri);
                RetrofitHelperFileServer retrofitHelperFileServer = new RetrofitHelperFileServer(getFileServer());
                FileApi fileApi = retrofitHelperFileServer.getService(FileApi.class);
                File file = new File(getRealPathFromURI(context, fileUri));
                if (mimeType.equals("image/png") || mimeType.equals("image/jpeg")) {

                    ProgressRequestBody requestFile = new ProgressRequestBody(file, mimeType, new ProgressRequestBody.UploadCallbacks() {

                        @Override
                        public void onProgressUpdate(int percentage) {
                            handler.onProgressUpdate(percentage);
                        }

                        @Override
                        public void onError() {

                        }

                        @Override
                        public void onFinish() {

                        }
                    });

                    MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), requestFile);
                    RequestBody name = RequestBody.create(MediaType.parse("text/plain"), file.getName());

                    Observable<Response<FileImageUpload>> uploadObservable = fileApi.sendImageFile(body, getToken(), TOKEN_ISSUER, name);
                    uploadObservable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Response<FileImageUpload>>() {
                        @Override
                        public void call(Response<FileImageUpload> fileUploadResponse) {
                            if (fileUploadResponse.isSuccessful()) {
                                boolean hasError = fileUploadResponse.body().isHasError();
                                if (hasError) {
                                    String errorMessage = fileUploadResponse.body().getMessage();
                                    int errorCode = fileUploadResponse.body().getErrorCode();
                                    String jsonError = getErrorOutPut(errorMessage, errorCode, null);
                                    if (log) Logger.e(jsonError);
                                } else {
                                    FileImageUpload fileImageUpload = fileUploadResponse.body();

                                    ChatResponse<ResultImageFile> chatResponse = new ChatResponse<>();
                                    ResultImageFile resultImageFile = new ResultImageFile();
                                    chatResponse.setUniqueId(uniqueId);
                                    resultImageFile.setId(fileImageUpload.getResult().getId());
                                    resultImageFile.setHashCode(fileImageUpload.getResult().getHashCode());
                                    resultImageFile.setName(fileImageUpload.getResult().getName());
                                    resultImageFile.setHeight(fileImageUpload.getResult().getHeight());
                                    resultImageFile.setWidth(fileImageUpload.getResult().getWidth());
                                    resultImageFile.setActualHeight(fileImageUpload.getResult().getActualHeight());
                                    resultImageFile.setActualWidth(fileImageUpload.getResult().getActualWidth());

                                    chatResponse.setResult(resultImageFile);

                                    String imageJson = gson.toJson(chatResponse);

                                    if (log) Logger.i("RECEIVE_UPLOAD_IMAGE");
                                    if (log) Logger.json(imageJson);

                                    handler.onFinish(imageJson, chatResponse);
                                }
                            }
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            ErrorOutPut error = new ErrorOutPut(true, throwable.getMessage(), 0, null);
                            String jsonError = JsonUtil.getJson(error);
                            handler.onError(jsonError, error);
                            if (log) Logger.e(throwable.getMessage());
                        }
                    });
                } else {
                    String jsonError = getErrorOutPut(ChatConstant.ERROR_NOT_IMAGE, ChatConstant.ERROR_CODE_NOT_IMAGE, null);
                    ErrorOutPut error = new ErrorOutPut(true, ChatConstant.ERROR_NOT_IMAGE, ChatConstant.ERROR_CODE_NOT_IMAGE, null);
                    handler.onError(jsonError, error);
                    if (log) Logger.e(jsonError);
                    return null;
                }
            } else {
                String jsonError = getErrorOutPut(ChatConstant.ERROR_READ_EXTERNAL_STORAGE_PERMISSION, ChatConstant.ERROR_CODE_READ_EXTERNAL_STORAGE_PERMISSION, null);
                ErrorOutPut error = new ErrorOutPut(true, ChatConstant.ERROR_NOT_IMAGE, ChatConstant.ERROR_CODE_NOT_IMAGE, null);
                handler.onError(jsonError, error);
                if (log) Logger.e(jsonError);
                return null;
            }
        } else {
            String jsonError = getErrorOutPut(ChatConstant.ERROR_READ_EXTERNAL_STORAGE_PERMISSION, ChatConstant.ERROR_CODE_READ_EXTERNAL_STORAGE_PERMISSION, null);
            ErrorOutPut error = new ErrorOutPut(true, ChatConstant.ERROR_READ_EXTERNAL_STORAGE_PERMISSION, ChatConstant.ERROR_CODE_READ_EXTERNAL_STORAGE_PERMISSION, null);
            handler.onError(jsonError, error);
            if (log) Logger.e("FileServer url Is null");
            return null;
        }
        return uniqueId;
    }


    public String uploadImage(Activity activity, Uri fileUri) {
        String uniqueId = null;
        try {
            if (fileServer != null && fileUri != null) {
                if (Permission.Check_READ_STORAGE(activity)) {
                    String path = FilePick.getSmartFilePath(getContext(), fileUri);
                    File file = new File(path);
                    if (file.exists()) {
                        uniqueId = generateUniqueId();
                        String mimeType = handleMimType(fileUri, file);
                        if (mimeType.equals("image/png") || mimeType.equals("image/jpeg")) {
                            RetrofitHelperFileServer retrofitHelperFileServer = new RetrofitHelperFileServer(getFileServer());
                            FileApi fileApi = retrofitHelperFileServer.getService(FileApi.class);
                            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
                            MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), requestFile);
                            RequestBody name = RequestBody.create(MediaType.parse("text/plain"), file.getName());

                            Observable<Response<FileImageUpload>> uploadObservable = fileApi.sendImageFile(body, getToken(), TOKEN_ISSUER, name);
                            String finalUniqueId = uniqueId;
                            uploadObservable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Response<FileImageUpload>>() {
                                @Override
                                public void call(Response<FileImageUpload> fileUploadResponse) {
                                    if (fileUploadResponse.isSuccessful()) {
                                        boolean hasError = fileUploadResponse.body().isHasError();
                                        if (hasError) {
                                            String errorMessage = fileUploadResponse.body().getMessage();
                                            int errorCode = fileUploadResponse.body().getErrorCode();
                                            String jsonError = getErrorOutPut(errorMessage, errorCode, null);
                                            if (log) Logger.e(jsonError);
                                        } else {
                                            FileImageUpload fileImageUpload = fileUploadResponse.body();
                                            ChatResponse<ResultImageFile> chatResponse = new ChatResponse<>();
                                            ResultImageFile resultImageFile = new ResultImageFile();
                                            chatResponse.setUniqueId(finalUniqueId);
                                            resultImageFile.setId(fileImageUpload.getResult().getId());
                                            resultImageFile.setHashCode(fileImageUpload.getResult().getHashCode());
                                            resultImageFile.setName(fileImageUpload.getResult().getName());
                                            resultImageFile.setHeight(fileImageUpload.getResult().getHeight());
                                            resultImageFile.setWidth(fileImageUpload.getResult().getWidth());
                                            resultImageFile.setActualHeight(fileImageUpload.getResult().getActualHeight());
                                            resultImageFile.setActualWidth(fileImageUpload.getResult().getActualWidth());

                                            chatResponse.setResult(resultImageFile);

                                            String imageJson = gson.toJson(chatResponse);

                                            listenerManager.callOnUploadImageFile(imageJson, chatResponse);
                                            if (log) Logger.i("RECEIVE_UPLOAD_IMAGE");
                                            if (log) Logger.json(imageJson);
                                        }
                                    }
                                }
                            }, throwable -> {
                                String jsonError = getErrorOutPut(ChatConstant.ERROR_UNKNOWN_EXCEPTION, ChatConstant.ERROR_CODE_UNKNOWN_EXCEPTION, null);
                                if (log) Logger.e(jsonError);
                            });
                        } else {
                            String jsonError = getErrorOutPut(ChatConstant.ERROR_NOT_IMAGE, ChatConstant.ERROR_CODE_NOT_IMAGE, null);
                            if (log) Logger.e(jsonError);
                            uniqueId = null;
                        }
                    }
                } else {
                    String jsonError = getErrorOutPut(ChatConstant.ERROR_READ_EXTERNAL_STORAGE_PERMISSION, ChatConstant.ERROR_CODE_READ_EXTERNAL_STORAGE_PERMISSION, null);
                    if (log) Logger.e(jsonError);
                    uniqueId = null;
                }
            } else {
                if (log) Logger.e("FileServer url Is null");
                uniqueId = null;
            }
        } catch (Exception e) {
            if (log) Logger.e(e.getCause().getMessage());
            uniqueId = null;
        }
        return uniqueId;
    }

    //TODO size problem
    public String uploadFile(Activity activity, Uri uri) {
        String uniqueId;
        try {
            if (Permission.Check_READ_STORAGE(activity)) {
                if (getFileServer() != null) {
                    String path = FilePick.getSmartFilePath(getContext(), uri);
                    File file = new File(path);
                    String mimeType = handleMimType(uri, file);
                    if (file.exists()) {
                        uniqueId = generateUniqueId();
                        long fileSize = file.length();
                        RetrofitHelperFileServer retrofitHelperFileServer = new RetrofitHelperFileServer(getFileServer());
                        FileApi fileApi = retrofitHelperFileServer.getService(FileApi.class);
                        RequestBody name = RequestBody.create(MediaType.parse("text/plain"), file.getName());
                        RequestBody requestFile = RequestBody.create(MediaType.parse(mimeType), file);

                        MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);
                        Observable<Response<FileUpload>> uploadObservable = fileApi.sendFile(body, getToken(), TOKEN_ISSUER, name);
                        uploadObservable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Response<FileUpload>>() {
                            @Override
                            public void call(Response<FileUpload> fileUploadResponse) {
                                if (fileUploadResponse.isSuccessful()) {
                                    boolean hasError = fileUploadResponse.body().isHasError();
                                    if (hasError) {
                                        String errorMessage = fileUploadResponse.body().getMessage();
                                        int errorCode = fileUploadResponse.body().getErrorCode();
                                        String jsonError = getErrorOutPut(errorMessage, errorCode, null);
                                        if (log) Logger.e(jsonError);
                                    } else {
                                        ResultFile result = fileUploadResponse.body().getResult();

                                        ChatResponse<ResultFile> chatResponse = new ChatResponse<>();
                                        result.setSize(fileSize);
                                        chatResponse.setUniqueId(uniqueId);
                                        chatResponse.setResult(result);
                                        String json = gson.toJson(chatResponse);

                                        listenerManager.callOnUploadFile(json, chatResponse);
                                        if (log) Logger.i("RECEIVE_UPLOAD_FILE");
                                        if (log) Logger.json(json);
                                    }
                                }
                            }
                        }, throwable -> {
                            String jsonError = getErrorOutPut(throwable.getCause().getMessage(), ChatConstant.ERROR_CODE_UNKNOWN_EXCEPTION, null);
                            if (log) Logger.e(jsonError);
                        });
                    } else {
                        if (log) Logger.e("File is not Exist");
                        return null;
                    }
                } else {
                    if (log) Logger.e("FileServer url Is null");
                    return null;
                }
            } else {
                String jsonError = getErrorOutPut(ChatConstant.ERROR_READ_EXTERNAL_STORAGE_PERMISSION, ChatConstant.ERROR_CODE_READ_EXTERNAL_STORAGE_PERMISSION, null);
                if (log) Logger.e(jsonError);
                return null;
            }
        } catch (Exception e) {
            if (log) Logger.e(e.getCause().getMessage());
            return null;
        }
        return uniqueId;
    }

    //Todo error code
    public void uploadFileProgress(Context context, Activity activity, String fileUri, Uri uri, ProgressHandler.onProgressFile handler) {
        if (Permission.Check_READ_STORAGE(activity)) {
            if (getFileServer() != null) {
                String mimeType = context.getContentResolver().getType(uri);
                File file = new File(fileUri);
                RetrofitHelperFileServer retrofitHelperFileServer = new RetrofitHelperFileServer(getFileServer());
                FileApi fileApi = retrofitHelperFileServer.getService(FileApi.class);
                RequestBody name = RequestBody.create(MediaType.parse("text/plain"), file.getName());
                ProgressRequestBody requestFile = new ProgressRequestBody(file, mimeType, new ProgressRequestBody.UploadCallbacks() {
                    @Override
                    public void onProgressUpdate(int percentage) {
                        handler.onProgressUpdate(percentage);
                    }

                    @Override
                    public void onError() {

                    }

                    @Override
                    public void onFinish() {

                    }
                });


                MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);
                Observable<Response<FileUpload>> uploadObservable = fileApi.sendFile(body, getToken(), TOKEN_ISSUER, name);
                uploadObservable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Response<FileUpload>>() {
                    @Override
                    public void call(Response<FileUpload> fileUploadResponse) {
                        if (fileUploadResponse.isSuccessful()) {
                            boolean hasError = fileUploadResponse.body().isHasError();
                            if (hasError) {
                                String errorMessage = fileUploadResponse.body().getMessage();
                                int errorCode = fileUploadResponse.body().getErrorCode();
                                String jsonError = getErrorOutPut(errorMessage, errorCode, null);
                                ErrorOutPut error = new ErrorOutPut(true, errorMessage, errorCode, null);
                                handler.onError(jsonError, error);
                                if (log) Logger.e(jsonError);
                            } else {
                                FileUpload result = fileUploadResponse.body();
                                String json = JsonUtil.getJson(result);
//                                listenerManager.callOnUploadFile(json,);

                                handler.onFinish(json, result);

                                if (log) Logger.json(json);
                            }
                        }
                    }
                }, throwable -> {
                    ErrorOutPut error = new ErrorOutPut(true, throwable.getMessage(), 0, null);
                    String json = JsonUtil.getJson(error);
                    handler.onError(json, error);
                    if (log) Logger.e(throwable.getMessage());
                });
            } else {

                if (log) Logger.e("FileServer url Is null");
            }

        } else {
            String jsonError = getErrorOutPut(ChatConstant.ERROR_READ_EXTERNAL_STORAGE_PERMISSION, ChatConstant.ERROR_CODE_READ_EXTERNAL_STORAGE_PERMISSION, null);
            ErrorOutPut error = new ErrorOutPut(true, ChatConstant.ERROR_READ_EXTERNAL_STORAGE_PERMISSION, ChatConstant.ERROR_CODE_READ_EXTERNAL_STORAGE_PERMISSION, null);
            handler.onError(jsonError, error);
            if (log) Logger.e(jsonError);
        }
    }

    public String getFile(long fileId, String hashCode, boolean downloadable) {
        String url = getFileServer() + "nzh/file/" + "?fileId=" + fileId + "&downloadable=" + downloadable + "&hashCode=" + hashCode;
        return url;
    }

    public String getImage(long imageId, String hashCode, boolean downloadable) {
        String url = getFileServer() + "nzh/image/" + "?imageId=" + imageId + "&downloadable=" + downloadable + "&hashCode=" + hashCode;
        return url;
    }

    /**
     * Remove the peerId and send ping again but this time
     * peerId that was set in the server was removed
     */
    public void logOutSocket() {
        async.logOut();
    }

    /**
     * Notice : You should consider that this method is for rename group and you have to be the admin
     * to change the thread name if not you don't have the Permission
     */
    @Deprecated
    public String renameThread(long threadId, String title, ChatHandler handler) {
        String uniqueId = generateUniqueId();
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setType(Constants.RENAME);
        chatMessage.setSubjectId(threadId);
        chatMessage.setContent(title);
        chatMessage.setToken(getToken());
        chatMessage.setTokenIssuer("1");
        chatMessage.setUniqueId(uniqueId);
        setCallBacks(null, null, null, true, Constants.RENAME, null, uniqueId);
        String asyncContent = JsonUtil.getJson(chatMessage);
        sendAsyncMessage(asyncContent, 4, "SEND_RENAME_THREAD");
        if (handler != null) {
            handler.onRenameThread(uniqueId);
        }
        return uniqueId;
    }

    /**
     * @param contactIds List of CONTACT IDs
     * @param threadId   Id of the thread that you are {*NOTICE*}admin of that and you want to
     *                   add someone as a participant.
     */
    public String addParticipants(long threadId, List<Long> contactIds, String typeCode, ChatHandler handler) {

        AddParticipant addParticipant = new AddParticipant();
        String uniqueId = generateUniqueId();
        addParticipant.setSubjectId(threadId);
        addParticipant.setUniqueId(uniqueId);
        JsonArray contacts = new JsonArray();
        for (Long p : contactIds) {
            contacts.add(p);
        }
        addParticipant.setContent(contacts.toString());
        addParticipant.setSubjectId(threadId);
        addParticipant.setToken(getToken());
        addParticipant.setTokenIssuer("1");
        addParticipant.setUniqueId(uniqueId);
        if (typeCode != null && !typeCode.isEmpty()) {
            addParticipant.setTypeCode(typeCode);
        } else {
            addParticipant.setTypeCode(getTypeCode());
        }

        addParticipant.setType(Constants.ADD_PARTICIPANT);
        String asyncContent = JsonUtil.getJson(addParticipant);
        setCallBacks(null, null, null, true, Constants.ADD_PARTICIPANT, null, uniqueId);
        sendAsyncMessage(asyncContent, 4, "SEND_ADD_PARTICIPANTS");
        if (handler != null) {
            handler.onAddParticipants(uniqueId);
        }

        return uniqueId;
    }

    /**
     * @param participantIds List of PARTICIPANT IDs from Thread's Participants object
     * @param threadId       Id of the thread that we wants to remove their participant
     */
    public String removeParticipants(long threadId, List<Long> participantIds, String typeCode, ChatHandler handler) {

        String uniqueId = generateUniqueId();
        RemoveParticipant removeParticipant = new RemoveParticipant();
        removeParticipant.setTokenIssuer("1");
        removeParticipant.setType(Constants.REMOVE_PARTICIPANT);
        removeParticipant.setSubjectId(threadId);
        removeParticipant.setToken(getToken());
        removeParticipant.setUniqueId(uniqueId);

        if (typeCode != null && !typeCode.isEmpty()) {
            removeParticipant.setTypeCode(typeCode);
        } else {
            removeParticipant.setTypeCode(getTypeCode());
        }

        JsonArray contacts = new JsonArray();
        for (Long p : participantIds) {
            contacts.add(p);
        }
        removeParticipant.setContent(contacts.toString());

        String asyncContent = JsonUtil.getJson(removeParticipant);
        sendAsyncMessage(asyncContent, 4, "SEND_REMOVE_PARTICIPANT");
        setCallBacks(null, null, null, true, Constants.REMOVE_PARTICIPANT, null, uniqueId);
        if (handler != null) {
            handler.onRemoveParticipants(uniqueId);
        }
        return uniqueId;
    }

    public String leaveThread(long threadId, String typeCode, ChatHandler handler) {
        String uniqueId = generateUniqueId();
        RemoveParticipant removeParticipant = new RemoveParticipant();

        removeParticipant.setSubjectId(threadId);
        removeParticipant.setToken(getToken());
        removeParticipant.setTokenIssuer("1");
        removeParticipant.setUniqueId(uniqueId);
        removeParticipant.setType(Constants.LEAVE_THREAD);

        if (typeCode != null && !typeCode.isEmpty()) {
            removeParticipant.setTypeCode(typeCode);
        } else {
            removeParticipant.setTypeCode(getTypeCode());
        }

        setCallBacks(null, null, null, true, Constants.LEAVE_THREAD, null, uniqueId);
        String json = JsonUtil.getJson(removeParticipant);
        sendAsyncMessage(json, 4, "SEND_LEAVE_THREAD");
        if (handler != null) {
            handler.onLeaveThread(uniqueId);
        }
        return uniqueId;
    }

    /**
     * forward message
     *
     * @param threadId   destination thread id
     * @param messageIds Array of message ids that we want to forward them
     */
    public List<String> forwardMessage(long threadId, ArrayList<Long> messageIds) {
        ChatMessageForward chatMessageForward = new ChatMessageForward();
        ObjectMapper mapper = new ObjectMapper();
        ArrayList<String> uniqueIds = new ArrayList<>();
        chatMessageForward.setSubjectId(threadId);
        ArrayList<Callback> callbacks = new ArrayList<>();

        for (int i = 0; i < messageIds.size(); i++) {
            String uniqueId = generateUniqueId();
            uniqueIds.add(uniqueId);
            Callback callback = new Callback();
            callback.setDelivery(true);
            callback.setSeen(true);
            callback.setSent(true);
            callback.setUniqueId(uniqueId);
            callbacks.add(callback);
        }
        threadCallbacks.put(threadId, callbacks);
        try {
            chatMessageForward.setUniqueId(mapper.writeValueAsString(uniqueIds));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        chatMessageForward.setContent(messageIds.toString());
        chatMessageForward.setToken(getToken());
        chatMessageForward.setTokenIssuer("1");
        chatMessageForward.setType(Constants.FORWARD_MESSAGE);
        if (typeCode != null && !typeCode.isEmpty()) {
            chatMessageForward.setTypeCode(typeCode);
        } else {
            chatMessageForward.setTypeCode(getTypeCode());
        }
        String asyncContent = JsonUtil.getJson(chatMessageForward);
        sendAsyncMessage(asyncContent, 4, "SEND_FORWARD_MESSAGE");
        return uniqueIds;
    }

    /**
     * Reply the message in the current thread and send az message and receive at the
     *
     * @param messageContent content of the reply message
     * @param threadId       id of the thread
     * @param messageId      of the message that we want to reply
     * @param metaData       meta data of the message
     */
    public String replyMessage(String messageContent, long threadId, long messageId, String metaData, String typeCode, ChatHandler handler) {
        String uniqueId = generateUniqueId();

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setUniqueId(uniqueId);
        chatMessage.setRepliedTo(messageId);
        chatMessage.setSubjectId(threadId);
        chatMessage.setTokenIssuer("1");
        chatMessage.setToken(getToken());
        chatMessage.setContent(messageContent);
        chatMessage.setTime(1000);
        chatMessage.setType(Constants.MESSAGE);
        if (metaData != null) {
            chatMessage.setSystemMetadata(metaData);
        }
        if (typeCode != null && !typeCode.isEmpty()) {
            chatMessage.setTypeCode(typeCode);
        } else {
            chatMessage.setTypeCode(getTypeCode());
        }
        chatMessage.setSystemMetadata(metaData);
        JsonAdapter<ChatMessage> chatMessageJsonAdapter = moshi.adapter(ChatMessage.class);
        String asyncContent = chatMessageJsonAdapter.toJson(chatMessage);

        setThreadCallbacks(threadId, uniqueId);
        sendAsyncMessage(asyncContent, 4, "SEND_REPLY_MESSAGE");
        if (handler != null) {
            handler.onReplyMessage(uniqueId);
        }
        return uniqueId;
    }

    /**
     * DELETE MESSAGE IN THREAD
     *
     * @param messageId    Id of the message that you want to be removed.
     * @param deleteForAll If you want to delete message for everyone you can set it true if u don't want
     *                     you can set it false or even null.
     */
    public String deleteMessage(long messageId, Boolean deleteForAll, ChatHandler handler) {
        deleteForAll = deleteForAll != null ? deleteForAll : false;
        String uniqueId = generateUniqueId();
        BaseMessage baseMessage = new BaseMessage();
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setDeleteForAll(deleteForAll);
        String content = JsonUtil.getJson(deleteMessage);
        baseMessage.setContent(content);
        baseMessage.setSubjectId(messageId);
        baseMessage.setToken(getToken());
        baseMessage.setTokenIssuer("1");
        baseMessage.setType(Constants.DELETE_MESSAGE);
        baseMessage.setUniqueId(uniqueId);
        if (typeCode != null && !typeCode.isEmpty()) {
            baseMessage.setTypeCode(typeCode);
        } else {
            baseMessage.setTypeCode(getTypeCode());
        }
        String asyncContent = JsonUtil.getJson(baseMessage);
        sendAsyncMessage(asyncContent, 4, "SEND_DELETE_MESSAGE");
        setCallBacks(null, null, null, true, Constants.DELETE_MESSAGE, null, uniqueId);
        if (handler != null) {
            handler.onDeleteMessage(uniqueId);
        }
        return uniqueId;
    }

    /**
     * Get the list of threads or you can just pass the thread id that you want
     *
     * @param count  number of thread
     * @param offset specified offset you want
     */
    public String getThreads(Integer count, Long offset, ArrayList<Integer> threadIds, String threadName, String typeCode, ChatHandler handler) {
        String uniqueId = generateUniqueId();
        Long offsets = offset;
        count = count != null ? count : 50;
        offset = offset != null ? offset : 0;
        try {
            if (cache) {
                if (messageDatabaseHelper.getThreads(count, offset) != null) {
                    List<Thread> threads = messageDatabaseHelper.getThreads(count, offset);
                    ChatResponse<ResultThreads> chatResponse = new ChatResponse<>();
                    int contentCount = messageDatabaseHelper.getThreadCount();

                    ResultThreads resultThreads = new ResultThreads();
                    resultThreads.setThreads(threads);
                    resultThreads.setContentCount(contentCount);
                    chatResponse.setErrorCode(0);
                    chatResponse.setErrorMessage("");
                    chatResponse.setHasError(false);
                    chatResponse.setCache(true);

                    if (threads.size() + offset < contentCount) {
                        resultThreads.setHasNext(true);
                    } else {
                        resultThreads.setHasNext(false);
                    }
                    resultThreads.setNextOffset(offset + threads.size());
                    chatResponse.setResult(resultThreads);

                    String threadJson = gson.toJson(chatResponse);
                    listenerManager.callOnGetThread(threadJson, chatResponse);
                    if (log) Logger.i("CACHE_GET_THREAD");
                    if (log) Logger.json(threadJson);
                }
            }
            ChatMessageContent chatMessageContent = new ChatMessageContent();

            if (offset == null) {
                chatMessageContent.setOffset(0);
                offsets = 0L;
            } else {
                chatMessageContent.setOffset(offset);
            }

            if (threadName != null) {
                chatMessageContent.setName(threadName);
            }

            JsonObject jObj;

            if (threadIds != null && threadIds.size() > 0) {
                chatMessageContent.setThreadIds(threadIds);
                jObj = (JsonObject) gson.toJsonTree(chatMessageContent);

            } else {
                jObj = (JsonObject) gson.toJsonTree(chatMessageContent);
                jObj.remove("threadIds");

            }

            jObj.remove("lastMessageId");
            jObj.remove("firstMessageId");

            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setContent(jObj.toString());
            chatMessage.setType(Constants.GET_THREADS);
            chatMessage.setTokenIssuer("1");
            chatMessage.setToken(getToken());
            chatMessage.setUniqueId(uniqueId);
            if (typeCode != null && !typeCode.isEmpty()) {
                chatMessage.setTypeCode(typeCode);
            } else {
                chatMessage.setTypeCode(getTypeCode());
            }

            JsonAdapter<ChatMessage> chatMessageJsonAdapter = moshi.adapter(ChatMessage.class);
            String asyncContent = chatMessageJsonAdapter.toJson(chatMessage);
            setCallBacks(null, null, null, true, Constants.GET_THREADS, offsets, uniqueId);

            sendAsyncMessage(asyncContent, 3, "Get thread send");
            if (handler != null) {
                handler.onGetThread(uniqueId);
            }
            return uniqueId;
        } catch (Exception e) {
            Logger.e(e.getCause().getMessage());
        }
        return uniqueId;
    }

    //TODO working progress
    public String getThreads(RequestThread requestThread) {
        String uniqueId = null;

        return uniqueId;
    }

    /**
     * Get history of the thread
     * <p>
     * count    count of the messages
     * order    If order is empty [default = desc] and also you have two option [ asc | desc ]
     * lastMessageId
     * FirstMessageId
     *
     * @param threadId Id of the thread that we want to get the history
     */
    public String getHistory(History history, long threadId, String typeCode, ChatHandler handler) {

        long offsets = history.getOffset();

        if (history.getCount() != 0) {
            history.setCount(history.getCount());
        } else {
            history.setCount(50L);
        }

        if (history.getOffset() != 0) {
            history.setOffset(history.getOffset());
        } else {
            history.setOffset(0L);
            offsets = 0;
        }

        JsonObject jObj = (JsonObject) gson.toJsonTree(history);
        if (history.getLastMessageId() == 0) {
            jObj.remove("lastMessageId");
        }

        if (history.getFirstMessageId() == 0) {
            jObj.remove("firstMessageId");
        }

        String uniqueId = generateUniqueId();

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setContent(jObj.toString());
        chatMessage.setType(Constants.GET_HISTORY);
        chatMessage.setToken(getToken());
        chatMessage.setTokenIssuer("1");
        chatMessage.setUniqueId(uniqueId);
        chatMessage.setSubjectId(threadId);
        if (typeCode != null && !typeCode.isEmpty()) {
            chatMessage.setTypeCode(typeCode);
        } else {
            chatMessage.setTypeCode(getTypeCode());
        }
        JsonAdapter<ChatMessage> chatMessageJsonAdapter = moshi.adapter(ChatMessage.class);
        String asyncContent = chatMessageJsonAdapter.toJson(chatMessage);
        setCallBacks(null, null, null, true, Constants.GET_HISTORY, offsets, uniqueId);
        sendAsyncMessage(asyncContent, 3, "SEND GET THREAD HISTORY");
        if (handler != null) {
            handler.onGetHistory(uniqueId);
        }
        return uniqueId;
    }

    public String searchHistory(NosqlListMessageCriteriaVO messageCriteriaVO, String typeCode, ChatHandler handler) {

        JsonAdapter<NosqlListMessageCriteriaVO> messageContentJsonAdapter = moshi.adapter(NosqlListMessageCriteriaVO.class);
        String content = messageContentJsonAdapter.toJson(messageCriteriaVO);

        String uniqueId = generateUniqueId();
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setContent(content);
        chatMessage.setType(Constants.GET_HISTORY);
        chatMessage.setToken(getToken());
        chatMessage.setTokenIssuer("1");
        chatMessage.setUniqueId(uniqueId);
        chatMessage.setSubjectId(messageCriteriaVO.getMessageThreadId());
        if (typeCode != null && !typeCode.isEmpty()) {
            chatMessage.setTypeCode(typeCode);
        } else {
            chatMessage.setTypeCode(getTypeCode());
        }
        JsonAdapter<ChatMessage> chatMessageJsonAdapter = moshi.adapter(ChatMessage.class);

        String asyncContent = chatMessageJsonAdapter.toJson(chatMessage);
        setCallBacks(null, null, null, true, Constants.GET_HISTORY, messageCriteriaVO.getOffset(), uniqueId);
        sendAsyncMessage(asyncContent, 3, "SEND SEARCH0. HISTORY");
        if (handler != null) {
            handler.onSearchHistory(uniqueId);
        }
        return uniqueId;
    }

    /**
     * Get all of the contacts of the user
     */
    public String getContacts(Integer count, Long offset, String typeCode, ChatHandler handler) {

        Long offsets = offset;
        String uniqueId;
        if (cache) {
            ArrayList<Contact> arrayList = new ArrayList<>(messageDatabaseHelper.getContacts());
            ChatResponse<ResultContact> chatResponse = new ChatResponse<>();

            ResultContact resultContact = new ResultContact();
            resultContact.setContacts(arrayList);
            chatResponse.setResult(resultContact);
            resultContact.setContentCount(messageDatabaseHelper.getContacts().size());

            String contactJson = JsonUtil.getJson(chatResponse);

            listenerManager.callOnGetContacts(contactJson, chatResponse);
        }

        ChatMessageContent chatMessageContent = new ChatMessageContent();

        if (offset != null) {
            chatMessageContent.setOffset(offset);
        } else {
            chatMessageContent.setOffset(0);
            offsets = 0L;
        }

        JsonObject jObj = (JsonObject) gson.toJsonTree(chatMessageContent);
        jObj.remove("lastMessageId");
        jObj.remove("firstMessageId");
        if (count != null) {
            jObj.remove("count");
            jObj.addProperty("size", count);
        } else {
            jObj.remove("count");
            jObj.addProperty("size", 50);
        }

        uniqueId = generateUniqueId();
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setContent(jObj.toString());
        chatMessage.setType(Constants.GET_CONTACTS);
        chatMessage.setToken(getToken());
        chatMessage.setUniqueId(uniqueId);
        if (typeCode != null && !typeCode.isEmpty()) {
            chatMessage.setTypeCode(typeCode);
        } else {
            chatMessage.setTypeCode(getTypeCode());
        }
        JsonAdapter<ChatMessage> chatMessageJsonAdapter = moshi.adapter(ChatMessage.class);
        String asyncContent = chatMessageJsonAdapter.toJson(chatMessage);
        setCallBacks(null, null, null, true, Constants.GET_CONTACTS, offsets, uniqueId);
        sendAsyncMessage(asyncContent, 3, "GET_CONTACT_SEND");
        if (handler != null) {
            handler.onGetContact(uniqueId);
        }
        return uniqueId;
    }

    public String searchContact(SearchContact searchContact) {
        String uniqueId = generateUniqueId();
        String type_code;
        if (searchContact.getTypeCode() != null && !searchContact.getTypeCode().isEmpty()) {
            type_code = searchContact.getTypeCode();
        } else {
            type_code = getTypeCode();
        }
        if (chatReady) {
            Observable<Response<SearchContactVO>> observable = contactApi.searchContact(getToken(), TOKEN_ISSUER,
                    searchContact.getId()
                    , searchContact.getFirstName()
                    , searchContact.getLastName()
                    , searchContact.getEmail()
                    , generateUniqueId()
                    , searchContact.getOffset()
                    , searchContact.getSize()
                    , type_code
                    , searchContact.getQuery()
                    , searchContact.getCellphoneNumber());
            observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Response<SearchContactVO>>() {
                @Override
                public void call(Response<SearchContactVO> contactResponse) {

                    if (contactResponse.isSuccessful()) {
                        SearchContactVO contact = contactResponse.body();
                        String response = JsonUtil.getJson(contact);
                        listenerManager.callOnSearchContact(response, contact);
                        if (log) Logger.json(response);
                        if (log) Logger.i("RECEIVE_SEARCH_CONTACT");
                    } else {

                        if (contactResponse.body() != null) {
                            String errorMessage = contactResponse.body().getMessage() != null ? contactResponse.body().getMessage() : "";
                            int errorCode = contactResponse.body().getErrorCode() != null ? contactResponse.body().getErrorCode() : 0;
                            String error = getErrorOutPut(errorMessage, errorCode, uniqueId);
                            if (log) Logger.json(error);
                        }
                    }

                }
            }, (Throwable throwable) -> Logger.e(throwable.getMessage()));
        } else {
            String jsonError = getErrorOutPut(ChatConstant.ERROR_CHAT_READY, ChatConstant.ERROR_CODE_CHAT_READY, null);
            if (log) Logger.json(jsonError);

        }
        return uniqueId;
    }

    /**
     * Add one contact to the contact list
     *
     * @param firstName       Notice: if just put fistName without lastName its ok.
     * @param lastName        last name of the contact
     * @param cellphoneNumber Notice: If you just  put the cellPhoneNumber doesn't necessary to add email
     * @param email           email of the contact
     */
    public String addContact(String firstName, String lastName, String cellphoneNumber, String email, String typeCode) {

        if (typeCode == null || typeCode.isEmpty()) {
            typeCode = getTypeCode();
        }

        String uniqueId = generateUniqueId();
        Observable<Response<Contacts>> addContactObservable;
        if (chatReady) {
            addContactObservable = contactApi.addContact(getToken(), 1, firstName, lastName, email, uniqueId, cellphoneNumber, typeCode);
            addContactObservable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(addContactResponse -> {
                if (addContactResponse.isSuccessful()) {
                    Contacts contacts = addContactResponse.body();
                    if (!contacts.getHasError()) {

                        OutPutAddContact outPutAddContact = Util.getReformatOutPutAddContact(contacts, uniqueId);

                        String contactsJson = gson.toJson(outPutAddContact);

                        listenerManager.callOnAddContact(contactsJson);
                        if (log) Logger.json(contactsJson);
                        if (log) Logger.i("RECEIVED_ADD_CONTACT");
                    } else {
                        String jsonError = getErrorOutPut(contacts.getMessage(), contacts.getErrorCode(), null);
                        if (log) Logger.e(jsonError);
                    }

                }
            }, (Throwable throwable) ->
            {
                Logger.e("Error on add contact", throwable.getMessage());
                Logger.e(throwable.getMessage());
            });
        } else {
            String jsonError = getErrorOutPut(ChatConstant.ERROR_CHAT_READY, ChatConstant.ERROR_CODE_CHAT_READY, null);
            if (log) Logger.json(jsonError);
        }
        return uniqueId;
    }

    /**
     * Remove contact with the user id
     *
     * @param userId id of the user that we want to remove from contact list
     */
    public String removeContact(long userId) {
        String uniqueId = generateUniqueId();
        if (chatReady) {
            Observable<Response<ContactRemove>> removeContactObservable = contactApi.removeContact(getToken(), 1, userId);
            removeContactObservable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(response -> {
                if (response.isSuccessful()) {
                    ContactRemove contactRemove = response.body();
                    if (!contactRemove.getHasError()) {
                        String contactRemoveJson = JsonUtil.getJson(contactRemove);
                        listenerManager.callOnRemoveContact(contactRemoveJson);
                        if (log) Logger.json(contactRemoveJson);
                    } else {
                        String jsonError = getErrorOutPut(contactRemove.getErrorMessage(), contactRemove.getErrorCode(), uniqueId);
                        if (log) Logger.e(jsonError);
                    }
                }
            }, (Throwable throwable) -> {
                if (log) Logger.e("Error on remove contact", throwable.getMessage());
                if (log) Logger.e(throwable.getMessage());
//                String jsonError = getErrorOutPut("Error on remove contact", contactRemove.getErrorCode(), uniqueId);
            });
        } else {
            String jsonError = getErrorOutPut(ChatConstant.ERROR_CHAT_READY, ChatConstant.ERROR_CODE_CHAT_READY, null);
            if (log) Logger.json(jsonError);
        }
        return uniqueId;
    }

    /**
     * Update contacts
     * all of the params all required to update
     */
    public String updateContact(long userId, String firstName, String lastName, String cellphoneNumber, String email) {

        String uniqueId = generateUniqueId();
        if (chatReady) {
            Observable<Response<UpdateContact>> updateContactObservable = contactApi.updateContact(getToken(), 1
                    , userId, firstName, lastName, email, generateUniqueId(), cellphoneNumber);
            updateContactObservable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(response -> {
                if (response.isSuccessful()) {
                    UpdateContact updateContact = response.body();
                    if (!response.body().getHasError()) {
                        OutPutUpdateContact outPut = new OutPutUpdateContact();
                        outPut.setMessage(updateContact.getMessage());
                        outPut.setErrorCode(updateContact.getErrorCode());
                        outPut.setHasError(updateContact.getHasError());
                        outPut.setOtt(updateContact.getOtt());
                        outPut.setReferenceNumber(updateContact.getReferenceNumber());
                        outPut.setCount(updateContact.getCount());
                        ResultUpdateContact resultUpdateContact = new ResultUpdateContact();
                        resultUpdateContact.setContacts(updateContact.getResult());
                        outPut.setResult(resultUpdateContact);
                        String json = JsonUtil.getJson(outPut);
                        listenerManager.callOnUpdateContact(json);
                        Logger.json(json);
                    } else {
                        String jsonError = getErrorOutPut(response.body().getMessage(), response.body().getErrorCode(), null);
                        if (log) Logger.e(jsonError);
                    }
                }
            }, (Throwable throwable) ->
            {
                if (throwable != null) {
                    Logger.e("cause" + "" + throwable.getCause());
                }
            });
        } else {
            String jsonError = getErrorOutPut(ChatConstant.ERROR_CHAT_READY, ChatConstant.ERROR_CODE_CHAT_READY, null);
            if (log) Logger.json(jsonError);
        }

        return uniqueId;
    }

    public void mapSearch(String searchTerm, Double latitude, Double longitude) {
        RetrofitHelperMap retrofitHelperMap = new RetrofitHelperMap("https://api.neshan.org/");
        MapApi mapApi = retrofitHelperMap.getService(MapApi.class);
        Observable<Response<MapNeshan>> observable = mapApi.mapSearch("8b77db18704aa646ee5aaea13e7370f4f88b9e8c"
                , searchTerm, latitude, longitude);
        observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Response<MapNeshan>>() {
            @Override
            public void call(Response<MapNeshan> mapNeshanResponse) {
                OutPutMapNeshan outPutMapNeshan = new OutPutMapNeshan();
                if (mapNeshanResponse.isSuccessful()) {
                    MapNeshan mapNeshan = mapNeshanResponse.body();

                    outPutMapNeshan = new OutPutMapNeshan();
                    outPutMapNeshan.setCount(mapNeshan.getCount());
                    ResultMap resultMap = new ResultMap();
                    resultMap.setMaps(mapNeshan.getItems());
                    outPutMapNeshan.setResult(resultMap);
                    String json = JsonUtil.getJson(outPutMapNeshan);
                    listenerManager.callOnMapSearch(json, outPutMapNeshan);
                    if (log) Logger.i("RECEIVE_MAP_SEARCH");
                    if (log) Logger.json(json);
                } else {
                    ErrorOutPut errorOutPut = new ErrorOutPut();
                    errorOutPut.setErrorCode(mapNeshanResponse.code());
                    errorOutPut.setErrorMessage(mapNeshanResponse.message());
                    errorOutPut.setHasError(true);
                    String json = JsonUtil.getJson(outPutMapNeshan);
                    listenerManager.callOnError(json, errorOutPut);
                }
            }
        }, (Throwable throwable) -> {
            ErrorOutPut errorOutPut = new ErrorOutPut();
            errorOutPut.setErrorMessage(throwable.getMessage());
            errorOutPut.setHasError(true);
            String json = JsonUtil.getJson(errorOutPut);

            listenerManager.callOnError(json, errorOutPut);
        });
    }

    public void mapRouting(String origin, String destination) {
        RetrofitHelperMap retrofitHelperMap = new RetrofitHelperMap("https://api.neshan.org/");
        MapApi mapApi = retrofitHelperMap.getService(MapApi.class);
        Observable<Response<MapRout>> responseObservable = mapApi.mapRouting("8b77db18704aa646ee5aaea13e7370f4f88b9e8c"
                , origin, destination, true);
        responseObservable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Response<MapRout>>() {
            @Override
            public void call(Response<MapRout> mapRoutResponse) {
                if (mapRoutResponse.isSuccessful()) {
                    MapRout mapRout = mapRoutResponse.body();
                    OutPutMapRout outPutMapRout = new OutPutMapRout();
                    outPutMapRout.setResult(mapRout);
                    String jsonMapRout = JsonUtil.getJson(outPutMapRout);
                    listenerManager.callOnMapRouting(jsonMapRout);
                    Logger.i("RECEIVE_MAP_ROUTING");
                    Logger.json(jsonMapRout);
                }
            }
        }, (Throwable throwable) -> {
            Logger.e(throwable, "Error on map routing");
        });
    }

    public String block(Long contactId, String typeCode, ChatHandler handler) {
        BlockContactId blockAcount = new BlockContactId();
        blockAcount.setContactId(contactId);
        String uniqueId = generateUniqueId();
        String json = JsonUtil.getJson(blockAcount);
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setContent(json);
        chatMessage.setToken(getToken());
        chatMessage.setUniqueId(uniqueId);
        chatMessage.setTokenIssuer("1");
        chatMessage.setType(Constants.BLOCK);

        if (typeCode != null && !typeCode.isEmpty()) {
            chatMessage.setTypeCode(typeCode);
        } else {
            chatMessage.setTypeCode(getTypeCode());
        }


        setCallBacks(null, null, null, true, Constants.BLOCK, null, uniqueId);
        String asyncContent = JsonUtil.getJson(chatMessage);
        sendAsyncMessage(asyncContent, 4, "SEND_BLOCK");
        if (handler != null) {
            handler.onBlock(uniqueId);
        }
        return uniqueId;
    }

    public String unblock(long blockId, ChatHandler handler, String typeCode) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSubjectId(blockId);
        String uniqueId = generateUniqueId();
        chatMessage.setToken(getToken());
        chatMessage.setUniqueId(uniqueId);
        chatMessage.setTokenIssuer("1");
        chatMessage.setType(Constants.UNBLOCK);

        if (typeCode != null && !typeCode.isEmpty()) {
            chatMessage.setTypeCode(typeCode);
        } else {
            chatMessage.setTypeCode(getTypeCode());
        }

        setCallBacks(null, null, null, true, Constants.UNBLOCK, null, uniqueId);
        String asyncContent = JsonUtil.getJson(chatMessage);
        sendAsyncMessage(asyncContent, 4, "SEND_UN_BLOCK");
        if (handler != null) {
            handler.onUnBlock(uniqueId);
        }
        return uniqueId;
    }

    //TODO SPam test
    public void spam(long threadId, String typeCode) {

        String uniqueId = generateUniqueId();
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setType(Constants.SPAM_PV_THREAD);
        chatMessage.setTokenIssuer("1");
        chatMessage.setToken(getToken());
        chatMessage.setUniqueId(uniqueId);
        chatMessage.setSubjectId(threadId);

        if (typeCode != null && !typeCode.isEmpty()) {
            chatMessage.setTypeCode(typeCode);
        } else {
            chatMessage.setTypeCode(getTypeCode());
        }

        setCallBacks(null, null, null, true, Constants.SPAM_PV_THREAD, null, uniqueId);
        String asyncContent = JsonUtil.getJson(chatMessage);
        sendAsyncMessage(asyncContent, 4, "SEND_REPORT_SPAM");
    }

    public String getBlockList(Long count, Integer offset, String typeCode, ChatHandler handler) {

        ChatMessageContent chatMessageContent = new ChatMessageContent();
        if (offset != null) {
            chatMessageContent.setOffset(offset);
        }
        if (count != null) {
            chatMessageContent.setCount(count);
        }
        String json = JsonUtil.getJson(chatMessageContent);

        String uniqueId = generateUniqueId();
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setContent(json);
        chatMessage.setType(Constants.GET_BLOCKED);
        chatMessage.setTokenIssuer("1");
        chatMessage.setToken(getToken());
        chatMessage.setUniqueId(uniqueId);
        if (typeCode != null && !typeCode.isEmpty()) {
            chatMessage.setTypeCode(typeCode);
        } else {
            chatMessage.setTypeCode(getTypeCode());
        }

        setCallBacks(null, null, null, true, Constants.GET_BLOCKED, null, uniqueId);
        String asyncContent = JsonUtil.getJson(chatMessage);
        sendAsyncMessage(asyncContent, 4, "SEND_BLOCK_List");
        if (handler != null) {
            handler.onGetBlockList(uniqueId);
        }
        return uniqueId;
    }

    /**
     * Create the thread to p to p/channel/group. The list below is showing all of the thread type
     * int NORMAL = 0;
     * int OWNER_GROUP = 1;
     * int PUBLIC_GROUP = 2;
     * int CHANNEL_GROUP = 4;
     * int TO_BE_USER_ID = 5;
     * <p>
     * int CHANNEL = 8;
     */
    public String createThread(int threadType, Invitee[] invitee, String threadTitle, String typeCode, ChatHandler handler) {
        List<Invitee> invitees = new ArrayList<>(Arrays.asList(invitee));
        ChatThread chatThread = new ChatThread();
        chatThread.setType(threadType);
        chatThread.setInvitees(invitees);
        chatThread.setTitle(threadTitle);

        String contentThreadChat = JsonUtil.getJson(chatThread);
        String uniqueId = generateUniqueId();
        ChatMessage chatMessage = getChatMessage(contentThreadChat, uniqueId, typeCode);

        setCallBacks(null, null, null, true, Constants.INVITATION, null, uniqueId);
        String asyncContent = JsonUtil.getJson(chatMessage);
        sendAsyncMessage(asyncContent, 4, "SEND_CREATE_THREAD");
        if (handler != null) {
            handler.onCreateThread(uniqueId);
        }
        return uniqueId;
    }

    //TODO in Progress
    public String createThreadWithMessage(CreateThreadRequest threadRequest, String typeCode) {

        if (threadRequest.getMessage().getForwardedMessageIds() != null && threadRequest.getMessage().getForwardedMessageIds().size() > 0) {
            List<Long> messageIds = threadRequest.getMessage().getForwardedMessageIds();
            List<String> uniqueIds = new ArrayList<>();
            for (Long ids : messageIds) {
                String uniqueId = generateUniqueId();
                uniqueIds.add(uniqueId);
//                setThreadCallbacks();
            }
            threadRequest.getMessage().setForwardedUniqueIds(uniqueIds);
        }

        String uniqueId = generateUniqueId();
        String content = gson.toJson(threadRequest);
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setTypeCode(typeCode);
        chatMessage.setContent(content);
        chatMessage.setUniqueId(uniqueId);
        chatMessage.setToken(getToken());
        chatMessage.setTokenIssuer("1");


        return uniqueId;
    }


    public String updateThreadInfo(long threadId, ThreadInfoVO threadInfoVO, String typeCode, ChatHandler handler) {

        JsonObject jObj = (JsonObject) new GsonBuilder().create().toJsonTree(threadInfoVO);
        jObj.remove("title");
        jObj.addProperty("name", threadInfoVO.getTitle());

        String content = gson.toJson(jObj);

        String uniqueId = generateUniqueId();
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setTokenIssuer("1");
        chatMessage.setToken(getToken());
        chatMessage.setSubjectId(threadId);
        chatMessage.setUniqueId(uniqueId);
        chatMessage.setType(Constants.UPDATE_THREAD_INFO);
        chatMessage.setContent(content);
        if (typeCode != null && !typeCode.isEmpty()) {
            chatMessage.setTypeCode(typeCode);
        } else {
            chatMessage.setTypeCode(getTypeCode());
        }

        String asyncContent = JsonUtil.getJson(chatMessage);
        setCallBacks(null, null, null, true, Constants.UPDATE_THREAD_INFO, null, uniqueId);
        sendAsyncMessage(asyncContent, 4, "SEND_UPDATE_THREAD_INFO");
        if (handler != null) {
            handler.onUpdateThreadInfo(uniqueId);
        }
        return uniqueId;
    }

    /**
     * Get the participant list of specific thread
     *
     * @param threadId id of the thread we want to ge the participant list
     */
    public String getThreadParticipants(Integer count, Long offset, long threadId, String typeCode, ChatHandler handler) {

        ChatMessageContent chatMessageContent = new ChatMessageContent();
        if (count == null) {
            chatMessageContent.setCount(50);
        } else {
            chatMessageContent.setCount(count);
        }

        if (offset == null) {
            chatMessageContent.setOffset(0);
        } else {
            chatMessageContent.setOffset(offset);
        }


        JsonAdapter<ChatMessageContent> messageContentJsonAdapter = moshi.adapter(ChatMessageContent.class);
        String content = messageContentJsonAdapter.toJson(chatMessageContent);
        String uniqueId = generateUniqueId();
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setContent(content);
        chatMessage.setType(Constants.THREAD_PARTICIPANTS);
        chatMessage.setTokenIssuer("1");
        chatMessage.setToken(getToken());
        chatMessage.setUniqueId(uniqueId);
        chatMessage.setSubjectId(threadId);

        if (typeCode != null && !typeCode.isEmpty()) {
            chatMessage.setTypeCode(typeCode);
        } else {
            chatMessage.setTypeCode(getTypeCode());
        }

        JsonAdapter<ChatMessage> chatMessageJsonAdapter = moshi.adapter(ChatMessage.class);
        String asyncContent = chatMessageJsonAdapter.toJson(chatMessage);
        setCallBacks(null, null, null, true, Constants.THREAD_PARTICIPANTS, offset, uniqueId);
        sendAsyncMessage(asyncContent, 3, "SEND_THREAD_PARTICIPANT");
        if (handler != null) {
            handler.onGetThreadParticipant(uniqueId);
        }
        return uniqueId;
    }

    public String seenMessage(long messageId, long ownerId, ChatHandler handler) {

        String uniqueId = generateUniqueId();
        if (ownerId != getUserId()) {
            ChatMessage message = new ChatMessage();
            message.setType(Constants.SEEN);
            message.setContent(String.valueOf(messageId));
            message.setTokenIssuer("1");
            message.setToken(getToken());
            message.setUniqueId(uniqueId);
            message.setTime(1000);

            JsonAdapter<ChatMessage> chatMessageJsonAdapter = moshi.adapter(ChatMessage.class);
            String asyncContent = chatMessageJsonAdapter.toJson(message);
            sendAsyncMessage(asyncContent, 4, "SEND_SEEN_MESSAGE");
            if (handler != null) {
                handler.onSeen(uniqueId);
            }
        }
        return uniqueId;
    }

    /**
     * Get the information of the current user
     */
    public String getUserInfo(String typeCode, ChatHandler handler) {
        try {
            if (cache) {
                if (messageDatabaseHelper.getUserInfo() != null) {
                    UserInfo userInfo = messageDatabaseHelper.getUserInfo();
                    ChatResponse<ResultUserInfo> chatResponse = new ChatResponse<>();

                    ResultUserInfo result = new ResultUserInfo();

                    setUserId(userInfo.getId());
                    result.setUser(userInfo);
                    chatResponse.setErrorCode(0);
                    chatResponse.setErrorMessage("");
                    chatResponse.setHasError(false);
                    chatResponse.setResult(result);
                    chatResponse.setUniqueId("");

                    String userInfoJson = JsonUtil.getJson(chatResponse);
                    listenerManager.callOnUserInfo(userInfoJson, chatResponse);
                    if (log) Logger.i("CACHE_USER_INFO");
                    if (log) Logger.json(userInfoJson);
                }
            }

            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setType(Constants.USER_INFO);
            String uniqueId = generateUniqueId();
            chatMessage.setUniqueId(uniqueId);
            chatMessage.setToken(getToken());
            chatMessage.setTokenIssuer("1");

            if (typeCode != null && !typeCode.isEmpty()) {
                chatMessage.setTypeCode(typeCode);
            } else {
                chatMessage.setTypeCode(getTypeCode());
            }

            setCallBacks(null, null, null, true, Constants.USER_INFO, null, uniqueId);
            String asyncContent = JsonUtil.getJson(chatMessage);

            if (asyncReady) {
                if (log) Logger.i("SEND_USER_INFO");
                if (log) Logger.json(asyncContent);
                async.sendMessage(asyncContent, 3);

                if (handler != null) {
                    handler.onGetUserInfo(uniqueId);
                }
            } else {
                String jsonError = getErrorOutPut(ChatConstant.ERROR_CHAT_READY, ChatConstant.ERROR_CODE_CHAT_READY, null);
                uniqueId = null;
                Logger.e(jsonError);
            }
            return uniqueId;
        } catch (Exception e) {
            if (log) Logger.e(e.getCause().getMessage());
        }
        return null;
    }

    /**
     * Mute the thread so notification is off for that thread
     */
    public String muteThread(long threadId, String typeCode, ChatHandler handler) {
        try {
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setType(Constants.MUTE_THREAD);
            chatMessage.setToken(getToken());
            chatMessage.setTokenIssuer("1");
            chatMessage.setSubjectId(threadId);
            String uniqueId = generateUniqueId();
            chatMessage.setUniqueId(uniqueId);
            if (typeCode != null && !typeCode.isEmpty()) {
                chatMessage.setTypeCode(typeCode);
            } else {
                chatMessage.setTypeCode(getTypeCode());
            }
            setCallBacks(null, null, null, true, Constants.MUTE_THREAD, null, uniqueId);

            String asyncContent = JsonUtil.getJson(chatMessage);
            sendAsyncMessage(asyncContent, 4, "SEND_MUTE_THREAD");
            if (handler != null) {
                handler.onMuteThread(uniqueId);
            }
            return uniqueId;
        } catch (Exception e) {
            if (log) Logger.e(e.getCause().getMessage());
        }
        return null;
    }

    /**
     * Unmute the thread so notification is on for that thread
     */
    public String unMuteThread(long threadId, String typeCode, ChatHandler handler) {
        try {
            String uniqueId = generateUniqueId();
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setType(Constants.UN_MUTE_THREAD);
            chatMessage.setToken(getToken());
            chatMessage.setTokenIssuer("1");
            chatMessage.setSubjectId(threadId);
            chatMessage.setUniqueId(uniqueId);
            if (typeCode != null && !typeCode.isEmpty()) {
                chatMessage.setTypeCode(typeCode);
            } else {
                chatMessage.setTypeCode(getTypeCode());
            }
            setCallBacks(null, null, null, true, Constants.UN_MUTE_THREAD, null, uniqueId);
            String asyncContent = JsonUtil.getJson(chatMessage);
            sendAsyncMessage(asyncContent, 4, "SEND_UN_MUTE_THREAD");
            if (handler != null) {
                handler.onUnMuteThread(uniqueId);
            }
            return uniqueId;
        } catch (Exception e) {
            if (log) Logger.e(e.getCause().getMessage());
        }
        return null;
    }

    /**
     * Message can be edit when you pass the message id and the edited
     * content in order to edit your Message.
     */
    public String editMessage(int messageId, String messageContent, String metaData, String typeCode, ChatHandler handler) {
        try {
            String uniqueId = generateUniqueId();

            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setType(Constants.EDIT_MESSAGE);
            chatMessage.setToken(getToken());
            chatMessage.setUniqueId(uniqueId);
            chatMessage.setSubjectId(messageId);
            chatMessage.setContent(messageContent);
            chatMessage.setSystemMetadata(metaData);
            chatMessage.setTokenIssuer("1");

            if (typeCode != null && !typeCode.isEmpty()) {
                chatMessage.setTypeCode(typeCode);
            } else {
                chatMessage.setTypeCode(getTypeCode());
            }
            String asyncContent = JsonUtil.getJson(chatMessage);
            setCallBacks(null, null, null, true, Constants.EDIT_MESSAGE, null, uniqueId);
            sendAsyncMessage(asyncContent, 4, "SEND_EDIT_MESSAGE");
            if (handler != null) {
                handler.onEditMessage(uniqueId);
            }
            return uniqueId;
        } catch (Exception e) {
            if (log) Logger.e(e.getCause().getMessage());
        }
        return null;
    }

    public String deliveredMessageList(long messageId) {

        String uniqueId = generateUniqueId();

        try {
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setToken(getToken());
            chatMessage.setTokenIssuer("1");
            chatMessage.setUniqueId(uniqueId);
            chatMessage.setSubjectId(messageId);
            chatMessage.setType(Constants.DELIVERED_MESSAGE_LIST);
            setCallBacks(null, null, null, true, Constants.DELIVERED_MESSAGE_LIST, null, uniqueId);
            String asyncContent = gson.toJson(chatMessage);
            sendAsyncMessage(asyncContent, 4, "SEND_DELIVERED_MESSAGE_LIST");

        } catch (Exception e) {
            if (log) Logger.e(e.getCause().getMessage());
        }

        return uniqueId;
    }

    //Get the list of the person that saw the specific message
    public String seenMessageList(long messageId) {
        String uniqueId = generateUniqueId();

        try {
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setType(Constants.SEEN_MESSAGE_LIST);
            chatMessage.setSubjectId(messageId);
            chatMessage.setTokenIssuer("1");
            chatMessage.setToken(getToken());
            chatMessage.setUniqueId(uniqueId);

            setCallBacks(null, null, null, true, Constants.SEEN_MESSAGE_LIST, null, uniqueId);
            String asyncContent = gson.toJson(chatMessage);
            sendAsyncMessage(asyncContent, 4, "SEND_SEEN_MESSAGE_LIST");
        } catch (Exception e) {
            if (log) Logger.e(e.getCause().getMessage());
        }

        return uniqueId;
    }

    /**
     * Add a listener to receive events on this Chat.
     *
     * @param listener A listener to add.
     * @return {@code this} object.
     */
    public Chat addListener(ChatListener listener) {
        listenerManager.addListener(listener, log);
        return this;
    }

    public Chat addListeners(List<ChatListener> listeners) {
        listenerManager.addListeners(listeners);
        return this;
    }

    public Chat removeListener(ChatListener listener) {
        listenerManager.removeListener(listener);
        return this;
    }

    public LiveData<String> getState() {
        return async.getStateLiveData();
    }

    /*
     * If you want to disable cache Set isCacheables to false
     * */
    public boolean isCacheables(boolean cache) {
        this.cache = cache;
        return cache;
    }

    @NonNull
    private String getErrorOutPut(String errorMessage, long errorCode, String uniqueId) {
        ErrorOutPut error = new ErrorOutPut(true, errorMessage, errorCode, uniqueId);
        String jsonError = JsonUtil.getJson(error);
        listenerManager.callOnError(jsonError, error);
        return jsonError;
    }

    public String getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    private class BlockContactId {
        private long contactId;

        public long getContactId() {
            return contactId;
        }

        public void setContactId(long contactId) {
            this.contactId = contactId;
        }
    }

    /**
     * Ping for staying chat alive
     */
    private void ping() {
        if (chatState && async.getPeerId() != null) {
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setType(Constants.PING);
            chatMessage.setTokenIssuer("1");
            chatMessage.setToken(getToken());
            JsonAdapter<ChatMessage> chatMessageJsonAdapter = moshi.adapter(ChatMessage.class);
            String asyncContent = chatMessageJsonAdapter.toJson(chatMessage);
            sendAsyncMessage(asyncContent, 4, "CHAT PING");
        }
    }

    private void pingAfterSetToken() {
        checkToken = true;
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setType(Constants.PING);
        chatMessage.setTokenIssuer("1");
        chatMessage.setToken(getToken());
        JsonAdapter<ChatMessage> chatMessageJsonAdapter = moshi.adapter(ChatMessage.class);
        String asyncContent = chatMessageJsonAdapter.toJson(chatMessage);
        async.sendMessage(asyncContent, 4);
        if (log) Logger.i("CHAT PING FOR CHECK TOKEN AUTHENTICATION");
        if (log) Logger.json(asyncContent);
    }

    private String handleMimType(Uri uri, File file) {
        String mimType;

        if (context.getContentResolver().getType(uri) != null) {
            mimType = context.getContentResolver().getType(uri);
        } else {
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            int index = file.getName().lastIndexOf('.') + 1;
            String ext = file.getName().substring(index).toLowerCase();
            mimType = mime.getMimeTypeFromExtension(ext);
        }
        return mimType;
    }

    @SuppressLint("MissingPermission")
    private boolean isConnected(Activity context) {
        boolean isConnected = false;
        if (Permission.Check_ACCESS_NETWORK_STATE(context)) {
            NetworkInfo activeNetwork;
            ConnectivityManager cm =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            activeNetwork = cm.getActiveNetworkInfo();
            isConnected = activeNetwork != null &&
                    activeNetwork.isConnectedOrConnecting();
        }
        return isConnected;
    }

    private void handleError(ChatMessage chatMessage) {

        Error error = JsonUtil.fromJSON(chatMessage.getContent(), Error.class);
        if (error.getCode() == 401) {
            pingHandler.removeCallbacksAndMessages(null);
        } else if (error.getCode() == 21) {
            String errorMessage = error.getMessage();
            long errorCode = error.getCode();

            String errorJson = getErrorOutPut(errorMessage, errorCode, chatMessage.getUniqueId());
            if (log) Logger.json(errorJson);
            pingHandler.removeCallbacksAndMessages(null);
            chatReady = false;
            listenerManager.callOnChatState("ASYNC_READY");
            async.setStateLiveData("ASYNC_READY");
            return;
//            async.logOut();
        }
        String errorMessage = error.getMessage();
        long errorCode = error.getCode();

        String errorJson = getErrorOutPut(errorMessage, errorCode, chatMessage.getUniqueId());
        if (log) Logger.json(errorJson);
    }

    private void handleLastSeenUpdated(ChatMessage chatMessage) {
        if (log) Logger.i("LAST_SEEN_UPDATED");
        if (log) Logger.i(chatMessage.getContent());
        listenerManager.callOnLastSeenUpdated(chatMessage.getContent());
    }

    private void handleThreadInfoUpdated(ChatMessage chatMessage) {
        OutPutInfoThread outPutInfoThread = new OutPutInfoThread();
        ResultThread resultThread = new ResultThread();
        Thread thread = gson.fromJson(chatMessage.getContent(), Thread.class);
        resultThread.setThread(thread);
        outPutInfoThread.setResult(resultThread);
        listenerManager.callOnThreadInfoUpdated(chatMessage.getContent());
        if (log) Logger.i("THREAD_INFO_UPDATED");
        if (log) Logger.json(chatMessage.getContent());
    }

    private void handleRemoveFromThread(ChatMessage chatMessage) {
        if (log) Logger.i("RECEIVED_REMOVE_FROM_THREAD", chatMessage);
        listenerManager.callOnRemovedFromThread(chatMessage.getContent());
    }

    private void handleOnPing(ChatMessage chatMessage) {
        if (log) Logger.i("RECEIVED_CHAT_PING", chatMessage);
        if (checkToken) {
            chatReady = true;
            listenerManager.callOnChatState(CHAT_READY);
            async.setStateLiveData(CHAT_READY);
            checkToken = false;
        }
    }

    /**
     * When the new message arrived we send the deliver message to the sender user.
     */
    private void handleNewMessage(ChatMessage chatMessage) {

        try {
            MessageVO messageVO = gson.fromJson(chatMessage.getContent(), MessageVO.class);

            ChatResponse<ResultNewMessage> chatResponse = new ChatResponse<>();
            chatResponse.setUniqueId(chatMessage.getUniqueId());
            chatResponse.setHasError(false);
            chatResponse.setErrorCode(0);
            chatResponse.setErrorMessage("");
            ResultNewMessage resultNewMessage = new ResultNewMessage();
            resultNewMessage.setMessageVO(messageVO);
            resultNewMessage.setThreadId(chatMessage.getSubjectId());
            chatResponse.setResult(resultNewMessage);
            String json = gson.toJson(chatResponse);
            listenerManager.callOnNewMessage(json, chatResponse);
            long ownerId = 0;
            if (messageVO != null) {
                ownerId = messageVO.getParticipant().getId();
            }

            if (log) Logger.i("RECEIVED_NEW_MESSAGE");
            if (log) Logger.json(json);
            if (ownerId != getUserId()) {
                ChatMessage message = getChatMessage(messageVO);
                JsonAdapter<ChatMessage> chatMessageJsonAdapter = moshi.adapter(ChatMessage.class);
                String asyncContent = chatMessageJsonAdapter.toJson(message);
                async.sendMessage(asyncContent, 4);
                Logger.i("SEND_DELIVERY_MESSAGE");
                Logger.json(asyncContent);
            }
        } catch (Exception e) {
            if (log) Logger.e(e.getCause().getMessage());
        }

    }

    //TODO Problem in message id in forwardMessage
    private void handleSent(ChatMessage chatMessage, String messageUniqueId, long threadId) {

        try {
            if (threadCallbacks.containsKey(threadId)) {
                ArrayList<Callback> callbacks = threadCallbacks.get(threadId);
                for (Callback callback : callbacks) {
                    if (messageUniqueId.equals(callback.getUniqueId())) {
                        int indexUnique = callbacks.indexOf(callback);
                        if (callbacks.get(indexUnique).isSent()) {

                            ChatResponse<ResultMessage> chatResponse = new ChatResponse<>();

                            ResultMessage resultMessage = new ResultMessage();

                            chatResponse.setErrorCode(0);
                            chatResponse.setErrorMessage("");
                            chatResponse.setHasError(false);
                            chatResponse.setUniqueId(callback.getUniqueId());

                            resultMessage.setConversationId(chatMessage.getSubjectId());
                            resultMessage.setMessageId(Long.valueOf(chatMessage.getContent()));
                            chatResponse.setResult(resultMessage);

                            String json = gson.toJson(chatResponse);
                            listenerManager.callOnSentMessage(json, chatResponse);

                            runOnUIThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (handlerSend.get(callback.getUniqueId()) != null) {
                                        handlerSend.get(callback.getUniqueId()).onSentResult(chatMessage.getContent());
                                    }
                                }
                            });

                            Callback callbackUpdateSent = new Callback();
                            callbackUpdateSent.setSent(false);
                            callbackUpdateSent.setDelivery(callback.isDelivery());
                            callbackUpdateSent.setSeen(callback.isSeen());
                            callbackUpdateSent.setUniqueId(callback.getUniqueId());

                            callbacks.set(indexUnique, callbackUpdateSent);
                            threadCallbacks.put(threadId, callbacks);
                            if (log) Logger.json(json);
                            if (log) Logger.i("RECEIVED_SENT_MESSAGE");
                        }
                        break;
                    }
                }
            }
        } catch (Exception e) {
            if (log) Logger.e(e.getCause().getMessage());
        }

    }

    static {
        sUIThreadHandler = new Handler(Looper.getMainLooper());
    }

    protected static void runOnUIThread(Runnable runnable) {
        if (sUIThreadHandler != null) {
            sUIThreadHandler.post(runnable);
        } else {
            runnable.run();
        }
    }

    private void handleSeen(ChatMessage chatMessage, String messageUniqueId, long threadId) {

        if (threadCallbacks.containsKey(threadId)) {
            ArrayList<Callback> callbacks = threadCallbacks.get(threadId);
            for (Callback callback : callbacks) {
                if (messageUniqueId.equals(callback.getUniqueId())) {
                    int indexUnique = callbacks.indexOf(callback);
                    while (indexUnique > -1) {
                        if (callbacks.get(indexUnique).isSeen()) {
                            ResultMessage resultMessage = gson.fromJson(chatMessage.getContent(), ResultMessage.class);
                            if (callbacks.get(indexUnique).isDelivery()) {

                                ChatResponse<ResultMessage> chatResponse = new ChatResponse<>();

                                chatResponse.setErrorMessage("");
                                chatResponse.setErrorCode(0);
                                chatResponse.setHasError(false);
                                chatResponse.setUniqueId(callback.getUniqueId());
                                chatResponse.setResult(resultMessage);

                                String json = gson.toJson(chatResponse);

                                listenerManager.callOnDeliveryMessage(json, chatResponse);

                                Callback callbackUpdateSent = new Callback();
                                callbackUpdateSent.setSent(callback.isSent());
                                callbackUpdateSent.setDelivery(false);
                                callbackUpdateSent.setSeen(callback.isSeen());
                                callbackUpdateSent.setUniqueId(callback.getUniqueId());

                                callbacks.set(indexUnique, callbackUpdateSent);
                                threadCallbacks.put(threadId, callbacks);
                                if (log) {
                                    Logger.i("RECEIVED_DELIVERED_MESSAGE");
                                    Logger.json(json);
                                }
                            }

                            ChatResponse<ResultMessage> chatResponse = new ChatResponse<>();

                            chatResponse.setErrorMessage("");
                            chatResponse.setErrorCode(0);
                            chatResponse.setHasError(false);
                            chatResponse.setUniqueId(callback.getUniqueId());
                            chatResponse.setResult(resultMessage);

                            String json = gson.toJson(chatResponse);
                            listenerManager.callOnSeenMessage(json, chatResponse);
                            callbacks.remove(indexUnique);
                            threadCallbacks.put(threadId, callbacks);
                            if (log)
                                Logger.i("Is Seen" + " " + "Unique Id" + callback.getUniqueId());
                        }
                        indexUnique--;
                    }
                    break;
                }
            }
        }
    }

    private void handleDelivery(ChatMessage chatMessage, String messageUniqueId, long threadId) {

        try {
            if (threadCallbacks.containsKey(threadId)) {
                ArrayList<Callback> callbacks = threadCallbacks.get(threadId);
                for (Callback callback : callbacks) {
                    if (messageUniqueId.equals(callback.getUniqueId())) {
                        int indexUnique = callbacks.indexOf(callback);
                        while (indexUnique > -1) {
                            if (callbacks.get(indexUnique).isDelivery()) {
                                ChatResponse<ResultMessage> chatResponse = new ChatResponse<>();
                                ResultMessage resultMessage = gson.fromJson(chatMessage.getContent(), ResultMessage.class);
                                chatResponse.setErrorMessage("");
                                chatResponse.setErrorCode(0);
                                chatResponse.setHasError(false);
                                chatResponse.setUniqueId(callback.getUniqueId());

                                chatResponse.setResult(resultMessage);
                                String json = gson.toJson(chatResponse);

                                listenerManager.callOnDeliveryMessage(json, chatResponse);

                                Callback callbackUpdateSent = new Callback();
                                callbackUpdateSent.setSent(callback.isSent());
                                callbackUpdateSent.setDelivery(false);
                                callbackUpdateSent.setSeen(callback.isSeen());
                                callbackUpdateSent.setUniqueId(callback.getUniqueId());
                                callbacks.set(indexUnique, callbackUpdateSent);
                                threadCallbacks.put(threadId, callbacks);
                                if (log) {
                                    Logger.i("RECEIVED_DELIVERED_MESSAGE");
                                    Logger.json(json);
                                }
                            }
                            indexUnique--;
                        }
                        break;
                    }
                }
            }
        } catch (Exception e) {
            if (log) Logger.e(e.getCause().getMessage());
        }
    }

    private void handleForwardMessage(ChatMessage chatMessage) {
        MessageVO messageVO = gson.fromJson(chatMessage.getContent(), MessageVO.class);
        ChatResponse<ResultNewMessage> chatResponse = new ChatResponse<>();
        ResultNewMessage resultMessage = new ResultNewMessage();
        resultMessage.setThreadId(chatMessage.getSubjectId());
        resultMessage.setMessageVO(messageVO);
        chatResponse.setResult(resultMessage);
        String json = gson.toJson(chatResponse);

        long ownerId = 0;
        if (messageVO != null) {
            ownerId = messageVO.getParticipant().getId();
        }
        if (log) Logger.i("RECEIVED_FORWARD_MESSAGE");
        if (log) Logger.json(json);
        listenerManager.callOnNewMessage(json, chatResponse);
        if (ownerId != getUserId()) {
            ChatMessage message = getChatMessage(messageVO);
            JsonAdapter<ChatMessage> chatMessageJsonAdapter = moshi.adapter(ChatMessage.class);
            String asyncContent = chatMessageJsonAdapter.toJson(message);
            if (log) Logger.i("SEND_DELIVERY_MESSAGE");
            if (log) Logger.json(asyncContent);
            async.sendMessage(asyncContent, 4);
        }
    }

    private void handleSyncContact(ChatMessage chatMessage, Callback callback) {
        try {

            List<Contact> contacts = gson.fromJson(chatMessage.getContent(), new TypeToken<ArrayList<Contact>>() {
            }.getType());

            hasNextContact = contacts.size() + callback.getOffset() < chatMessage.getContentCount();
            nextOffsetContact = callback.getOffset() + contacts.size();

            if (hasNextContact) {
                getContacts(50, nextOffsetContact, getTypeCode(), null);
                serverContacts.addAll(contacts);
                return;
            }

            ArrayList<String> firstNames = new ArrayList<>();
            ArrayList<String> cellphoneNumbers = new ArrayList<>();
            ArrayList<String> lastNames = new ArrayList<>();

            if (serverContacts != null) {
                List<Contact> phoneContacts = getPhoneContact(getContext());
                HashMap<String, String> mapServerContact = new HashMap<>();
                for (int a = 0; a < serverContacts.size(); a++) {
                    mapServerContact.put(serverContacts.get(a).getCellphoneNumber(), serverContacts.get(a).getFirstName());
                }
                for (int j = 0; j < phoneContacts.size(); j++) {
                    if (!mapServerContact.containsKey(phoneContacts.get(j).getCellphoneNumber())) {
                        firstNames.add(phoneContacts.get(j).getFirstName());
                        cellphoneNumbers.add(phoneContacts.get(j).getCellphoneNumber());
                        lastNames.add(phoneContacts.get(j).getLastName());
                    }
//                    else if (){
//
//                    }
                }
            }

            if (!firstNames.isEmpty() || !cellphoneNumbers.isEmpty()) {
                addContacts(firstNames, lastNames, cellphoneNumbers);
                syncContacts = true;
            }
            syncContact = false;
        } catch (Exception e) {
            if (log) Logger.e(e.getCause().getMessage());
        }
    }

    private void handleResponseMessage(Callback callback, ChatMessage chatMessage, String messageUniqueId) {

        if (callback.getRequestType() >= 0) {
            switch (callback.getRequestType()) {

                case Constants.GET_HISTORY:

                    handleOutPutGetHistory(callback, chatMessage, messageUniqueId);
                    break;
                case Constants.GET_CONTACTS:

                    handleGetContact(callback, chatMessage, messageUniqueId);
                    break;
                case Constants.UPDATE_THREAD_INFO:

                    handleUpdateThreadInfo(chatMessage, messageUniqueId, callback);
                    break;
                case Constants.GET_THREADS:

                    handleGetThreads(callback, chatMessage, messageUniqueId);
                    break;
                case Constants.INVITATION:

                    handleCreateThread(callback, chatMessage, messageUniqueId);
                    break;
                case Constants.MUTE_THREAD:

                    if (callback.isResult()) {
                        ChatResponse<ResultMute> chatResponse = new ChatResponse<>();
                        String muteThreadJson = reformatMuteThread(chatMessage, chatResponse);
                        listenerManager.callOnMuteThread(muteThreadJson, chatResponse);
                        messageCallbacks.remove(messageUniqueId);
                        if (log) Logger.i("RECEIVE_MUTE_THREAD");
                        if (log) Logger.i(muteThreadJson);
                    }
                    break;
                case Constants.UN_MUTE_THREAD:

                    if (callback.isResult()) {
                        ChatResponse<ResultMute> chatResponse = new ChatResponse<>();
                        String unmuteThreadJson = reformatMuteThread(chatMessage, chatResponse);
                        listenerManager.callOnUnmuteThread(unmuteThreadJson, chatResponse);
                        messageCallbacks.remove(messageUniqueId);
                        if (log) Logger.i("RECEIVE_UN_MUTE_THREAD");
                        if (log) Logger.i(unmuteThreadJson);
                    }
                    break;
                case Constants.EDIT_MESSAGE:

                    if (callback.isResult()) {
                        if (log) Logger.i("RECEIVE_EDIT_MESSAGE");
                        if (log) Logger.json(chatMessage.getContent());
                        listenerManager.callOnEditedMessage(chatMessage.getContent());
                        messageCallbacks.remove(messageUniqueId);
                    }

                    break;
                case Constants.USER_INFO:

                    handleOnGetUserInfo(chatMessage, messageUniqueId, callback);
                    break;
                case Constants.THREAD_PARTICIPANTS:

                    if (callback.isResult()) {
                        ChatResponse<ResultParticipant> chatResponse = reformatThreadParticipants(callback, chatMessage);

                        String jsonParticipant = JsonUtil.getJson(chatResponse);
                        listenerManager.callOnGetThreadParticipant(jsonParticipant, chatResponse);
                        messageCallbacks.remove(messageUniqueId);
                        if (log) Logger.i("RECEIVE_PARTICIPANT");
                        if (log) Logger.json(jsonParticipant);
                    }

                    break;
                case Constants.ADD_PARTICIPANT:
                    if (callback.isResult()) {
                        handleAddParticipant(chatMessage, messageUniqueId);
                    }

                    break;
                case Constants.REMOVE_PARTICIPANT:
                    if (callback.isResult()) {
                        handleOutPutRemoveParticipant(callback, chatMessage, messageUniqueId);
                    }

                    break;
                case Constants.LEAVE_THREAD:
                    if (callback.isResult()) {
                        handleOutPutLeaveThread(chatMessage, messageUniqueId);
                    }

                    break;
                case Constants.RENAME:

                    break;
                case Constants.DELETE_MESSAGE:
                    handleOutPutDeleteMsg(chatMessage);

                    break;
                case Constants.BLOCK:
                    if (callback.isResult()) {
                        handleOutPutBlock(chatMessage, messageUniqueId);
                    }

                    break;
                case Constants.UNBLOCK:
                    if (callback.isResult()) {
                        handleUnBlock(chatMessage, messageUniqueId);
                    }

                    break;
                case Constants.GET_BLOCKED:
                    if (callback.isResult()) {
                        handleOutPutGetBlockList(chatMessage);
                    }

                    break;
            }
        }

    }

    private void handleGetContact(Callback callback, ChatMessage chatMessage, String messageUniqueId) {
        if (syncContact) {
            handleSyncContact(chatMessage, callback);

        } else {

            if (callback.isResult()) {
                ChatResponse<ResultContact> chatResponse = reformatGetContactResponse(chatMessage, callback);
                String contactJson = gson.toJson(chatResponse);
                listenerManager.callOnGetContacts(contactJson, chatResponse);
                messageCallbacks.remove(messageUniqueId);
                if (log) Logger.i("RECEIVE_GET_CONTACT");
                if (log) Logger.json(contactJson);
            }
        }
    }

    private void handleCreateThread(Callback callback, ChatMessage chatMessage, String messageUniqueId) {
        if (callback.isResult()) {
            OutPutThread outPutThread = new OutPutThread();
            String inviteJson = reformatCreateThread(chatMessage, outPutThread);
            listenerManager.callOnCreateThread(inviteJson, outPutThread);
            messageCallbacks.remove(messageUniqueId);
            if (log) Logger.i("RECEIVE_CREATE_THREAD");
            if (log) Logger.json(inviteJson);
        }
    }

    private void handleGetThreads(Callback callback, ChatMessage chatMessage, String messageUniqueId) {

        if (callback.isResult()) {
            ChatResponse<ResultThreads> chatResponse = reformatGetThreadsResponse(chatMessage, callback);
            String threadJson = JsonUtil.getJson(chatResponse);
            listenerManager.callOnGetThread(threadJson, chatResponse);
            messageCallbacks.remove(messageUniqueId);
            if (log) Logger.i("RECEIVE_GET_THREAD");
            if (log) Logger.json(threadJson);
        }
    }

    private void handleUpdateThreadInfo(ChatMessage chatMessage, String messageUniqueId, Callback callback) {

        ChatResponse<ResultThread> chatResponse = new ChatResponse<>();
        if (callback.isResult()) {

            Thread thread = gson.fromJson(chatMessage.getContent(), Thread.class);

            ResultThread resultThread = new ResultThread();

            resultThread.setThread(thread);
            chatResponse.setErrorCode(0);
            chatResponse.setErrorMessage("");
            chatResponse.setHasError(false);
            chatResponse.setUniqueId(chatMessage.getUniqueId());
            chatResponse.setResult(resultThread);

            String threadJson = gson.toJson(chatResponse);
            messageCallbacks.remove(messageUniqueId);

            listenerManager.callOnUpdateThreadInfo(threadJson, chatResponse);
            if (log) Logger.i("RECEIVE_UPDATE_THREAD_INFO");
            if (log) Logger.json(threadJson);
        }
    }

    //TODO error
    private void handleOnGetUserInfo(ChatMessage chatMessage, String messageUniqueId, Callback callback) {

        if (callback.isResult()) {
            userInfoResponse = true;
            ChatResponse<ResultUserInfo> chatResponse = new ChatResponse<>();
            UserInfo userInfo = gson.fromJson(chatMessage.getContent(), UserInfo.class);
            String userInfoJson = reformatUserInfo(chatMessage, chatResponse, userInfo);
            listenerManager.callOnUserInfo(userInfoJson, chatResponse);
            messageCallbacks.remove(messageUniqueId);
            if (log) Logger.i("RECEIVE_USER_INFO");
            if (log) Logger.json(userInfoJson);

            listenerManager.callOnChatState("CHAT_READY");
            async.setStateLiveData("CHAT_READY");
            chatReady = true;

            if (log) Logger.i("CHAT_READY");
            if (ping) {
                long lastSentMessageTimeout = 9 * 1000;
                lastSentMessageTime = new Date().getTime();
                ping = false;
                if (state) {
                    pingHandler.postDelayed(() -> {
                        long currentTime = new Date().getTime();
                        if (currentTime - lastSentMessageTime > lastSentMessageTimeout) {
                            ping();

                        }
                    }, 20000);
                } else {
                    Logger.e("Async is Close");
                }
            }
        }
    }

    private void retryOnGetUserInfo() {
        runOnUIUserInfoThread(new Runnable() {
            @Override
            public void run() {
                if (userInfoResponse) {
                    getUserInfoHandler.removeCallbacksAndMessages(null);
                } else {
                    if (retryStepUserInfo < 60) retryStepUserInfo *= 2;
                    getUserInfo(getTypeCode(), null);
                    runOnUIUserInfoThread(this::run, retryStepUserInfo * 1000);
                    if (log)
                        Logger.e("getUserInfo " + " retry in " + retryStepUserInfo + " s ");
                }
            }
        }, retryStepUserInfo * 1000);
        if (retryStepUserInfo < 60) retryStepUserInfo *= 4;
    }

    static {
        getUserInfoHandler = new Handler(Looper.getMainLooper());
    }

    protected static void runOnUIUserInfoThread(Runnable runnable, long delayedTime) {
        if (getUserInfoHandler != null) {
            getUserInfoHandler.postDelayed(runnable, delayedTime);
        } else {
            runnable.run();
        }

    }


    private void handleOutPutLeaveThread(ChatMessage chatMessage, String messageUniqueId) {

        ChatResponse<ResultLeaveThread> chatResponse = new ChatResponse<>();

        ResultLeaveThread leaveThread = gson.fromJson(chatMessage.getContent(), ResultLeaveThread.class);
        leaveThread.setThreadId(chatMessage.getSubjectId());
        chatResponse.setErrorCode(0);
        chatResponse.setHasError(false);
        chatResponse.setErrorMessage("");
        chatResponse.setUniqueId(chatMessage.getUniqueId());
        chatResponse.setResult(leaveThread);

        String jsonThread = gson.toJson(chatResponse);

        listenerManager.callOnThreadLeaveParticipant(jsonThread, chatResponse);
        messageCallbacks.remove(messageUniqueId);
        if (log) Logger.i("RECEIVE_LEAVE_THREAD");
        if (log) Logger.json(jsonThread);
    }

    private void handleAddParticipant(ChatMessage chatMessage, String messageUniqueId) {
        Thread thread = gson.fromJson(chatMessage.getContent(), Thread.class);
        ChatResponse<ResultAddParticipant> chatResponse = new ChatResponse<>();

        ResultAddParticipant resultAddParticipant = new ResultAddParticipant();
        resultAddParticipant.setThread(thread);
        chatResponse.setErrorCode(0);
        chatResponse.setErrorMessage("");
        chatResponse.setHasError(false);
        chatResponse.setResult(resultAddParticipant);
        chatResponse.setUniqueId(chatMessage.getUniqueId());

        String jsonAddParticipant = gson.toJson(chatResponse);

        listenerManager.callOnThreadAddParticipant(jsonAddParticipant, chatResponse);
        messageCallbacks.remove(messageUniqueId);
        if (log) Logger.i("RECEIVE_ADD_PARTICIPANT");
        if (log) Logger.json(jsonAddParticipant);
    }

    private void handleOutPutDeleteMsg(ChatMessage chatMessage) {

        ChatResponse<ResultDeleteMessage> chatResponse = new ChatResponse<>();
        chatResponse.setErrorCode(0);
        chatResponse.setErrorMessage("");
        chatResponse.setHasError(false);
        chatResponse.setUniqueId(chatMessage.getUniqueId());

        ResultDeleteMessage resultDeleteMessage = new ResultDeleteMessage();
        DeleteMessageContent deleteMessage = new DeleteMessageContent();
        deleteMessage.setId(Long.valueOf(chatMessage.getContent()));
        resultDeleteMessage.setDeletedMessage(deleteMessage);
        chatResponse.setResult(resultDeleteMessage);

        String jsonDeleteMsg = gson.toJson(chatResponse);

        listenerManager.callOnDeleteMessage(jsonDeleteMsg, chatResponse);
        if (log) Logger.i("RECEIVE_DELETE_MESSAGE");
        if (log) Logger.json(jsonDeleteMsg);
    }

    private void handleOutPutBlock(ChatMessage chatMessage, String messageUniqueId) {

        Contact contact = gson.fromJson(chatMessage.getContent(), Contact.class);
        ChatResponse<ResultBlock> chatResponse = new ChatResponse<>();
        ResultBlock resultBlock = new ResultBlock();
        resultBlock.setContact(contact);
        chatResponse.setResult(resultBlock);
        chatResponse.setErrorCode(0);
        chatResponse.setHasError(false);
        chatResponse.setUniqueId(chatMessage.getUniqueId());

        String jsonBlock = gson.toJson(chatResponse);
        listenerManager.callOnBlock(jsonBlock, chatResponse);
        if (log) Logger.i("RECEIVE_BLOCK");
        if (log) Logger.json(jsonBlock);
        messageCallbacks.remove(messageUniqueId);
    }

    private void handleUnBlock(ChatMessage chatMessage, String messageUniqueId) {

        Contact contact = gson.fromJson(chatMessage.getContent(), Contact.class);
        ChatResponse<ResultBlock> chatResponse = new ChatResponse<>();
        ResultBlock resultBlock = new ResultBlock();
        resultBlock.setContact(contact);
        chatResponse.setResult(resultBlock);
        chatResponse.setErrorCode(0);
        chatResponse.setHasError(false);
        chatResponse.setUniqueId(chatMessage.getUniqueId());

        String jsonUnBlock = gson.toJson(chatResponse);
        listenerManager.callOnUnBlock(jsonUnBlock, chatResponse);
        if (log) Logger.i("RECEIVE_UN_BLOCK");
        if (log) Logger.json(jsonUnBlock);
        messageCallbacks.remove(messageUniqueId);
    }

    private void handleOutPutGetBlockList(ChatMessage chatMessage) {
        ChatResponse<ResultBlockList> chatResponse = new ChatResponse<>();
        chatResponse.setErrorCode(0);
        chatResponse.setHasError(false);
        chatResponse.setUniqueId(chatMessage.getUniqueId());
        ResultBlockList resultBlockList = new ResultBlockList();

        List<Contact> contacts = gson.fromJson(chatMessage.getContent(), new TypeToken<ArrayList<Contact>>() {
        }.getType());
        resultBlockList.setContacts(contacts);
        chatResponse.setResult(resultBlockList);
        String jsonGetBlock = JsonUtil.getJson(chatResponse);
        listenerManager.callOnGetBlockList(jsonGetBlock, chatResponse);
        if (log) Logger.i("RECEIVE_GET_BLOCK_LIST");
        if (log) Logger.json(jsonGetBlock);
    }

    private void handleOutPutRemoveParticipant(Callback callback, ChatMessage chatMessage, String messageUniqueId) {

        ChatResponse<ResultParticipant> chatResponse = reformatThreadParticipants(callback, chatMessage);

        String jsonRmParticipant = gson.toJson(chatResponse);

        listenerManager.callOnThreadRemoveParticipant(jsonRmParticipant, chatResponse);
        messageCallbacks.remove(messageUniqueId);
        if (log) Logger.i("RECEIVE_REMOVE_PARTICIPANT");
        if (log) Logger.json(jsonRmParticipant);
    }

    private void handleOutPutGetHistory(Callback callback, ChatMessage chatMessage, String messageUniqueId) {
        ChatResponse<ResultHistory> chatResponse = new ChatResponse<>();

        ResultHistory resultHistory = new ResultHistory();

        List<MessageVO> messageVOS = gson.fromJson(chatMessage.getContent(), new TypeToken<ArrayList<MessageVO>>() {
        }.getType());

        resultHistory.setNextOffset(callback.getOffset() + messageVOS.size());
        resultHistory.setContentCount(chatMessage.getContentCount());
        if (messageVOS.size() + callback.getOffset() < chatMessage.getContentCount()) {
            resultHistory.setHasNext(true);
        } else {
            resultHistory.setHasNext(false);
        }

        resultHistory.setHistory(messageVOS);
        chatResponse.setErrorCode(0);
        chatResponse.setHasError(false);
        chatResponse.setErrorMessage("");
        chatResponse.setResult(resultHistory);
        chatResponse.setUniqueId(chatMessage.getUniqueId());

        String json = JsonUtil.getJson(chatResponse);
        listenerManager.callOnGetThreadHistory(json, chatResponse);

        messageCallbacks.remove(messageUniqueId);
        if (log) Logger.i("RECEIVE_GET_HISTORY");
        if (log) Logger.json(json);
    }

    private ChatResponse<ResultParticipant> reformatThreadParticipants(Callback callback, ChatMessage chatMessage) {

        ArrayList<Participant> participants = gson.fromJson(chatMessage.getContent(), new TypeToken<ArrayList<Participant>>() {
        }.getType());

        ChatResponse<ResultParticipant> outPutParticipant = new ChatResponse<>();
        outPutParticipant.setErrorCode(0);
        outPutParticipant.setErrorMessage("");
        outPutParticipant.setHasError(false);
        outPutParticipant.setUniqueId(chatMessage.getUniqueId());

        ResultParticipant resultParticipant = new ResultParticipant();

        resultParticipant.setContentCount(chatMessage.getContentCount());
        if (participants.size() + callback.getOffset() < chatMessage.getContentCount()) {
            resultParticipant.setHasNext(true);
        } else {
            resultParticipant.setHasNext(false);
        }

        resultParticipant.setParticipants(participants);
        outPutParticipant.setResult(resultParticipant);
        resultParticipant.setNextOffset(callback.getOffset() + participants.size());
        return outPutParticipant;
    }

    private void sendTextMessageWithFile(String description, long threadId, String metaData, String systemMetadata, String uniqueId, String typeCode, Integer messageType) {
        ChatMessage chatMessage = new ChatMessage();
        if (systemMetadata != null) {
            chatMessage.setSystemMetadata(systemMetadata);
        }

        if (typeCode != null && !typeCode.isEmpty()) {
            chatMessage.setTypeCode(typeCode);
        } else {
            chatMessage.setTypeCode(getTypeCode());
        }

        if (messageType != null) {
            chatMessage.setMessageType(messageType);
        }
        chatMessage.setContent(description);
        chatMessage.setType(Constants.MESSAGE);
        chatMessage.setTokenIssuer("1");
        chatMessage.setToken(getToken());
        chatMessage.setMetadata(metaData);


        chatMessage.setUniqueId(uniqueId);
        chatMessage.setTime(1000);
        chatMessage.setSubjectId(threadId);

        JsonAdapter<ChatMessage> chatMessageJsonAdapter = moshi.adapter(ChatMessage.class);
        String asyncContent = chatMessageJsonAdapter.toJson(chatMessage);

        setThreadCallbacks(threadId, uniqueId);
        sendAsyncMessage(asyncContent, 4, "SEND_TXT_MSG_WITH_FILE");
    }

    private void setThreadCallbacks(long threadId, String uniqueId) {
        if (chatReady) {
            Callback callback = new Callback();
            callback.setDelivery(true);
            callback.setSeen(true);
            callback.setSent(true);
            callback.setUniqueId(uniqueId);
            ArrayList<Callback> callbacks = new ArrayList<>();
            callbacks.add(callback);
            threadCallbacks.put(threadId, callbacks);
        }
    }

    private void sendAsyncMessage(String asyncContent, int asyncMsgType, String logMessage) {
        if (chatReady) {
            if (log) Logger.i(logMessage);
            if (log) Logger.json(asyncContent);
            try {
                async.sendMessage(asyncContent, asyncMsgType);
            } catch (Exception e) {
                if (log) Logger.e(e.getMessage());
                return;
            }
            long lastSentMessageTimeout = 9 * 1000;
            lastSentMessageTime = new Date().getTime();
            if (state) {
                pingHandler.postDelayed(() -> {
                    long currentTime = new Date().getTime();
                    if (currentTime - lastSentMessageTime > lastSentMessageTimeout) {
                        ping();
                    }
                }, 20000);
            } else {
                Error error = new Error();
//                error.setCode();
                if (log) Logger.e("Async is Close");
            }
        } else {
            String jsonError = getErrorOutPut(ChatConstant.ERROR_CHAT_READY, ChatConstant.ERROR_CODE_CHAT_READY, null);
            if (log) Logger.e(jsonError);
        }
    }

    /**
     * Get the list of the Device Contact
     */
    private List<Contact> getPhoneContact(Context context) {
        String name, phoneNumber, lastName, timeStamp;
        Cursor cursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        if (cursor == null) throw new AssertionError();
        ArrayList<Contact> storeContacts = new ArrayList<>();
        while (cursor.moveToNext()) {
            name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            lastName = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME));
//            timeStamp = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_LAST_UPDATED_TIMESTAMP));
            phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            Contact contact = new Contact();
            char ch1 = phoneNumber.charAt(0);
            if (Character.toString(ch1) != "+") {
                contact.setCellphoneNumber(phoneNumber.replaceAll(Character.toString(ch1), "+98"));
            }
            contact.setCellphoneNumber(phoneNumber.replaceAll(" ", ""));
            contact.setFirstName(name.replaceAll(" ", ""));
            contact.setLastName(lastName.replaceAll(" ", ""));
            storeContacts.add(contact);
        }
        cursor.close();
        return storeContacts;
    }

    private String getRealPathFromURI(Context context, Uri contentUri) {
        String[] strings = {MediaStore.Images.Media.DATA};
        CursorLoader loader = new CursorLoader(context, contentUri, strings, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(column_index);
        cursor.close();
        return result;
    }

    private void uploadFileMessage(Activity activity, String description, long threadId,
                                   String mimeType, String filePath, String metadata,
                                   String uniqueId, String typeCode, Integer messageType) {
        try {
            if (Permission.Check_READ_STORAGE(activity)) {
                if (getFileServer() != null) {
                    File file = new File(filePath);
                    long file_size;
                    if (file.exists() || file.isFile()) {
                        file_size = file.length();

                        RetrofitHelperFileServer retrofitHelperFileServer = new RetrofitHelperFileServer(getFileServer());
                        FileApi fileApi = retrofitHelperFileServer.getService(FileApi.class);
                        RequestBody name = RequestBody.create(MediaType.parse("multipart/form-data"), file.getName());
                        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
                        MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);
                        Observable<Response<FileUpload>> uploadObservable = fileApi.sendFile(body, getToken(), TOKEN_ISSUER, name);
                        long finalFile_size = file_size;
                        uploadObservable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Response<FileUpload>>() {
                            @Override
                            public void call(Response<FileUpload> fileUploadResponse) {
                                if (fileUploadResponse.isSuccessful()) {
                                    boolean error = fileUploadResponse.body().isHasError();
                                    if (error) {
                                        String errorMessage = fileUploadResponse.body().getMessage();
                                        if (log) Logger.e(errorMessage);
                                    } else {

                                        ResultFile result = fileUploadResponse.body().getResult();
                                        int fileId = result.getId();
                                        String hashCode = result.getHashCode();

                                        ChatResponse<ResultFile> chatResponse = new ChatResponse<>();
                                        chatResponse.setResult(result);
                                        chatResponse.setUniqueId(uniqueId);
                                        result.setSize(file_size);
                                        String json = gson.toJson(chatResponse);

                                        listenerManager.callOnUploadFile(json, chatResponse);
                                        if (log) Logger.i("RECEIVE_UPLOAD_FILE");
                                        if (log) Logger.json(json);


                                        MetaDataFile metaDataFile = new MetaDataFile();
                                        FileMetaDataContent metaDataContent = new FileMetaDataContent();
                                        metaDataContent.setHashCode(hashCode);
                                        metaDataContent.setId(fileId);
                                        metaDataContent.setName(result.getName());
                                        metaDataContent.setMimeType(mimeType);
                                        metaDataContent.setSize(finalFile_size);
                                        metaDataContent.setLink(getFile(fileId, hashCode, true));

                                        metaDataFile.setFile(metaDataContent);

                                        String jsonMeta = JsonUtil.getJson(metaDataFile);
                                        if (log) Logger.json(jsonMeta);
                                        sendTextMessageWithFile(description, threadId, jsonMeta, metadata, uniqueId, typeCode, messageType);
                                    }
                                }
                            }
                        }, throwable -> {
                            if (log) Logger.e(throwable.getMessage());
                        });
                    } else {
                        if (log) Logger.e("File Is Not Exist");
                    }
                } else {
                    if (log) Logger.e("FileServer url Is null");
                }
            } else {
                String jsonError = getErrorOutPut(ChatConstant.ERROR_READ_EXTERNAL_STORAGE_PERMISSION, ChatConstant.ERROR_CODE_READ_EXTERNAL_STORAGE_PERMISSION, null);
                if (log) Logger.e(jsonError);
            }
        } catch (Exception e) {
            if (log) Logger.e(e.getCause().getMessage());
        }

    }

    private void uploadImageFileMessage(Context context, Activity activity, String description, long threadId,
                                        Uri fileUri, String mimeType, String metadata, String uniqueId,
                                        String typeCode, Integer messageType) {
        if (fileServer != null) {
            if (Permission.Check_READ_STORAGE(activity)) {
                String path = FilePick.getSmartFilePath(context, fileUri);
                File file = new File(path);
                if (file.exists()) {
                    long fileSize = file.length();
                    RetrofitHelperFileServer retrofitHelperFileServer = new RetrofitHelperFileServer(getFileServer());
                    FileApi fileApi = retrofitHelperFileServer.getService(FileApi.class);

                    RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
                    MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), requestFile);
                    RequestBody name = RequestBody.create(MediaType.parse("text/plain"), file.getName());

                    Observable<Response<FileImageUpload>> uploadObservable = fileApi.sendImageFile(body, getToken(), TOKEN_ISSUER, name);
                    uploadObservable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Response<FileImageUpload>>() {
                        @Override
                        public void call(Response<FileImageUpload> fileUploadResponse) {
                            if (fileUploadResponse.isSuccessful()) {
                                boolean hasError = fileUploadResponse.body().isHasError();
                                if (hasError) {
                                    String errorMessage = fileUploadResponse.body().getMessage();
                                    int errorCode = fileUploadResponse.body().getErrorCode();
                                    String jsonError = getErrorOutPut(errorMessage, errorCode, null);
                                    if (log) Logger.e(jsonError);
                                } else {

                                    ResultImageFile result = fileUploadResponse.body().getResult();
                                    int imageId = result.getId();
                                    String hashCode = result.getHashCode();

                                    ChatResponse<ResultImageFile> chatResponse = new ChatResponse<>();
                                    ResultImageFile resultImageFile = new ResultImageFile();
                                    chatResponse.setUniqueId(uniqueId);
                                    resultImageFile.setId(result.getId());
                                    resultImageFile.setHashCode(result.getHashCode());
                                    resultImageFile.setName(result.getName());
                                    resultImageFile.setHeight(result.getHeight());
                                    resultImageFile.setWidth(result.getWidth());
                                    resultImageFile.setActualHeight(result.getActualHeight());
                                    resultImageFile.setActualWidth(result.getActualWidth());

                                    chatResponse.setResult(resultImageFile);

                                    String imageJson = gson.toJson(chatResponse);

                                    listenerManager.callOnUploadImageFile(imageJson, chatResponse);
                                    if (log) Logger.i("RECEIVE_UPLOAD_IMAGE");
                                    if (log) Logger.json(imageJson);

                                    MetaDataImageFile metaData = new MetaDataImageFile();
                                    FileImageMetaData fileMetaData = new FileImageMetaData();
                                    fileMetaData.setHashCode(hashCode);
                                    fileMetaData.setId(imageId);
                                    fileMetaData.setName(result.getName());
                                    fileMetaData.setActualHeight(result.getActualHeight());
                                    fileMetaData.setActualWidth(result.getActualWidth());
                                    fileMetaData.setMimeType(mimeType);
                                    fileMetaData.setSize(fileSize);
                                    fileMetaData.setLink(getImage(imageId, hashCode, true));
                                    metaData.setFile(fileMetaData);

                                    String metaJson = JsonUtil.getJson(metaData);
                                    sendTextMessageWithFile(description, threadId, metaJson, metadata, uniqueId, typeCode, messageType);
                                    if (log) Logger.json(metaJson);

                                }
                            }
                        }
                    }, throwable -> {
                        String jsonError = getErrorOutPut(ChatConstant.ERROR_UNKNOWN_EXCEPTION, ChatConstant.ERROR_CODE_UNKNOWN_EXCEPTION, null);
                        if (log) Logger.e(jsonError);

                    });
                } else {
                    if (log) Logger.e("File Is Not Exist");
                }

            } else {
                String jsonError = getErrorOutPut(ChatConstant.ERROR_READ_EXTERNAL_STORAGE_PERMISSION, ChatConstant.ERROR_CODE_READ_EXTERNAL_STORAGE_PERMISSION, null);
                if (log) Logger.e(jsonError);
            }
        } else {
            if (log) Logger.e("FileServer url Is null");
        }
    }

    //model
    private class DeleteMessage {
        private boolean deleteForAll;

        public boolean isDeleteForAll() {
            return deleteForAll;
        }

        public void setDeleteForAll(boolean deleteForAll) {
            this.deleteForAll = deleteForAll;
        }
    }

    //TODO make it public
    // Add list of contacts with their mobile numbers and their cellphoneNumbers
    private void addContacts(ArrayList<String> firstNames, ArrayList<String> lastNames, ArrayList<String> cellphoneNumbers) {
        ArrayList<String> emails = new ArrayList<>();
        for (int i = 0; i < cellphoneNumbers.size(); i++) {
            emails.add("");
        }
        Observable<Response<AddContacts>> addContactsObservable;
        if (getPlatformHost() != null) {
            addContactsObservable = contactApi.addContacts(getToken(), TOKEN_ISSUER, firstNames, lastNames, emails, cellphoneNumbers, cellphoneNumbers);
            addContactsObservable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Response<AddContacts>>() {
                @Override
                public void call(Response<AddContacts> contactsResponse) {
                    boolean error = contactsResponse.body().getHasError();
                    if (contactsResponse.isSuccessful()) {
                        if (error) {
                            String jsonError = getErrorOutPut(contactsResponse.body().getMessage(), contactsResponse.body().getErrorCode(), null);
                            if (log) Logger.e(jsonError);
                        } else {
                            AddContacts contacts = contactsResponse.body();
                            String contactsJson = JsonUtil.getJson(contacts);
                            if (syncContacts) {
                                listenerManager.callOnSyncContact(contactsJson);
                                if (log) Logger.i("SYNC_CONTACT");
                                syncContacts = false;
                            } else {
                                listenerManager.callOnAddContact(contactsJson);
                                if (log) Logger.i("ADD_CONTACTS");
                            }
                            if (log) Logger.json(contactsJson);
                        }
                    }
                }
            }, new Action1<Throwable>() {
                @Override
                public void call(Throwable throwable) {
                    if (log) Logger.e("Error on add contacts", throwable.toString());
                    if (log) Logger.e(throwable.getCause().getMessage());
                }
            });
        }
    }

    @NonNull
    private ChatMessage getChatMessage(String contentThreadChat, String uniqueId, String typeCode) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setContent(contentThreadChat);
        chatMessage.setType(Constants.INVITATION);
        chatMessage.setToken(getToken());
        chatMessage.setUniqueId(uniqueId);
        chatMessage.setTokenIssuer("1");
        if (typeCode != null && !typeCode.isEmpty()) {
            chatMessage.setTypeCode(typeCode);
        } else {
            chatMessage.setTypeCode(getTypeCode());
        }
        return chatMessage;
    }

    private String onMessage() {
        return async.getMessageLiveData().getValue();
    }

    /**
     * Get the manager that manages registered listeners.
     */
    ChatListenerManager getListenerManager() {
        return listenerManager;
    }

    @NonNull
    private ChatMessage getChatMessage(MessageVO jsonMessage) {
        ChatMessage message = new ChatMessage();
        message.setType(Constants.DELIVERY);
        message.setContent(String.valueOf(jsonMessage.getId()));
        message.setTokenIssuer("1");
        message.setToken(getToken());
        message.setUniqueId(generateUniqueId());
        message.setTime(1000);
        return message;
    }

    private String reformatUserInfo(ChatMessage chatMessage, ChatResponse<ResultUserInfo> outPutUserInfo, UserInfo userInfo) {

        ResultUserInfo result = new ResultUserInfo();

        if (cache) {
            messageDatabaseHelper.saveUserInfo(userInfo);
        }

        setUserId(userInfo.getId());
        result.setUser(userInfo);
        outPutUserInfo.setErrorCode(0);
        outPutUserInfo.setErrorMessage("");
        outPutUserInfo.setHasError(false);
        outPutUserInfo.setResult(result);
        outPutUserInfo.setUniqueId(chatMessage.getUniqueId());

        return JsonUtil.getJson(outPutUserInfo);
    }

    private String reformatMuteThread(ChatMessage chatMessage, ChatResponse<ResultMute> outPut) {
        ResultMute resultMute = new ResultMute();
        resultMute.setThreadId(Long.valueOf(chatMessage.getContent()));
        outPut.setResult(resultMute);
        outPut.setHasError(false);
        outPut.setErrorMessage("");
        outPut.setUniqueId(chatMessage.getUniqueId());
        gson.toJson(outPut);
        return gson.toJson(outPut);
    }

    private String reformatCreateThread(ChatMessage chatMessage, OutPutThread outPutThread) {
        if (log) Log.i("RECEIVE_INVITATION ", chatMessage.getContent());
        ResultThread resultThread = new ResultThread();

        Thread thread = gson.fromJson(chatMessage.getContent(), Thread.class);

        resultThread.setThread(thread);
        outPutThread.setHasError(false);
        outPutThread.setErrorCode(0);
        outPutThread.setErrorMessage("");
        outPutThread.setResult(resultThread);
        outPutThread.setUniqueId(chatMessage.getUniqueId());
        return JsonUtil.getJson(outPutThread);
    }

    private void deviceIdRequest(String ssoHost, String serverAddress, String appId, String severName) {
        if (log) Logger.i("GET_DEVICE_ID");
        currentDeviceExist = false;

        RetrofitHelperSsoHost retrofitHelperSsoHost = new RetrofitHelperSsoHost(ssoHost);
        TokenApi tokenApi = retrofitHelperSsoHost.getService(TokenApi.class);
        rx.Observable<Response<DeviceResult>> listObservable = tokenApi.getDeviceId("Bearer" + " " + getToken());
        listObservable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(deviceResults -> {
            if (deviceResults.isSuccessful()) {
                ArrayList<Device> devices = deviceResults.body().getDevices();
                for (Device device : devices) {
                    if (device.isCurrent()) {
                        currentDeviceExist = true;
                        if (log) Logger.i("DEVICE_ID :" + device.getUid());
                        async.connect(serverAddress, appId, severName, token, ssoHost, device.getUid());
                        break;
                    }
                }
                if (!currentDeviceExist) {
                    String jsonError = getErrorOutPut(ChatConstant.ERROR_CURRENT_DEVICE, ChatConstant.ERROR_CODE_CURRENT_DEVICE, null);
                    if (log) Logger.e(jsonError);
                }
            } else {
                if (deviceResults.code() == 401) {
                    String jsonError = getErrorOutPut("unauthorized", deviceResults.code(), null);
                    if (log) Logger.e(jsonError);
                } else {
                    String jsonError = getErrorOutPut(deviceResults.message(), deviceResults.code(), null);
                    if (log) Logger.e(jsonError);
                }
            }


        }, (Throwable throwable) -> {
            if (log) Logger.e("Error on get devices");
            if (log) Logger.e(throwable.getMessage());

        });
    }

    /**
     * Reformat the get thread response
     */
    private ChatResponse<ResultThreads> reformatGetThreadsResponse(ChatMessage chatMessage, Callback callback) {
        ChatResponse<ResultThreads> outPutThreads = new ChatResponse<>();
        ArrayList<Thread> threads = gson.fromJson(chatMessage.getContent(), new TypeToken<ArrayList<Thread>>() {
        }.getType());

        if (cache) {
            ArrayList<ThreadVo> threadVos = gson.fromJson(chatMessage.getContent(), new TypeToken<ArrayList<ThreadVo>>() {
            }.getType());
            messageDatabaseHelper.saveThreads(threadVos);
        }


        ResultThreads resultThreads = new ResultThreads();
        resultThreads.setThreads(threads);
        resultThreads.setContentCount(chatMessage.getContentCount());
        outPutThreads.setErrorCode(0);
        outPutThreads.setErrorMessage("");
        outPutThreads.setHasError(false);
        outPutThreads.setUniqueId(chatMessage.getUniqueId());

        if (threads.size() + callback.getOffset() < chatMessage.getContentCount()) {
            resultThreads.setHasNext(true);
        } else {
            resultThreads.setHasNext(false);
        }
        resultThreads.setNextOffset(callback.getOffset() + threads.size());
        outPutThreads.setResult(resultThreads);
        return outPutThreads;
    }

    @NonNull
    private String reformatError(boolean hasError, ChatMessage chatMessage, OutPutHistory outPut) {
        Error error = JsonUtil.fromJSON(chatMessage.getContent(), Error.class);
        Log.e("RECEIVED_ERROR", chatMessage.getContent());
        Log.e("ErrorMessage", error.getMessage());
        Log.e("ErrorCode", String.valueOf(error.getCode()));
        outPut.setHasError(hasError);
        outPut.setErrorMessage(error.getMessage());
        outPut.setErrorCode(error.getCode());
        return JsonUtil.getJson(outPut);
    }

    @NonNull
    private ChatResponse<ResultContact> reformatGetContactResponse(ChatMessage chatMessage, Callback callback) {
        ResultContact resultContact = new ResultContact();
        ChatResponse<ResultContact> outPutContact = new ChatResponse<>();
        ArrayList<Contact> contacts = gson.fromJson(chatMessage.getContent(), new TypeToken<ArrayList<Contact>>() {
        }.getType());
        Log.i(String.valueOf(cache), "");
        if (cache) {
            messageDatabaseHelper.save(contacts);
            ArrayList<Contact> contactsList = new ArrayList<>(messageDatabaseHelper.getContacts());
            resultContact.setContacts(contactsList);

        } else {
            resultContact.setContacts(contacts);
        }
        resultContact.setContentCount(chatMessage.getContentCount());

        if (contacts.size() + callback.getOffset() < chatMessage.getContentCount()) {
            resultContact.setHasNext(true);
        } else {
            resultContact.setHasNext(false);
        }
        resultContact.setNextOffset(callback.getOffset() + contacts.size());
        resultContact.setContentCount(chatMessage.getContentCount());

        outPutContact.setResult(resultContact);
        outPutContact.setErrorMessage("");
        outPutContact.setUniqueId(chatMessage.getUniqueId());
        return outPutContact;
    }

    private static synchronized String generateUniqueId() {
        return UUID.randomUUID().toString();
    }

    public void setToken(String token) {
        this.token = token;
        if (asyncReady) {
            pingAfterSetToken();
        }
    }

    private String getToken() {
        return token;
    }

    private long getUserId() {
        return userId;
    }

    private void setUserId(long userId) {
        this.userId = userId;
    }

    private void setCallBacks(Boolean delivery, Boolean sent, Boolean seen, Boolean result, int requestType, Long offset, String uniqueId) {
        if (chatReady || asyncReady) {
            delivery = delivery != null ? delivery : false;
            sent = sent != null ? sent : false;
            seen = seen != null ? seen : false;
            result = result != null ? result : false;
            offset = offset != null ? offset : 0;
            Callback callback = new Callback();
            callback.setDelivery(delivery);
            callback.setOffset(offset);
            callback.setSeen(seen);
            callback.setSent(sent);
            callback.setRequestType(requestType);
            callback.setResult(result);
            messageCallbacks.put(uniqueId, callback);
        }
    }

    private void setPlatformHost(String platformHost) {
        this.platformHost = platformHost;
    }

    private String getPlatformHost() {
        return platformHost;
    }

    private void setContext(Context context) {
        this.context = context;
    }

    private Context getContext() {
        return context;
    }

    private void setFileServer(String fileServer) {
        this.fileServer = fileServer;
    }

    private String getFileServer() {
        return fileServer;
    }

    public interface GetThreadHandler {
        void onGetThread();
    }

    public interface SendTextMessageHandler {
        void onSent(String uniqueId, long threadId);

        void onSentResult(String content);
    }
}