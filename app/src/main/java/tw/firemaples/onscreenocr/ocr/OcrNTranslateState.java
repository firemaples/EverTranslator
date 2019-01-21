package tw.firemaples.onscreenocr.ocr;

public enum OcrNTranslateState {
    OCR_INIT(1), OCR_RUNNING(2), OCR_FINISHED(3),
    TRANSLATING(4), TRANSLATED(5);

    private int step;

    OcrNTranslateState(int step) {
        this.step = step;
    }

    public int getStep() {
        return step;
    }
}
