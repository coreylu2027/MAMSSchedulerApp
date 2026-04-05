# MAMS Scheduler App

MAMS Scheduler App is a Java desktop app for building weekly school schedules, loading teacher requests, and publishing an HTML version of the week for sharing.

The full end-user guide lives in [docs/USER_GUIDE.md](docs/USER_GUIDE.md).

## Launching the app

### Option 1: Run the packaged macOS app

If you already have a built app image, open:

- `dist/MAMSSchedulerApp.app`

### Option 2: Build and run from the command line

This project targets Java 22.

```bash
mvn package
java -jar target/MAMSSchedulerApp-1.0-SNAPSHOT.jar
```

### Option 3: Run from an IDE

Run the `main` method in:

- `edu.mams.app.forms.WeekSelector`

## Files the app uses

- `schedule.json`: saved weeks
- `Teacher Request Form.csv`: default teacher-request import file
- `output_java.html`: generated HTML preview/export
- `style.css`: styling for the HTML output

## For users

If you want instructions for creating a week, generating days, loading requests, using split schedules, and exporting HTML, start here:

- [User Guide](docs/USER_GUIDE.md)
