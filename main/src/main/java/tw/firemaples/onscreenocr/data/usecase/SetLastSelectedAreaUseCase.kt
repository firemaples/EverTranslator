package tw.firemaples.onscreenocr.data.usecase

import android.graphics.Rect
import tw.firemaples.onscreenocr.data.repo.PreferenceRepository
import javax.inject.Inject

class SetLastSelectedAreaUseCase @Inject constructor(
    private val preferenceRepository: PreferenceRepository,
) {
    operator fun invoke(selectedRect: Rect) {
        preferenceRepository.setLastSelectedArea(selectedRect)
    }
}
