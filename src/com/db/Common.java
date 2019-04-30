package com.db;

import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.InputStream;
import com.db.Leaf.*;

    // Class for common methods

public class Common {



    public TreeItem<Leaf> newItem(Leaf node) {
        TreeItem<Leaf> nodeItem;
        if(node.LeafType == LeafEnum.COMPONENT ) {
            String image = node.Category;
            String iconPath = "/img/" + image + ".png";
            InputStream iconStream = getClass().getResourceAsStream(iconPath);
            if (iconStream != null) {
                Image icon = new Image(getClass().getResourceAsStream(iconPath));
                nodeItem = new TreeItem<>(node, new ImageView(icon));
            }
            else
                nodeItem = new TreeItem<>(node);
        }
        else
            nodeItem = new TreeItem<>(node);
        return nodeItem;
    }


}

