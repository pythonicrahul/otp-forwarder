package com.example.otpforwarder;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface EventLogDao {

    @Insert
    void insert(EventLog log);

    @Query("SELECT * FROM EventLog ORDER BY timestamp DESC LIMIT 100")
    List<EventLog> getLatestLogs();

    @Query("DELETE FROM EventLog WHERE id NOT IN (SELECT id FROM EventLog ORDER BY timestamp DESC LIMIT 100)")
    void deleteOldLogs();
}
