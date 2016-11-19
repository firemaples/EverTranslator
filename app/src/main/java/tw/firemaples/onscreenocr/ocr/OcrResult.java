package tw.firemaples.onscreenocr.ocr;

import android.graphics.Bitmap;
import android.graphics.Rect;

import com.googlecode.tesseract.android.ResultIterator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by firem_000 on 2016/3/6.
 */
public class OcrResult {

    private String text;
    private String translatedText;
    private ResultIterator resultIterator;
    private ArrayList<Rect> boxRects;
    /**
     * Region of detected text
     */
    private Rect rect;
    /**
     * Region of text area with margins
     */
    private Rect subRect;
    /**
     * Region of result text area
     */
    private Rect touchRect;
    private int textWidth, textHeight;
    private DebugInfo debugInfo;

    public void setRect(Rect rect) {
        this.rect = rect;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setResultIterator(ResultIterator resultIterator) {
        this.resultIterator = resultIterator;
    }

    public void setBoxRects(ArrayList<Rect> boxRects) {
        this.boxRects = boxRects;
    }

    public void setSubRect(Rect subRect) {
        this.subRect = subRect;
    }

    public String getText() {
        return text;
    }

    public ResultIterator getResultIterator() {
        return resultIterator;
    }

    public ArrayList<Rect> getBoxRects() {
        return boxRects;
    }

    public Rect getRect() {
        return rect;
    }

    public Rect getSubRect() {
        return subRect;
    }

    public void setTranslatedText(String translatedText) {
        this.translatedText = translatedText;
    }

    public String getTranslatedText() {
        return translatedText;
    }

    public void setTouchRect(Rect touchRect) {
        this.touchRect = touchRect;
    }

    public Rect getTouchRect() {
        return touchRect;
    }

    public int getTextWidth() {
        return textWidth;
    }

    public void setTextWidth(int textWidth) {
        this.textWidth = textWidth;
    }

    public int getTextHeight() {
        return textHeight;
    }

    public void setTextHeight(int textHeight) {
        this.textHeight = textHeight;
    }

    public void setDebugInfo(DebugInfo debugInfo) {
        this.debugInfo = debugInfo;
    }

    public DebugInfo getDebugInfo() {
        return debugInfo;
    }

    public static class DebugInfo {
        private Bitmap croppedBitmap;
        private List<String> infoList = new ArrayList<>();

        public void setCroppedBitmap(Bitmap croppedBitmap) {
            this.croppedBitmap = croppedBitmap;
        }

        public Bitmap getCroppedBitmap() {
            return croppedBitmap;
        }

        public void addInfoString(String info) {
            infoList.add(info);
        }

        public List<String> getInfoList() {
            return infoList;
        }
    }
}
