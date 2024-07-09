package com.github.diegoberaldin.raccoonforlemmy.core.preferences.appconfig

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class DefaultAppConfigStore(
    private val localDataSource: AppConfigDataSource,
    private val remoteDataSource: AppConfigDataSource,
    dispatcher: CoroutineDispatcher,
) : AppConfigStore {
    override val appConfig = MutableStateFlow(AppConfig())
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    override fun initialize() {
        scope.launch {
            val localValue = localDataSource.get()
            appConfig.update { localValue }

            val remoteValue = remoteDataSource.get()
            localDataSource.update(remoteValue)
            appConfig.update { remoteValue }
        }
    }
}
