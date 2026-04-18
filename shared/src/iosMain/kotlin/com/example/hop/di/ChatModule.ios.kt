package com.example.hop.di

import com.example.hop.chat.ChatRepository
import com.example.hop.chat.IosChatRepositoryImpl
import org.koin.core.module.Module
import org.koin.dsl.module

actual val chatRepositoryModule: Module = module {
    single<ChatRepository> { IosChatRepositoryImpl() }
}
