package tw.firemaples.onscreenocr.data.usecase

import kotlinx.coroutines.flow.combine
import tw.firemaples.onscreenocr.data.repo.RecognitionRepository
import javax.inject.Inject

class GetCurrentOCRLangUseCase @Inject constructor(
    private val recognitionRepository: RecognitionRepository,
) {
    operator fun invoke() =
        combine(
            recognitionRepository.ocrProvider,
            recognitionRepository.ocrLanguage,
        ) { provider, lang -> provider to lang }
}
