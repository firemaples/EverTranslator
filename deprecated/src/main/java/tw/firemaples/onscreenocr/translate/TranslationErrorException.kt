package tw.firemaples.onscreenocr.translate

import java.lang.Exception

class TranslationErrorException(message: String?, cause: Throwable?, val type: ErrorType = ErrorType.Undetermined) : Exception(message, cause)

enum class ErrorType {
    ExceededDataLimit,
    Undetermined
}