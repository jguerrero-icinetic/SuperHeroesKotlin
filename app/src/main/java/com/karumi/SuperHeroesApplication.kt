package com.karumi

import android.app.Application
import android.content.Context
import com.github.salomonbrys.kodein.Kodein.Module
import com.github.salomonbrys.kodein.KodeinAware
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.conf.ConfigurableKodein
import com.github.salomonbrys.kodein.instance
import com.github.salomonbrys.kodein.provider
import com.github.salomonbrys.kodein.singleton
import com.karumi.common.RealTimeProvider
import com.karumi.common.TimeProvider
import com.karumi.data.repository.MemorySuperHeroDataSource
import com.karumi.data.repository.NetworkSuperHeroDataSource
import com.karumi.data.repository.SuperHeroDataSource
import com.karumi.data.repository.SuperHeroRepository
import com.karumi.marvelapiclient.CharacterApiClient
import com.karumi.marvelapiclient.MarvelApiConfig

class SuperHeroesApplication : Application(), KodeinAware {
    override val kodein = ConfigurableKodein(mutable = true)
    var overrideModule: Module? = null

    override fun onCreate() {
        super.onCreate()
        resetInjection()
    }

    fun addModule(activityModules: Module) {
        kodein.addImport(activityModules, true)
        if (overrideModule != null) {
            kodein.addImport(overrideModule!!, true)
        }
    }

    fun resetInjection() {
        kodein.clear()
        kodein.addImport(appDependencies(), true)
    }

    private fun appDependencies(): Module {
        return Module(allowSilentOverride = true) {
            bind<SuperHeroRepository>() with provider {
                SuperHeroRepository(listOf(instance<SuperHeroDataSource>(),
                    NetworkSuperHeroDataSource(instance())))
            }
            bind<SuperHeroDataSource>() with singleton {
                MemorySuperHeroDataSource(instance())
            }
            bind<TimeProvider>() with instance(RealTimeProvider())
            bind<CharacterApiClient>() with provider { CharacterApiClient(instance()) }
            bind<MarvelApiConfig>() with instance(
                MarvelApiConfig.with(BuildConfig.MARVEL_PUBLIC_KEY, BuildConfig.MARVEL_PRIVATE_KEY))
        }
    }
}

fun Context.asApp() = this.applicationContext as SuperHeroesApplication