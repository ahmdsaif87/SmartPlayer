package id.smartplayer.data.db;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile SeriesDao _seriesDao;

  private volatile EpisodeDao _episodeDao;

  private volatile PlaybackStateDao _playbackStateDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `series` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `seriesKey` TEXT NOT NULL, `displayTitle` TEXT NOT NULL, `seasonNumber` INTEGER NOT NULL, `coverMediaStoreId` INTEGER, `lastUpdatedSec` INTEGER NOT NULL, `posterUrl` TEXT, `backdropUrl` TEXT, `synopsis` TEXT, `genres` TEXT, `totalSeasonEpisodes` INTEGER, `isMovie` INTEGER NOT NULL)");
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_series_seriesKey` ON `series` (`seriesKey`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_series_lastUpdatedSec` ON `series` (`lastUpdatedSec`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `episodes` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `seriesId` INTEGER NOT NULL, `mediaStoreId` INTEGER NOT NULL, `contentUri` TEXT NOT NULL, `absolutePath` TEXT NOT NULL, `relativePath` TEXT NOT NULL, `displayName` TEXT NOT NULL, `sizeBytes` INTEGER NOT NULL, `durationMs` INTEGER NOT NULL, `dateAddedSec` INTEGER NOT NULL, `dateModifiedSec` INTEGER NOT NULL, `episodeNumber` INTEGER, `episodeVersion` INTEGER, `seasonNumber` INTEGER NOT NULL, `releaseGroup` TEXT, `resolution` TEXT, `parseConfidence` TEXT NOT NULL, `fingerprint` TEXT NOT NULL, `episodeTitle` TEXT, `synopsis` TEXT, FOREIGN KEY(`seriesId`) REFERENCES `series`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_episodes_mediaStoreId` ON `episodes` (`mediaStoreId`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_episodes_absolutePath` ON `episodes` (`absolutePath`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_episodes_seriesId_episodeNumber` ON `episodes` (`seriesId`, `episodeNumber`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_episodes_fingerprint` ON `episodes` (`fingerprint`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `playback_state` (`fingerprint` TEXT NOT NULL, `lastPlayedPosition` INTEGER NOT NULL, `durationMs` INTEGER NOT NULL, `isFinished` INTEGER NOT NULL, `updatedAtEpochMs` INTEGER NOT NULL, PRIMARY KEY(`fingerprint`))");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_playback_state_updatedAtEpochMs` ON `playback_state` (`updatedAtEpochMs`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '4ad4180be349cc530dbc049cefc4d624')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `series`");
        db.execSQL("DROP TABLE IF EXISTS `episodes`");
        db.execSQL("DROP TABLE IF EXISTS `playback_state`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        db.execSQL("PRAGMA foreign_keys = ON");
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsSeries = new HashMap<String, TableInfo.Column>(12);
        _columnsSeries.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSeries.put("seriesKey", new TableInfo.Column("seriesKey", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSeries.put("displayTitle", new TableInfo.Column("displayTitle", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSeries.put("seasonNumber", new TableInfo.Column("seasonNumber", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSeries.put("coverMediaStoreId", new TableInfo.Column("coverMediaStoreId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSeries.put("lastUpdatedSec", new TableInfo.Column("lastUpdatedSec", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSeries.put("posterUrl", new TableInfo.Column("posterUrl", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSeries.put("backdropUrl", new TableInfo.Column("backdropUrl", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSeries.put("synopsis", new TableInfo.Column("synopsis", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSeries.put("genres", new TableInfo.Column("genres", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSeries.put("totalSeasonEpisodes", new TableInfo.Column("totalSeasonEpisodes", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSeries.put("isMovie", new TableInfo.Column("isMovie", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSeries = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesSeries = new HashSet<TableInfo.Index>(2);
        _indicesSeries.add(new TableInfo.Index("index_series_seriesKey", true, Arrays.asList("seriesKey"), Arrays.asList("ASC")));
        _indicesSeries.add(new TableInfo.Index("index_series_lastUpdatedSec", false, Arrays.asList("lastUpdatedSec"), Arrays.asList("ASC")));
        final TableInfo _infoSeries = new TableInfo("series", _columnsSeries, _foreignKeysSeries, _indicesSeries);
        final TableInfo _existingSeries = TableInfo.read(db, "series");
        if (!_infoSeries.equals(_existingSeries)) {
          return new RoomOpenHelper.ValidationResult(false, "series(id.smartplayer.data.db.entities.SeriesEntity).\n"
                  + " Expected:\n" + _infoSeries + "\n"
                  + " Found:\n" + _existingSeries);
        }
        final HashMap<String, TableInfo.Column> _columnsEpisodes = new HashMap<String, TableInfo.Column>(20);
        _columnsEpisodes.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEpisodes.put("seriesId", new TableInfo.Column("seriesId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEpisodes.put("mediaStoreId", new TableInfo.Column("mediaStoreId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEpisodes.put("contentUri", new TableInfo.Column("contentUri", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEpisodes.put("absolutePath", new TableInfo.Column("absolutePath", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEpisodes.put("relativePath", new TableInfo.Column("relativePath", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEpisodes.put("displayName", new TableInfo.Column("displayName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEpisodes.put("sizeBytes", new TableInfo.Column("sizeBytes", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEpisodes.put("durationMs", new TableInfo.Column("durationMs", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEpisodes.put("dateAddedSec", new TableInfo.Column("dateAddedSec", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEpisodes.put("dateModifiedSec", new TableInfo.Column("dateModifiedSec", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEpisodes.put("episodeNumber", new TableInfo.Column("episodeNumber", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEpisodes.put("episodeVersion", new TableInfo.Column("episodeVersion", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEpisodes.put("seasonNumber", new TableInfo.Column("seasonNumber", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEpisodes.put("releaseGroup", new TableInfo.Column("releaseGroup", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEpisodes.put("resolution", new TableInfo.Column("resolution", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEpisodes.put("parseConfidence", new TableInfo.Column("parseConfidence", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEpisodes.put("fingerprint", new TableInfo.Column("fingerprint", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEpisodes.put("episodeTitle", new TableInfo.Column("episodeTitle", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEpisodes.put("synopsis", new TableInfo.Column("synopsis", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysEpisodes = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysEpisodes.add(new TableInfo.ForeignKey("series", "CASCADE", "NO ACTION", Arrays.asList("seriesId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesEpisodes = new HashSet<TableInfo.Index>(4);
        _indicesEpisodes.add(new TableInfo.Index("index_episodes_mediaStoreId", true, Arrays.asList("mediaStoreId"), Arrays.asList("ASC")));
        _indicesEpisodes.add(new TableInfo.Index("index_episodes_absolutePath", false, Arrays.asList("absolutePath"), Arrays.asList("ASC")));
        _indicesEpisodes.add(new TableInfo.Index("index_episodes_seriesId_episodeNumber", false, Arrays.asList("seriesId", "episodeNumber"), Arrays.asList("ASC", "ASC")));
        _indicesEpisodes.add(new TableInfo.Index("index_episodes_fingerprint", false, Arrays.asList("fingerprint"), Arrays.asList("ASC")));
        final TableInfo _infoEpisodes = new TableInfo("episodes", _columnsEpisodes, _foreignKeysEpisodes, _indicesEpisodes);
        final TableInfo _existingEpisodes = TableInfo.read(db, "episodes");
        if (!_infoEpisodes.equals(_existingEpisodes)) {
          return new RoomOpenHelper.ValidationResult(false, "episodes(id.smartplayer.data.db.entities.EpisodeEntity).\n"
                  + " Expected:\n" + _infoEpisodes + "\n"
                  + " Found:\n" + _existingEpisodes);
        }
        final HashMap<String, TableInfo.Column> _columnsPlaybackState = new HashMap<String, TableInfo.Column>(5);
        _columnsPlaybackState.put("fingerprint", new TableInfo.Column("fingerprint", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlaybackState.put("lastPlayedPosition", new TableInfo.Column("lastPlayedPosition", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlaybackState.put("durationMs", new TableInfo.Column("durationMs", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlaybackState.put("isFinished", new TableInfo.Column("isFinished", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlaybackState.put("updatedAtEpochMs", new TableInfo.Column("updatedAtEpochMs", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysPlaybackState = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesPlaybackState = new HashSet<TableInfo.Index>(1);
        _indicesPlaybackState.add(new TableInfo.Index("index_playback_state_updatedAtEpochMs", false, Arrays.asList("updatedAtEpochMs"), Arrays.asList("ASC")));
        final TableInfo _infoPlaybackState = new TableInfo("playback_state", _columnsPlaybackState, _foreignKeysPlaybackState, _indicesPlaybackState);
        final TableInfo _existingPlaybackState = TableInfo.read(db, "playback_state");
        if (!_infoPlaybackState.equals(_existingPlaybackState)) {
          return new RoomOpenHelper.ValidationResult(false, "playback_state(id.smartplayer.data.db.entities.PlaybackStateEntity).\n"
                  + " Expected:\n" + _infoPlaybackState + "\n"
                  + " Found:\n" + _existingPlaybackState);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "4ad4180be349cc530dbc049cefc4d624", "7b626633dced837698934911e2ec6fb4");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "series","episodes","playback_state");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    final boolean _supportsDeferForeignKeys = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP;
    try {
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = FALSE");
      }
      super.beginTransaction();
      if (_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA defer_foreign_keys = TRUE");
      }
      _db.execSQL("DELETE FROM `series`");
      _db.execSQL("DELETE FROM `episodes`");
      _db.execSQL("DELETE FROM `playback_state`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = TRUE");
      }
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(SeriesDao.class, SeriesDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(EpisodeDao.class, EpisodeDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(PlaybackStateDao.class, PlaybackStateDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public SeriesDao seriesDao() {
    if (_seriesDao != null) {
      return _seriesDao;
    } else {
      synchronized(this) {
        if(_seriesDao == null) {
          _seriesDao = new SeriesDao_Impl(this);
        }
        return _seriesDao;
      }
    }
  }

  @Override
  public EpisodeDao episodeDao() {
    if (_episodeDao != null) {
      return _episodeDao;
    } else {
      synchronized(this) {
        if(_episodeDao == null) {
          _episodeDao = new EpisodeDao_Impl(this);
        }
        return _episodeDao;
      }
    }
  }

  @Override
  public PlaybackStateDao playbackStateDao() {
    if (_playbackStateDao != null) {
      return _playbackStateDao;
    } else {
      synchronized(this) {
        if(_playbackStateDao == null) {
          _playbackStateDao = new PlaybackStateDao_Impl(this);
        }
        return _playbackStateDao;
      }
    }
  }
}
