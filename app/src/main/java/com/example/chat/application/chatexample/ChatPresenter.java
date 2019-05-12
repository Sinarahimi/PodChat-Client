package com.example.chat.application.chatexample;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.fanap.podchat.ProgressHandler;
import com.fanap.podchat.chat.Chat;
import com.fanap.podchat.chat.ChatAdapter;
import com.fanap.podchat.chat.ChatHandler;
import com.fanap.podchat.chat.ChatListener;
import com.fanap.podchat.mainmodel.History;
import com.fanap.podchat.mainmodel.Invitee;
import com.fanap.podchat.mainmodel.NosqlListMessageCriteriaVO;
import com.fanap.podchat.mainmodel.ResultDeleteMessage;
import com.fanap.podchat.mainmodel.SearchContact;
import com.fanap.podchat.mainmodel.ThreadInfoVO;
import com.fanap.podchat.model.ChatResponse;
import com.fanap.podchat.model.ErrorOutPut;
import com.fanap.podchat.model.OutPutMapNeshan;
import com.fanap.podchat.model.OutPutThread;
import com.fanap.podchat.model.ResultAddContact;
import com.fanap.podchat.model.ResultAddParticipant;
import com.fanap.podchat.model.ResultBlock;
import com.fanap.podchat.model.ResultBlockList;
import com.fanap.podchat.model.ResultContact;
import com.fanap.podchat.model.ResultFile;
import com.fanap.podchat.model.ResultHistory;
import com.fanap.podchat.model.ResultImageFile;
import com.fanap.podchat.model.ResultLeaveThread;
import com.fanap.podchat.model.ResultMessage;
import com.fanap.podchat.model.ResultMute;
import com.fanap.podchat.model.ResultNewMessage;
import com.fanap.podchat.model.ResultParticipant;
import com.fanap.podchat.model.ResultRemoveContact;
import com.fanap.podchat.model.ResultStaticMapImage;
import com.fanap.podchat.model.ResultThread;
import com.fanap.podchat.model.ResultThreads;
import com.fanap.podchat.model.ResultUpdateContact;
import com.fanap.podchat.model.ResultUserInfo;
import com.fanap.podchat.requestobject.RequestAddAdmin;
import com.fanap.podchat.requestobject.RequestAddParticipants;
import com.fanap.podchat.requestobject.RequestClearHistory;
import com.fanap.podchat.requestobject.RequestConnect;
import com.fanap.podchat.requestobject.RequestCreateThread;
import com.fanap.podchat.requestobject.RequestDeleteMessage;
import com.fanap.podchat.requestobject.RequestDeliveredMessageList;
import com.fanap.podchat.requestobject.RequestFileMessage;
import com.fanap.podchat.requestobject.RequestForwardMessage;
import com.fanap.podchat.requestobject.RequestGetAdmin;
import com.fanap.podchat.requestobject.RequestGetHistory;
import com.fanap.podchat.requestobject.RequestLeaveThread;
import com.fanap.podchat.requestobject.RequestLocationMessage;
import com.fanap.podchat.requestobject.RequestMapReverse;
import com.fanap.podchat.requestobject.RequestMapStaticImage;
import com.fanap.podchat.requestobject.RequestMessage;
import com.fanap.podchat.requestobject.RequestRemoveParticipants;
import com.fanap.podchat.requestobject.RequestReplyFileMessage;
import com.fanap.podchat.requestobject.RequestReplyMessage;
import com.fanap.podchat.requestobject.RequestSeenMessageList;
import com.fanap.podchat.requestobject.RequestSignalMsg;
import com.fanap.podchat.requestobject.RequestThread;
import com.fanap.podchat.requestobject.RequestThreadInfo;
import com.fanap.podchat.requestobject.RequestUnBlock;
import com.fanap.podchat.requestobject.RequestUpdateContact;
import com.fanap.podchat.requestobject.RequestUploadFile;
import com.fanap.podchat.requestobject.RetryUpload;

import java.util.ArrayList;
import java.util.List;

public class ChatPresenter extends ChatAdapter implements ChatContract.presenter {

    private Chat chat;
    private ChatContract.view view;
    private Context context;
    private Activity activity;

