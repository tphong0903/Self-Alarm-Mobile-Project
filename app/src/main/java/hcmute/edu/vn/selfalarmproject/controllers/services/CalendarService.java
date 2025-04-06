package hcmute.edu.vn.selfalarmproject.controllers.services;


import android.util.Log;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class CalendarService {
    private com.google.api.services.calendar.Calendar service;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    public CalendarService(com.google.api.services.calendar.Calendar service) {
        this.service = service;
    }

    public List<Event> fetchCalendarEvents(int daytext, int Month, int Year) {
        Callable<List<Event>> task = () -> {
            try {
                boolean checkTime = (Month == 0);
                Calendar calendar = Calendar.getInstance();
                LocalDate selectedDate2 = LocalDate.now();
                calendar.set(Calendar.DAY_OF_MONTH, daytext == -99 ? 1 : daytext);
                calendar.set(Calendar.MONTH, checkTime ? selectedDate2.getMonthValue() - 1 : Month);
                calendar.set(Calendar.YEAR, checkTime ? selectedDate2.getYear() : Year);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                DateTime firstDayOfMonth = new DateTime(calendar.getTime());

                calendar.set(Calendar.DAY_OF_MONTH, daytext == -99 ? calendar.getActualMaximum(Calendar.DAY_OF_MONTH) : daytext);
                calendar.set(Calendar.HOUR_OF_DAY, 23);
                calendar.set(Calendar.MINUTE, 59);
                calendar.set(Calendar.SECOND, 59);
                DateTime lastDayOfMonth = new DateTime(calendar.getTime());


                Events events = service.events().list("primary")
                        .setTimeMin(firstDayOfMonth)
                        .setTimeMax(lastDayOfMonth)
                        .setOrderBy("startTime")
                        .setSingleEvents(true)
                        .execute();

                return events.getItems();
            } catch (Exception e) {
                Log.e("Error", "Lỗi khi lấy sự kiện", e);
                return new ArrayList<>();
            }
        };

        Future<List<Event>> future = executor.submit(task);
        try {
            return future.get();
        } catch (Exception e) {
            Log.e("Error", "Lỗi khi lấy kết quả sự kiện", e);
            return new ArrayList<>();
        }
    }

    public void deleteEvent(String id) {
        executor.execute(() -> {
            try {
                service.events().delete("primary", id).execute();
            } catch (IOException e) {
                Log.e("Error", "Lỗi khi xóa sự kiện", e);
            }
        });
    }

    public void editEvent(String id, String title, String description, String startTime, String endTime, String date) {
        executor.execute(() -> {
            try {
                Event event = service.events().get("primary", id).execute();
                event.setSummary(title);
                event.setDescription(description);
                SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

                Date parsedDate = inputFormat.parse(date);
                String formattedDate = outputFormat.format(parsedDate);
                if (!startTime.equals("N/A") && !endTime.equals("N/A")) {

                    SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                    dateTimeFormat.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));

                    Date startDateTime = dateTimeFormat.parse(formattedDate + " " + startTime);
                    Date endDateTime = dateTimeFormat.parse(formattedDate + " " + endTime);

                    EventDateTime startEventDateTime = new EventDateTime()
                            .setDateTime(new com.google.api.client.util.DateTime(startDateTime))
                            .setTimeZone("UTC");

                    EventDateTime endEventDateTime = new EventDateTime()
                            .setDateTime(new com.google.api.client.util.DateTime(endDateTime))
                            .setTimeZone("UTC");

                    event.setStart(startEventDateTime);
                    event.setEnd(endEventDateTime);
                } else {
                    EventDateTime startEventDateTime = new EventDateTime()
                            .setDate(new com.google.api.client.util.DateTime(formattedDate))
                            .setTimeZone("UTC");
                    event.setStart(startEventDateTime);
                    event.setEnd(startEventDateTime);
                }
                service.events().update("primary", id, event).execute();

            } catch (Exception e) {
                Log.e("Error", "Lỗi khi xóa sự kiện", e);
            }
        });
    }

    public void addEvent(String title, String description, String startTime, String endTime, String date) {
        executor.execute(() -> {
            try {
                UUID uuid = UUID.randomUUID();
                Event event = new Event();
                String eventId = uuid.toString().replace("-", "");
                event.setId(eventId);
                event.setSummary(title);
                event.setDescription(description);
                SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

                Date parsedDate = inputFormat.parse(date);
                String formattedDate = outputFormat.format(parsedDate);
                if (!startTime.equals("N/A") && !endTime.equals("N/A")) {
                    SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                    dateTimeFormat.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));

                    Date startDateTime = dateTimeFormat.parse(formattedDate + " " + startTime);
                    Date endDateTime = dateTimeFormat.parse(formattedDate + " " + endTime);

                    EventDateTime startEventDateTime = new EventDateTime()
                            .setDateTime(new com.google.api.client.util.DateTime(startDateTime))
                            .setTimeZone("UTC");

                    EventDateTime endEventDateTime = new EventDateTime()
                            .setDateTime(new com.google.api.client.util.DateTime(endDateTime))
                            .setTimeZone("UTC");

                    event.setStart(startEventDateTime);
                    event.setEnd(endEventDateTime);
                } else {
                    EventDateTime startEventDateTime = new EventDateTime()
                            .setDate(new com.google.api.client.util.DateTime(formattedDate))
                            .setTimeZone("UTC");
                    event.setStart(startEventDateTime);
                    event.setEnd(startEventDateTime);
                }
                service.events().insert("primary", event).execute();

            } catch (Exception e) {
                Log.e("Error", "Lỗi khi xóa sự kiện", e);
            }
        });
    }
}
