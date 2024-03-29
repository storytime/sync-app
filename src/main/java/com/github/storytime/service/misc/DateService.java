package com.github.storytime.service.misc;

import com.github.storytime.model.aws.AppUser;
import com.github.storytime.model.pb.jaxb.statement.response.ok.Response.Data.Info.Statements.Statement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.xml.datatype.XMLGregorianCalendar;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.SUNDAY;
import static java.time.Instant.ofEpochMilli;
import static java.time.Instant.ofEpochSecond;
import static java.time.LocalDateTime.of;
import static java.time.ZoneId.of;
import static java.time.ZonedDateTime.now;
import static java.time.ZonedDateTime.ofInstant;
import static java.time.temporal.TemporalAdjusters.firstDayOfMonth;
import static java.time.temporal.TemporalAdjusters.firstDayOfNextMonth;
import static java.util.Set.of;

@Component
public class DateService {

    public static final int ONE_DAY_IN_HRS = 24;
    public static final int ONE_DAY = 1;
    public static final int START_HOUR = 0;
    public static final int START_MIN = 0;
    public static final int START_SEC = 0;
    public static final int FIRST_DAT_OF_MO = 1;
    private static final Set<DayOfWeek> WEEKEND = of(SATURDAY, SUNDAY);
    private final DateTimeFormatter isoDateTimeFormatter;
    private final DateTimeFormatter pbDateTimeFormatter;
    private final DateTimeFormatter zenDateTimeFormatter;

    @Autowired
    public DateService(final DateTimeFormatter isoDateTimeFormatter,
                       final DateTimeFormatter zenDateTimeFormatter,
                       final DateTimeFormatter pbDateTimeFormatter) {
        this.isoDateTimeFormatter = isoDateTimeFormatter;
        this.pbDateTimeFormatter = pbDateTimeFormatter;
        this.zenDateTimeFormatter = zenDateTimeFormatter;
    }

    public String toPbFormat(final ZonedDateTime zonedDateTime) {
        return pbDateTimeFormatter.format(zonedDateTime);
    }

    public ZonedDateTime millisAwsUserDate(final Long millis, final AppUser u) {
        final Instant instant = ofEpochMilli(millis);
        return ofInstant(instant, of(u.getTimeZone()));
    }

    public ZonedDateTime secToUserDate(final Long secs, final AppUser u) {
        final Instant instant = ofEpochSecond(secs);
        return ofInstant(instant, of(u.getTimeZone()));
    }

    public String millisToIsoFormat(final Long millis, final AppUser u) {
        return isoDateTimeFormatter.format(millisAwsUserDate(millis, u));
    }

    public String millisToIsoFormat(final ZonedDateTime zonedDateTime) {
        return isoDateTimeFormatter.format(zonedDateTime);
    }

    public ZonedDateTime xmlDateTimeToZoned(final XMLGregorianCalendar d, final XMLGregorianCalendar t, final String timeZone) {
        return of(d.getYear(), d.getMonth(), d.getDay(), t.getHour(), t.getMinute(), t.getSecond()).atZone(of(timeZone));
    }

    public String toZenFormat(final XMLGregorianCalendar d,
                              final XMLGregorianCalendar t,
                              final String tz) {
        return zenDateTimeFormatter.format(xmlDateTimeToZoned(d, t, tz));
    }


    public ZonedDateTime getPrevMouthLastBusiness(final Statement s,
                                                  final String timeZone) {
        final XMLGregorianCalendar trandate = s.getTrandate();
        final ZonedDateTime startDate = getPbStatementZonedDateTime(timeZone, trandate).minusMonths(1);

        ZonedDateTime start = startDate.with(firstDayOfMonth());
        final ZonedDateTime end = startDate.with(firstDayOfNextMonth());
        final List<ZonedDateTime> businessDays = new ArrayList<>();

        while (start.isBefore(end)) {
            if (!WEEKEND.contains(start.getDayOfWeek())) {
                businessDays.add(start);
            }
            start = start.plusDays(ONE_DAY);
        }

        return businessDays.get(businessDays.size() - ONE_DAY);
    }

    public ZonedDateTime getPbStatementZonedDateTime(final String timeZone, final XMLGregorianCalendar trandate) {
        return of(trandate.getYear(), trandate.getMonth(), trandate.getDay(), START_HOUR, START_MIN, START_SEC)
                .atZone(of(timeZone));
    }

    public long getStartOfMouthInSeconds(int year, int mouth, final AppUser u) {
        return YearMonth.of(year, mouth)
                .atDay(FIRST_DAT_OF_MO)
                .atStartOfDay(of(u.getTimeZone()))
                .toInstant()
                .getEpochSecond();
    }

    public long getEndOfMouthInSeconds(int year, int mouth, final AppUser u) {
        return YearMonth.of(year, mouth)
                .atEndOfMonth()
                .atStartOfDay(of(u.getTimeZone()))
                .plusHours(ONE_DAY_IN_HRS)
                .toInstant()
                .getEpochSecond();
    }

    public long getUserStarDateInMillis(final AppUser appUser) {
        return now().withZoneSameInstant(of(appUser.getTimeZone())).toInstant().toEpochMilli();
    }
}