    public ChatPresenter(Context context, ChatContract.view view, Activity activity) {
        chat = Chat.init(context);
//        RefWatcher refWatcher = LeakCanary.installedRefWatcher();
//        refWatcher.watch(chat);

        chat.addListener(this);
        chat.addListener(new ChatListener() {
            @Override
            public void onSent(String content, ChatResponse<ResultMessage> response) {

            }
        });

        chat.isCacheables(false);
        chat.isLoggable(true);
        chat.rawLog(true);
        chat.setSignalIntervalTime(2000);
//        chat.setExpireAmount(180);
        this.activity = activity;
        this.context = context;
        this.view = view;
    }

    @Override
    public void sendLocationMessage(RequestLocationMessage request) {
        chat.sendLocationMessage(request);
    }

    @Override
    public void OnLogEvent(String log) {
        view.onLogEvent(log);
    }



    @Override
    public void isDatabaseOpen() {
        chat.isDbOpen();
    }

    @Override
    public void retryUpload(RetryUpload retry, ProgressHandler.sendFileMessage handler) {
        chat.retryUpload(retry, handler);
    }

    @Override
    public void resendMessage(String uniqueId) {
        chat.resendMessage(uniqueId);
    }

    @Override
    public void cancelMessage(String uniqueId) {
        chat.cancelMessage(uniqueId);
    }

    @Override
    public void retryUpload(String uniqueId) {

    }


    @Override
    public void cancelUpload(String uniqueId) {
        chat.cancelUpload(uniqueId);
    }

    @Override
    public void seenMessageList(RequestSeenMessageList requestParams) {
    }

    @Override
    public void deliveredMessageList(RequestDeliveredMessageList requestParams) {
//        RequestDeliveredMessageList requestDeliveredMessageList = new RequestDeliveredMessageList
//                .Builder(messageId)
//                .build();
//        chat.getMessageDeliveredList(requestDeliveredMessageList);
    }

    @Override
    public void createThreadWithMessage(RequestCreateThread threadRequest) {
//        RequestThreadInnerMessage innerMessage = new RequestThreadInnerMessage.Builder()
//                .message()
//                .forwardedMessageIds()
//                .metadata()
//                .systemMetadata()
//                .type()
//                .build();
//
//        RequestCreateThread createThread = new RequestCreateThread
//                .Builder(threadType , invitees)
//                .message(innerMessage)
//                .build();


        chat.createThreadWithMessage(threadRequest);
    }

    @Override
    public String createThread(int threadType, Invitee[] invitee, String threadTitle, String description, String image
            , String metadata) {
        return chat.createThread(threadType, invitee, threadTitle, description, image, metadata, null);
    }

    @Override
    public void getThreads(RequestThread requestThread, ChatHandler handler) {
        chat.getThreads(requestThread, handler);
    }

    @Override
    public void getThreads(Integer count, Long offset, ArrayList<Integer> threadIds, String threadName, long creatorCoreUserId, long partnerCoreUserId, long partnerCoreContactId, ChatHandler handler) {
        chat.getThreads(count, offset, threadIds, threadName, creatorCoreUserId, partnerCoreUserId, partnerCoreContactId, handler);
    }

    @Override
    public void setToke(String token) {
        chat.setToken(token);
    }

    @Override
    public void connect(String serverAddress, String appId, String severName,
                        String token, String ssoHost, String platformHost, String fileServer, String typeCode) {
        chat.connect(serverAddress, appId, severName, token, ssoHost, platformHost, fileServer, typeCode);
//        PodNotify podNotify = new PodNotify.builder()
//                .setAppId(appId)
//                .setServerName("172.16.110.61:8017")
//                .setSocketServerAddress(serverAddress)
//                .setSsoHost(ssoHost)
//                .setToken(token)
//                .build(context);
//
//        podNotify.start(context);
        RequestConnect requestConnect = new RequestConnect
                .Builder(serverAddress, appId, severName, token, ssoHost, platformHost, fileServer)
                .build();
        chat.connect(requestConnect);
    }

    @Override
    public void mapSearch(String searchTerm, Double latitude, Double longitude) {
        chat.mapSearch(searchTerm, latitude, longitude);
    }

    @Override
    public void mapRouting(String origin, String originLng) {
        chat.mapRouting(origin, originLng);
    }

    @Override
    public void mapStaticImage(RequestMapStaticImage request) {
        chat.mapStaticImage(request);
    }

    @Override
    public void mapReverse(RequestMapReverse request) {
        chat.mapReverse(request);
    }

    @Override
    public void getThreads(Integer count, Long offset, ArrayList<Integer> threadIds, String threadName, ChatHandler handler) {
        chat.getThreads(count, offset, threadIds, threadName, handler);
    }

