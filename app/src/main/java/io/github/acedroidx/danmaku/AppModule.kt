package io.github.acedroidx.danmaku

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.acedroidx.danmaku.data.home.DanmakuConfigDatabase
import javax.inject.Singleton

//https://stackoverflow.com/questions/63146318/how-to-create-and-use-a-room-database-in-kotlin-dagger-hilt

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Singleton // Tell Dagger-Hilt to create a singleton accessible everywhere in ApplicationCompenent (i.e. everywhere in the application)
    @Provides
    fun provideDanmakuConfigDatabase(
        @ApplicationContext app: Context
    ) = Room.databaseBuilder(
        app,
        DanmakuConfigDatabase::class.java,
        "danmaku-config"
    ).build() // The reason we can construct a database for the repo

    @Singleton
    @Provides
    fun provideDanmakuConfigDao(db: DanmakuConfigDatabase) = db.danmakuConfigDao() // The reason we can implement a Dao for the database
}