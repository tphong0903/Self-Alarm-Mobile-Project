package hcmute.edu.vn.selfalarmproject.utils;

import static hcmute.edu.vn.selfalarmproject.views.MainActivity.account;

import android.app.Activity;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

import hcmute.edu.vn.selfalarmproject.controllers.services.CalendarService;

public class GoogleCalendarManager {
    private HttpTransport transport;
    private final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
    private static GoogleAccountCredential credential;
    private final Activity activity;
    private com.google.api.services.calendar.Calendar service;
    private CalendarService calendarService;

    public GoogleCalendarManager(Activity activity) {
        this.activity = activity;
        try {
            transport = GoogleNetHttpTransport.newTrustedTransport();
            credential = GoogleAccountCredential.usingOAuth2(
                    activity.getApplicationContext(), Collections.singleton(CalendarScopes.CALENDAR));
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        credential.setSelectedAccount(account.getAccount());
        service = new com.google.api.services.calendar.Calendar.Builder(transport, jsonFactory, credential)
                .setApplicationName("SelfAlarmProject")
                .build();
        calendarService = new CalendarService(service);
    }

    public void addEvent(String title, String description, String startTime, String endTime, String date) {
        calendarService.addEvent(title, description, startTime, endTime, date);
    }

    public List<Event> fetchCalendarEvents(int daytext, int Month, int Year) {
        return calendarService.fetchCalendarEvents(daytext, Month, Year);
    }

    public void deleteEvent(String id) {
        calendarService.deleteEvent(id);
    }

    public void editEvent(String id, String title, String description, String startTime, String endTime, String date) {
        calendarService.editEvent(id, title, description, startTime, endTime, date);
    }

}
