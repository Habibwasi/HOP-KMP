package com.example.hop.di

import com.example.hop.chat.AndroidChatRepositoryImpl
import com.example.hop.chat.ChatRepository
import org.koin.core.module.Module
import org.koin.dsl.module

actual val chatRepositoryModule: Module = module {
    single<ChatRepository> { AndroidChatRepositoryImpl() }
}
