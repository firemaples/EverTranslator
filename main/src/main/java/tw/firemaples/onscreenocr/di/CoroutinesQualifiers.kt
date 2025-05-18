package tw.firemaples.onscreenocr.di

import javax.inject.Qualifier

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class DefaultDispatcher

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class IoDispatcher

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class MainDispatcher

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class MainImmediateDispatcher

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class MainImmediateCoroutineScope

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class MainCoroutineScope

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class DefaultCoroutineScope
