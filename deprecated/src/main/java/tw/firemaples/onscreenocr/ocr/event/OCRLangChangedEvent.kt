package tw.firemaples.onscreenocr.ocr.event

import tw.firemaples.onscreenocr.event.BaseEvent

class OCRLangChangedEvent(val langCode: String, val langDisplayCode: String) : BaseEvent