module Matrix {
    requires transitive javafx.fxml;
    requires transitive javafx.base;
    requires transitive javafx.graphics;
    requires transitive javafx.controls;
    requires transitive java.sql;
    requires transitive gson;
    requires transitive commons.lang3;
    exports com.db to javafx.graphics, javafx.fxml;
    opens com.db to javafx.fxml, javafx.base;
}