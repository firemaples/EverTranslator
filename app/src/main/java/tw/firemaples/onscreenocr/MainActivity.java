package tw.firemaples.onscreenocr;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.googlecode.leptonica.android.ReadFile;
import com.googlecode.tesseract.android.ResultIterator;
import com.googlecode.tesseract.android.TessBaseAPI;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private TessBaseAPI baseApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                takeScreenshot();
                fromRes();
            }
        });

        OnScreenTranslateService.start(this);
        finish();

//        baseApi = new TessBaseAPI();
//        baseApi.init(Environment.getExternalStorageDirectory().getPath() + "/tesseract/", "jpn", TessBaseAPI.OEM_TESSERACT_ONLY);
//        baseApi.setPageSegMode(TessBaseAPI.PageSegMode.PSM_AUTO_OSD);
    }

    private void fromRes(){
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.qq);
        decode(bitmap);
    }

    private void takeScreenshot() {
        ImageView iv = (ImageView) findViewById(R.id.imageView);
        TextView tv = (TextView) findViewById(R.id.textView);
        iv.setImageDrawable(null);
        tv.setText(null);

        // create bitmap screen capture
        View v1 = getWindow().getDecorView().getRootView();
        v1.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
        v1.setDrawingCacheEnabled(false);

        iv.setImageBitmap(bitmap);

        decode(bitmap);
    }

    private void decode(Bitmap bitmap){
        ImageView iv = (ImageView) findViewById(R.id.imageView);
        TextView tv = (TextView) findViewById(R.id.textView);

        iv.setImageBitmap(bitmap);

        baseApi.setImage(ReadFile.readBitmap(bitmap));
        String result = baseApi.getUTF8Text();
        ResultIterator iterator = baseApi.getResultIterator();
        int[] lastBoundingBox;
        ArrayList<Rect> charBoxes = new ArrayList<Rect>();
        iterator.begin();
        do {
            lastBoundingBox = iterator.getBoundingBox(TessBaseAPI.PageIteratorLevel.RIL_SYMBOL);
            Rect lastRectBox = new Rect(lastBoundingBox[0], lastBoundingBox[1],
                    lastBoundingBox[2], lastBoundingBox[3]);
            charBoxes.add(lastRectBox);
            iterator.getUTF8Text(TessBaseAPI.PageIteratorLevel.RIL_WORD);
        } while (iterator.next(TessBaseAPI.PageIteratorLevel.RIL_SYMBOL));
        iterator.delete();

        //            int[] ints = baseAPI.wordConfidences();
//            Pixa words = baseAPI.getWords();
//            ResultIterator iterator = baseAPI.getResultIterator();
//            int level = TessBaseAPI.PageIteratorLevel.RIL_SYMBOL;
//            iterator.begin();
//            do {
//                String chr = iterator.getUTF8Text(level);
//                Tool.LogInfo("**Char: " + chr);
//                List<Pair<String, Double>> choicesAndConfidence = iterator.getChoicesAndConfidence(level);
////                for (Pair<String, Double> choices :
////                        choicesAndConfidence) {
////                    Tool.LogInfo("      - " + choices.first + " : " + choices.second);
////                }
//            } while (iterator.next(level));
//            iterator.delete();

        tv.setText(result);

        log(result);
    }

    private void log(String str) {
        Log.i("myLog", str);
    }

}
