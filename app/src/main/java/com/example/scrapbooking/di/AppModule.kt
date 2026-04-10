package com.example.scrapbooking.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.Room
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
	@Module
	@InstallIn(SingletonComponent::class)
	object RoomModule {
		@Provides
		@Singleton
		fun provideDatabase(@ApplicationContext context: android.content.Context): com.example.scrapbooking.data.local.ScrapBookingDatabase {
			val MIGRATION_1_2 = object : Migration(1, 2) {
				override fun migrate(database: SupportSQLiteDatabase) {
					database.execSQL("ALTER TABLE stamps ADD COLUMN date TEXT")
					database.execSQL("ALTER TABLE stamps ADD COLUMN time TEXT")
					database.execSQL("ALTER TABLE stamps ADD COLUMN location TEXT")
				}
			}

			return Room.databaseBuilder(
				context,
				com.example.scrapbooking.data.local.ScrapBookingDatabase::class.java,
				"scrapbooking.db"
			).addMigrations(MIGRATION_1_2).build()
		}

		@Provides
		fun provideStampDao(db: com.example.scrapbooking.data.local.ScrapBookingDatabase): com.example.scrapbooking.data.local.StampDao =
			db.stampDao()
	}

	@Module
	@InstallIn(SingletonComponent::class)
	abstract class Bindings {
		@Binds
		abstract fun bindStampRepository(
			impl: com.example.scrapbooking.data.repository.StampRepositoryImpl
		): com.example.scrapbooking.domain.repository.StampRepository
	}
}