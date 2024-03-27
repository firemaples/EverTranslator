package tw.firemaples.onscreenocr.data.usecase

import kotlinx.coroutines.flow.combine
import tw.firemaples.onscreenocr.data.repo.RecognitionRepository
import tw.firemaples.onscreenocr.recognition.TextRecognizer
import javax.inject.Inject

class GetCurrentOCRDisplayLangCodeUseCase @Inject constructor(
    private val recognitionRepository: RecognitionRepository,
) {
    operator fun invoke() =
        recognitionRepository.ocrProvider
            .combine(recognitionRepository.ocrLanguage) { provider, lang ->
                TextRecognizer.getRecognizer(provider).parseToDisplayLangCode(lang)
            }
}
