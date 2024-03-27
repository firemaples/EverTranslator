package tw.firemaples.onscreenocr.data.usecase

import tw.firemaples.onscreenocr.data.repo.TranslatorRepository
import javax.inject.Inject

class GetCurrentTranslatorTypeUseCase @Inject constructor(
    private val translatorRepository: TranslatorRepository,
) {
    operator fun invoke() = translatorRepository.currentProviderType
}
