package id.smartplayer.data.db;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import id.smartplayer.data.db.entities.SeriesEntity;
import id.smartplayer.data.db.entities.SeriesSummary;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
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
public final class SeriesDao_Impl implements SeriesDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<SeriesEntity> __insertionAdapterOfSeriesEntity;

  private final EntityDeletionOrUpdateAdapter<SeriesEntity> __updateAdapterOfSeriesEntity;

  private final SharedSQLiteStatement __preparedStmtOfUpdateMetadata;

  private final SharedSQLiteStatement __preparedStmtOfUpdateTimestamps;

  private final SharedSQLiteStatement __preparedStmtOfDeleteOrphans;

  private final SharedSQLiteStatement __preparedStmtOfDeleteById;

  public SeriesDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfSeriesEntity = new EntityInsertionAdapter<SeriesEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `series` (`id`,`seriesKey`,`displayTitle`,`seasonNumber`,`coverMediaStoreId`,`lastUpdatedSec`,`posterUrl`,`backdropUrl`,`synopsis`,`genres`,`totalSeasonEpisodes`,`isMovie`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SeriesEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getSeriesKey());
        statement.bindString(3, entity.getDisplayTitle());
        statement.bindLong(4, entity.getSeasonNumber());
        if (entity.getCoverMediaStoreId() == null) {
          statement.bindNull(5);
        } else {
          statement.bindLong(5, entity.getCoverMediaStoreId());
        }
        statement.bindLong(6, entity.getLastUpdatedSec());
        if (entity.getPosterUrl() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getPosterUrl());
        }
        if (entity.getBackdropUrl() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getBackdropUrl());
        }
        if (entity.getSynopsis() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getSynopsis());
        }
        if (entity.getGenres() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getGenres());
        }
        if (entity.getTotalSeasonEpisodes() == null) {
          statement.bindNull(11);
        } else {
          statement.bindLong(11, entity.getTotalSeasonEpisodes());
        }
        final int _tmp = entity.isMovie() ? 1 : 0;
        statement.bindLong(12, _tmp);
      }
    };
    this.__updateAdapterOfSeriesEntity = new EntityDeletionOrUpdateAdapter<SeriesEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `series` SET `id` = ?,`seriesKey` = ?,`displayTitle` = ?,`seasonNumber` = ?,`coverMediaStoreId` = ?,`lastUpdatedSec` = ?,`posterUrl` = ?,`backdropUrl` = ?,`synopsis` = ?,`genres` = ?,`totalSeasonEpisodes` = ?,`isMovie` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SeriesEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getSeriesKey());
        statement.bindString(3, entity.getDisplayTitle());
        statement.bindLong(4, entity.getSeasonNumber());
        if (entity.getCoverMediaStoreId() == null) {
          statement.bindNull(5);
        } else {
          statement.bindLong(5, entity.getCoverMediaStoreId());
        }
        statement.bindLong(6, entity.getLastUpdatedSec());
        if (entity.getPosterUrl() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getPosterUrl());
        }
        if (entity.getBackdropUrl() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getBackdropUrl());
        }
        if (entity.getSynopsis() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getSynopsis());
        }
        if (entity.getGenres() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getGenres());
        }
        if (entity.getTotalSeasonEpisodes() == null) {
          statement.bindNull(11);
        } else {
          statement.bindLong(11, entity.getTotalSeasonEpisodes());
        }
        final int _tmp = entity.isMovie() ? 1 : 0;
        statement.bindLong(12, _tmp);
        statement.bindLong(13, entity.getId());
      }
    };
    this.__preparedStmtOfUpdateMetadata = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "\n"
                + "        UPDATE series \n"
                + "        SET posterUrl = ?, \n"
                + "            backdropUrl = ?, \n"
                + "            synopsis = ?, \n"
                + "            genres = ?, \n"
                + "            totalSeasonEpisodes = ? \n"
                + "        WHERE id = ?\n"
                + "    ";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateTimestamps = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE series SET lastUpdatedSec = ?, coverMediaStoreId = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteOrphans = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM series WHERE id NOT IN (SELECT DISTINCT seriesId FROM episodes)";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM series WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final SeriesEntity series, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfSeriesEntity.insertAndReturnId(series);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final SeriesEntity series, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfSeriesEntity.handle(series);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateMetadata(final long seriesId, final String poster, final String backdrop,
      final String synopsis, final String genres, final Integer totalEps,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateMetadata.acquire();
        int _argIndex = 1;
        if (poster == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, poster);
        }
        _argIndex = 2;
        if (backdrop == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, backdrop);
        }
        _argIndex = 3;
        if (synopsis == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, synopsis);
        }
        _argIndex = 4;
        if (genres == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, genres);
        }
        _argIndex = 5;
        if (totalEps == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindLong(_argIndex, totalEps);
        }
        _argIndex = 6;
        _stmt.bindLong(_argIndex, seriesId);
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
          __preparedStmtOfUpdateMetadata.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object updateTimestamps(final long seriesId, final long lastUpdatedSec, final Long coverId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateTimestamps.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, lastUpdatedSec);
        _argIndex = 2;
        if (coverId == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindLong(_argIndex, coverId);
        }
        _argIndex = 3;
        _stmt.bindLong(_argIndex, seriesId);
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
          __preparedStmtOfUpdateTimestamps.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteOrphans(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteOrphans.acquire();
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
          __preparedStmtOfDeleteOrphans.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteById(final long id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteById.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
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
          __preparedStmtOfDeleteById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<SeriesSummary>> observeDashboard() {
    final String _sql = "\n"
            + "        SELECT s.id, s.displayTitle, s.seasonNumber, s.coverMediaStoreId, s.lastUpdatedSec,\n"
            + "               s.posterUrl, s.backdropUrl, s.synopsis, s.genres, s.totalSeasonEpisodes, s.isMovie,\n"
            + "               COUNT(e.id) AS episodeCount,\n"
            + "               COALESCE(SUM(CASE WHEN p.isFinished = 1 THEN 1 ELSE 0 END), 0) AS finishedCount\n"
            + "        FROM series s\n"
            + "        JOIN episodes e ON e.seriesId = s.id\n"
            + "        LEFT JOIN playback_state p ON p.fingerprint = e.fingerprint\n"
            + "        GROUP BY s.id\n"
            + "        ORDER BY s.lastUpdatedSec DESC\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"series", "episodes",
        "playback_state"}, new Callable<List<SeriesSummary>>() {
      @Override
      @NonNull
      public List<SeriesSummary> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = 0;
          final int _cursorIndexOfDisplayTitle = 1;
          final int _cursorIndexOfSeasonNumber = 2;
          final int _cursorIndexOfCoverMediaStoreId = 3;
          final int _cursorIndexOfLastUpdatedSec = 4;
          final int _cursorIndexOfPosterUrl = 5;
          final int _cursorIndexOfBackdropUrl = 6;
          final int _cursorIndexOfSynopsis = 7;
          final int _cursorIndexOfGenres = 8;
          final int _cursorIndexOfTotalSeasonEpisodes = 9;
          final int _cursorIndexOfIsMovie = 10;
          final int _cursorIndexOfEpisodeCount = 11;
          final int _cursorIndexOfFinishedCount = 12;
          final List<SeriesSummary> _result = new ArrayList<SeriesSummary>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SeriesSummary _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpDisplayTitle;
            _tmpDisplayTitle = _cursor.getString(_cursorIndexOfDisplayTitle);
            final int _tmpSeasonNumber;
            _tmpSeasonNumber = _cursor.getInt(_cursorIndexOfSeasonNumber);
            final Long _tmpCoverMediaStoreId;
            if (_cursor.isNull(_cursorIndexOfCoverMediaStoreId)) {
              _tmpCoverMediaStoreId = null;
            } else {
              _tmpCoverMediaStoreId = _cursor.getLong(_cursorIndexOfCoverMediaStoreId);
            }
            final long _tmpLastUpdatedSec;
            _tmpLastUpdatedSec = _cursor.getLong(_cursorIndexOfLastUpdatedSec);
            final String _tmpPosterUrl;
            if (_cursor.isNull(_cursorIndexOfPosterUrl)) {
              _tmpPosterUrl = null;
            } else {
              _tmpPosterUrl = _cursor.getString(_cursorIndexOfPosterUrl);
            }
            final String _tmpBackdropUrl;
            if (_cursor.isNull(_cursorIndexOfBackdropUrl)) {
              _tmpBackdropUrl = null;
            } else {
              _tmpBackdropUrl = _cursor.getString(_cursorIndexOfBackdropUrl);
            }
            final String _tmpSynopsis;
            if (_cursor.isNull(_cursorIndexOfSynopsis)) {
              _tmpSynopsis = null;
            } else {
              _tmpSynopsis = _cursor.getString(_cursorIndexOfSynopsis);
            }
            final String _tmpGenres;
            if (_cursor.isNull(_cursorIndexOfGenres)) {
              _tmpGenres = null;
            } else {
              _tmpGenres = _cursor.getString(_cursorIndexOfGenres);
            }
            final Integer _tmpTotalSeasonEpisodes;
            if (_cursor.isNull(_cursorIndexOfTotalSeasonEpisodes)) {
              _tmpTotalSeasonEpisodes = null;
            } else {
              _tmpTotalSeasonEpisodes = _cursor.getInt(_cursorIndexOfTotalSeasonEpisodes);
            }
            final boolean _tmpIsMovie;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsMovie);
            _tmpIsMovie = _tmp != 0;
            final int _tmpEpisodeCount;
            _tmpEpisodeCount = _cursor.getInt(_cursorIndexOfEpisodeCount);
            final int _tmpFinishedCount;
            _tmpFinishedCount = _cursor.getInt(_cursorIndexOfFinishedCount);
            _item = new SeriesSummary(_tmpId,_tmpDisplayTitle,_tmpSeasonNumber,_tmpCoverMediaStoreId,_tmpLastUpdatedSec,_tmpPosterUrl,_tmpBackdropUrl,_tmpSynopsis,_tmpGenres,_tmpTotalSeasonEpisodes,_tmpIsMovie,_tmpEpisodeCount,_tmpFinishedCount);
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
  public Object findByKey(final String key, final Continuation<? super SeriesEntity> $completion) {
    final String _sql = "SELECT * FROM series WHERE seriesKey = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, key);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<SeriesEntity>() {
      @Override
      @Nullable
      public SeriesEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSeriesKey = CursorUtil.getColumnIndexOrThrow(_cursor, "seriesKey");
          final int _cursorIndexOfDisplayTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "displayTitle");
          final int _cursorIndexOfSeasonNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "seasonNumber");
          final int _cursorIndexOfCoverMediaStoreId = CursorUtil.getColumnIndexOrThrow(_cursor, "coverMediaStoreId");
          final int _cursorIndexOfLastUpdatedSec = CursorUtil.getColumnIndexOrThrow(_cursor, "lastUpdatedSec");
          final int _cursorIndexOfPosterUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "posterUrl");
          final int _cursorIndexOfBackdropUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "backdropUrl");
          final int _cursorIndexOfSynopsis = CursorUtil.getColumnIndexOrThrow(_cursor, "synopsis");
          final int _cursorIndexOfGenres = CursorUtil.getColumnIndexOrThrow(_cursor, "genres");
          final int _cursorIndexOfTotalSeasonEpisodes = CursorUtil.getColumnIndexOrThrow(_cursor, "totalSeasonEpisodes");
          final int _cursorIndexOfIsMovie = CursorUtil.getColumnIndexOrThrow(_cursor, "isMovie");
          final SeriesEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpSeriesKey;
            _tmpSeriesKey = _cursor.getString(_cursorIndexOfSeriesKey);
            final String _tmpDisplayTitle;
            _tmpDisplayTitle = _cursor.getString(_cursorIndexOfDisplayTitle);
            final int _tmpSeasonNumber;
            _tmpSeasonNumber = _cursor.getInt(_cursorIndexOfSeasonNumber);
            final Long _tmpCoverMediaStoreId;
            if (_cursor.isNull(_cursorIndexOfCoverMediaStoreId)) {
              _tmpCoverMediaStoreId = null;
            } else {
              _tmpCoverMediaStoreId = _cursor.getLong(_cursorIndexOfCoverMediaStoreId);
            }
            final long _tmpLastUpdatedSec;
            _tmpLastUpdatedSec = _cursor.getLong(_cursorIndexOfLastUpdatedSec);
            final String _tmpPosterUrl;
            if (_cursor.isNull(_cursorIndexOfPosterUrl)) {
              _tmpPosterUrl = null;
            } else {
              _tmpPosterUrl = _cursor.getString(_cursorIndexOfPosterUrl);
            }
            final String _tmpBackdropUrl;
            if (_cursor.isNull(_cursorIndexOfBackdropUrl)) {
              _tmpBackdropUrl = null;
            } else {
              _tmpBackdropUrl = _cursor.getString(_cursorIndexOfBackdropUrl);
            }
            final String _tmpSynopsis;
            if (_cursor.isNull(_cursorIndexOfSynopsis)) {
              _tmpSynopsis = null;
            } else {
              _tmpSynopsis = _cursor.getString(_cursorIndexOfSynopsis);
            }
            final String _tmpGenres;
            if (_cursor.isNull(_cursorIndexOfGenres)) {
              _tmpGenres = null;
            } else {
              _tmpGenres = _cursor.getString(_cursorIndexOfGenres);
            }
            final Integer _tmpTotalSeasonEpisodes;
            if (_cursor.isNull(_cursorIndexOfTotalSeasonEpisodes)) {
              _tmpTotalSeasonEpisodes = null;
            } else {
              _tmpTotalSeasonEpisodes = _cursor.getInt(_cursorIndexOfTotalSeasonEpisodes);
            }
            final boolean _tmpIsMovie;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsMovie);
            _tmpIsMovie = _tmp != 0;
            _result = new SeriesEntity(_tmpId,_tmpSeriesKey,_tmpDisplayTitle,_tmpSeasonNumber,_tmpCoverMediaStoreId,_tmpLastUpdatedSec,_tmpPosterUrl,_tmpBackdropUrl,_tmpSynopsis,_tmpGenres,_tmpTotalSeasonEpisodes,_tmpIsMovie);
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
  public Object findById(final long id, final Continuation<? super SeriesEntity> $completion) {
    final String _sql = "SELECT * FROM series WHERE id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<SeriesEntity>() {
      @Override
      @Nullable
      public SeriesEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSeriesKey = CursorUtil.getColumnIndexOrThrow(_cursor, "seriesKey");
          final int _cursorIndexOfDisplayTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "displayTitle");
          final int _cursorIndexOfSeasonNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "seasonNumber");
          final int _cursorIndexOfCoverMediaStoreId = CursorUtil.getColumnIndexOrThrow(_cursor, "coverMediaStoreId");
          final int _cursorIndexOfLastUpdatedSec = CursorUtil.getColumnIndexOrThrow(_cursor, "lastUpdatedSec");
          final int _cursorIndexOfPosterUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "posterUrl");
          final int _cursorIndexOfBackdropUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "backdropUrl");
          final int _cursorIndexOfSynopsis = CursorUtil.getColumnIndexOrThrow(_cursor, "synopsis");
          final int _cursorIndexOfGenres = CursorUtil.getColumnIndexOrThrow(_cursor, "genres");
          final int _cursorIndexOfTotalSeasonEpisodes = CursorUtil.getColumnIndexOrThrow(_cursor, "totalSeasonEpisodes");
          final int _cursorIndexOfIsMovie = CursorUtil.getColumnIndexOrThrow(_cursor, "isMovie");
          final SeriesEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpSeriesKey;
            _tmpSeriesKey = _cursor.getString(_cursorIndexOfSeriesKey);
            final String _tmpDisplayTitle;
            _tmpDisplayTitle = _cursor.getString(_cursorIndexOfDisplayTitle);
            final int _tmpSeasonNumber;
            _tmpSeasonNumber = _cursor.getInt(_cursorIndexOfSeasonNumber);
            final Long _tmpCoverMediaStoreId;
            if (_cursor.isNull(_cursorIndexOfCoverMediaStoreId)) {
              _tmpCoverMediaStoreId = null;
            } else {
              _tmpCoverMediaStoreId = _cursor.getLong(_cursorIndexOfCoverMediaStoreId);
            }
            final long _tmpLastUpdatedSec;
            _tmpLastUpdatedSec = _cursor.getLong(_cursorIndexOfLastUpdatedSec);
            final String _tmpPosterUrl;
            if (_cursor.isNull(_cursorIndexOfPosterUrl)) {
              _tmpPosterUrl = null;
            } else {
              _tmpPosterUrl = _cursor.getString(_cursorIndexOfPosterUrl);
            }
            final String _tmpBackdropUrl;
            if (_cursor.isNull(_cursorIndexOfBackdropUrl)) {
              _tmpBackdropUrl = null;
            } else {
              _tmpBackdropUrl = _cursor.getString(_cursorIndexOfBackdropUrl);
            }
            final String _tmpSynopsis;
            if (_cursor.isNull(_cursorIndexOfSynopsis)) {
              _tmpSynopsis = null;
            } else {
              _tmpSynopsis = _cursor.getString(_cursorIndexOfSynopsis);
            }
            final String _tmpGenres;
            if (_cursor.isNull(_cursorIndexOfGenres)) {
              _tmpGenres = null;
            } else {
              _tmpGenres = _cursor.getString(_cursorIndexOfGenres);
            }
            final Integer _tmpTotalSeasonEpisodes;
            if (_cursor.isNull(_cursorIndexOfTotalSeasonEpisodes)) {
              _tmpTotalSeasonEpisodes = null;
            } else {
              _tmpTotalSeasonEpisodes = _cursor.getInt(_cursorIndexOfTotalSeasonEpisodes);
            }
            final boolean _tmpIsMovie;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsMovie);
            _tmpIsMovie = _tmp != 0;
            _result = new SeriesEntity(_tmpId,_tmpSeriesKey,_tmpDisplayTitle,_tmpSeasonNumber,_tmpCoverMediaStoreId,_tmpLastUpdatedSec,_tmpPosterUrl,_tmpBackdropUrl,_tmpSynopsis,_tmpGenres,_tmpTotalSeasonEpisodes,_tmpIsMovie);
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
