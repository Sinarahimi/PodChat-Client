package com.fanap.podchat.persistance;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.fanap.podchat.cachemodel.CacheForwardInfo;
import com.fanap.podchat.cachemodel.CacheLastMessageVO;
import com.fanap.podchat.cachemodel.CacheParticipant;
import com.fanap.podchat.cachemodel.CacheReplyInfoVO;
import com.fanap.podchat.cachemodel.ThreadVo;
import com.fanap.podchat.mainmodel.Inviter;
import com.fanap.podchat.mainmodel.UserInfo;
import com.fanap.podchat.model.ConversationSummery;
import com.fanap.podchat.persistance.dao.MessageDao;
import com.fanap.podchat.mainmodel.Contact;

@Database(entities = {
        Contact.class,
        Inviter.class,
        UserInfo.class,
        CacheLastMessageVO.class,
        CacheForwardInfo.class,
        CacheParticipant.class,
        CacheReplyInfoVO.class,
        ConversationSummery.class,
        ThreadVo.class
}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public static final String DATABASE_DB = "cache.db";
    private static AppDatabase appDatabase;

    public static AppDatabase getInstance(Context context) {
        if (appDatabase == null) {
            appDatabase = Room.databaseBuilder(context, AppDatabase.class, DATABASE_DB).allowMainThreadQueries().build();
        }
        return appDatabase;
    }

    public abstract MessageDao getMessageDao();
}
