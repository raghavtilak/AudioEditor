package com.raghav.audioeditor;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.raghav.audioeditor.ListView.SongModel;

import java.util.List;

@Dao
public interface SongDao {
    @Query("SELECT * FROM SongModel")
    List<SongModel> getAll();

    @Query("SELECT * FROM SongModel WHERE title LIKE :title " + "LIMIT 1")
    SongModel findByTitle(String title);

    @Insert
    void insert(SongModel song);

    @Insert
    void insertAll(SongModel... songs);

    @Delete
    void delete(SongModel song);
}