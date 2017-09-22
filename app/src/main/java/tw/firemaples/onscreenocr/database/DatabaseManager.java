package tw.firemaples.onscreenocr.database;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Collections;

import tw.firemaples.onscreenocr.utils.KeyId;
import tw.firemaples.onscreenocr.utils.Tool;

/**
 * Created by louis1chen on 01/05/2017.
 */

public class DatabaseManager {
    private static DatabaseManager _instance;

    private FirebaseDatabase db;

    private ServiceHolderModel translateService;

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

        String key = ServiceHolderModel.getKey();
        Tool.logInfo("Use key["+key+"] for Firebase db");
        DatabaseReference translateServiceRef = db.getReference(key);
        translateServiceRef.keepSynced(true);

        //current translate service
        translateServiceRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Tool.logInfo("ServiceHolderModel onDataChange");
                DatabaseManager.this.translateService = dataSnapshot.getValue(ServiceHolderModel.class);
                Tool.logInfo("Current translate service is " + translateService.getUsingService().name);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Tool.logError("Database error: " + databaseError.getMessage());
                //noinspection ThrowableResultOfMethodCallIgnored
                databaseError.toException().printStackTrace();
            }
        });
    }

    public ServiceHolderModel getTranslateServiceHolder() {
        return translateService == null ? getDefaultTranslateService() : translateService;
    }

    private ServiceHolderModel getDefaultTranslateService() {
        translateService = new ServiceHolderModel();
        translateService.usingOrder = Collections.singletonList(ServiceHolderModel.SERVICE_MICROSOFT_API);

        ServiceModel microsoftService = new ServiceModel();
        microsoftService.name = ServiceHolderModel.SERVICE_MICROSOFT_API;
        microsoftService.key = KeyId.MICROSOFT_TRANSLATE_SUBSCRIPTION_KEY;
        translateService.services.add(microsoftService);

        return translateService;
    }
}
