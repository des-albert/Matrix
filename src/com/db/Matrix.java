package com.db;

import com.google.gson.Gson;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.lang3.ArrayUtils;

import java.io.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static com.db.Base.getPrimaryStage;

public class Matrix {

    static TreeItem<Leaf> selectItem, componentRootItem, assemblyRootItem;
    static ObservableList<String> summary_name = FXCollections.observableArrayList();
    static Statement sr, st;
    static HashMap<String, TreeItem<Leaf>> categoryHash = new HashMap<>();
    static HashMap<String, TreeItem<Leaf>> componentHash = new HashMap<>();
    static HashMap<String, TreeItem<Leaf>> assemblyHash = new HashMap<>();
    static HashMap<Integer, TreeItem<Leaf>> blockHash = new HashMap<>();
    private HashMap<String, Summary> sumHash = new HashMap<>();

    @FXML
    Label labelControlStatus, labelComponentStatus;
    @FXML
    TreeView<Leaf> treeViewComponent, treeViewAssembly, treeViewBlock, treeViewBuild;
    @FXML
    Button buttonQuit;
    @FXML
    ImageView imageViewTrash, imageViewBuildTrash;
    @FXML
    TabPane tabPaneMain;
    @FXML
    AnchorPane anchorPaneComponent, anchorPaneBuild;
    @FXML
    TableView<Summary> tableViewSummary;
    @FXML
    TableColumn<Summary, String> tableSumColName;
    @FXML
    TableColumn<Summary, Integer> tableSumColValue;

    private TreeItem<Leaf> blockRootItem, buildRootItem;
    private TreeItem<Leaf>  parentItem, buildBaseItem;
    private ObservableList<Summary> summaryData = FXCollections.observableArrayList();
    private Properties info = new Properties();
    private Stage componentStage;
    private ContextMenu folderContext = new ContextMenu();
    private ContextMenu assemblyContext = new ContextMenu();
    private ContextMenu componentContext = new ContextMenu();
    private ContextMenu blockContext = new ContextMenu();
    private ContextMenu buildContext = new ContextMenu();
    private ContextMenu detailContext = new ContextMenu();
    private Common com;
    private DataFormat dfCC, dfCA, dfAC, dfAA, dfBl, dfBC;
    private int blockCount = 0;

    @FXML
    private void componentDragDetected(MouseEvent event) {

        if (event.isDragDetect()) {
            if (treeViewComponent.getSelectionModel().getSelectedItem() != null) {
                Leaf node = treeViewComponent.getSelectionModel().getSelectedItem().getValue();
                Dragboard db = treeViewComponent.startDragAndDrop(TransferMode.COPY_OR_MOVE);
                ClipboardContent content = new ClipboardContent();
                if (node.LeafType == Leaf.LeafEnum.COMPONENT) {
                    content.put(dfCC, node);
                } else {
                    Leaf parent = treeViewComponent.getSelectionModel().getSelectedItem().getParent().getValue();
                    content.put(dfCA, node);
                    content.put(dfCC, parent);
                }
                db.setContent(content);
            }
            event.consume();
        }
    }

    @FXML
    private void assemblyDragDetected(MouseEvent event) {
        if (event.isDragDetect()) {
            if (treeViewAssembly.getSelectionModel().getSelectedItem() != null) {
                Leaf node = treeViewAssembly.getSelectionModel().getSelectedItem().getValue();
                Dragboard db = treeViewAssembly.startDragAndDrop(TransferMode.COPY_OR_MOVE);
                ClipboardContent content = new ClipboardContent();
                if (node.LeafType == Leaf.LeafEnum.ASSEMBLY) {
                    content.put(dfAA, node);
                } else {
                    Leaf parent = treeViewAssembly.getSelectionModel().getSelectedItem().getParent().getValue();
                    content.put(dfAC, node);
                    content.put(dfAA, parent);
                }
                db.setContent(content);
            }
            event.consume();
        }
    }

