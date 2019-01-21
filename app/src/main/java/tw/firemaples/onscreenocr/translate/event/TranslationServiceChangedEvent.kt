package tw.firemaples.onscreenocr.translate.event

import tw.firemaples.onscreenocr.event.BaseEvent
import tw.firemaples.onscreenocr.translate.TranslationService

class TranslationServiceChangedEvent(val translationService: TranslationService) : BaseEvent