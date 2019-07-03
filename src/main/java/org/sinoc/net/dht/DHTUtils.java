package org.sinoc.net.dht;

import static org.sinoc.net.dht.Bucket.*;

import java.util.List;

public class DHTUtils {

    public static void printAllLeafs(Bucket root){
        SaveLeaf saveLeaf = new SaveLeaf();
        root.traverseTree(saveLeaf);

        for (Bucket bucket : saveLeaf.getLeafs())
            System.out.println(bucket);
    }

    public static List<Bucket> getAllLeafs(Bucket root){
        SaveLeaf saveLeaf = new SaveLeaf();
        root.traverseTree(saveLeaf);

        return saveLeaf.getLeafs();
    }
}
