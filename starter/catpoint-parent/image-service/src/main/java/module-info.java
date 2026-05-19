module com.udacity.catpoint.imageservice {
    requires java.desktop;

    requires org.slf4j;

    requires software.amazon.awssdk.services.rekognition;
    requires software.amazon.awssdk.auth;
    requires software.amazon.awssdk.core;
    requires software.amazon.awssdk.regions;

    requires com.google.gson;

    exports com.udacity.catpoint.image;
}
