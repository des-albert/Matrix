package com.db;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;

import static com.db.Matrix.*;

public class updateAssembly {

    @FXML
    Button ButtonUpdateAssembly, ButtonUpdateAssemblyDone;
    @FXML
    TextField TextAssemblyName, TextAssemblyContent;
    @FXML
    RadioButton RadioButtonMax, RadioButtonExact, RadioButtonRequired, RadioButtonUnlimited;
    @FXML
    ToggleGroup CountGroup;
    @FXML
    Label LabelAddAssemblyStatus;

    private String currentName;


    public void initialize() {

        currentName = selectItem.getValue().Name;
        TextAssemblyName.setText(currentName);
        TextAssemblyContent.setText(Integer.toString(selectItem.getValue().Content));
    }
    public void ButtonUpdateAssemblyDoneOnAction() {
        Stage stage = (Stage) ButtonUpdateAssemblyDone.getScene().getWindow();
        stage.close();
    }
    public void ButtonUpdateAssemblyOnAction() {

        String countType = CountGroup.getSelectedToggle().getUserData().toString();
        String assemblyName = TextAssemblyName.getText();
        if (assemblyName.isEmpty() || countType.isEmpty() && ! RadioButtonUnlimited.isSelected()  || TextAssemblyName.getText().isEmpty()) {
            LabelAddAssemblyStatus.setText("Fields cannot be NULL");
            return;
        }
        int assemblyContent;
        try {

        if (! RadioButtonUnlimited.isSelected())
            assemblyContent = Integer.parseInt(TextAssemblyContent.getText());
        else
            assemblyContent = 0;

            if (! currentName.equals(assemblyName)) {
                String SQL = "ALTER TABLE LINK DROP CONSTRAINT LINK_ASSEMBLY_NAME_FK";
                sr.execute(SQL);
                SQL = "UPDATE ASSEMBLY SET NAME = '" + assemblyName + "'," +
                        "CONTENT = " + assemblyContent + ", SELECTOR = '" + countType +
                        "' WHERE NAME = '" + currentName + "'";

                sr.executeUpdate(SQL);
                SQL = "UPDATE LINK SET ASSEMBLY_NAME = '" + assemblyName + "' WHERE ASSEMBLY_NAME = '" + currentName + "'";
                sr.executeUpdate(SQL);

                SQL = "ALTER TABLE LINK ADD CONSTRAINT LINK_ASSEMBLY_NAME_FK FOREIGN KEY (ASSEMBLY_NAME) REFERENCES ASSEMBLY (NAME)";
                sr.execute(SQL);

            }
            else {
                String SQL = "UPDATE ASSEMBLY SET CONTENT = " + assemblyContent + ", SELECTOR = '" + countType +
                        "' WHERE NAME = '" + assemblyName + "'";

                sr.executeUpdate(SQL);
            }
            selectItem.getValue().Name = assemblyName;
            assemblyHash.remove(currentName, selectItem);
            assemblyHash.put(assemblyName, selectItem);
            LabelAddAssemblyStatus.setText("Assembly Update Success");
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