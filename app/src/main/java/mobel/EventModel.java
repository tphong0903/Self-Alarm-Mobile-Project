package mobel;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.EventDateTime;

public class EventModel implements Parcelable {
    private String id;
    private String summary;
    private String description;
    private String eventType;
    private String startTime;
    private String endTime;
    private String originalStartTime;

    private int typeDateTime;

    public EventModel(String id, String summary, String description, String eventType, EventDateTime startTime, EventDateTime endTime, EventDateTime originalStartTime, int typeDateTime) {
        this.summary = summary;
        this.id = id;
        this.description = description;
        this.eventType = eventType;

        this.startTime = (startTime != null) ?
                (startTime.getDateTime() != null ? startTime.getDateTime().toStringRfc3339() : startTime.getDate().toStringRfc3339())
                : "N/A";

        this.endTime = (endTime != null) ?
                (endTime.getDateTime() != null ? endTime.getDateTime().toStringRfc3339() : endTime.getDate().toStringRfc3339())
                : "N/A";

        this.originalStartTime = (originalStartTime != null) ?
                (originalStartTime.getDateTime() != null ? originalStartTime.getDateTime().toStringRfc3339() : originalStartTime.getDate().toStringRfc3339())
                : "N/A";

        this.typeDateTime = typeDateTime;
    }

    protected EventModel(Parcel in) {
        summary = in.readString();
        id = in.readString();
        description = in.readString();
        eventType = in.readString();
        startTime = in.readString();
        endTime = in.readString();
        originalStartTime = in.readString();
        typeDateTime = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(summary);
        dest.writeString(id);
        dest.writeString(description);
        dest.writeString(eventType);
        dest.writeString(startTime);
        dest.writeString(endTime);
        dest.writeString(originalStartTime);
        dest.writeInt(typeDateTime);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<EventModel> CREATOR = new Creator<EventModel>() {
        @Override
        public EventModel createFromParcel(Parcel in) {
            return new EventModel(in);
        }

        @Override
        public EventModel[] newArray(int size) {
            return new EventModel[size];
        }
    };

    public String getSummary() {
        return summary;
    }

    public String getID() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getEventType() {
        return eventType;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getOriginalStartTime() {
        return originalStartTime;
    }

    public int getTypeDateTime() {
        return typeDateTime;
    }

    public EventDateTime getStartTimeAsEventDateTime() {
        return new EventDateTime().setDateTime(DateTime.parseRfc3339(startTime));
    }

    public EventDateTime getEndTimeAsEventDateTime() {
        return new EventDateTime().setDateTime(DateTime.parseRfc3339(endTime));
    }

    public EventDateTime getOriginalStartTimeAsEventDateTime() {
        return new EventDateTime().setDateTime(DateTime.parseRfc3339(originalStartTime));
    }
}
