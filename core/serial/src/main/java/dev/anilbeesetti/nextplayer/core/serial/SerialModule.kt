package dev.anilbeesetti.nextplayer.core.serial

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // ✅ Makes SerialTriggerHandler available across the app
object SerialModule {

    @Singleton
    @Provides
    fun provideSerialTriggerHandler(@ApplicationContext context: Context): SerialTriggerHandler {
        return SerialTriggerHandler(context)
    }
}