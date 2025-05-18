package tw.firemaples.onscreenocr.data.usecase

import tw.firemaples.onscreenocr.data.repo.SettingRepository
import javax.inject.Inject

class GetLimitResultViewMaxWidthUseCase @Inject constructor(
    private val settingRepository: SettingRepository,
) {
    operator fun invoke(): Boolean = settingRepository.limitResultViewMaxWidth()
}
