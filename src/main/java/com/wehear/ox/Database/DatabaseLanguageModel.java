package com.wehear.ox.Database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class DatabaseLanguageModel {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name="language_name")
    public String languageName;


}
