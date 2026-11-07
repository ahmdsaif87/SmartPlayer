package id.smartplayer.data.db;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.EntityUpsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import id.smartplayer.data.db.entities.PlaybackStateEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class PlaybackStateDao_Impl implements PlaybackStateDao {
  private final RoomDatabase __db;

  private final SharedSQLiteStatement __preparedStmtOfPurgeStale;

  private final EntityUpsertionAdapter<PlaybackStateEntity> __upsertionAdapterOfPlaybackStateEntity;

  public PlaybackStateDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__preparedStmtOfPurgeStale = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM playback_state WHERE updatedAtEpochMs < ? AND fingerprint NOT IN (SELECT fingerprint FROM episodes)";
        return _query;
      }
    };
    this.__upsertionAdapterOfPlaybackStateEntity = new EntityUpsertionAdapter<PlaybackStateEntity>(new EntityInsertionAdapter<PlaybackStateEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT INTO `playback_state` (`fingerprint`,`lastPlayedPosition`,`durationMs`,`isFinished`,`updatedAtEpochMs`) VALUES (?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final PlaybackStateEntity entity) {
        statement.bindString(1, entity.getFingerprint());
        statement.bindLong(2, entity.getLastPlayedPosition());
        statement.bindLong(3, entity.getDurationMs());
        final int _tmp = entity.isFinished() ? 1 : 0;
        statement.bindLong(4, _tmp);
        statement.bindLong(5, entity.getUpdatedAtEpochMs());
      }
    }, new EntityDeletionOrUpdateAdapter<PlaybackStateEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE `playback_state` SET `fingerprint` = ?,`lastPlayedPosition` = ?,`durationMs` = ?,`isFinished` = ?,`updatedAtEpochMs` = ? WHERE `fingerprint` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final PlaybackStateEntity entity) {
        statement.bindString(1, entity.getFingerprint());
        statement.bindLong(2, entity.getLastPlayedPosition());
        statement.bindLong(3, entity.getDurationMs());
        final int _tmp = entity.isFinished() ? 1 : 0;
        statement.bindLong(4, _tmp);
        statement.bindLong(5, entity.getUpdatedAtEpochMs());
        statement.bindString(6, entity.getFingerprint());
      }
    });
  }

  @Override
  public Object purgeStale(final long cutoff, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfPurgeStale.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, cutoff);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfPurgeStale.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object save(final PlaybackStateEntity state,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __upsertionAdapterOfPlaybackStateEntity.upsert(state);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object get(final String fp, final Continuation<? super PlaybackStateEntity> $completion) {
    final String _sql = "SELECT * FROM playback_state WHERE fingerprint = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, fp);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<PlaybackStateEntity>() {
      @Override
      @Nullable
      public PlaybackStateEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfFingerprint = CursorUtil.getColumnIndexOrThrow(_cursor, "fingerprint");
          final int _cursorIndexOfLastPlayedPosition = CursorUtil.getColumnIndexOrThrow(_cursor, "lastPlayedPosition");
          final int _cursorIndexOfDurationMs = CursorUtil.getColumnIndexOrThrow(_cursor, "durationMs");
          final int _cursorIndexOfIsFinished = CursorUtil.getColumnIndexOrThrow(_cursor, "isFinished");
          final int _cursorIndexOfUpdatedAtEpochMs = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAtEpochMs");
          final PlaybackStateEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpFingerprint;
            _tmpFingerprint = _cursor.getString(_cursorIndexOfFingerprint);
            final long _tmpLastPlayedPosition;
            _tmpLastPlayedPosition = _cursor.getLong(_cursorIndexOfLastPlayedPosition);
            final long _tmpDurationMs;
            _tmpDurationMs = _cursor.getLong(_cursorIndexOfDurationMs);
            final boolean _tmpIsFinished;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsFinished);
            _tmpIsFinished = _tmp != 0;
            final long _tmpUpdatedAtEpochMs;
            _tmpUpdatedAtEpochMs = _cursor.getLong(_cursorIndexOfUpdatedAtEpochMs);
            _result = new PlaybackStateEntity(_tmpFingerprint,_tmpLastPlayedPosition,_tmpDurationMs,_tmpIsFinished,_tmpUpdatedAtEpochMs);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<PlaybackStateEntity> observe(final String fp) {
    final String _sql = "SELECT * FROM playback_state WHERE fingerprint = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, fp);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"playback_state"}, new Callable<PlaybackStateEntity>() {
      @Override
      @Nullable
      public PlaybackStateEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfFingerprint = CursorUtil.getColumnIndexOrThrow(_cursor, "fingerprint");
          final int _cursorIndexOfLastPlayedPosition = CursorUtil.getColumnIndexOrThrow(_cursor, "lastPlayedPosition");
          final int _cursorIndexOfDurationMs = CursorUtil.getColumnIndexOrThrow(_cursor, "durationMs");
          final int _cursorIndexOfIsFinished = CursorUtil.getColumnIndexOrThrow(_cursor, "isFinished");
          final int _cursorIndexOfUpdatedAtEpochMs = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAtEpochMs");
          final PlaybackStateEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpFingerprint;
            _tmpFingerprint = _cursor.getString(_cursorIndexOfFingerprint);
            final long _tmpLastPlayedPosition;
            _tmpLastPlayedPosition = _cursor.getLong(_cursorIndexOfLastPlayedPosition);
            final long _tmpDurationMs;
            _tmpDurationMs = _cursor.getLong(_cursorIndexOfDurationMs);
            final boolean _tmpIsFinished;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsFinished);
            _tmpIsFinished = _tmp != 0;
            final long _tmpUpdatedAtEpochMs;
            _tmpUpdatedAtEpochMs = _cursor.getLong(_cursorIndexOfUpdatedAtEpochMs);
            _result = new PlaybackStateEntity(_tmpFingerprint,_tmpLastPlayedPosition,_tmpDurationMs,_tmpIsFinished,_tmpUpdatedAtEpochMs);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
