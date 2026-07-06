# Jmix Commercial Add-ons Demo

This project lets you see Jmix commercial add-ons in action.

If you cloned this project directly from the GitHub repository, you need an active Enterprise license to open it in the IDE and run it. In this case, the commercial add-on artifacts will be downloaded from the `https://global.repo.jmix.io/repository/premium` repository.

If you downloaded this project as an [Enterprise Trial](https://docs.jmix.io/jmix/account-management.html#enterprise-trial) from your Jmix account page, it includes the `trial_repository` folder with the trial add-on artifacts. In this case, these artifacts are used, so you don't need an active Enterprise license.

Open the root project in the IDE with the Jmix Studio plugin installed and refer to each subproject’s README file for instructions on running them:

- [BPM](demo/bpm/README.md)
- [Business Calendars](demo/business-calendars/README.md)
- [Dynamic Model](demo/dynamic-model/README.md)
- [Kanban](demo/kanban/README.md)
- [Grouping Data Grid](demo/grouping-data-grid/README.md)
- [Maps](demo/maps/README.md)
- [Notifications](demo/notifications/README.md)
- [Tabbed Application Mode](demo/tabbed-mode/README.md)
- [UI Constraints](demo/uiconstraints/README.md)
- [WebDAV](demo/webdav/README.md)

To test the commercial add-ons in another project, copy the `trial_repository` folder from this project. Then, modify your `build.gradle` file to use the trial artifacts, as it is done in this project.

Please note that trial versions of commercial add-ons expire 28 days after requesting the trial Enterprise subscription. Applications using expired trial add-ons will not run.