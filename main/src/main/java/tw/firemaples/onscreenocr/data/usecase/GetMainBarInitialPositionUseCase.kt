package tw.firemaples.onscreenocr.data.usecase

import android.graphics.Point
import tw.firemaples.onscreenocr.data.repo.PreferenceRepository
import tw.firemaples.onscreenocr.data.repo.SettingRepository
import javax.inject.Inject

class GetMainBarInitialPositionUseCase @Inject constructor(
    private val settingRepository: SettingRepository,
    private val preferenceRepository: PreferenceRepository,
) {
    operator fun invoke() =
        if (settingRepository.shouldRestoreMainBarPosition())
            preferenceRepository.getLastMainBarPosition()
        else Point(0, 0)
}
