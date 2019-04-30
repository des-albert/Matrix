package com.db;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;

import static com.db.Matrix.*;

public class addComponent {
    private Common com;
    @FXML
    Button ButtonAddPartDone, ButtonAddPart;
    @FXML
    Label LabelAddStatus;
    @FXML
    TextField TextPartCode, TextPartDescription, TextPartCat;
    @FXML
    TextField Text_Count_A, Text_Count_B, Text_Count_C, Text_Count_D;
    @FXML
    ComboBox<String> combo_Total_A, combo_Total_B, combo_Total_C, combo_Total_D;

    public void initialize() {
                com = new Common();
                Leaf select = selectItem.getValue();
                TextPartCat.setText(select.Category);
                LabelAddStatus.setText("");
                combo_Total_A.setItems(summary_name);
                combo_Total_B.setItems(summary_name);
                combo_Total_C.setItems(summary_name);
                combo_Total_D.setItems(summary_name);
            }
            public void ButtonAddPartAction() {

                String description = TextPartDescription.getText();
                String category = TextPartCat.getText();
                String code = TextPartCode.getText();

                if (code.isEmpty() || description.isEmpty() || category.isEmpty() ) {
                    LabelAddStatus.setText("Fields cannot be NULL");
                    return;
                }

                String SQL = "INSERT INTO COMPONENT (CODE, DESCRIPTION, CATEGORY) VALUES ('" +
                        code.toUpperCase() + "', '" + description + "', '" + category + "')";

                try {
                    sr.executeUpdate(SQL);
                    TreeItem<Leaf> parentItem = selectItem;
                    if (categoryHash.get(category) == null) {
                Leaf parent = new Leaf(category);
                parentItem = new TreeItem<>(parent);
                categoryHash.put(category, parentItem);
                componentRootItem.getChildren().add(parentItem);
            }
            Leaf node = new Leaf(code, TextPartDescription.getText(), category );
            TreeItem<Leaf> nodeItem = com.newItem(node);
            parentItem.getChildren().add(nodeItem);
            componentHash.put(code, nodeItem);

            if ( !combo_Total_A.getSelectionModel().isEmpty()) {
                String selection_A = combo_Total_A.getSelectionModel().getSelectedItem();
                if (!summary_name.contains(selection_A)) {
                    summary_name.add(selection_A);
                    SQL = "INSERT INTO SUMMARY (NAME) VALUES ('" + selection_A + "')";
                    sr.execute(SQL);
                }

                SQL = "INSERT INTO SUM_LINK (COMPONENT_CODE, INCREMENT, SUM_NAME) VALUES ('" + code + "', " +
                            Text_Count_A.getText() + ", '" + selection_A + "')";
                sr.execute(SQL);
            }
            if ( !combo_Total_B.getSelectionModel().isEmpty()) {
                String selection_B= combo_Total_B.getSelectionModel().getSelectedItem();
                if (!summary_name.contains(selection_B)) {
                    summary_name.add(selection_B);
                    SQL = "INSERT INTO SUMMARY (NAME) VALUES ('" + selection_B + "')";
                    sr.execute(SQL);
                }

                SQL = "INSERT INTO SUM_LINK (COMPONENT_CODE, INCREMENT, SUM_NAME) VALUES ('" + code + "', " +
                        Text_Count_A.getText() + ", '" + selection_B + "')";
                sr.execute(SQL);
            }
            if ( !combo_Total_C.getSelectionModel().isEmpty()) {
                String selection_C = combo_Total_C.getSelectionModel().getSelectedItem();
                if (!summary_name.contains(selection_C)) {
                    summary_name.add(selection_C);
                    SQL = "INSERT INTO SUMMARY (NAME) VALUES ('" + selection_C + "')";
                    sr.execute(SQL);
                }

                SQL = "INSERT INTO SUM_LINK (COMPONENT_CODE, INCREMENT, SUM_NAME) VALUES ('" + code + "', " +
                        Text_Count_A.getText() + ", '" + selection_C + "')";
                sr.execute(SQL);
            }
            if ( !combo_Total_D.getSelectionModel().isEmpty()) {
                String selection_D = combo_Total_D.getSelectionModel().getSelectedItem();
                if (!summary_name.contains(selection_D)) {
                    summary_name.add(selection_D);
                    SQL = "INSERT INTO SUMMARY (NAME) VALUES ('" + selection_D + "')";
                    sr.execute(SQL);
                }

                SQL = "INSERT INTO SUM_LINK (COMPONENT_CODE, INCREMENT, SUM_NAME) VALUES ('" + code + "', " +
                        Text_Count_A.getText() + ", '" + selection_D + "')";
                sr.execute(SQL);
            }

            LabelAddStatus.setText("Atom " + code + " Addition Success");
        }
        catch(SQLException ex){
            LabelAddStatus.setText(ex.getMessage());
        }
    }
    public void ButtonAddPartDoneAction() {
        Stage stage = (Stage) ButtonAddPartDone.getScene().getWindow();
        stage.close();
    }
}