    @Override
    public void getUserInfo(ChatHandler handler) {
        chat.getUserInfo(handler);
    }

    @Override
    public void getHistory(History history, long threadId, ChatHandler handler) {
        String uniq = chat.getHistory(history, threadId, handler);
        if (uniq != null) {
            Log.i("un", uniq);
        }
    }

    @Override
    public void getHistory(RequestGetHistory request, ChatHandler handler) {
        chat.getHistory(request, handler);
    }

    @Override
    public void searchHistory(NosqlListMessageCriteriaVO builderListMessage, ChatHandler handler) {
        chat.searchHistory(builderListMessage, handler);
    }

    @Override
    public void getContact(Integer count, Long offset, ChatHandler handler) {
        chat.getContacts(count, offset, handler);
    }

    @Override
    public void createThread(int threadType, Invitee[] invitee, String threadTitle, String description, String image
            , String systemMetadata, ChatHandler handler) {
        chat.createThread(threadType, invitee, threadTitle, description, image, systemMetadata, handler);
    }

    @Override
    public void sendTextMessage(String textMessage, long threadId, Integer messageType, String metaData, ChatHandler handler) {
        chat.sendTextMessage(textMessage, threadId, messageType, metaData, handler);
    }

    @Override
    public void sendTextMessage(RequestMessage requestMessage, ChatHandler handler) {

//        RequestMessage message = new RequestMessage
//                .Builder(textMessage,threadId)
//                .build();
//

        chat.sendTextMessage(requestMessage, null);
    }

    @Override
    public void replyMessage(String messageContent, long threadId, long messageId, Integer messageType, ChatHandler handler) {
        chat.replyMessage(messageContent, threadId, messageId, "meta", messageType, handler);
    }

    @Override
    public void replyFileMessage(RequestReplyFileMessage request, ProgressHandler.sendFileMessage handler) {
        chat.replyFileMessage(request, handler);
    }

    @Override
    public void replyMessage(RequestReplyMessage request, ChatHandler handler) {
        chat.replyMessage(request, handler);
    }

    @Override
    public void muteThread(int threadId, ChatHandler handler) {
//        RequestMuteThread requestMuteThread = new RequestMuteThread.Builder()
//                .threadId(threadId)
//                .build();
//        chat.muteThread(requestMuteThread);
        chat.muteThread(threadId, handler);
    }

    @Override
    public void renameThread(long threadId, String title, ChatHandler handler) {
        chat.renameThread(threadId, title, handler);
    }

    @Override
    public void unMuteThread(int threadId, ChatHandler handler) {
//        RequestMuteThread muteThread = new RequestMuteThread.Builder()
//                .threadId(threadId)
//                .build();
//        chat.unMuteThread(muteThread,null);

        chat.unMuteThread(threadId, handler);
    }

    @Override
    public void editMessage(int messageId, String messageContent, String metaData, ChatHandler handler) {
//        RequestEditMessage editMessage = new RequestEditMessage.
//                Builder(messageContent,messageId)
//                .metaData()
//                .build();
//        chat.editMessage(editMessage,null);
        chat.editMessage(messageId, messageContent, metaData, handler);
    }

    @Override
    public void getThreadParticipant(int count, Long offset, long threadId, ChatHandler handler) {
//        RequestThreadParticipant participant = new RequestThreadParticipant.Builder(threadId)
//                .count()
//                .offset()
//                .build();
//        chat.getThreadParticipants()
        chat.getThreadParticipants(count, offset, threadId, handler);
    }

    @Override
    public void addContact(String firstName, String lastName, String cellphoneNumber, String email) {
        chat.addContact(firstName, lastName, cellphoneNumber, email);
    }

    @Override
    public void removeContact(long id) {
        chat.removeContact(id);
    }

    @Override
    public void searchContact(SearchContact searchContact) {
        chat.searchContact(searchContact);
    }

    @Override
    public void block(Long contactId, Long userId, Long threadId, ChatHandler handler) {
//        RequestBlock requestBlock = new RequestBlock.Builder()
//                .contactId()
//                .threadId()
//                .userId()
//                .build();
//        chat.block(requestBlock,null);
        chat.block(contactId, userId, threadId, handler);
    }

    @Override
    public void unBlock(Long blockId, Long userId, Long threadId, Long contactId, ChatHandler handler) {

        chat.unblock(blockId, userId, threadId, contactId, handler);
    }

