module com.udacity.catpoint.securityservice {
    requires java.desktop;
    requires java.prefs;

    requires com.google.common;
    requires com.google.gson;

    requires org.slf4j;

    requires com.udacity.catpoint.imageservice;

    exports com.udacity.catpoint.application;
    exports com.udacity.catpoint.data;
    exports com.udacity.catpoint.service;

    opens com.udacity.catpoint.data to com.google.gson;
}
