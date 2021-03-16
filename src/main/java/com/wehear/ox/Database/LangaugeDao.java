package com.wehear.ox.Database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface LangaugeDao {
    @Query("SELECT * From DatabaseLanguageModel")
    List<DatabaseLanguageModel> getAll();


    @Insert
    void  insert(DatabaseLanguageModel model);

    @Delete
    void delete(DatabaseLanguageModel model);

}
