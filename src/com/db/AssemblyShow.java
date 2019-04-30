package com.db;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import static com.db.Matrix.selectItem;

public class AssemblyShow {

    @FXML
    Button buttonAssemblyClose;
    @FXML
    Label labelComponent;
    @FXML
    TableView<AssemblyInfo> AssemblyTableView;
    @FXML
    TableColumn<AssemblyInfo, String> columnAssemblyName, columnAssemblyType;
    @FXML
    TableColumn<AssemblyInfo, Integer> columnAssemblyMaxCount, columnAssemblyContents;

    private ObservableList<AssemblyInfo> data = FXCollections.observableArrayList();

    public void initialize() {

        columnAssemblyName.setCellValueFactory(new PropertyValueFactory<>("assemblyNameColumn"));
        columnAssemblyMaxCount.setCellValueFactory(new PropertyValueFactory<>("assemblyMaxColumn"));
        columnAssemblyType.setCellValueFactory(new PropertyValueFactory<>("assemblyTypeColumn"));
        columnAssemblyContents.setCellValueFactory(new PropertyValueFactory<>("assemblyQtyColumn"));

        columnAssemblyMaxCount.setStyle("-fx-alignment: CENTER-RIGHT;");
        columnAssemblyType.setStyle("-fx-alignment: CENTER;");
        columnAssemblyContents.setStyle("-fx-alignment: CENTER-RIGHT;");

        Leaf leafNode = selectItem.getValue();
        labelComponent.setText("Assembly Status of " + leafNode.Description);
        int num_Assemblies = leafNode.assemblyCount;
        if (num_Assemblies > 0) {
            AssemblyTableView.setItems(data);
            for (int i = 0; i < num_Assemblies; i++) {
                int max = leafNode.assemblyMax[i];
                String assembly = "M";
                if (max < 0) {
                    max = -max;
                    assembly = "E";
                }
                AssemblyInfo row = new AssemblyInfo(leafNode.assemblyName[i], max, assembly, leafNode.assemblyQty[i]);
                data.add(row);
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Part has no Assemblies");
            alert.showAndWait();


        }
    }

    public void ButtonAssemblyCloseOnAction() {
            Stage stage = (Stage) buttonAssemblyClose.getScene().getWindow();
            stage.close();
    }
}