    @Override
    public void unBlock(RequestUnBlock request, ChatHandler handler) {

//        RequestUnBlock requestUnBlock = new RequestUnBlock.Builder()
//                .blockId()
//                .contactId()
//                .threadId()
//                .userId()
//                .build();requestUnBlock = new RequestUnBlock.Builder()
//                .blockId()
//                .contactId()
//                .threadId()
//                .userId()
//                .build();

        chat.unblock(request, handler);
    }

    @Override
    public void spam(long threadId) {

//        RequestSpam requestSpam = new RequestSpam.Builder()
//                .threadId()
//                .build();

        chat.spam(threadId);
    }

    @Override
    public void getBlockList(Long count, Long offset, ChatHandler handler) {
        chat.getBlockList(count, offset, handler);
    }

    @Override
    public String sendFileMessage(Context context, Activity activity, String description, long threadId, Uri fileUri, String metaData, Integer messageType, ProgressHandler.sendFileMessage handler) {
        return chat.sendFileMessage( activity, description, threadId, fileUri, metaData, messageType, handler);
    }

    @Override
    public void sendFileMessage(RequestFileMessage requestFileMessage, ProgressHandler.sendFileMessage handler) {
//        RequestFileMessage fileMessage = new RequestFileMessage.
//                Builder(activity,threadId,fileUri)
//                .messageType()
//                .systemMetadata()
//                .build();
//


        chat.sendFileMessage(requestFileMessage, handler);
    }

    @Override
    public void syncContact(Activity activity) {
        chat.syncContact(activity);
    }

    @Override
    public void forwardMessage(long threadId, ArrayList<Long> messageIds) {
        chat.forwardMessage(threadId, messageIds);
    }

    @Override
    public void forwardMessage(RequestForwardMessage request) {
        chat.forwardMessage(request);
    }

    @Override
    public void updateContact(int id, String firstName, String lastName, String cellphoneNumber, String email) {
        chat.updateContact(id, firstName, lastName, cellphoneNumber, email);
    }

    @Override
    public void updateContact(RequestUpdateContact updateContact) {
        chat.updateContact(updateContact);
    }

    @Override
    public void uploadImage(Activity activity, Uri fileUri) {

//        RequestUploadImage requestUploadImage = new RequestUploadImage.Builder(activity,fileUri)
//                .build();
//
//        chat.uploadImage(requestUploadImage);

        chat.uploadImage(activity, fileUri);
    }

    @Override
    public void uploadFile(@NonNull Activity activity, @NonNull Uri uri) {

        RequestUploadFile uploadFile = new RequestUploadFile.Builder(activity, uri)
                .build();
        chat.uploadFile(uploadFile);
        chat.uploadFile(activity, uri);
    }

    @Override
    public void seenMessage(int messageId, long ownerId, ChatHandler handler) {
        chat.seenMessage(messageId, ownerId, handler);
    }

    @Override
    public void logOut() {
        chat.logOutSocket();
    }

    @Override
    public void removeParticipants(long threadId, List<Long> contactIds, ChatHandler handler) {
        chat.removeParticipants(threadId, contactIds, handler);
    }

    @Override
    public void removeParticipants(RequestRemoveParticipants requestRemoveParticipants, ChatHandler handler) {
        chat.removeParticipants(requestRemoveParticipants, handler);
    }

    @Override
    public void addParticipants(long threadId, List<Long> contactIds, ChatHandler handler) {
        chat.addParticipants(threadId, contactIds, handler);
    }

    @Override
    public void addParticipants(RequestAddParticipants requestAddParticipants, ChatHandler handler) {
        chat.addParticipants(requestAddParticipants, handler);
    }

    @Override
    public void leaveThread(long threadId, ChatHandler handler) {

        RequestLeaveThread leaveThread = new RequestLeaveThread.Builder(threadId)
                .build();
        chat.leaveThread(leaveThread,null);

        chat.leaveThread(threadId, handler);
    }

    @Override
    public void updateThreadInfo(long threadId, ThreadInfoVO threadInfoVO, ChatHandler handler) {
        chat.updateThreadInfo(threadId, threadInfoVO, handler);
    }

    @Override
    public void updateThreadInfo(RequestThreadInfo request, ChatHandler handler) {
//        RequestThreadInfo threadInfo = new RequestThreadInfo.Builder()
//                .threadId()
//                .description()
//                .image()
//                .metadat()
//                .name()
//                .build();
        chat.updateThreadInfo(request, handler);
    }

