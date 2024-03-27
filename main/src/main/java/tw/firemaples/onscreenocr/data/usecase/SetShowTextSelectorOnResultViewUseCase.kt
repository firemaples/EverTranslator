package tw.firemaples.onscreenocr.data.usecase

import tw.firemaples.onscreenocr.data.repo.PreferenceRepository
import javax.inject.Inject

class SetShowTextSelectorOnResultViewUseCase @Inject constructor(
    private val preferenceRepository: PreferenceRepository,
) {
    operator fun invoke(show: Boolean) {
        preferenceRepository.setShowTextSelectionOnResultView(show = show)
    }
}
