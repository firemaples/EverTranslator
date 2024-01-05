package tw.firemaples.onscreenocr.data.usecase

import tw.firemaples.onscreenocr.data.repo.PreferenceRepository
import javax.inject.Inject

class SaveLastMainBarPositionUseCase @Inject constructor(
    private val preferenceRepository: PreferenceRepository,
) {
    operator fun invoke(x: Int, y: Int) =
        preferenceRepository.saveLastMainBarPosition(x = x, y = y)
}