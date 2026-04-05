# MAMS Scheduler App User Guide

## What this app does

MAMS Scheduler App helps you build one school week at a time. You can:

- create and save weekly schedules
- generate day layouts from built-in templates
- import or enter teacher requests
- manually adjust individual blocks
- add notes and clubs
- publish the week to an HTML page

The app saves schedule data in `schedule.json` and writes the HTML preview to `output_java.html`.

## Before you start

The app currently works with these built-in classes and teachers:

- `Math` - `Durost`
- `Physics` - `Chase`
- `CS` - `Taricco`
- `STEM` - `Crowthers`
- `Hum` - `Small`
- `Lang` - `Wildfong`

Teacher requests are matched to classes by teacher name, so the teacher name in a request must match one of the built-in teacher names above.

If you are running the app yourself, open the packaged app image if one was provided. If you are running from source, see the setup steps in `README.md`.

## Opening the app

When the app starts, it opens the **Week Selector** window.

From there you can:

- `Open` an existing week
- `New` to create the next week
- `Delete` to remove a saved week
- `Cancel` to exit the app

New weeks are created Monday through Friday. The app automatically carries the next day number forward from the most recently saved week.

## Recommended workflow

### 1. Create or open a week

Open an existing week if you are editing something you already saved. Use `New` if you are building the next week.

### 2. Choose how you want to build the week

You have three main ways to start:

- `Quick Generate`: fastest way to set templates for all five days at once
- `Generate Template`: fills the selected day automatically from the current template, classes, sections, and requests
- `Insert Blank Template`: inserts the time structure for the selected day without filling every class slot

Use `Quick Generate` when you want the app to build most of the week for you.

Use `Insert Blank Template` when you want the daily structure first and plan to assign classes manually.

### 3. Review one day at a time

Use the day dropdown near the top of the editor to move between Monday through Friday.

For each day, check:

- the selected template
- the section group
- whether the day should use split scheduling
- the class list for that day
- any requests, notes, or clubs

### 4. Save your work

Click `Save` when you want to keep the week. The app writes the week into `schedule.json`.

Important:

- there is no auto-save
- `Open HTML` previews the week but does not save it to `schedule.json`
- if you close or cancel without saving, your edits for that week are lost

## Week Editor controls

### Day selector

The day dropdown lets you switch between the five days in the current week.

### Open HTML

Creates `output_java.html` and opens it in your browser. Use this to preview or share the formatted week.

### Quick Generate

Opens a dialog where you can choose a template for each day, choose classes for each day, and optionally enable split scheduling.

Default template choices are:

- Monday: `Class Meeting Day`
- Tuesday: `Homeroom Day`
- Wednesday: `Flex Day`
- Thursday: `PE Day`
- Friday: `Class Meeting Day`

### Template dropdown

Built-in templates are:

- `Class Meeting Day`
- `Homeroom Day`
- `Flex Day`
- `PE Day`
- `No School`

### View Requests

Opens the request manager for the current week. You can load requests from CSV or add them manually.

### Generate

Rebuilds the selected day around the blocks you currently have on screen. This is useful after you:

- leave some sections as `(Open)`
- manually assign some blocks
- change block types or times
- load requests after a day already exists

### Generate Template

Builds a complete day from the selected template. It uses the day's current classes, sections, split setting, and loaded requests.

### Section selector

Lets you choose between:

- `RGB`
- `XYZ`

### Split and partner split class

Turn on `Split` when the day should use split scheduling. Then choose the partner split class.

Current split behavior:

- `Lang` is the primary split class
- the middle section is the one that gets split
- the split section is shown as `Intermediate` and `Advanced`

If the selected partner class is not included in that day's class list, split generation will be disabled for that day.

### Insert Blank Template

Builds the day's time structure from the selected template without fully assigning every class. This is useful when you want a starting layout and plan to fine-tune the schedule manually.

### Edit Classes

Lets you choose which classes are available for the selected day. Only the checked classes can be used when generating that day.

### Clear Classes

Removes class assignments from class blocks but keeps the day structure and timing.

### Clear All

Resets the selected day to a single open block starting at `7:45`.

### Notes and Clubs

At the bottom of the editor, you can enter:

- `Notes`
- `Clubs`

Enter one item per line. These appear in the footer of the HTML output.

## Editing blocks manually

Each row in the main editor represents one block.

For each row you can:

- change the block type
- change the start time in 15-minute steps
- use `+` to insert a block after the current row
- use `-` to delete the current row

Available block types are:

- `All School`
- `Sections`
- `Split Section`
- `PE`

### All School blocks

Use these for items like:

- `Lunch`
- `Homeroom`
- `Class Meeting`
- `Flex`
- custom all-school events

You can also enter an optional reason.

### Sections blocks

Use these when you want to assign one class to each section.

### Split Section blocks

Use these when one section should be split into two half-sections. The middle section is split into `Intermediate` and `Advanced`.

### PE blocks

Use these to set PE group names and activities.

## Working with teacher requests

Open `View Requests` to manage requests for the current week.

From that window you can:

- `Load Requests` from a CSV file
- `Add` a request manually
- `Edit` an existing request
- `Delete` a request

Manual requests use minutes for the length field. CSV imports use hours.

### Request types

The app supports two request types:

- `All School`: turns the nearest class block into an all-school block for that teacher's class
- `Avoid`: prevents that teacher's class from being scheduled during the requested time

### CSV format

The default request file is:

- `Teacher Request Form.csv`

Expected columns:

```csv
Timestamp,Teacher,Date,Type of Request,Time,Length (hours),Reason
```

Example:

```csv
"2025/12/01 9:19:15 PM EST","Small","2025-12-01","All School","08:45","1","Satire Project"
"2025/12/02 12:37:46 PM EST","Wildfong","2025-12-02","Avoid","07:45","3.5",""
```

Supported request values:

- teacher names must match the app's built-in teacher list
- date can be `yyyy-mm-dd` or common `m/d/yyyy` formats
- time can be `08:45`, `8:45 AM`, or similar standard time values
- length is entered in hours in the CSV

When you use `Load Requests`, the imported requests are merged into the week's existing requests. Exact duplicates are not added twice.

Automatic generation note:

- `Quick Generate`, `Generate Template`, and `Insert Blank Template` also read from the default `Teacher Request Form.csv` file
- if you rely on automatic request loading, keep that file up to date

## Files you should know about

- `schedule.json`: your saved schedules
- `Teacher Request Form.csv`: default request import file
- `output_java.html`: generated browser-friendly version of the current week
- `style.css`: styling for the HTML output

## Tips and troubleshooting

- Save often. The app does not auto-save.
- If a request does not apply, check the teacher name first.
- If split generation is skipped, make sure the partner split class is included in that day's selected classes.
- If you want the app to do most of the work, start with `Quick Generate`.
- If you want more manual control, start with `Insert Blank Template`.
- Use `Open HTML` any time you want a preview of the current week layout.
