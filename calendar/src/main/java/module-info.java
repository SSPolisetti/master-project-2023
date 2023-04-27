module com.polisetti_won {
    requires transitive javafx.graphics;
    //requires fr.brouillard.oss.cssfx;
    requires transitive javafx.controls;
    requires transitive com.calendarfx.view;
    requires java.sql;
    requires javafx.fxml;

    opens com.polisetti_won to javafx.fxml;
    exports com.polisetti_won;
}