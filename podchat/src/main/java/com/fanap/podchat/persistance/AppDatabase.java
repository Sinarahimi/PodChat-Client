package com.fanap.podchat.persistance;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import com.fanap.podchat.cachemodel.CacheContact;
import com.fanap.podchat.cachemodel.CacheForwardInfo;
import com.fanap.podchat.cachemodel.CacheLastMessageVO;
import com.fanap.podchat.cachemodel.CacheMessageVO;
import com.fanap.podchat.cachemodel.CacheParticipant;
import com.fanap.podchat.cachemodel.CacheReplyInfoVO;
import com.fanap.podchat.cachemodel.CacheThreadParticipant;
import com.fanap.podchat.cachemodel.PhoneContact;
import com.fanap.podchat.cachemodel.ThreadVo;
import com.fanap.podchat.cachemodel.queue.SendingQueueCache;
import com.fanap.podchat.cachemodel.queue.UploadingQueueCache;
import com.fanap.podchat.cachemodel.queue.WaitQueueCache;
import com.fanap.podchat.chat.mainmodel.Inviter;
import com.fanap.podchat.chat.mainmodel.UserInfo;
import com.fanap.podchat.model.ConversationSummery;
import com.fanap.podchat.persistance.dao.MessageDao;
import com.fanap.podchat.persistance.dao.MessageQueueDao;
import com.fanap.podchat.persistance.dao.PhoneContactDao;

@Database(entities = {
        CacheContact.class,
        Inviter.class,
        UserInfo.class,
        CacheLastMessageVO.class,
        CacheForwardInfo.class,
        CacheParticipant.class,
        CacheReplyInfoVO.class,
        ConversationSummery.class,
        CacheMessageVO.class,
        WaitQueueCache.class,
        UploadingQueueCache.class,
        SendingQueueCache.class,
        CacheThreadParticipant.class,
        PhoneContact.class,
        ThreadVo.class
}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    static final int VERSION = 1;

    public abstract MessageDao getMessageDao();

    public abstract MessageQueueDao getMessageQueueDao();

    public abstract PhoneContactDao getPhoneContactDao();
}
