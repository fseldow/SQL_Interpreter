package operator;

import io.BinaryTupleWriter;
import model.Tuple;
import io.TupleWriter;
import io.BufferStateWrapper;
import util.Catalog;
import util.Constants;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Map;

/**
 * Abstract class for operator
 * Created by Yufu Mo
 */
public abstract class PhysicalOperator {

    /**
     * get the next tuple of the operator's output
     * return null if the operator has no more output
     * @return the next tuple of the operator's output
     */
    public abstract Tuple getNextTuple();

    /**
     * reset the operator's state and start returning its output again from the
     * beginning
     */
    public abstract void reset();

    /**
     * @return the current schema of the operator
     */
    public abstract Map<String, Integer> getSchema();


    public void dump(int i) {
        String path = Catalog.getInstance().getOutputPath() + i;
        TupleWriter tupleWriter = new BinaryTupleWriter(path, getSchema().size());
        Tuple tuple = getNextTuple();
        while (tuple != null) {
            tupleWriter.writeNextTuple(tuple);
            tuple = getNextTuple();
        }
        // finish
        tupleWriter.writeNextTuple(null);
    }


}
