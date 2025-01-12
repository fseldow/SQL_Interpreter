package btree;

import util.Constants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Serializer class for b+ tree serializing and java NIO to write
 * serialized stream to file
 *
 * @author Yufu Mo
 */
public class Serializer {

    private FileChannel fc;                    // The file channel for reader
    private ByteBuffer buffer;                // The buffer page.
    private int pageNum;                    // The current page number.
    private int leafCount;

    public Serializer(String path) {
        try {
            fc = new FileOutputStream(new File(path)).getChannel();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // allocate the buffer size for output page
        buffer = buffer.allocate(Constants.PAGE_SIZE);
        pageNum = 1;
        leafCount = 0;
    }


    /**
     * Serialize method
     * @param node input node to serialize and write to buffer
     * @return
     */
    public int serialize(TreeNode node) {
        long position = Constants.PAGE_SIZE * (long) pageNum;

        // initialize the buffer.
        try {
            fc.position(position);
            bufferFlush();
            // searilize the leaf node.
            if (node instanceof LeafNode) {
                leafCount++;
                LeafNode leafNode = (LeafNode) node;
                int numOfEntries = leafNode.getDataEntries().size();

                // put leaf node flag
                buffer.putInt(0);
                buffer.putInt(numOfEntries);
                for (DataEntry data : leafNode.getDataEntries()) {
                    buffer.putInt(data.getKey());
                    buffer.putInt(data.getRids().size());
                    for (Rid rid : data.getRids()) {
                        buffer.putInt(rid.getPageId());
                        buffer.putInt(rid.getTupleId());
                    }
                }
            }

            if (node instanceof IndexNode) {
                IndexNode indexNode = (IndexNode) node;

                // put index node flag
                buffer.putInt(1);
                buffer.putInt(indexNode.keys.size());
                for (Integer key : indexNode.getKeys()) {
                    buffer.putInt(key);
                }
                for (Integer addr : indexNode.getChildrenAddresses()) {
                    buffer.putInt(addr);
                }
            }

            // finally padding zeros at the end.
            while (buffer.hasRemaining()) {
                buffer.putInt(0);
            }

            buffer.flip();

            fc.write(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return pageNum++;
    }


    /**
     * finish method which is necessary finishing up the serializing
     * @param order
     */
    public void finish(int order) {
        try {
            // initialize the buffer.
            fc.position(0);
            bufferFlush();

            buffer.putInt(pageNum - 1);    // The address of the root.
            buffer.putInt(leafCount);    // The number of leaves in the tree.
            buffer.putInt(order);    // The order of the tree.

            // finally padding zeros at the end.
            while (buffer.hasRemaining()) {
                buffer.putInt(0);
            }
            buffer.flip();
            fc.write(buffer);
            fc.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Helper method that erases the buffer by filling zeros.
    private void bufferFlush() {
        buffer.clear();
        buffer.put(new byte[Constants.PAGE_SIZE]);
        buffer.clear();
    }

}
