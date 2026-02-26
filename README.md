current steps for testing:
1) run the WeekEdit.java main method
2) a ui should appear allowing you to select / replace classes.
3) setting a class to open and clicking generate will solve / replace the open blocks
4) this will not save the day. clicking save will save the day into object storage (persistent memory is not implemented yet) and update the html output
5) request form can be accessed here:
6) public view: https://forms.gle/Hx5vzp1UCmz4iK7WA
7) note i believe clicking download responses from the private view and downloading the csv from the google sheets file output two different csvs. i believe both are supported but this hasn't been tested fully yet. the name of the file can be changed in RequestLoader.java (final constant).
8) things that still need to be implemented.
9) persistent storage across runs, likely using a json, txt, or xmll file (done)
10) selecting templates for each day and generating. likely using a week view rather than a per day view. currently the template is set in the Tester.java file (done)
11) implementing splits. this is likely to be accomplished by setting two blocks with lang and the other split class to be filled, and then generating the schedule around that (done)
12) PE data and graphic support
