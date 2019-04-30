package com.db;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import static com.db.Matrix.*;

public class addAssembly {

    @FXML
    Button ButtonAddAssembly, ButtonAddAssemblyDone;
    @FXML
    TextField TextAssemblyName, TextAssemblyCount;
    @FXML
    RadioButton RadioButtonMax, RadioButtonExact, RadioButtonRequired, RadioButtonUnlimited;
    @FXML
    ToggleGroup CountGroup;
    @FXML
    Label LabelAddAssemblyStatus;


    public void initialize() {

    }
    public void ButtonAddAssemblyDoneOnAction() {
        Stage stage = (Stage) ButtonAddAssemblyDone.getScene().getWindow();
        stage.close();
    }
    public void ButtonAddAssemblyOnAction() {
        int assemblyCount;
        String countType = CountGroup.getSelectedToggle().getUserData().toString();
        String assemblyName = TextAssemblyName.getText();
        if (assemblyName.isEmpty() || countType.isEmpty() && ! RadioButtonUnlimited.isSelected()  || TextAssemblyName.getText().isEmpty()) {
            LabelAddAssemblyStatus.setText("Fields cannot be NULL");
            return;
        }
        try {
        if (! RadioButtonUnlimited.isSelected())
            assemblyCount = Integer.parseInt(TextAssemblyCount.getText());
        else
            assemblyCount = 0;

            String SQL = "INSERT INTO ASSEMBLY (NAME, CONTENT, SELECTOR) VALUES  ('" +
                    assemblyName + "', " + assemblyCount  + ", '" + countType + "')";
            sr.executeUpdate(SQL);

            Leaf addBond = new Leaf(assemblyName, assemblyCount, countType);
            TreeItem<Leaf> nodeItem = new TreeItem<>(addBond);
            assemblyRootItem.getChildren().add(nodeItem);
            assemblyHash.put(assemblyName, nodeItem);
            LabelAddAssemblyStatus.setText("Assembly Addition Success");
            LabelAddAssemblyStatus.setStyle("-fx-text-fill: orange");

        }
        catch(SQLException ex) {
            LabelAddAssemblyStatus.setText(ex.getMessage());
            LabelAddAssemblyStatus.setStyle("-fx-text-fill: red");
        }

        catch (NumberFormatException ex){
            LabelAddAssemblyStatus.setText("Non Integer Count Input");
            LabelAddAssemblyStatus.setStyle("-fx-text-fill: red");
        }
    }
}