    @Override
    public void deleteMessage(ArrayList<Long> messageIds, Boolean deleteForAll, ChatHandler handler) {
        chat.deleteMessage(messageIds, deleteForAll, handler);
    }

    @Override
    public void deleteMessage(RequestDeleteMessage deleteMessage, ChatHandler handler) {
        chat.deleteMessage(deleteMessage, handler);
    }

    @Override
    public void uploadImageProgress(Context context, Activity activity, Uri fileUri, ProgressHandler.onProgress handler) {

//        RequestUploadImage requestUploadImage = new RequestUploadImage.Builder(activity,fileUri)
//                .build();
//        chat.uploadImageProgress(requestUploadImage, new ProgressHandler.onProgress() {
//            @Override
//            public void onProgressUpdate(String uniqueId, int bytesSent, int totalBytesSent, int totalBytesToSend) {
//
//            }
//
//            @Override
//            public void onFinish(String imageJson, ChatResponse<ResultImageFile> chatResponse) {
//
//            }
//
//            @Override
//            public void onError(String jsonError, ErrorOutPut error) {
//
//            }
//        });

        chat.uploadImageProgress(context, activity, fileUri, handler);

    }

    @Override
    public void uploadFileProgress(Context context, Activity activity, Uri fileUri, ProgressHandler.onProgressFile handler) {
        chat.uploadFileProgress(context, activity, null, fileUri, handler);
    }

    @Override
    public void setAdmin(RequestAddAdmin requestAddAdmin) {
        chat.setAdmin(requestAddAdmin);
    }

    @Override
    public void clearHistory(RequestClearHistory requestClearHistory) {

//        RequestClearHistory requestClearHistory = new RequestClearHistory
//                .Builder(threadId)
//                .build();
        chat.clearHistory(requestClearHistory);
    }

    @Override
    public void getAdminList(RequestGetAdmin requestGetAdmin) {
//        RequestGetAdmin requestGetAdmin = new RequestGetAdmin
//                .Builder(threadId)
//                .build();
        chat.getAdminList(requestGetAdmin);
    }

    /*
    *
    * */
    @Override
    public String startSignalMessage(RequestSignalMsg requestSignalMsg) {
        return chat.startSignalMessage(requestSignalMsg);
    }

    @Override
    public void stopSignalMessage(String uniqueId) {
        chat.stopSignalMessage(uniqueId);
    }

    //View
    @Override
    public void onDeliver(String content, ChatResponse<ResultMessage> chatResponse) {
        super.onDeliver(content, chatResponse);
        view.onGetDeliverMessage();
    }

    @Override
    public void onGetThread(String content, ChatResponse<ResultThreads> thread) {
        super.onGetThread(content, thread);
        view.onGetThreadList(content, thread);
    }

    @Override
    public void onThreadInfoUpdated(String content, ChatResponse<ResultThread> response) {
    }

    @Override
    public void onGetContacts(String content, ChatResponse<ResultContact> outPutContact) {
        super.onGetContacts(content, outPutContact);

        view.onGetContacts();
    }

    @Override
    public void onSeen(String content, ChatResponse<ResultMessage> chatResponse) {
        super.onSeen(content, chatResponse);
        view.onGetSeenMessage();
    }

    @Override
    public void onUserInfo(String content, ChatResponse<ResultUserInfo> outPutUserInfo) {
        view.onGetUserInfo();
    }

    @Override
    public void onSent(String content, ChatResponse<ResultMessage> chatResponse) {
        super.onSent(content, chatResponse);
        view.onSentMessage();
    }

