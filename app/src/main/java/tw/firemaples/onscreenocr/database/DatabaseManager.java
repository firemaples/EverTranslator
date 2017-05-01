package tw.firemaples.onscreenocr.database;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import tw.firemaples.onscreenocr.utils.KeyId;
import tw.firemaples.onscreenocr.utils.Tool;

/**
 * Created by louis1chen on 01/05/2017.
 */

public class DatabaseManager {
    private static DatabaseManager _instance;

    private FirebaseDatabase db;

    private TranslateServiceModel translateService;

    private DatabaseManager() {
        setup();
    }

    public static DatabaseManager getInstance() {
        if (_instance == null) {
            _instance = new DatabaseManager();
        }

        return _instance;
    }

    private void setup() {
        db = FirebaseDatabase.getInstance();
        db.setPersistenceEnabled(true);

        DatabaseReference translateServiceRef = db.getReference(TranslateServiceModel.getKey());
        translateServiceRef.keepSynced(true);

        //current translate service
        translateServiceRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Tool.logInfo("TranslateServiceModel onDataChange");
                DatabaseManager.this.translateService = dataSnapshot.getValue(TranslateServiceModel.class);
                Tool.logInfo("Current translate service is " + translateService.getCurrent().name());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Tool.logError("Database error: " + databaseError.getMessage());
                //noinspection ThrowableResultOfMethodCallIgnored
                databaseError.toException().printStackTrace();
            }
        });
    }

    public TranslateServiceModel getTranslateService() {
        return translateService == null ? getDefaultTranslateService() : translateService;
    }

    private TranslateServiceModel getDefaultTranslateService() {
        TranslateServiceModel model = new TranslateServiceModel();
        translateService.current = 0;

        ServiceModel microsoftService = new ServiceModel();
        microsoftService.name = TranslateServiceModel.TranslateServiceEnum.microsoft;
        microsoftService.key = KeyId.MICROSOFT_TRANSLATE_SUBSCRIPTION_KEY;
        translateService.services.add(microsoftService);

        return model;
    }
}
