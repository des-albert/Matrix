package com.db;

import java.io.*;

public class Leaf implements Serializable {

    public enum LeafEnum {
        FOLDER, COMPONENT, ASSEMBLY, BLOCK
    }

    LeafEnum LeafType;
    String Description;
    String Code;
    String Category;
    String Name;
    int Content;
    String Selector;

    int Quantity;
    int childCount;
    int Hash;
    int parentHash;
    int tabCount;
    int tabHash[];
    int parentAssemblyIndex;

    int sumCount;
    String sumName[];
    int sumIncrement[];
    int itemCount;
    int totalCount;
    int blockCount;

    int assemblyCount;
    String assemblyName[];
    int assemblyHash[];
    int assemblyMax[];
    int assemblyQty[];

    //  Folder

    Leaf(String category) {
        this.Category = category;
        this.LeafType = LeafEnum.FOLDER;
    }

    // Component

    Leaf(String code, String desc, String category) {
        this.Code = code;
        this.Description = desc;
        this.Category = category;
        this.LeafType = LeafEnum.COMPONENT;
        Hash = hashCode();
    }

    // Assembly

    Leaf(String name, int content, String selector) {
        this.Name = name;
        this.Content = content;
        this.Selector = selector;
        this.LeafType = LeafEnum.ASSEMBLY;
    }

    // Block

    Leaf(String label, int count) {
        this.Name = label;
        this.blockCount = count;
        Hash = hashCode();
        this.Quantity = 1;
        this.LeafType = LeafEnum.BLOCK;
    }

    Leaf deepClone() {
        Leaf copy = null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(this);
            ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
            ObjectInputStream in = new ObjectInputStream(bis);
            copy = (Leaf) in.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return copy;
    }
}
