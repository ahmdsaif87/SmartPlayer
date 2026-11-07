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
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.room.util.StringUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import id.smartplayer.data.db.entities.EpisodeEntity;
import id.smartplayer.data.db.entities.EpisodeRow;
import id.smartplayer.data.db.entities.EpisodeSnapshot;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class EpisodeDao_Impl implements EpisodeDao {
  private final RoomDatabase __db;

  private final EntityUpsertionAdapter<EpisodeEntity> __upsertionAdapterOfEpisodeEntity;

  public EpisodeDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__upsertionAdapterOfEpisodeEntity = new EntityUpsertionAdapter<EpisodeEntity>(new EntityInsertionAdapter<EpisodeEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT INTO `episodes` (`id`,`seriesId`,`mediaStoreId`,`contentUri`,`absolutePath`,`relativePath`,`displayName`,`sizeBytes`,`durationMs`,`dateAddedSec`,`dateModifiedSec`,`episodeNumber`,`episodeVersion`,`seasonNumber`,`releaseGroup`,`resolution`,`parseConfidence`,`fingerprint`,`episodeTitle`,`synopsis`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final EpisodeEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getSeriesId());
        statement.bindLong(3, entity.getMediaStoreId());
        statement.bindString(4, entity.getContentUri());
        statement.bindString(5, entity.getAbsolutePath());
        statement.bindString(6, entity.getRelativePath());
        statement.bindString(7, entity.getDisplayName());
        statement.bindLong(8, entity.getSizeBytes());
        statement.bindLong(9, entity.getDurationMs());
        statement.bindLong(10, entity.getDateAddedSec());
        statement.bindLong(11, entity.getDateModifiedSec());
        if (entity.getEpisodeNumber() == null) {
          statement.bindNull(12);
        } else {
          statement.bindLong(12, entity.getEpisodeNumber());
        }
        if (entity.getEpisodeVersion() == null) {
          statement.bindNull(13);
        } else {
          statement.bindLong(13, entity.getEpisodeVersion());
        }
        statement.bindLong(14, entity.getSeasonNumber());
        if (entity.getReleaseGroup() == null) {
          statement.bindNull(15);
        } else {
          statement.bindString(15, entity.getReleaseGroup());
        }
        if (entity.getResolution() == null) {
          statement.bindNull(16);
        } else {
          statement.bindString(16, entity.getResolution());
        }
        statement.bindString(17, entity.getParseConfidence());
        statement.bindString(18, entity.getFingerprint());
        if (entity.getEpisodeTitle() == null) {
          statement.bindNull(19);
        } else {
          statement.bindString(19, entity.getEpisodeTitle());
        }
        if (entity.getSynopsis() == null) {
          statement.bindNull(20);
        } else {
          statement.bindString(20, entity.getSynopsis());
        }
      }
    }, new EntityDeletionOrUpdateAdapter<EpisodeEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE `episodes` SET `id` = ?,`seriesId` = ?,`mediaStoreId` = ?,`contentUri` = ?,`absolutePath` = ?,`relativePath` = ?,`displayName` = ?,`sizeBytes` = ?,`durationMs` = ?,`dateAddedSec` = ?,`dateModifiedSec` = ?,`episodeNumber` = ?,`episodeVersion` = ?,`seasonNumber` = ?,`releaseGroup` = ?,`resolution` = ?,`parseConfidence` = ?,`fingerprint` = ?,`episodeTitle` = ?,`synopsis` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final EpisodeEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getSeriesId());
        statement.bindLong(3, entity.getMediaStoreId());
        statement.bindString(4, entity.getContentUri());
        statement.bindString(5, entity.getAbsolutePath());
        statement.bindString(6, entity.getRelativePath());
        statement.bindString(7, entity.getDisplayName());
        statement.bindLong(8, entity.getSizeBytes());
        statement.bindLong(9, entity.getDurationMs());
        statement.bindLong(10, entity.getDateAddedSec());
        statement.bindLong(11, entity.getDateModifiedSec());
        if (entity.getEpisodeNumber() == null) {
          statement.bindNull(12);
        } else {
          statement.bindLong(12, entity.getEpisodeNumber());
        }
        if (entity.getEpisodeVersion() == null) {
          statement.bindNull(13);
        } else {
          statement.bindLong(13, entity.getEpisodeVersion());
        }
        statement.bindLong(14, entity.getSeasonNumber());
        if (entity.getReleaseGroup() == null) {
          statement.bindNull(15);
        } else {
          statement.bindString(15, entity.getReleaseGroup());
        }
        if (entity.getResolution() == null) {
          statement.bindNull(16);
        } else {
          statement.bindString(16, entity.getResolution());
        }
        statement.bindString(17, entity.getParseConfidence());
        statement.bindString(18, entity.getFingerprint());
        if (entity.getEpisodeTitle() == null) {
          statement.bindNull(19);
        } else {
          statement.bindString(19, entity.getEpisodeTitle());
        }
        if (entity.getSynopsis() == null) {
          statement.bindNull(20);
        } else {
          statement.bindString(20, entity.getSynopsis());
        }
        statement.bindLong(21, entity.getId());
      }
    });
  }

  @Override
  public Object upsertAll(final List<EpisodeEntity> items,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __upsertionAdapterOfEpisodeEntity.upsert(items);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<EpisodeRow>> observeEpisodes(final long seriesId) {
    final String _sql = "\n"
            + "        SELECT e.*, p.lastPlayedPosition AS resumePositionMs, COALESCE(p.isFinished, 0) AS isFinished\n"
            + "        FROM episodes e \n"
            + "        LEFT JOIN playback_state p ON p.fingerprint = e.fingerprint\n"
            + "        WHERE e.seriesId = ?\n"
            + "        ORDER BY (e.episodeNumber IS NULL) ASC, e.episodeNumber ASC, e.episodeVersion ASC, e.displayName COLLATE NOCASE ASC\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, seriesId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"episodes",
        "playback_state"}, new Callable<List<EpisodeRow>>() {
      @Override
      @NonNull
      public List<EpisodeRow> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSeriesId = CursorUtil.getColumnIndexOrThrow(_cursor, "seriesId");
          final int _cursorIndexOfMediaStoreId = CursorUtil.getColumnIndexOrThrow(_cursor, "mediaStoreId");
          final int _cursorIndexOfContentUri = CursorUtil.getColumnIndexOrThrow(_cursor, "contentUri");
          final int _cursorIndexOfAbsolutePath = CursorUtil.getColumnIndexOrThrow(_cursor, "absolutePath");
          final int _cursorIndexOfRelativePath = CursorUtil.getColumnIndexOrThrow(_cursor, "relativePath");
          final int _cursorIndexOfDisplayName = CursorUtil.getColumnIndexOrThrow(_cursor, "displayName");
          final int _cursorIndexOfSizeBytes = CursorUtil.getColumnIndexOrThrow(_cursor, "sizeBytes");
          final int _cursorIndexOfDurationMs = CursorUtil.getColumnIndexOrThrow(_cursor, "durationMs");
          final int _cursorIndexOfDateAddedSec = CursorUtil.getColumnIndexOrThrow(_cursor, "dateAddedSec");
          final int _cursorIndexOfDateModifiedSec = CursorUtil.getColumnIndexOrThrow(_cursor, "dateModifiedSec");
          final int _cursorIndexOfEpisodeNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "episodeNumber");
          final int _cursorIndexOfEpisodeVersion = CursorUtil.getColumnIndexOrThrow(_cursor, "episodeVersion");
          final int _cursorIndexOfSeasonNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "seasonNumber");
          final int _cursorIndexOfReleaseGroup = CursorUtil.getColumnIndexOrThrow(_cursor, "releaseGroup");
          final int _cursorIndexOfResolution = CursorUtil.getColumnIndexOrThrow(_cursor, "resolution");
          final int _cursorIndexOfParseConfidence = CursorUtil.getColumnIndexOrThrow(_cursor, "parseConfidence");
          final int _cursorIndexOfFingerprint = CursorUtil.getColumnIndexOrThrow(_cursor, "fingerprint");
          final int _cursorIndexOfEpisodeTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "episodeTitle");
          final int _cursorIndexOfSynopsis = CursorUtil.getColumnIndexOrThrow(_cursor, "synopsis");
          final int _cursorIndexOfResumePositionMs = CursorUtil.getColumnIndexOrThrow(_cursor, "resumePositionMs");
          final int _cursorIndexOfIsFinished = CursorUtil.getColumnIndexOrThrow(_cursor, "isFinished");
          final List<EpisodeRow> _result = new ArrayList<EpisodeRow>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final EpisodeRow _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpSeriesId;
            _tmpSeriesId = _cursor.getLong(_cursorIndexOfSeriesId);
            final long _tmpMediaStoreId;
            _tmpMediaStoreId = _cursor.getLong(_cursorIndexOfMediaStoreId);
            final String _tmpContentUri;
            _tmpContentUri = _cursor.getString(_cursorIndexOfContentUri);
            final String _tmpAbsolutePath;
            _tmpAbsolutePath = _cursor.getString(_cursorIndexOfAbsolutePath);
            final String _tmpRelativePath;
            _tmpRelativePath = _cursor.getString(_cursorIndexOfRelativePath);
            final String _tmpDisplayName;
            _tmpDisplayName = _cursor.getString(_cursorIndexOfDisplayName);
            final long _tmpSizeBytes;
            _tmpSizeBytes = _cursor.getLong(_cursorIndexOfSizeBytes);
            final long _tmpDurationMs;
            _tmpDurationMs = _cursor.getLong(_cursorIndexOfDurationMs);
            final long _tmpDateAddedSec;
            _tmpDateAddedSec = _cursor.getLong(_cursorIndexOfDateAddedSec);
            final long _tmpDateModifiedSec;
            _tmpDateModifiedSec = _cursor.getLong(_cursorIndexOfDateModifiedSec);
            final Integer _tmpEpisodeNumber;
            if (_cursor.isNull(_cursorIndexOfEpisodeNumber)) {
              _tmpEpisodeNumber = null;
            } else {
              _tmpEpisodeNumber = _cursor.getInt(_cursorIndexOfEpisodeNumber);
            }
            final Integer _tmpEpisodeVersion;
            if (_cursor.isNull(_cursorIndexOfEpisodeVersion)) {
              _tmpEpisodeVersion = null;
            } else {
              _tmpEpisodeVersion = _cursor.getInt(_cursorIndexOfEpisodeVersion);
            }
            final int _tmpSeasonNumber;
            _tmpSeasonNumber = _cursor.getInt(_cursorIndexOfSeasonNumber);
            final String _tmpReleaseGroup;
            if (_cursor.isNull(_cursorIndexOfReleaseGroup)) {
              _tmpReleaseGroup = null;
            } else {
              _tmpReleaseGroup = _cursor.getString(_cursorIndexOfReleaseGroup);
            }
            final String _tmpResolution;
            if (_cursor.isNull(_cursorIndexOfResolution)) {
              _tmpResolution = null;
            } else {
              _tmpResolution = _cursor.getString(_cursorIndexOfResolution);
            }
            final String _tmpParseConfidence;
            _tmpParseConfidence = _cursor.getString(_cursorIndexOfParseConfidence);
            final String _tmpFingerprint;
            _tmpFingerprint = _cursor.getString(_cursorIndexOfFingerprint);
            final String _tmpEpisodeTitle;
            if (_cursor.isNull(_cursorIndexOfEpisodeTitle)) {
              _tmpEpisodeTitle = null;
            } else {
              _tmpEpisodeTitle = _cursor.getString(_cursorIndexOfEpisodeTitle);
            }
            final String _tmpSynopsis;
            if (_cursor.isNull(_cursorIndexOfSynopsis)) {
              _tmpSynopsis = null;
            } else {
              _tmpSynopsis = _cursor.getString(_cursorIndexOfSynopsis);
            }
            final Long _tmpResumePositionMs;
            if (_cursor.isNull(_cursorIndexOfResumePositionMs)) {
              _tmpResumePositionMs = null;
            } else {
              _tmpResumePositionMs = _cursor.getLong(_cursorIndexOfResumePositionMs);
            }
            final boolean _tmpIsFinished;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsFinished);
            _tmpIsFinished = _tmp != 0;
            _item = new EpisodeRow(_tmpId,_tmpSeriesId,_tmpMediaStoreId,_tmpContentUri,_tmpAbsolutePath,_tmpRelativePath,_tmpDisplayName,_tmpSizeBytes,_tmpDurationMs,_tmpDateAddedSec,_tmpDateModifiedSec,_tmpEpisodeNumber,_tmpEpisodeVersion,_tmpSeasonNumber,_tmpReleaseGroup,_tmpResolution,_tmpParseConfidence,_tmpFingerprint,_tmpEpisodeTitle,_tmpSynopsis,_tmpResumePositionMs,_tmpIsFinished);
            _result.add(_item);
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

  @Override
  public Object getEpisodes(final long seriesId,
      final Continuation<? super List<EpisodeRow>> $completion) {
    final String _sql = "\n"
            + "        SELECT e.*, p.lastPlayedPosition AS resumePositionMs, COALESCE(p.isFinished, 0) AS isFinished\n"
            + "        FROM episodes e \n"
            + "        LEFT JOIN playback_state p ON p.fingerprint = e.fingerprint\n"
            + "        WHERE e.seriesId = ?\n"
            + "        ORDER BY (e.episodeNumber IS NULL) ASC, e.episodeNumber ASC, e.episodeVersion ASC, e.displayName COLLATE NOCASE ASC\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, seriesId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<EpisodeRow>>() {
      @Override
      @NonNull
      public List<EpisodeRow> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSeriesId = CursorUtil.getColumnIndexOrThrow(_cursor, "seriesId");
          final int _cursorIndexOfMediaStoreId = CursorUtil.getColumnIndexOrThrow(_cursor, "mediaStoreId");
          final int _cursorIndexOfContentUri = CursorUtil.getColumnIndexOrThrow(_cursor, "contentUri");
          final int _cursorIndexOfAbsolutePath = CursorUtil.getColumnIndexOrThrow(_cursor, "absolutePath");
          final int _cursorIndexOfRelativePath = CursorUtil.getColumnIndexOrThrow(_cursor, "relativePath");
          final int _cursorIndexOfDisplayName = CursorUtil.getColumnIndexOrThrow(_cursor, "displayName");
          final int _cursorIndexOfSizeBytes = CursorUtil.getColumnIndexOrThrow(_cursor, "sizeBytes");
          final int _cursorIndexOfDurationMs = CursorUtil.getColumnIndexOrThrow(_cursor, "durationMs");
          final int _cursorIndexOfDateAddedSec = CursorUtil.getColumnIndexOrThrow(_cursor, "dateAddedSec");
          final int _cursorIndexOfDateModifiedSec = CursorUtil.getColumnIndexOrThrow(_cursor, "dateModifiedSec");
          final int _cursorIndexOfEpisodeNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "episodeNumber");
          final int _cursorIndexOfEpisodeVersion = CursorUtil.getColumnIndexOrThrow(_cursor, "episodeVersion");
          final int _cursorIndexOfSeasonNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "seasonNumber");
          final int _cursorIndexOfReleaseGroup = CursorUtil.getColumnIndexOrThrow(_cursor, "releaseGroup");
          final int _cursorIndexOfResolution = CursorUtil.getColumnIndexOrThrow(_cursor, "resolution");
          final int _cursorIndexOfParseConfidence = CursorUtil.getColumnIndexOrThrow(_cursor, "parseConfidence");
          final int _cursorIndexOfFingerprint = CursorUtil.getColumnIndexOrThrow(_cursor, "fingerprint");
          final int _cursorIndexOfEpisodeTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "episodeTitle");
          final int _cursorIndexOfSynopsis = CursorUtil.getColumnIndexOrThrow(_cursor, "synopsis");
          final int _cursorIndexOfResumePositionMs = CursorUtil.getColumnIndexOrThrow(_cursor, "resumePositionMs");
          final int _cursorIndexOfIsFinished = CursorUtil.getColumnIndexOrThrow(_cursor, "isFinished");
          final List<EpisodeRow> _result = new ArrayList<EpisodeRow>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final EpisodeRow _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpSeriesId;
            _tmpSeriesId = _cursor.getLong(_cursorIndexOfSeriesId);
            final long _tmpMediaStoreId;
            _tmpMediaStoreId = _cursor.getLong(_cursorIndexOfMediaStoreId);
            final String _tmpContentUri;
            _tmpContentUri = _cursor.getString(_cursorIndexOfContentUri);
            final String _tmpAbsolutePath;
            _tmpAbsolutePath = _cursor.getString(_cursorIndexOfAbsolutePath);
            final String _tmpRelativePath;
            _tmpRelativePath = _cursor.getString(_cursorIndexOfRelativePath);
            final String _tmpDisplayName;
            _tmpDisplayName = _cursor.getString(_cursorIndexOfDisplayName);
            final long _tmpSizeBytes;
            _tmpSizeBytes = _cursor.getLong(_cursorIndexOfSizeBytes);
            final long _tmpDurationMs;
            _tmpDurationMs = _cursor.getLong(_cursorIndexOfDurationMs);
            final long _tmpDateAddedSec;
            _tmpDateAddedSec = _cursor.getLong(_cursorIndexOfDateAddedSec);
            final long _tmpDateModifiedSec;
            _tmpDateModifiedSec = _cursor.getLong(_cursorIndexOfDateModifiedSec);
            final Integer _tmpEpisodeNumber;
            if (_cursor.isNull(_cursorIndexOfEpisodeNumber)) {
              _tmpEpisodeNumber = null;
            } else {
              _tmpEpisodeNumber = _cursor.getInt(_cursorIndexOfEpisodeNumber);
            }
            final Integer _tmpEpisodeVersion;
            if (_cursor.isNull(_cursorIndexOfEpisodeVersion)) {
              _tmpEpisodeVersion = null;
            } else {
              _tmpEpisodeVersion = _cursor.getInt(_cursorIndexOfEpisodeVersion);
            }
            final int _tmpSeasonNumber;
            _tmpSeasonNumber = _cursor.getInt(_cursorIndexOfSeasonNumber);
            final String _tmpReleaseGroup;
            if (_cursor.isNull(_cursorIndexOfReleaseGroup)) {
              _tmpReleaseGroup = null;
            } else {
              _tmpReleaseGroup = _cursor.getString(_cursorIndexOfReleaseGroup);
            }
            final String _tmpResolution;
            if (_cursor.isNull(_cursorIndexOfResolution)) {
              _tmpResolution = null;
            } else {
              _tmpResolution = _cursor.getString(_cursorIndexOfResolution);
            }
            final String _tmpParseConfidence;
            _tmpParseConfidence = _cursor.getString(_cursorIndexOfParseConfidence);
            final String _tmpFingerprint;
            _tmpFingerprint = _cursor.getString(_cursorIndexOfFingerprint);
            final String _tmpEpisodeTitle;
            if (_cursor.isNull(_cursorIndexOfEpisodeTitle)) {
              _tmpEpisodeTitle = null;
            } else {
              _tmpEpisodeTitle = _cursor.getString(_cursorIndexOfEpisodeTitle);
            }
            final String _tmpSynopsis;
            if (_cursor.isNull(_cursorIndexOfSynopsis)) {
              _tmpSynopsis = null;
            } else {
              _tmpSynopsis = _cursor.getString(_cursorIndexOfSynopsis);
            }
            final Long _tmpResumePositionMs;
            if (_cursor.isNull(_cursorIndexOfResumePositionMs)) {
              _tmpResumePositionMs = null;
            } else {
              _tmpResumePositionMs = _cursor.getLong(_cursorIndexOfResumePositionMs);
            }
            final boolean _tmpIsFinished;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsFinished);
            _tmpIsFinished = _tmp != 0;
            _item = new EpisodeRow(_tmpId,_tmpSeriesId,_tmpMediaStoreId,_tmpContentUri,_tmpAbsolutePath,_tmpRelativePath,_tmpDisplayName,_tmpSizeBytes,_tmpDurationMs,_tmpDateAddedSec,_tmpDateModifiedSec,_tmpEpisodeNumber,_tmpEpisodeVersion,_tmpSeasonNumber,_tmpReleaseGroup,_tmpResolution,_tmpParseConfidence,_tmpFingerprint,_tmpEpisodeTitle,_tmpSynopsis,_tmpResumePositionMs,_tmpIsFinished);
            _result.add(_item);
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
  public Object snapshots(final Continuation<? super List<EpisodeSnapshot>> $completion) {
    final String _sql = "SELECT mediaStoreId, dateModifiedSec, sizeBytes FROM episodes";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<EpisodeSnapshot>>() {
      @Override
      @NonNull
      public List<EpisodeSnapshot> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfMediaStoreId = 0;
          final int _cursorIndexOfDateModifiedSec = 1;
          final int _cursorIndexOfSizeBytes = 2;
          final List<EpisodeSnapshot> _result = new ArrayList<EpisodeSnapshot>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final EpisodeSnapshot _item;
            final long _tmpMediaStoreId;
            _tmpMediaStoreId = _cursor.getLong(_cursorIndexOfMediaStoreId);
            final long _tmpDateModifiedSec;
            _tmpDateModifiedSec = _cursor.getLong(_cursorIndexOfDateModifiedSec);
            final long _tmpSizeBytes;
            _tmpSizeBytes = _cursor.getLong(_cursorIndexOfSizeBytes);
            _item = new EpisodeSnapshot(_tmpMediaStoreId,_tmpDateModifiedSec,_tmpSizeBytes);
            _result.add(_item);
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
  public Object findById(final long id, final Continuation<? super EpisodeEntity> $completion) {
    final String _sql = "SELECT * FROM episodes WHERE id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<EpisodeEntity>() {
      @Override
      @Nullable
      public EpisodeEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSeriesId = CursorUtil.getColumnIndexOrThrow(_cursor, "seriesId");
          final int _cursorIndexOfMediaStoreId = CursorUtil.getColumnIndexOrThrow(_cursor, "mediaStoreId");
          final int _cursorIndexOfContentUri = CursorUtil.getColumnIndexOrThrow(_cursor, "contentUri");
          final int _cursorIndexOfAbsolutePath = CursorUtil.getColumnIndexOrThrow(_cursor, "absolutePath");
          final int _cursorIndexOfRelativePath = CursorUtil.getColumnIndexOrThrow(_cursor, "relativePath");
          final int _cursorIndexOfDisplayName = CursorUtil.getColumnIndexOrThrow(_cursor, "displayName");
          final int _cursorIndexOfSizeBytes = CursorUtil.getColumnIndexOrThrow(_cursor, "sizeBytes");
          final int _cursorIndexOfDurationMs = CursorUtil.getColumnIndexOrThrow(_cursor, "durationMs");
          final int _cursorIndexOfDateAddedSec = CursorUtil.getColumnIndexOrThrow(_cursor, "dateAddedSec");
          final int _cursorIndexOfDateModifiedSec = CursorUtil.getColumnIndexOrThrow(_cursor, "dateModifiedSec");
          final int _cursorIndexOfEpisodeNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "episodeNumber");
          final int _cursorIndexOfEpisodeVersion = CursorUtil.getColumnIndexOrThrow(_cursor, "episodeVersion");
          final int _cursorIndexOfSeasonNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "seasonNumber");
          final int _cursorIndexOfReleaseGroup = CursorUtil.getColumnIndexOrThrow(_cursor, "releaseGroup");
          final int _cursorIndexOfResolution = CursorUtil.getColumnIndexOrThrow(_cursor, "resolution");
          final int _cursorIndexOfParseConfidence = CursorUtil.getColumnIndexOrThrow(_cursor, "parseConfidence");
          final int _cursorIndexOfFingerprint = CursorUtil.getColumnIndexOrThrow(_cursor, "fingerprint");
          final int _cursorIndexOfEpisodeTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "episodeTitle");
          final int _cursorIndexOfSynopsis = CursorUtil.getColumnIndexOrThrow(_cursor, "synopsis");
          final EpisodeEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpSeriesId;
            _tmpSeriesId = _cursor.getLong(_cursorIndexOfSeriesId);
            final long _tmpMediaStoreId;
            _tmpMediaStoreId = _cursor.getLong(_cursorIndexOfMediaStoreId);
            final String _tmpContentUri;
            _tmpContentUri = _cursor.getString(_cursorIndexOfContentUri);
            final String _tmpAbsolutePath;
            _tmpAbsolutePath = _cursor.getString(_cursorIndexOfAbsolutePath);
            final String _tmpRelativePath;
            _tmpRelativePath = _cursor.getString(_cursorIndexOfRelativePath);
            final String _tmpDisplayName;
            _tmpDisplayName = _cursor.getString(_cursorIndexOfDisplayName);
            final long _tmpSizeBytes;
            _tmpSizeBytes = _cursor.getLong(_cursorIndexOfSizeBytes);
            final long _tmpDurationMs;
            _tmpDurationMs = _cursor.getLong(_cursorIndexOfDurationMs);
            final long _tmpDateAddedSec;
            _tmpDateAddedSec = _cursor.getLong(_cursorIndexOfDateAddedSec);
            final long _tmpDateModifiedSec;
            _tmpDateModifiedSec = _cursor.getLong(_cursorIndexOfDateModifiedSec);
            final Integer _tmpEpisodeNumber;
            if (_cursor.isNull(_cursorIndexOfEpisodeNumber)) {
              _tmpEpisodeNumber = null;
            } else {
              _tmpEpisodeNumber = _cursor.getInt(_cursorIndexOfEpisodeNumber);
            }
            final Integer _tmpEpisodeVersion;
            if (_cursor.isNull(_cursorIndexOfEpisodeVersion)) {
              _tmpEpisodeVersion = null;
            } else {
              _tmpEpisodeVersion = _cursor.getInt(_cursorIndexOfEpisodeVersion);
            }
            final int _tmpSeasonNumber;
            _tmpSeasonNumber = _cursor.getInt(_cursorIndexOfSeasonNumber);
            final String _tmpReleaseGroup;
            if (_cursor.isNull(_cursorIndexOfReleaseGroup)) {
              _tmpReleaseGroup = null;
            } else {
              _tmpReleaseGroup = _cursor.getString(_cursorIndexOfReleaseGroup);
            }
            final String _tmpResolution;
            if (_cursor.isNull(_cursorIndexOfResolution)) {
              _tmpResolution = null;
            } else {
              _tmpResolution = _cursor.getString(_cursorIndexOfResolution);
            }
            final String _tmpParseConfidence;
            _tmpParseConfidence = _cursor.getString(_cursorIndexOfParseConfidence);
            final String _tmpFingerprint;
            _tmpFingerprint = _cursor.getString(_cursorIndexOfFingerprint);
            final String _tmpEpisodeTitle;
            if (_cursor.isNull(_cursorIndexOfEpisodeTitle)) {
              _tmpEpisodeTitle = null;
            } else {
              _tmpEpisodeTitle = _cursor.getString(_cursorIndexOfEpisodeTitle);
            }
            final String _tmpSynopsis;
            if (_cursor.isNull(_cursorIndexOfSynopsis)) {
              _tmpSynopsis = null;
            } else {
              _tmpSynopsis = _cursor.getString(_cursorIndexOfSynopsis);
            }
            _result = new EpisodeEntity(_tmpId,_tmpSeriesId,_tmpMediaStoreId,_tmpContentUri,_tmpAbsolutePath,_tmpRelativePath,_tmpDisplayName,_tmpSizeBytes,_tmpDurationMs,_tmpDateAddedSec,_tmpDateModifiedSec,_tmpEpisodeNumber,_tmpEpisodeVersion,_tmpSeasonNumber,_tmpReleaseGroup,_tmpResolution,_tmpParseConfidence,_tmpFingerprint,_tmpEpisodeTitle,_tmpSynopsis);
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
  public Object findByFingerprint(final String fp,
      final Continuation<? super EpisodeEntity> $completion) {
    final String _sql = "SELECT * FROM episodes WHERE fingerprint = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, fp);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<EpisodeEntity>() {
      @Override
      @Nullable
      public EpisodeEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSeriesId = CursorUtil.getColumnIndexOrThrow(_cursor, "seriesId");
          final int _cursorIndexOfMediaStoreId = CursorUtil.getColumnIndexOrThrow(_cursor, "mediaStoreId");
          final int _cursorIndexOfContentUri = CursorUtil.getColumnIndexOrThrow(_cursor, "contentUri");
          final int _cursorIndexOfAbsolutePath = CursorUtil.getColumnIndexOrThrow(_cursor, "absolutePath");
          final int _cursorIndexOfRelativePath = CursorUtil.getColumnIndexOrThrow(_cursor, "relativePath");
          final int _cursorIndexOfDisplayName = CursorUtil.getColumnIndexOrThrow(_cursor, "displayName");
          final int _cursorIndexOfSizeBytes = CursorUtil.getColumnIndexOrThrow(_cursor, "sizeBytes");
          final int _cursorIndexOfDurationMs = CursorUtil.getColumnIndexOrThrow(_cursor, "durationMs");
          final int _cursorIndexOfDateAddedSec = CursorUtil.getColumnIndexOrThrow(_cursor, "dateAddedSec");
          final int _cursorIndexOfDateModifiedSec = CursorUtil.getColumnIndexOrThrow(_cursor, "dateModifiedSec");
          final int _cursorIndexOfEpisodeNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "episodeNumber");
          final int _cursorIndexOfEpisodeVersion = CursorUtil.getColumnIndexOrThrow(_cursor, "episodeVersion");
          final int _cursorIndexOfSeasonNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "seasonNumber");
          final int _cursorIndexOfReleaseGroup = CursorUtil.getColumnIndexOrThrow(_cursor, "releaseGroup");
          final int _cursorIndexOfResolution = CursorUtil.getColumnIndexOrThrow(_cursor, "resolution");
          final int _cursorIndexOfParseConfidence = CursorUtil.getColumnIndexOrThrow(_cursor, "parseConfidence");
          final int _cursorIndexOfFingerprint = CursorUtil.getColumnIndexOrThrow(_cursor, "fingerprint");
          final int _cursorIndexOfEpisodeTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "episodeTitle");
          final int _cursorIndexOfSynopsis = CursorUtil.getColumnIndexOrThrow(_cursor, "synopsis");
          final EpisodeEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpSeriesId;
            _tmpSeriesId = _cursor.getLong(_cursorIndexOfSeriesId);
            final long _tmpMediaStoreId;
            _tmpMediaStoreId = _cursor.getLong(_cursorIndexOfMediaStoreId);
            final String _tmpContentUri;
            _tmpContentUri = _cursor.getString(_cursorIndexOfContentUri);
            final String _tmpAbsolutePath;
            _tmpAbsolutePath = _cursor.getString(_cursorIndexOfAbsolutePath);
            final String _tmpRelativePath;
            _tmpRelativePath = _cursor.getString(_cursorIndexOfRelativePath);
            final String _tmpDisplayName;
            _tmpDisplayName = _cursor.getString(_cursorIndexOfDisplayName);
            final long _tmpSizeBytes;
            _tmpSizeBytes = _cursor.getLong(_cursorIndexOfSizeBytes);
            final long _tmpDurationMs;
            _tmpDurationMs = _cursor.getLong(_cursorIndexOfDurationMs);
            final long _tmpDateAddedSec;
            _tmpDateAddedSec = _cursor.getLong(_cursorIndexOfDateAddedSec);
            final long _tmpDateModifiedSec;
            _tmpDateModifiedSec = _cursor.getLong(_cursorIndexOfDateModifiedSec);
            final Integer _tmpEpisodeNumber;
            if (_cursor.isNull(_cursorIndexOfEpisodeNumber)) {
              _tmpEpisodeNumber = null;
            } else {
              _tmpEpisodeNumber = _cursor.getInt(_cursorIndexOfEpisodeNumber);
            }
            final Integer _tmpEpisodeVersion;
            if (_cursor.isNull(_cursorIndexOfEpisodeVersion)) {
              _tmpEpisodeVersion = null;
            } else {
              _tmpEpisodeVersion = _cursor.getInt(_cursorIndexOfEpisodeVersion);
            }
            final int _tmpSeasonNumber;
            _tmpSeasonNumber = _cursor.getInt(_cursorIndexOfSeasonNumber);
            final String _tmpReleaseGroup;
            if (_cursor.isNull(_cursorIndexOfReleaseGroup)) {
              _tmpReleaseGroup = null;
            } else {
              _tmpReleaseGroup = _cursor.getString(_cursorIndexOfReleaseGroup);
            }
            final String _tmpResolution;
            if (_cursor.isNull(_cursorIndexOfResolution)) {
              _tmpResolution = null;
            } else {
              _tmpResolution = _cursor.getString(_cursorIndexOfResolution);
            }
            final String _tmpParseConfidence;
            _tmpParseConfidence = _cursor.getString(_cursorIndexOfParseConfidence);
            final String _tmpFingerprint;
            _tmpFingerprint = _cursor.getString(_cursorIndexOfFingerprint);
            final String _tmpEpisodeTitle;
            if (_cursor.isNull(_cursorIndexOfEpisodeTitle)) {
              _tmpEpisodeTitle = null;
            } else {
              _tmpEpisodeTitle = _cursor.getString(_cursorIndexOfEpisodeTitle);
            }
            final String _tmpSynopsis;
            if (_cursor.isNull(_cursorIndexOfSynopsis)) {
              _tmpSynopsis = null;
            } else {
              _tmpSynopsis = _cursor.getString(_cursorIndexOfSynopsis);
            }
            _result = new EpisodeEntity(_tmpId,_tmpSeriesId,_tmpMediaStoreId,_tmpContentUri,_tmpAbsolutePath,_tmpRelativePath,_tmpDisplayName,_tmpSizeBytes,_tmpDurationMs,_tmpDateAddedSec,_tmpDateModifiedSec,_tmpEpisodeNumber,_tmpEpisodeVersion,_tmpSeasonNumber,_tmpReleaseGroup,_tmpResolution,_tmpParseConfidence,_tmpFingerprint,_tmpEpisodeTitle,_tmpSynopsis);
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
  public Flow<List<EpisodeRow>> observeContinueWatching() {
    final String _sql = "\n"
            + "        SELECT e.*, p.lastPlayedPosition AS resumePositionMs, COALESCE(p.isFinished, 0) AS isFinished\n"
            + "        FROM episodes e\n"
            + "        JOIN playback_state p ON p.fingerprint = e.fingerprint\n"
            + "        WHERE p.isFinished = 0 AND p.lastPlayedPosition > 5000\n"
            + "        ORDER BY p.updatedAtEpochMs DESC\n"
            + "        LIMIT 20\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"episodes",
        "playback_state"}, new Callable<List<EpisodeRow>>() {
      @Override
      @NonNull
      public List<EpisodeRow> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSeriesId = CursorUtil.getColumnIndexOrThrow(_cursor, "seriesId");
          final int _cursorIndexOfMediaStoreId = CursorUtil.getColumnIndexOrThrow(_cursor, "mediaStoreId");
          final int _cursorIndexOfContentUri = CursorUtil.getColumnIndexOrThrow(_cursor, "contentUri");
          final int _cursorIndexOfAbsolutePath = CursorUtil.getColumnIndexOrThrow(_cursor, "absolutePath");
          final int _cursorIndexOfRelativePath = CursorUtil.getColumnIndexOrThrow(_cursor, "relativePath");
          final int _cursorIndexOfDisplayName = CursorUtil.getColumnIndexOrThrow(_cursor, "displayName");
          final int _cursorIndexOfSizeBytes = CursorUtil.getColumnIndexOrThrow(_cursor, "sizeBytes");
          final int _cursorIndexOfDurationMs = CursorUtil.getColumnIndexOrThrow(_cursor, "durationMs");
          final int _cursorIndexOfDateAddedSec = CursorUtil.getColumnIndexOrThrow(_cursor, "dateAddedSec");
          final int _cursorIndexOfDateModifiedSec = CursorUtil.getColumnIndexOrThrow(_cursor, "dateModifiedSec");
          final int _cursorIndexOfEpisodeNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "episodeNumber");
          final int _cursorIndexOfEpisodeVersion = CursorUtil.getColumnIndexOrThrow(_cursor, "episodeVersion");
          final int _cursorIndexOfSeasonNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "seasonNumber");
          final int _cursorIndexOfReleaseGroup = CursorUtil.getColumnIndexOrThrow(_cursor, "releaseGroup");
          final int _cursorIndexOfResolution = CursorUtil.getColumnIndexOrThrow(_cursor, "resolution");
          final int _cursorIndexOfParseConfidence = CursorUtil.getColumnIndexOrThrow(_cursor, "parseConfidence");
          final int _cursorIndexOfFingerprint = CursorUtil.getColumnIndexOrThrow(_cursor, "fingerprint");
          final int _cursorIndexOfEpisodeTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "episodeTitle");
          final int _cursorIndexOfSynopsis = CursorUtil.getColumnIndexOrThrow(_cursor, "synopsis");
          final int _cursorIndexOfResumePositionMs = CursorUtil.getColumnIndexOrThrow(_cursor, "resumePositionMs");
          final int _cursorIndexOfIsFinished = CursorUtil.getColumnIndexOrThrow(_cursor, "isFinished");
          final List<EpisodeRow> _result = new ArrayList<EpisodeRow>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final EpisodeRow _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpSeriesId;
            _tmpSeriesId = _cursor.getLong(_cursorIndexOfSeriesId);
            final long _tmpMediaStoreId;
            _tmpMediaStoreId = _cursor.getLong(_cursorIndexOfMediaStoreId);
            final String _tmpContentUri;
            _tmpContentUri = _cursor.getString(_cursorIndexOfContentUri);
            final String _tmpAbsolutePath;
            _tmpAbsolutePath = _cursor.getString(_cursorIndexOfAbsolutePath);
            final String _tmpRelativePath;
            _tmpRelativePath = _cursor.getString(_cursorIndexOfRelativePath);
            final String _tmpDisplayName;
            _tmpDisplayName = _cursor.getString(_cursorIndexOfDisplayName);
            final long _tmpSizeBytes;
            _tmpSizeBytes = _cursor.getLong(_cursorIndexOfSizeBytes);
            final long _tmpDurationMs;
            _tmpDurationMs = _cursor.getLong(_cursorIndexOfDurationMs);
            final long _tmpDateAddedSec;
            _tmpDateAddedSec = _cursor.getLong(_cursorIndexOfDateAddedSec);
            final long _tmpDateModifiedSec;
            _tmpDateModifiedSec = _cursor.getLong(_cursorIndexOfDateModifiedSec);
            final Integer _tmpEpisodeNumber;
            if (_cursor.isNull(_cursorIndexOfEpisodeNumber)) {
              _tmpEpisodeNumber = null;
            } else {
              _tmpEpisodeNumber = _cursor.getInt(_cursorIndexOfEpisodeNumber);
            }
            final Integer _tmpEpisodeVersion;
            if (_cursor.isNull(_cursorIndexOfEpisodeVersion)) {
              _tmpEpisodeVersion = null;
            } else {
              _tmpEpisodeVersion = _cursor.getInt(_cursorIndexOfEpisodeVersion);
            }
            final int _tmpSeasonNumber;
            _tmpSeasonNumber = _cursor.getInt(_cursorIndexOfSeasonNumber);
            final String _tmpReleaseGroup;
            if (_cursor.isNull(_cursorIndexOfReleaseGroup)) {
              _tmpReleaseGroup = null;
            } else {
              _tmpReleaseGroup = _cursor.getString(_cursorIndexOfReleaseGroup);
            }
            final String _tmpResolution;
            if (_cursor.isNull(_cursorIndexOfResolution)) {
              _tmpResolution = null;
            } else {
              _tmpResolution = _cursor.getString(_cursorIndexOfResolution);
            }
            final String _tmpParseConfidence;
            _tmpParseConfidence = _cursor.getString(_cursorIndexOfParseConfidence);
            final String _tmpFingerprint;
            _tmpFingerprint = _cursor.getString(_cursorIndexOfFingerprint);
            final String _tmpEpisodeTitle;
            if (_cursor.isNull(_cursorIndexOfEpisodeTitle)) {
              _tmpEpisodeTitle = null;
            } else {
              _tmpEpisodeTitle = _cursor.getString(_cursorIndexOfEpisodeTitle);
            }
            final String _tmpSynopsis;
            if (_cursor.isNull(_cursorIndexOfSynopsis)) {
              _tmpSynopsis = null;
            } else {
              _tmpSynopsis = _cursor.getString(_cursorIndexOfSynopsis);
            }
            final Long _tmpResumePositionMs;
            if (_cursor.isNull(_cursorIndexOfResumePositionMs)) {
              _tmpResumePositionMs = null;
            } else {
              _tmpResumePositionMs = _cursor.getLong(_cursorIndexOfResumePositionMs);
            }
            final boolean _tmpIsFinished;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsFinished);
            _tmpIsFinished = _tmp != 0;
            _item = new EpisodeRow(_tmpId,_tmpSeriesId,_tmpMediaStoreId,_tmpContentUri,_tmpAbsolutePath,_tmpRelativePath,_tmpDisplayName,_tmpSizeBytes,_tmpDurationMs,_tmpDateAddedSec,_tmpDateModifiedSec,_tmpEpisodeNumber,_tmpEpisodeVersion,_tmpSeasonNumber,_tmpReleaseGroup,_tmpResolution,_tmpParseConfidence,_tmpFingerprint,_tmpEpisodeTitle,_tmpSynopsis,_tmpResumePositionMs,_tmpIsFinished);
            _result.add(_item);
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

  @Override
  public Object deleteByMediaStoreIds(final List<Long> ids,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
        _stringBuilder.append("DELETE FROM episodes WHERE mediaStoreId IN (");
        final int _inputSize = ids.size();
        StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
        _stringBuilder.append(")");
        final String _sql = _stringBuilder.toString();
        final SupportSQLiteStatement _stmt = __db.compileStatement(_sql);
        int _argIndex = 1;
        for (long _item : ids) {
          _stmt.bindLong(_argIndex, _item);
          _argIndex++;
        }
        __db.beginTransaction();
        try {
          _stmt.executeUpdateDelete();
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
