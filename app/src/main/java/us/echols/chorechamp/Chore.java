package us.echols.chorechamp;

import android.support.annotation.Nullable;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;

public class Chore {

    private static final DateFormat FULL_DATE_TIME_FORMAT = new SimpleDateFormat("EEEE, MMMM d 'at' h:mma");
    private static final DateFormat SHORT_TIME_FORMAT = new SimpleDateFormat("h:mm a");

    private long id;
    private final String name;
    private final String childName;
    private final long timeInMillis;
    private final boolean isRecurring;
    private boolean[] weekdays;
    private StatusType status = StatusType.ACTIVE;

    public enum StatusType {
        ACTIVE(0), PENDING(1), COMPLETE(2);

        final int value;

        StatusType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        @Nullable
        public static StatusType getEnum(int value) {
            for (StatusType r : StatusType.values()) {
                if (r.getValue() == value) {
                    return r;
                }
            }
            return null;
        }
    }

    public Chore(long id, String name, String childName, long timeInMillis) {
        this.id = id;
        this.name = name;
        this.childName = childName;
        this.isRecurring = false;
        this.timeInMillis = timeInMillis;
    }

    public Chore(long id, String name, String childName, long timeInMillis, boolean[] weekdays) {
        this.id = id;
        this.name = name;
        this.childName = childName;
        this.isRecurring = true;
        this.timeInMillis = timeInMillis;
        this.weekdays = weekdays;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getChildName() {
        return childName;
    }

    public long getTimeInMillis() {
        return timeInMillis;
    }

    public boolean isRecurring() {
        return isRecurring;
    }

    public boolean[] getWeekdays() {
        return weekdays;
    }

    public long getId() {
        return id;
    }

    public void setStatus(StatusType status) {
        this.status = status;
    }

    public StatusType getStatus() {
        return status;
    }

    public void approve() {
        int statusInt = status.getValue();
        if (statusInt++ < 2) {
            status = StatusType.getEnum(statusInt);
        }
    }

    public String getNextDue() {
        Calendar due = Calendar.getInstance();
        due.setTimeInMillis(timeInMillis);

        String[] strDays = new String[]{"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        StringBuilder builder = new StringBuilder();

        if (isRecurring) {
            int nextDueWeekday = getNextWeekday();
            Calendar c = Calendar.getInstance();
            int currentWeekday = c.get(Calendar.DAY_OF_WEEK);

            if (currentWeekday == nextDueWeekday) {
                builder.append("Today");
            } else if (currentWeekday == nextDueWeekday - 1) {
                builder.append("Tomorrow");
            } else {
                builder.append(strDays[nextDueWeekday]);
            }

            builder.append(" at ").append(SHORT_TIME_FORMAT.format(due.getTime()));

        } else {
            builder.append(FULL_DATE_TIME_FORMAT.format(due.getTime()));
        }

        return builder.toString();
    }

    private int getNextWeekday() {

        Calendar now = Calendar.getInstance();
        int currentHour = now.get(Calendar.HOUR_OF_DAY);
        int currentMinute = now.get(Calendar.MINUTE);
        int currentSeconds = now.get(Calendar.SECOND);
        int currentTime = currentSeconds + 60 * (currentMinute + (60 * currentHour));

        Calendar due = Calendar.getInstance();
        due.setTimeInMillis(timeInMillis);
        int dueHour = due.get(Calendar.HOUR_OF_DAY);
        int dueMinute = due.get(Calendar.MINUTE);
        int dueSeconds = due.get(Calendar.SECOND);
        int dueTime = dueSeconds + 60 * (dueMinute + (60 * dueHour));

        int pastTodayFlag = 0;
        if (currentTime > dueTime) {
            pastTodayFlag = 1;
        }
        int currentWeekday = now.get(Calendar.DAY_OF_WEEK);
        int nextDueWeekday = 1;
        for (int i = 0; i < 7; i++) {
            if (weekdays[i] && currentWeekday - 1 <= i) {
                nextDueWeekday = i + 1 + pastTodayFlag;
                break;
            }
        }
        return nextDueWeekday;
    }

    public boolean isOverdue(Calendar now) {
        boolean result = false;

        Calendar due = Calendar.getInstance();
        due.setTimeInMillis(timeInMillis);

        if (isRecurring) {
            int weekdayNow = now.get(Calendar.DAY_OF_WEEK);
            for (int w = 1; w <= weekdays.length; w++) {
                if(weekdays[w - 1] && weekdayNow == w) {
                    int hourDue = due.get(Calendar.HOUR_OF_DAY);
                    int minuteDue = due.get(Calendar.MINUTE);
                    int secondDue = due.get(Calendar.SECOND);
                    int timeDue = secondDue + 60 * (minuteDue + 60 * hourDue);
                    int hourNow = now.get(Calendar.HOUR_OF_DAY);
                    int minuteNow = now.get(Calendar.MINUTE);
                    int secondNow = now.get(Calendar.SECOND);
                    int timeNow = secondNow + 60 * (minuteNow + 60 * hourNow);
                    result = timeNow >= timeDue;
                }
            }
        } else {
            result = now.getTimeInMillis() >= timeInMillis;
        }

        return result;
    }

    public static class ChoreComparator implements Comparator<Chore> {
        @Override
        public int compare(Chore o1, Chore o2) {
            if (o1.isRecurring) {
                if (o2.isRecurring) {
                    int thisWeekday = o1.getNextWeekday();
                    int otherWeekday = o2.getNextWeekday();
                    return thisWeekday > otherWeekday ? 1 : -1;
                } else {
                    return -1;
                }
            } else {
                if (o2.isRecurring) {
                    return 1;
                } else {
                    if (o1.getTimeInMillis() == o2.getTimeInMillis()) {
                        return 0;
                    } else {
                        return o1.getTimeInMillis() > o2.getTimeInMillis() ? 1 : -1;
                    }
                }
            }
        }
    }

}
