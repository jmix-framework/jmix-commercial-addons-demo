# Jmix Business Calendars Demo

## Overview 

The project includes two business calendars:
- `Retail Business Calendar 2026` is defined in the [RetailCalendar2026](src/main/java/com/company/businesscalendars/RetailCalendar2026.java) class using annotations.
- `Standard Corporate Calendar 2026` is defined in the database.

[BusinessCalendarTestView](src/main/java/com/company/businesscalendars/view/businesscalendartest/BusinessCalendarTestView.java) demonstrates usage of business calendars API. It is available via the *Application → Business calendar test* menu item.

## Demo Scenario

1. Run the application and go to <http://localhost:8102> in your browser.
2. Log in as `admin` with password `admin`.
3. Open *Business calendars → Business calendars* view. You will see both calendars. The one that is defined in the database can be modified at runtime.
4. Open the *Application → Business calendar test* menu item. Select a calendar, enter a date and time and click the *Test* button.

## See Also

- [Business Calendars Documentation](https://docs.jmix.io/jmix/business-calendar/index.html)
- [Business Calendars on Marketplace](https://www.jmix.io/marketplace/business-calendars/)