    @FXML
    private void blockDragDetected(MouseEvent event) {
        if (treeViewBlock.getSelectionModel().getSelectedItem() != null) {
            Leaf node = treeViewBlock.getSelectionModel().getSelectedItem().getValue();
            Dragboard db = treeViewBlock.startDragAndDrop(TransferMode.COPY_OR_MOVE);
            ClipboardContent content = new ClipboardContent();
            if (node.LeafType == Leaf.LeafEnum.BLOCK) {
                content.put(dfBl, node);
            }
            else if (node.LeafType == Leaf.LeafEnum.COMPONENT) {
                content.put(dfBC, node);
            }
            db.setContent(content);
        }
        event.consume();
    }
    @FXML
    private void buildDragDetected(MouseEvent event) {
        if (treeViewBuild.getSelectionModel().getSelectedItem() != null) {
            Leaf node = treeViewBuild.getSelectionModel().getSelectedItem().getValue();
            Dragboard db = treeViewBuild.startDragAndDrop(TransferMode.COPY_OR_MOVE);
            ClipboardContent content = new ClipboardContent();
            if (node.LeafType == Leaf.LeafEnum.BLOCK) {
                content.put(dfBl, node);
            }
            db.setContent(content);
        }
        event.consume();
    }
    public void initialize () {

        tabPaneMain.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends Tab> ov, Tab t, Tab t1) -> {
            int index = tabPaneMain.getSelectionModel().getSelectedIndex();
            switch (index) {
                case 1:
                    if(anchorPaneComponent.getChildren().isEmpty()) {
                        anchorPaneBuild.getChildren().remove(treeViewComponent);
                        anchorPaneComponent.getChildren().add(treeViewComponent);
                    }
                    break;
                case 2:
                    if(anchorPaneBuild.getChildren().isEmpty()) {
                        anchorPaneComponent.getChildren().remove(treeViewComponent);
                        anchorPaneBuild.getChildren().add(treeViewComponent);
                    }
                    break;
            }

        });

        com = new Common();
        connectDB();
        setUp();
        defineContext();
        loadComponents();
        loadAssembly();

    }
    private void connectDB() {
        final String dbURL = "jdbc:derby:matrix";
        final String schema = "matrix";
        try {
            info.put("user", schema);
            Connection dataAssembly = DriverManager.getConnection(dbURL, info);
            sr = dataAssembly.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            st = dataAssembly.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            labelControlStatus.setText("Database Open");
            labelControlStatus.setStyle("-fx-text-fill: orange");

        } catch (Exception ex ){
            labelControlStatus.setText(ex.getMessage());
            labelControlStatus.setStyle("-fx-text-fill: red");
        }
    }

    private void setUp() {

        /* Summary Data Initialization */

        PropertyValueFactory<Summary, String> nameProperty = new PropertyValueFactory<>("name");
        PropertyValueFactory<Summary, Integer> valueProperty = new PropertyValueFactory<>("value");

        tableSumColName.setCellValueFactory(nameProperty);
        tableSumColName.setStyle("-fx-alignment: CENTER-LEFT");
        tableSumColValue.setCellValueFactory(valueProperty);
        tableSumColValue.setStyle("-fx-alignment: CENTER-RIGHT");
        tableViewSummary.setItems(summaryData);



        dfCC = new DataFormat("Component Tree Component");
        dfCA = new DataFormat("Component Tree Assembly");
        dfAC = new DataFormat("Assembly Tree Component");
        dfAA = new DataFormat("Assembly Tree Assembly");
        dfBl = new DataFormat("Block");
        dfBC = new DataFormat("Block Tree Component");

        Leaf componentRoot = new Leaf("Component");
        componentRootItem = new TreeItem<>(componentRoot);
        treeViewComponent.setRoot(componentRootItem);
        treeViewComponent.setShowRoot(false);

        Leaf assemblyRoot = new Leaf("Assembly");
        assemblyRootItem = new TreeItem<>(assemblyRoot);
        treeViewAssembly.setRoot(assemblyRootItem);
        treeViewAssembly.setShowRoot(false);

        Leaf blockRoot = new Leaf("Block");
        blockRootItem = new TreeItem<>(blockRoot);
        treeViewBlock.setRoot(blockRootItem);
        treeViewBlock.setShowRoot(false);

        Leaf buildRoot = new Leaf("Build");
        buildRootItem = new TreeItem<>(buildRoot);
        treeViewBuild.setRoot(buildRootItem);
        treeViewBuild.setShowRoot(false);
        Leaf buildBase = new Leaf("Solution", 0);
        buildBaseItem = com.newItem(buildBase);
        buildRootItem.getChildren().add(buildBaseItem);


        treeViewComponent.setCellFactory(cellData -> {
            final Tooltip tooltip = new Tooltip();
            TreeCell<Leaf> cell = new TreeCell<>() {
                @Override
                protected void updateItem(Leaf p, boolean empty) {
                    super.updateItem(p, empty);
                    if (empty || p == null) {
                        setText(null);
                        setGraphic(null);
                        setTooltip(null);
                    } else {
                        if (p.LeafType == Leaf.LeafEnum.FOLDER) {
                            setText(p.Category);
                            tooltip.setText("Folder");
                            setStyle("-fx-text-fill: blue");
                            setContextMenu(folderContext);
                        }
                        else if (p.LeafType == Leaf.LeafEnum.COMPONENT) {
                            setText(p.Description);
                            tooltip.setText(p.Code);
                            setStyle("-fx-text-fill: #cbf1c6");
                            setGraphic(getTreeItem().getGraphic());
                            setTooltip(tooltip);
                            setContextMenu(componentContext);
                        }
                        else if ((p.LeafType == Leaf.LeafEnum.ASSEMBLY)) {
                            setText(p.Name);
                            setStyle("-fx-text-fill: #f8d29f");
                            tooltip.setText(p.Selector + " - " + p.Content);
                            setTooltip(tooltip);
                            setContextMenu(null);
                        }
                    }
                }
            };
            cell.setOnDragOver(event -> {
                event.acceptTransferModes(TransferMode.COPY);
                event.consume();
            });
            cell.setOnDragDropped(event -> {
                if (event.getDragboard().hasContent(dfAA)) {

                    /* Add Assembly to Component */

                    parentItem = cell.getTreeItem();
                    Leaf assembly = (Leaf) event.getDragboard().getContent(dfAA);
                    Leaf component = parentItem.getValue();
                    String SQL = "INSERT INTO LINK (ASSEMBLY_NAME, COMPONENT_CODE, DIR) VALUES ('" +
                            assembly.Name + "', '" + component.Code + "', 'F')";
                    try {
                        sr.executeUpdate(SQL);
                    } catch (SQLException ex) {
                        labelComponentStatus.setText(ex.getMessage());
                        labelComponentStatus.setStyle("-fx-text-fill: red");
                    }

                    assemblyAdd(assembly, parentItem);

                    labelComponentStatus.setText("Assembly " + assembly.Name + " Added to Component " + component.Code);
                    labelComponentStatus.setStyle("-fx-text-fill: orange");

                }
                event.setDropCompleted(true);
                event.consume();
            });
            return cell;
        });

        treeViewAssembly.setCellFactory(cellData -> {
            final Tooltip tooltip = new Tooltip();
            TreeCell<Leaf> cell = new TreeCell<>() {
                @Override
                protected void updateItem(Leaf p, boolean empty) {
                    super.updateItem(p, empty);
                    if (empty || p == null) {
                        setText(null);
                        setGraphic(null);
                        setTooltip(null);
                    } else {
                        if (p.LeafType == Leaf.LeafEnum.ASSEMBLY) {
                            setText(p.Name);
                            setStyle("-fx-text-fill: #e4bf11");
                            tooltip.setText(p.Selector + " - " + p.Content);
                            setTooltip(tooltip);
                            setContextMenu(assemblyContext);
                        } else if (p.LeafType == Leaf.LeafEnum.COMPONENT) {
                            setText(p.Description);
                            tooltip.setText(p.Code);
                            setStyle("-fx-text-fill: #0e43da");
                            setGraphic(getTreeItem().getGraphic());
                            setTooltip(tooltip);
                            setContextMenu(null);
                        }
                    }
                }
            };
            cell.setOnDragOver(event -> {
                event.acceptTransferModes(TransferMode.COPY);
                event.consume();
            });
            cell.setOnDragDropped(event -> {
                if (event.getDragboard().hasContent(dfCC)) {

                    /* Add Component to Assembly */

                    Leaf node = (Leaf) event.getDragboard().getContent(dfCC);
                    parentItem = cell.getTreeItem();
                    Leaf parent = parentItem.getValue();
                    if (parent.LeafType == Leaf.LeafEnum.ASSEMBLY) {
                        TreeItem<Leaf> nodeItem = com.newItem(node.deepClone());
                        String SQL = "INSERT INTO LINK (ASSEMBLY_NAME, COMPONENT_CODE, DIR) VALUES ('" +
                                parent.Name + "', '" + node.Code + "', 'M')";
                        try {
                            sr.executeUpdate(SQL);
                            SQL = "SELECT COMPONENT_CODE FROM LINK INNER JOIN ASSEMBLY ON NAME=ASSEMBLY_NAME WHERE NAME = '" +
                                    parent.Name + "' AND DIR = 'F'";
                            ResultSet rs = sr.executeQuery(SQL);
                            while (rs.next()) {
                                String code = rs.getString("COMPONENT_CODE");
                                Leaf component = componentHash.get(code).getValue();
                                ArrayUtils.add(component.tabHash, parent.Name.hashCode());
                            }
                        } catch (SQLException ex) {
                            labelComponentStatus.setText(ex.getMessage());
                            labelComponentStatus.setStyle("-fx-text-fill: red");
                            return;
                        }

                        parentItem.getChildren().add(nodeItem);
                        labelComponentStatus.setText("Component " + node.Code + " added to Assembly " + parent.Name);
                        labelComponentStatus.setStyle("-fx-text-fill: orange");
                    }
                }
                event.setDropCompleted(true);
                event.consume();
            });
            return cell;
        });

        treeViewBlock.setCellFactory(cellData -> {
            final Tooltip tooltip = new Tooltip();
            TreeCell<Leaf> cell = new TreeCell<>() {
                @Override
                protected void updateItem(Leaf p, boolean empty) {
                    super.updateItem(p, empty);
                    if (empty || p == null) {
                        setText(null);
                        setGraphic(null);
                        setTooltip(null);
                    }
                    else {
                        if (p.LeafType == Leaf.LeafEnum.BLOCK) {
                            if (p.Quantity > 1)
                                setText(p.Quantity + " x " + p.Name);
                            else
                                setText(p.Name);
                            setContextMenu(blockContext);
                        } else if (p.LeafType == Leaf.LeafEnum.COMPONENT) {
                            if (p.itemCount > 1)
                                setText(p.itemCount + " x " + p.Description);
                            else
                                setText(p.Description);
                            tooltip.setText(p.totalCount + " x " + p.Code);
                            setStyle("-fx-text-fill: green");
                            setContextMenu(detailContext);
                        }
                        setTooltip(tooltip);
                        setGraphic(getTreeItem().getGraphic());
                    }
                }
            };
            cell.setOnDragOver(event -> {
                event.acceptTransferModes(TransferMode.COPY);
                event.consume();
            });
            cell.setOnDragDropped(event -> {
                if (event.getDragboard().hasContent(dfCC)) {
                    TreeItem<Leaf> targetNode = cell.getTreeItem();
                    Leaf node = (Leaf) event.getDragboard().getContent(dfCC);
                    Leaf leaf = node.deepClone();
                    TreeItem<Leaf> nodeItem = com.newItem(leaf);
                    if (targetNode.getValue().LeafType == Leaf.LeafEnum.BLOCK) {
                        leaf.totalCount = 1;
                        leaf.itemCount = 1;
                        targetNode.getChildren().add(nodeItem);
                        targetNode.setExpanded(true);
                        Leaf target = targetNode.getValue();
                        leaf.parentHash = target.Hash;
                        ++ cell.getTreeView().getRoot().getChildren().get(0).getValue().childCount;
                    } else {
                        Leaf target = targetNode.getValue();
                        leaf.parentHash = target.Hash;
                        int count = dropNode(leaf, target);
                        if (count > 0) {
                            cell.getTreeItem().getChildren().add(nodeItem);
                            cell.getTreeItem().setExpanded(true);
                            leaf.itemCount = count;
                            leaf.totalCount = count * target.totalCount;
                            ++ cell.getTreeView().getRoot().getChildren().get(0).getValue().childCount;
                            blockHash.put(leaf.Hash, nodeItem);
                        }
                    }

                } else if (event.getDragboard().hasContent(dfBl)) {
                    Leaf block = (Leaf) event.getDragboard().getContent(dfBl);
                    blockRootItem.getChildren().clear();
                    blockRootItem.getChildren().add(blockHash.get(block.Hash));
                }
                event.setDropCompleted(true);
                event.consume();
            });
            return cell;
        });

        treeViewBuild.setCellFactory(cellData -> {
            final Tooltip tooltip = new Tooltip();
            TreeCell<Leaf> cell = new TreeCell<>() {
                @Override
                protected void updateItem(Leaf p, boolean empty) {
                    super.updateItem(p, empty);
                    if (empty || p == null) {
                        setText(null);
                        setGraphic(null);
                        setTooltip(null);
                    } else {
                        if (p.LeafType == Leaf.LeafEnum.BLOCK) {
                            if (p.Quantity > 1)
                                setText(p.Quantity + " x " + p.Name);
                            else
                                setText(p.Name);
                            tooltip.setText("Block");
                            setStyle("-fx-text-fill: blue");
                            setContextMenu(buildContext);
                        }
                        setTooltip(tooltip);
                    }
                }
            };

            cell.setOnDragOver(event -> {
                event.acceptTransferModes(TransferMode.COPY);
                event.consume();
            });

            cell.setOnDragDropped(event -> {
                if (event.getDragboard().hasContent(dfBl)) {
                    Leaf block = (Leaf) event.getDragboard().getContent(dfBl);
                    TreeItem<Leaf> nodeItem = com.newItem(block);
                    buildBaseItem.getChildren().add(nodeItem);

                    summaryRefresh();
                }
                event.setDropCompleted(true);
                event.consume();
            });
            return cell;
        });

            /* Drag to Trash */

        imageViewTrash.setOnDragOver(event -> {
            event.acceptTransferModes(TransferMode.COPY);
            event.consume();
        });

        imageViewTrash.setOnDragEntered(event -> {
            String iconPath = "/img/GlassTrash.png";
            Image icon = new Image(getClass().getResourceAsStream(iconPath));
            imageViewTrash.setImage(icon);
            event.consume();
        });

        imageViewTrash.setOnDragExited(event -> {
            String iconPath = "/img/trash.png";
            Image icon = new Image(getClass().getResourceAsStream(iconPath));
            imageViewTrash.setImage(icon);
            event.consume();
        });
        imageViewTrash.setOnDragDropped(event -> {

            try {
                if (event.getDragboard().hasContent(dfAA)) {
                    Leaf assembly = (Leaf) event.getDragboard().getContent(dfAA);
                    if ((event.getDragboard().hasContent(dfAC))) {

                        /* Remove Component from Assembly */

                        Leaf component = (Leaf) event.getDragboard().getContent(dfAC);
                        String key = makeKey(component.Code, assembly.Name);
                        deleteTreeItem(assemblyHash, key);
                        String SQL = "DELETE FROM LINK WHERE COMPONENT_CODE = '" + component.Code +
                                "' AND DIR = 'M' AND ASSEMBLY_NAME = '" + assembly.Name + "'";
                        sr.executeUpdate(SQL);
                        labelComponentStatus.setText("Component" + component.Code + " Removed from Assembly " + assembly.Name);
                        labelComponentStatus.setStyle("-fx-text-fill: orange");
                    } else {

                        /* Delete Assembly and its links */

                        String SQL = "SELECT COMPONENT_CODE FROM LINK WHERE ASSEMBLY_NAME = '" + assembly.Name +
                                "' AND DIR = 'M'";
                        ResultSet rs = sr.executeQuery(SQL);
                        while (rs.next()) {
                            String code = rs.getString("COMPONENT_CODE");
                            String key = makeKey(code, assembly.Name);
                            deleteTreeItem(assemblyHash, key);
                        }

                        SQL = "DELETE FROM LINK WHERE ASSEMBLY_NAME = '" + assembly.Name + "'";
                        sr.executeUpdate(SQL);
                        SQL = "DELETE FROM ASSEMBLY WHERE NAME = '" + assembly.Name + "'";
                        sr.executeUpdate(SQL);
                        deleteTreeItem(assemblyHash, assembly.Name);
                        labelComponentStatus.setText("Assembly " + assembly.Name + " Deleted");
                        labelComponentStatus.setStyle("-fx-text-fill: orange");
                    }
                }
                else if (event.getDragboard().hasContent(dfCC)) {
                    Leaf component = (Leaf) event.getDragboard().getContent(dfCC);
                    if ((event.getDragboard().hasContent(dfCA))) {

                        /* Remove Assembly from Component */

                        Leaf assembly = (Leaf) event.getDragboard().getContent(dfCA);
                        String SQL = "DELETE FROM LINK WHERE COMPONENT_CODE = '" + component.Code +
                                "' AND DIR = 'F' AND ASSEMBLY_NAME = '" + assembly.Name + "'";
                        sr.executeUpdate(SQL);

                        assemblyDelete(assembly, component);

                        labelComponentStatus.setText("Assembly " + assembly.Name + " removed from Component " + component.Code);
                        labelComponentStatus.setStyle("-fx-text-fill: orange");

                    } else {

                        /* Delete Component and its links and summary items */

                        String SQL = "SELECT ASSEMBLY_NAME FROM LINK WHERE COMPONENT_CODE = '" + component.Code + "'";
                        ResultSet rs = sr.executeQuery(SQL);
                        while (rs.next()) {
                            String name = rs.getString("ASSEMBLY_NAME");
                            String key = makeKey(component.Code, name);
                            deleteTreeItem(assemblyHash, key);
                        }
                        deleteTreeItem(componentHash, component.Code);
                        SQL = "DELETE FROM LINK WHERE COMPONENT_CODE = '" + component.Code + "'";
                        sr.executeUpdate(SQL);
                        SQL = "DELETE FROM SUM_LINK WHERE COMPONENT_CODE = '" + component.Code + "'";
                        sr.executeUpdate(SQL);
                        SQL = "DELETE FROM COMPONENT WHERE CODE = '" + component.Code + "'";
                        sr.executeUpdate(SQL);

                        labelComponentStatus.setText("Component " + component.Code + " Deleted");
                        labelComponentStatus.setStyle("-fx-text-fill: orange");
                    }
                }


            } catch (SQLException ex) {
                labelComponentStatus.setText(ex.getMessage());
                labelComponentStatus.setStyle("-fx-text-fill: red");
            }
            event.setDropCompleted(true);
            event.consume();
        });

        imageViewBuildTrash.setOnDragOver(event -> {
            event.acceptTransferModes(TransferMode.COPY);
            event.consume();
        });

        imageViewBuildTrash.setOnDragEntered(event -> {
            String iconPath = "/img/GlassTrash.png";
            Image icon = new Image(getClass().getResourceAsStream(iconPath));
            imageViewBuildTrash.setImage(icon);
            event.consume();
        });

        imageViewBuildTrash.setOnDragExited(event -> {
            String iconPath = "/img/trash.png";
            Image icon = new Image(getClass().getResourceAsStream(iconPath));
            imageViewBuildTrash.setImage(icon);
            event.consume();
        });


        imageViewBuildTrash.setOnDragDropped(event -> {

            /* Remove component from Block */

            if (event.getDragboard().hasContent(dfBC)) {
                Leaf node = (Leaf) event.getDragboard().getContent(dfBC);
                deleteBlockItem(blockHash, node);

            }
            event.setDropCompleted(true);
            event.consume();
        });

    }
    private void assemblyAdd(Leaf addition, TreeItem<Leaf> nodeItem) {
        Leaf component = nodeItem.getValue();
        TreeItem<Leaf> addItem = com.newItem(addition);
        nodeItem.getChildren().add(addItem);
        String key = makeKey(addition.Name, component.Code);
        assemblyHash.put(key, addItem);
        
        ArrayUtils.add(component.assemblyName, addition.Name);
        ArrayUtils.add(component.assemblyHash, addition.Name.hashCode());
        ArrayUtils.add(component.assemblyMax, addition.Content);
        ArrayUtils.add(component.assemblyQty, 0);
        ++component.assemblyCount;
    }
    private void assemblyDelete(Leaf remove, Leaf component) {
        parentItem = componentHash.get(component);
        String key = makeKey(component.Code, remove.Name);
        selectItem = assemblyHash.get(key);
        parentItem.getChildren().remove(selectItem);
        assemblyHash.remove(key);

        int i = ArrayUtils.indexOf(component.assemblyName, remove.Name);
        ArrayUtils.remove(component.assemblyName, i);
        ArrayUtils.remove(component.assemblyHash, i);
        ArrayUtils.remove(component.assemblyMax, i);
        ArrayUtils.remove(component.assemblyQty, i);
        --component.assemblyCount;
    }

    private void deleteTreeItem(HashMap<String, TreeItem<Leaf>> hash, String key) {
        TreeItem<Leaf> item = hash.get(key);
        item.getParent().getChildren().remove(item);
        hash.remove(key, item);
    }
    private void deleteBlockItem(HashMap<Integer, TreeItem<Leaf>> hash, Leaf node) {
        TreeItem<Leaf> item = hash.get(node.Hash);
        TreeItem<Leaf> base = item;
        while (base.getParent() != null)
            base = base.getParent();
        Leaf blockBase =  base.getValue();
        --blockBase.childCount;
        Leaf parent = item.getParent().getValue();
        item.getParent().getChildren().remove(item);
        hash.remove(node.Hash, item);
        for (int j = 0; j < node.tabCount; j++) {
            for (int i = 0; i < parent.assemblyCount; i++) {
                if (node.tabHash[j] == parent.assemblyHash[i]) {
                    parent.assemblyQty[i] -= node.itemCount;
                    break;
                }
            }
        }
    }

    // Update Summary Display

    private void summaryRefresh() {
            summaryData.clear();
            sumHash.clear();
            for (TreeItem<Leaf> buildBlock : buildBaseItem.getChildren()) {
                Leaf blockItem = buildBlock.getValue();
                TreeItem<Leaf> hashItem = blockHash.get(blockItem.Hash);
                summaryUpdate(hashItem.getChildren().get(0), sumHash, summaryData, blockItem.Quantity);
            }
    }
    // Update Summary Display

    private void summaryUpdate(TreeItem<Leaf> item, HashMap<String, Summary> map,
                               ObservableList<Summary> summary, int count) {
        Leaf leaf = item.getValue();
        if (leaf.totalCount > 0)
            addSum(leaf, map, summary, count);
        for (TreeItem<Leaf> cp : item.getChildren()) {
            if (cp.getChildren().isEmpty()) {
                addSum(cp.getValue(), map, summary, count);
            } else
                summaryUpdate(cp, map, summary, count);
        }
    }

    private void addSum(Leaf leaf, HashMap<String, Summary> map, ObservableList<Summary> data, int count) {
        if (leaf.sumCount > 0) {
            for(int i = 0; i < leaf.sumCount; i++ ) {
                Summary sumValue;
                if (map.containsKey(leaf.sumName[i]))
                    sumValue = map.get(leaf.sumName[i]);
                else {
                    sumValue = new Summary(leaf.sumName[i], 0);
                    map.put(leaf.sumName[i], sumValue);
                    data.add(sumValue);
                }
                int index = data.indexOf(sumValue);
                Summary blockSum = data.get(index);
                int total = blockSum.getValue();
                blockSum.setValue(total + count * leaf.sumIncrement[i] * leaf.totalCount);
                data.set(index, blockSum);
            }
        }
    }

    //  Load Component Tree

    private void loadComponents() {

        try {
            String SQL = "SELECT CODE, DESCRIPTION, CATEGORY FROM COMPONENT ORDER BY CATEGORY, DESCRIPTION";
            ResultSet rs = sr.executeQuery(SQL);
            while (rs.next()) {
                String category = rs.getString("CATEGORY");
                String code = rs.getString("CODE");
                if ((parentItem = categoryHash.get(category)) == null) {
                    Leaf parent = new Leaf(category);
                    parentItem = new TreeItem<>(parent);
                    categoryHash.put(category, parentItem);
                    componentRootItem.getChildren().add(parentItem);
                }
                Leaf node = new Leaf(code, rs.getString("DESCRIPTION"),
                        rs.getString("CATEGORY"));
                TreeItem<Leaf> nodeItem = com.newItem(node);
                parentItem.getChildren().add(nodeItem);
                componentHash.put(code, nodeItem);

                /* Load Assembly Information */

                SQL = "SELECT NAME, CONTENT, SELECTOR FROM ASSEMBLY INNER JOIN LINK ON NAME = ASSEMBLY_NAME " +
                        "WHERE DIR = 'F' AND COMPONENT_CODE = '" + code + "'ORDER BY NAME";
                ResultSet rt = st.executeQuery(SQL);
                rt.last();
                node.assemblyCount = rt.getRow();
                if (node.assemblyCount > 0) {
                    node.assemblyName = new String[node.assemblyCount];
                    node.assemblyHash = new int[node.assemblyCount];
                    node.assemblyMax = new int[node.assemblyCount];
                    node.assemblyQty = new int[node.assemblyCount];
                    rt.beforeFirst();
                    int i = 0;
                    while (rt.next()) {
                        String name = rt.getString("NAME");
                        node.assemblyName[i] = name;
                        node.assemblyHash[i] = name.hashCode();
                        switch (rt.getString("SELECTOR")) {
                            case "M":
                                node.assemblyMax[i] = rt.getInt("CONTENT");
                                break;
                            case "E":
                                node.assemblyMax[i] = -rt.getInt("CONTENT");
                                break;
                            case "U":
                                node.assemblyMax[i] = 0;
                                break;
                        }
                        node.assemblyQty[i++] = 0;
                        Leaf assembly = new Leaf(name, rt.getInt("CONTENT"),
                                rt.getString("SELECTOR"));
                        TreeItem<Leaf> assemblyItem = com.newItem(assembly);
                        nodeItem.getChildren().add(assemblyItem);
                    }
                }
                rt.close();

                SQL = "SELECT ASSEMBLY_NAME FROM LINK WHERE DIR = 'M' AND COMPONENT_CODE = '" + code + "' ORDER BY ASSEMBLY_NAME";
                rt = st.executeQuery(SQL);
                rt.last();

                node.tabCount = rt.getRow();
                if (node.tabCount > 0) {
                    node.tabHash = new int[node.tabCount];
                    rt.beforeFirst();
                    int i = 0;
                    while (rt.next()) {
                        node.tabHash[i++] = rt.getString("ASSEMBLY_NAME").hashCode();
                    }
                }
                rt.close();

                SQL = "SELECT SUM_NAME, INCREMENT FROM SUM_LINK WHERE COMPONENT_CODE = '" + code + "'";
                rt = st.executeQuery(SQL);
                rt.last();
                node.sumCount = rt.getRow();
                if (node.sumCount > 0) {
                    node.sumName = new String[node.sumCount];
                    node.sumIncrement = new int[node.sumCount];
                    rt.beforeFirst();
                    int i = 0;
                    while (rt.next()) {
                        node.sumName[i] = rt.getString("SUM_NAME");
                        node.sumIncrement[i++] = rt.getInt("INCREMENT");
                    }
                }
                rt.close();
            }
            rs.close();
        }
        catch (SQLException ex)  {
            labelControlStatus.setText(ex.getMessage());
            labelControlStatus.setStyle("-fx-text-fill: red");
        }

        //  Load Summary Data

        try {
            String SQL = "SELECT NAME FROM SUMMARY ORDER BY NAME";
            ResultSet rs = sr.executeQuery(SQL);
            while (rs.next()) {
                summary_name.add(rs.getString("NAME"));
            }
        }
        catch (SQLException ex) {
            labelControlStatus.setText(ex.getMessage());
            labelControlStatus.setStyle("-fx-text-fill: red");
        }

    }

    //  Load Assembly Tree

    private void loadAssembly() {

        String SQL = "SELECT NAME, CONTENT, SELECTOR FROM ASSEMBLY ORDER BY NAME";
        try {
            ResultSet rs = sr.executeQuery(SQL);
            while (rs.next()) {
                String name = rs.getString("NAME");
                Leaf node = new Leaf(name, rs.getInt("CONTENT"),
                        rs.getString("SELECTOR"));
                TreeItem<Leaf> nodeItem = new TreeItem<>(node);
                assemblyRootItem.getChildren().add(nodeItem);
                assemblyHash.put(name, nodeItem);
            }

            SQL = "SELECT CODE, DESCRIPTION, CATEGORY, ASSEMBLY_NAME FROM COMPONENT INNER JOIN LINK ON CODE = COMPONENT_CODE " +
                    "WHERE DIR = 'M' ORDER BY DESCRIPTION";
            rs = sr.executeQuery(SQL);
            while (rs.next()) {
                String name = rs.getString("ASSEMBLY_NAME");
                String code = rs.getString("CODE");
                if ((parentItem = assemblyHash.get(name)) != null) {
                    Leaf node = new Leaf(code, rs.getString("DESCRIPTION"),
                            rs.getString("CATEGORY"));
                    TreeItem<Leaf> nodeItem = com.newItem(node);
                    parentItem.getChildren().add(nodeItem);
                    String key = makeKey(code, name);
                    assemblyHash.put(key, nodeItem);
                }
            }
        } catch (SQLException ex) {
            labelControlStatus.setText(ex.getMessage());
            labelControlStatus.setStyle("-fx-text-fill: red");
        }
    }
    private String makeKey(String a, String b) {
        return a + '~' + b;
    }

    private void defineContext() {
        MenuItem newComponent = new MenuItem("Add New Component");
        newComponent.setOnAction(e ->
        {
            selectItem = treeViewComponent.getSelectionModel().getSelectedItem();
            try {
                FXMLLoader fxmlFormLoader = new FXMLLoader(getClass().getResource("addComponent.fxml"));
                Parent partForm = fxmlFormLoader.load();
                componentStage = new Stage();
                componentStage.setTitle("Define New Component");
                componentStage.setScene(new Scene(partForm));
                componentStage.show();
            } catch (IOException ex) {
                labelComponentStatus.setText(ex.getMessage());
                labelComponentStatus.setStyle("-fx-text-fill: red");
            }
        });

        MenuItem updateComponent = new MenuItem("Update Component Details");
        updateComponent.setOnAction(e ->
        {
            selectItem = treeViewComponent.getSelectionModel().getSelectedItem();
            try {
                FXMLLoader fxmlFormLoader = new FXMLLoader(getClass().getResource("updateComponent.fxml"));
                Parent partForm = fxmlFormLoader.load();
                componentStage = new Stage();
                componentStage.setTitle("Update Component Details");
                componentStage.setScene(new Scene(partForm));
                componentStage.show();
            } catch (IOException ex) {
                labelComponentStatus.setText(ex.getMessage());
                labelComponentStatus.setStyle("-fx-text-fill: red");
            }
        });

        MenuItem newAssembly = new MenuItem("Define New Assembly");
        newAssembly.setOnAction(e ->
        {
            try {
                FXMLLoader fxmlFormLoader = new FXMLLoader(getClass().getResource("addAssembly.fxml"));
                Parent assemblyForm = fxmlFormLoader.load();
                Stage assemblyStage = new Stage();
                assemblyStage.setTitle("Define New Assembly");
                assemblyStage.setScene(new Scene(assemblyForm));
                assemblyStage.show();
            } catch (IOException ex) {
                labelComponentStatus.setText(ex.getMessage());
                labelComponentStatus.setStyle("-fx-text-fill: red");
            }
        });

        MenuItem updateAssembly = new MenuItem("Update Assembly");
        updateAssembly.setOnAction(e ->
        {
            selectItem = treeViewAssembly.getSelectionModel().getSelectedItem();
            try {
                FXMLLoader fxmlFormLoader = new FXMLLoader(getClass().getResource("updateAssembly.fxml"));
                Parent assemblyForm = fxmlFormLoader.load();
                Stage assemblyStage = new Stage();
                assemblyStage.setTitle("Update Assembly");
                assemblyStage.setScene(new Scene(assemblyForm));
                assemblyStage.show();
            } catch (IOException ex) {
                labelComponentStatus.setText(ex.getMessage());
                ex.printStackTrace();
                labelComponentStatus.setStyle("-fx-text-fill: red");
            }
        });

        MenuItem setQuantity = new MenuItem("Set Block Quantity");
        setQuantity.setOnAction(e -> {
            selectItem = treeViewBuild.getSelectionModel().getSelectedItem();
            TextInputDialog qtyDialog = new TextInputDialog();
            qtyDialog.setHeaderText("Enter Quantity :");
            Optional<String> countInput = qtyDialog.showAndWait();
            int nodeCount;
            if (countInput.isPresent())
                nodeCount = Integer.parseInt(countInput.get());
            else
                nodeCount = 1;
            Leaf block =  selectItem.getValue();
            block.Quantity = nodeCount;
            treeViewBuild.refresh();

            summaryRefresh();
        });

        MenuItem addLabel = new MenuItem("Add a Block Label");
        addLabel.setOnAction(e -> {
            selectItem = treeViewBlock.getSelectionModel().getSelectedItem();

            TextInputDialog labelDialog = new TextInputDialog();
            labelDialog.setHeaderText("Enter Label :");
            Optional<String> nodeLabel = labelDialog.showAndWait();
            if (nodeLabel.isPresent())
                (selectItem.getValue()).Name = nodeLabel.orElse(null);
            treeViewBlock.refresh();
        });

        MenuItem listAssembly = new MenuItem("List Assembly Details");
        listAssembly.setOnAction(e -> {
            selectItem = treeViewBlock.getSelectionModel().getSelectedItem();
            try {
                FXMLLoader fxmlFormLoader = new FXMLLoader(getClass().getResource("AssemblyShow.fxml"));
                Parent assemblyForm = fxmlFormLoader.load();
                Stage assemblyStage = new Stage();
                assemblyStage.setTitle("Assembly List");
                assemblyStage.setScene(new Scene(assemblyForm));
                assemblyStage.show();
            } catch (IOException ex) {
                labelComponentStatus.setText(ex.getMessage());
                labelComponentStatus.setStyle("-fx-text-fill: red");
            }
        });


        folderContext.getItems().add(newComponent);
        componentContext.getItems().add(updateComponent);
        assemblyContext.getItems().add(newAssembly);
        assemblyContext.getItems().add(updateAssembly);
        buildContext.getItems().add(setQuantity);
        blockContext.getItems().add(addLabel);
        detailContext.getItems().add(listAssembly);
    }

    public void ButtonNewBlockOnAction() {
        blockRootItem.getChildren().clear();
        Leaf block = new Leaf("Block", ++blockCount);
        TreeItem<Leaf> blockItem = com.newItem(block);
        blockRootItem.getChildren().add(blockItem);
        blockHash.put(block.Hash, blockItem);
    }

    public void ButtonNewSolutionOnAction() {
        buildRootItem.getChildren().clear();
        Leaf buildBase = new Leaf("Solution", 0);
        buildBaseItem = com.newItem(buildBase);
        buildRootItem.getChildren().add(buildBaseItem);
    }
    public void ButtonQuitOnAction() {
        Stage stage = (Stage) buttonQuit.getScene().getWindow();
        stage.close();
    }

    private int dropNode(Leaf leaf, Leaf branch) {
        int parentAssembly = 0;
        int qtyParent = 0, maxParent = 0, addQty;
        boolean match = false;
        for (int i = 0; i < leaf.tabCount; i++) {
            for (int j = 0; j < branch.assemblyCount; j++) {
                if (leaf.tabHash[i] == branch.assemblyHash[j]) {
                    match = true;
                    parentAssembly = j;
                    qtyParent = branch.assemblyQty[parentAssembly];
                    maxParent = branch.assemblyMax[parentAssembly];
                    break;
                }
            }
        }
        if (match) {
            Optional<String> addCount;
            leaf.parentAssemblyIndex = parentAssembly;
            for (; ; ) {
                TextInputDialog countDialog = new TextInputDialog();
                if (maxParent > qtyParent) {
                    countDialog.setHeaderText("Enter Quantity <= " + (maxParent - qtyParent) + " :");
                    addCount = countDialog.showAndWait();
                    if (addCount.isPresent()) {
                        addQty = Integer.parseInt(addCount.get());
                        if (addQty + qtyParent <= maxParent)
                            branch.assemblyQty[parentAssembly] = addQty + qtyParent;
                        return addQty;
                    }

                } else if (maxParent < 0 && qtyParent != -maxParent) {
                    branch.assemblyQty[parentAssembly] = -maxParent;
                    return -maxParent;
                } else if (maxParent == 0) {
                    countDialog.setHeaderText("Enter Quantity :");
                    addCount = countDialog.showAndWait();
                    if (addCount.isPresent()) {
                        addQty = Integer.parseInt(addCount.get());
                        branch.assemblyQty[parentAssembly] = addQty + qtyParent;
                        return addQty;
                    }
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setContentText("Part Bonds are full");
                    alert.showAndWait();
                    return 0;
                }
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            Image stopIcon = new Image(getClass().getResourceAsStream("/img/stop.png"));
            alert.setGraphic(new ImageView(stopIcon));
            alert.setContentText("Component is not valid here");
            alert.showAndWait();
            return 0;
        }
    }

    public void ButtonExportOnAction() {
        HashMap<String, Integer> exportHash = new HashMap<>();
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("csv file (*.csv)", "*.csv"));
            fileChooser.setTitle("Save csv file");
            File exportFile = fileChooser.showSaveDialog(getPrimaryStage());
            if (exportFile.exists())
                exportFile.delete();
            if (exportFile.createNewFile()) {
                FileWriter fw = new FileWriter(exportFile);
                BufferedWriter bw = new BufferedWriter(fw);

                for (TreeItem<Leaf> block : buildBaseItem.getChildren()) {
                    Leaf blockItem = block.getValue();
                    TreeItem<Leaf> objItem = blockHash.get(blockItem.Hash);
                    exportTree(objItem.getChildren().get(0), exportHash, blockItem.Quantity);
                }
                for (Map.Entry<String, Integer> entry : exportHash.entrySet()) {
                    bw.write(entry.getKey() + "," + entry.getValue());
                    bw.newLine();
                }
                bw.close();
                fw.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void exportTree(TreeItem<Leaf> item, HashMap<String, Integer> map, int qty) {

        Leaf component = item.getValue();
        if (component.totalCount > 0)
            updateTotal(component, map, qty);
        for (TreeItem<Leaf> cp : item.getChildren()) {
            if (cp.getChildren().isEmpty()) {
                updateTotal(cp.getValue(), map, qty);
            } else
                exportTree(cp, map, qty);
        }
    }

    private void updateTotal(Leaf component, HashMap<String, Integer> map, int qty) {
        if (map.containsKey(component.Code))
            map.put(component.Code, map.get(component.Code) + qty * component.totalCount);
        else
            map.put(component.Code, qty * component.totalCount);
    }

    public void ButtonLoadOnAction() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("json file (*.json)", "*.json"));
        fileChooser.setTitle("Open json file");
        File jsonFile = fileChooser.showOpenDialog(componentStage);
        Gson gson = new Gson();
        try {
            FileReader fr = new FileReader(jsonFile);
            BufferedReader br = new BufferedReader(fr);
            readTree(br, gson);
            br.close();
            fr.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        summaryRefresh();
    }

    public void ButtonSaveOnAction() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("json file (*.json)", "*.json"));
        fileChooser.setTitle("Save json file");
        File jsonFile = fileChooser.showSaveDialog(componentStage);
        Gson gson = new Gson();

        if (jsonFile.exists())
            jsonFile.delete();

        try {
            if (jsonFile.createNewFile()) {
                FileWriter fw = new FileWriter(jsonFile);
                BufferedWriter bw = new BufferedWriter(fw);
                for (TreeItem<Leaf> block : buildBaseItem.getChildren()) {
                    Leaf blockItem = block.getValue();
                    TreeItem<Leaf> objItem = blockHash.get(blockItem.Hash);
                    String json = gson.toJson(blockItem);
                    bw.write(json);
                    bw.newLine();
                    saveTree(objItem.getChildren().get(0), bw, gson);
                }
                bw.close();
                fw.close();
            }
        } catch (IOException ex) {
            labelControlStatus.setText(ex.getMessage());
            labelControlStatus.setStyle("-fx-text-fill: red");
        }
    }

    private void readTree(BufferedReader br, Gson gs) {
        String line;
        Leaf component, jsonComponent;
        blockHash.clear();
        buildBaseItem.getChildren().clear();
        try {
            while ((line = br.readLine()) != null && line.length() != 0) {
                Leaf block = gs.fromJson(line, Leaf.class);
                blockCount = block.blockCount;
                TreeItem<Leaf> blockItem = com.newItem(block);
                blockHash.put(block.Hash, blockItem);
                for (int i = 0; i < block.childCount; i++) {
                    line = br.readLine();
                    jsonComponent = gs.fromJson(line, Leaf.class);
                    component = (componentHash.get(jsonComponent.Code).getValue()).deepClone();

                    /* Copy json Atom to current Atom object */

                    for (int j = 0; j < jsonComponent.assemblyCount; j++) {
                        int k = ArrayUtils.indexOf(component.assemblyName, jsonComponent.assemblyName[j]);
                        component.assemblyName[k] = jsonComponent.assemblyName[j];
                        component.assemblyMax[k] = jsonComponent.assemblyMax[j];
                        component.assemblyHash[k] = jsonComponent.assemblyHash[j];
                        component.assemblyQty[k] = jsonComponent.assemblyQty[j];
                    }
                    component.parentAssemblyIndex = jsonComponent.parentAssemblyIndex;
                    component.parentHash = jsonComponent.parentHash;
                    component.Hash = jsonComponent.Hash;
                    component.itemCount = jsonComponent.itemCount;
                    component.totalCount = jsonComponent.totalCount;
                    TreeItem<Leaf> componentItem = com.newItem(component);
                    TreeItem<Leaf> parentItem = blockHash.get(component.parentHash);
                    parentItem.getChildren().add(componentItem);
                    blockHash.put(component.Hash, componentItem);
                }
                TreeItem<Leaf> addItem = com.newItem(block);
                buildBaseItem.getChildren().add(addItem);
            }
        } catch (IOException ex) {
            labelControlStatus.setText(ex.getMessage());
            labelControlStatus.setStyle("-fx-text-fill: red");
        }
    }

    private void saveTree(TreeItem<Leaf> item, BufferedWriter bw, Gson gs) {
        String json = "";
        try {
            Leaf component = item.getValue();
            if (component.totalCount > 0)
                json = gs.toJson(item.getValue());
            bw.write(json);
            bw.newLine();
            for (TreeItem<Leaf> cp : item.getChildren()) {
                if (cp.getChildren().isEmpty()) {
                    json = gs.toJson(cp.getValue());
                    bw.write(json);
                    bw.newLine();
                } else
                    saveTree(cp, bw, gs);
            }
        } catch (IOException ex) {
            labelControlStatus.setText(ex.getMessage());
            labelControlStatus.setStyle("-fx-text-fill: red");
        }
    }
}
