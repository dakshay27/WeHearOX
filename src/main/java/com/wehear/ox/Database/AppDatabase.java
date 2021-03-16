package com.wehear.ox.Database;


import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {DatabaseLanguageModel.class},version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract LangaugeDao langaugeDao();
}
