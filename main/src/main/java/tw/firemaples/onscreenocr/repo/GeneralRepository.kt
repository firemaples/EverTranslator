package tw.firemaples.onscreenocr.repo

import android.graphics.Rect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import tw.firemaples.onscreenocr.pref.AppPref

class GeneralRepository {
    fun isRememberLastSelection(): Flow<Boolean> = flow {
        emit(AppPref.rememberLastSelectedArea)
    }.flowOn(Dispatchers.Default)

    fun getLastRememberedSelectionArea(): Flow<Rect?> = flow {
        emit(AppPref.lastRememberedSelectedArea)
    }.flowOn(Dispatchers.Default)
}
