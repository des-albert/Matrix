package com.db;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;

import static com.db.Matrix.*;

public class updateComponent {

    @FXML
    Button ButtonUpdateComponentClose, ButtonUpdateComponent;
    @FXML
    TextField TextComponentCode, TextComponentDescription, TextCategory, TextTotalCount_A, TextTotalCount_B, TextTotalCount_C, TextTotalCount_D;
    @FXML
    ComboBox<String> comboTotal_A, comboTotal_B, comboTotal_C, comboTotal_D;
    @FXML
    Label LabelUpdateStatus;
    private String code;
    private Leaf component;
    private String countA, countB, countC, countD;
    public void initialize() {
        TextComponentCode.setEditable(false);
        component = selectItem.getValue();
        code = component.Code;
        TextComponentCode.setText(code);
        TextComponentDescription.setText(component.Description);
        TextComponentDescription.setEditable(true);
        TextCategory.setText(component.Category);
        TextCategory.setEditable(true);
        TextTotalCount_A.setText(null);
        TextTotalCount_B.setText(null);
        TextTotalCount_C.setText(null);
        TextTotalCount_D.setText(null);

        String SQL = "SELECT DISTINCT NAME FROM SUMMARY";
        String[] totals;
        try {
            ResultSet rs = sr.executeQuery(SQL);
            rs.last();
            totals = new String[rs.getRow()];
            rs.beforeFirst();

            int i = 0;
            while (rs.next()) {
                totals[i++] = rs.getString("NAME");
            }
            comboTotal_A.getItems().addAll(totals);
            comboTotal_B.getItems().addAll(totals);
            comboTotal_C.getItems().addAll(totals);
            comboTotal_D.getItems().addAll(totals);
        }
        catch(SQLException ex){
            LabelUpdateStatus.setText(ex.getMessage());
        }
        SQL = "SELECT SUM_NAME, INCREMENT FROM SUM_LINK WHERE COMPONENT_CODE = '" + code + "' FETCH FIRST 4 ROWS ONLY";

        try {
            ResultSet rs = sr.executeQuery(SQL);
            String totalName;
            int i = 0;
            while (rs.next()) {
                totalName = rs.getString("SUM_NAME");
                switch (i) {
                    case 0:
                        comboTotal_A.getSelectionModel().select(totalName);
                        comboTotal_A.setEditable(true);
                        countA = Integer.toString(rs.getInt("INCREMENT"));
                        TextTotalCount_A.setText(countA);
                        TextTotalCount_A.setEditable(true);
                        break;
                    case 1:
                        comboTotal_B.getSelectionModel().select(totalName);
                        comboTotal_B.setEditable(true);
                        countB = Integer.toString(rs.getInt("INCREMENT"));
                        TextTotalCount_B.setText(countB);
                        TextTotalCount_B.setEditable(true);
                        break;
                    case 2:
                        comboTotal_C.getSelectionModel().select(totalName);
                        comboTotal_C.setEditable(true);
                        countC = Integer.toString(rs.getInt("INCREMENT"));
                        TextTotalCount_C.setText(countC);
                        TextTotalCount_C.setEditable(true);
                        break;
                    case 3:
                        comboTotal_D.getSelectionModel().select(totalName);
                        comboTotal_D.setEditable(true);
                        countD = Integer.toString(rs.getInt("INCREMENT"));
                        TextTotalCount_D.setText(countD);
                        TextTotalCount_D.setEditable(true);
                        break;
                }
                ++i;
            }

        } catch (SQLException ex) {
            LabelUpdateStatus.setText(ex.getMessage());
        }
    }

    public void ButtonUpdateComponentCloseAction() {
        Stage stage = (Stage) ButtonUpdateComponentClose.getScene().getWindow();
        stage.close();
    }

      public void ButtonUpdateComponentAction() {

        String description = TextComponentDescription.getText();
        String category = TextCategory.getText();
        String SQL = "UPDATE COMPONENT SET DESCRIPTION = '" +  description + "', CATEGORY = '" +
                category + "' WHERE CODE = '" + code + "'";
        try {
            sr.executeUpdate(SQL);

            SQL = "DELETE FROM SUM_LINK WHERE COMPONENT_CODE = '" + code + "'";
            sr.executeUpdate(SQL);

            if(TextTotalCount_A.getText() != null) {
              SQL = "INSERT INTO SUM_LINK (COMPONENT_CODE, INCREMENT, SUM_NAME) VALUES ('" + code + "', " +
                      TextTotalCount_A.getText() + ", '" + comboTotal_A.getSelectionModel().getSelectedItem() + "')";
                sr.executeUpdate(SQL);
            }
            if(TextTotalCount_B.getText() != null) {
                SQL = "INSERT INTO SUM_LINK (COMPONENT_CODE, INCREMENT, SUM_NAME) VALUES ('" + code + "', " +
                        TextTotalCount_B.getText() + ", '" + comboTotal_B.getSelectionModel().getSelectedItem() + "')";
                sr.executeUpdate(SQL);
            }
            if(TextTotalCount_C.getText() != null) {
                SQL = "INSERT INTO SUM_LINK (COMPONENT_CODE, INCREMENT, SUM_NAME) VALUES ('" + code + "', " +
                        TextTotalCount_C.getText() + ", '" + comboTotal_C.getSelectionModel().getSelectedItem() + "')";
                sr.executeUpdate(SQL);
            }
            if(TextTotalCount_D.getText() != null) {
                SQL = "INSERT INTO SUM_LINK (COMPONENT_CODE, INCREMENT, SUM_NAME) VALUES ('" + code + "', " +
                        TextTotalCount_D.getText() + ", '" + comboTotal_D.getSelectionModel().getSelectedItem() + "')";
                sr.executeUpdate(SQL);
            }
        }
        catch (SQLException ex) {
            LabelUpdateStatus.setText(ex.getMessage());
            return;
        }

        /* Update Atom Tree */

        TreeItem<Leaf> parentItem;

        if (! component.Category.equals(category)) {
            component.Category = category;
            if ( (parentItem = componentHash.get(category)) == null) {
                Leaf parent = new Leaf(category);
                parentItem = new TreeItem<>(parent);
                componentHash.put(category, parentItem);
                componentRootItem.getChildren().add(parentItem);
            }
            InputStream iconStream = getClass().getResourceAsStream("/img/" + category  + ".png");
            if (iconStream != null) {
                Image icon = new Image(iconStream);
                selectItem.setGraphic(new ImageView(icon));
            }
            selectItem.getParent().getChildren().remove(selectItem);
            parentItem.getChildren().add(selectItem);
        }
        selectItem.setValue(component);

        LabelUpdateStatus.setText("Component Update Success");

    }
}