    @Override
    public void onError(String content, ErrorOutPut outPutError) {
        super.onError(content, outPutError);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                Toast.makeText(context, content, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onCreateThread(String content, ChatResponse<ResultThread> outPutThread) {
        super.onCreateThread(content, outPutThread);
        view.onCreateThread();
    }

    @Override
    public void onGetThreadParticipant(String content, ChatResponse<ResultParticipant> outPutParticipant) {
        super.onGetThreadParticipant(content, outPutParticipant);
        view.onGetThreadParticipant();
    }

    @Override
    public void onEditedMessage(String content, ChatResponse<ResultNewMessage> chatResponse) {
        super.onEditedMessage(content, chatResponse);
        view.onEditMessage();
    }

    @Override
    public void onGetHistory(String content, ChatResponse<ResultHistory> history) {
        super.onGetHistory(content, history);
        view.onGetThreadHistory();
    }

    @Override
    public void onMuteThread(String content, ChatResponse<ResultMute> outPutMute) {
        super.onMuteThread(content, outPutMute);
        view.onMuteThread();
    }

    @Override
    public void onUnmuteThread(String content, ChatResponse<ResultMute> outPutMute) {
        super.onUnmuteThread(content, outPutMute);
        view.onUnMuteThread();
    }

    @Override
    public void onRenameThread(String content, OutPutThread outPutThread) {
        super.onRenameThread(content, outPutThread);
        view.onRenameGroupThread();
    }

    @Override
    public void onContactAdded(String content, ChatResponse<ResultAddContact> chatResponse) {
        super.onContactAdded(content, chatResponse);
        view.onAddContact();
    }

    @Override
    public void onUpdateContact(String content, ChatResponse<ResultUpdateContact> chatResponse) {
        super.onUpdateContact(content, chatResponse);
        view.onUpdateContact();
    }

    @Override
    public void onUploadFile(String content, ChatResponse<ResultFile> response) {
        super.onUploadFile(content, response);
        view.onUploadFile();
    }


    @Override
    public void onUploadImageFile(String content, ChatResponse<ResultImageFile> chatResponse) {
        super.onUploadImageFile(content, chatResponse);
        view.onUploadImageFile();
    }

    @Override
    public void onRemoveContact(String content, ChatResponse<ResultRemoveContact> response) {
        super.onRemoveContact(content, response);
        view.onRemoveContact();
    }

    @Override
    public void onThreadAddParticipant(String content, ChatResponse<ResultAddParticipant> outPutAddParticipant) {
        super.onThreadAddParticipant(content, outPutAddParticipant);
        view.onAddParticipant();
    }

    @Override
    public void onThreadRemoveParticipant(String content, ChatResponse<ResultParticipant> chatResponse) {
        super.onThreadRemoveParticipant(content, chatResponse);
        view.onRemoveParticipant();
    }


    @Override
    public void onDeleteMessage(String content, ChatResponse<ResultDeleteMessage> outPutDeleteMessage) {
        super.onDeleteMessage(content, outPutDeleteMessage);
        view.onDeleteMessage();
    }

    @Override
    public void onThreadLeaveParticipant(String content, ChatResponse<ResultLeaveThread> response) {
        super.onThreadLeaveParticipant(content, response);
        view.onLeaveThread();
    }

    @Override
    public void onChatState(String state) {
        view.onState(state);
    }

    @Override
    public void onNewMessage(String content, ChatResponse<ResultNewMessage> chatResponse) {
        super.onNewMessage(content, chatResponse);
//        outPutNewMessage = JsonUtil.fromJSON(content, OutPutNewMessage.class);
//        MessageVO messageVO = outPutNewMessage.getResult();
//        Participant participant = messageVO.getParticipant();

//        long id = messageVO.getId();
//        chat.seenMessage(id, participant.getId(), new ChatHandler() {
//            @Override
//            public void onSeen(String uniqueId) {
//                super.onSeen(uniqueId);
//            }
//        });
    }

    @Override
    public void onBlock(String content, ChatResponse<ResultBlock> outPutBlock) {
        super.onBlock(content, outPutBlock);
        view.onBlock();
    }

    @Override
    public void onUnBlock(String content, ChatResponse<ResultBlock> outPutBlock) {
        super.onUnBlock(content, outPutBlock);
        view.onUnblock();
    }

    @Override
    public void onMapSearch(String content, OutPutMapNeshan outPutMapNeshan) {
        super.onMapSearch(content, outPutMapNeshan);
        view.onMapSearch();
    }

    @Override
    public void onMapRouting(String content) {
        view.onMapRouting();
    }

    @Override
    public void onGetBlockList(String content, ChatResponse<ResultBlockList> outPutBlockList) {
        super.onGetBlockList(content, outPutBlockList);
        view.ongetBlockList();
    }

    @Override
    public void OnSeenMessageList(String content, ChatResponse<ResultParticipant> chatResponse) {

//        RequestSeenMessageList seenMessageList = new RequestSeenMessageList
//                .Builder(messageId)
//                .build();
//        chat.getMessageSeenList(seenMessageList);
    }

    @Override
    public void onSearchContact(String content, ChatResponse<ResultContact> chatResponse) {
        super.onSearchContact(content, chatResponse);
        view.onSearchContact();
    }

    @Override
    public void OnStaticMap(ChatResponse<ResultStaticMapImage> chatResponse) {
        view.onMapStaticImage(chatResponse);
    }
}
