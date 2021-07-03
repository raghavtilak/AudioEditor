package com.raghav.audioeditor;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.raghav.audioeditor.ListView.SongModel;

@Database(entities = {SongModel.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract SongDao songDao();
}