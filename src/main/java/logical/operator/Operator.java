package logical.operator;

import com.sql.interpreter.PhysicalPlanBuilder;
import model.BufferStateWrapper;
import model.Tuple;
import model.TupleWriter;
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
public abstract class Operator implements TupleWriter {

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
     * for debugging, get all the tuples at once and put them in a file.
     * @param i the index of the output file.
     */
    public void dump(int i) {
        String path = Catalog.getInstance().getOutputPath();
        BufferedWriter output;
        try {
            File file = new File(path + i);
            StringBuilder sb = new StringBuilder();
            output = new BufferedWriter(new FileWriter(file));
            Tuple tuple = getNextTuple();
            while(tuple != null){
                sb.append(tuple.toString());
                sb.append("\n");
                System.out.println(tuple);
                tuple = getNextTuple();
            }
            output.write(sb.toString());
            output.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
        reset();
    }

    public void dump2(int i, String s) {
        String path = Catalog.getInstance().getOutputPath();
        BufferedWriter output;
        try{
            File file = new File(s + i);
            FileOutputStream fout = new FileOutputStream(file);
            FileChannel fc = fout.getChannel();
            BufferStateWrapper bufferStateWrapper = new BufferStateWrapper(2 * Constants.INT_SIZE,
                    ByteBuffer.allocate(Constants.PAGE_SIZE), getSchema().size());

            while (writeNextTuple(bufferStateWrapper)) {
                // the buffer has no space for a tuple
                if (!bufferStateWrapper.hasSpace()) {
                    bufferStateWrapper.writeBuffer(fc);
                    bufferStateWrapper = new BufferStateWrapper(2 * Constants.INT_SIZE,
                            ByteBuffer.allocate(Constants.PAGE_SIZE), getSchema().size());
                }
                else {

                }
            }
//
//            while((byt = writePage())!=null){
//                byt.limit(byt.capacity());
//                byt.position(0);
//                fc.write(byt);
//            }
            fout.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @return the current schema of the operator
     */
    public abstract Map<String, Integer> getSchema();

    @Override
    public boolean writeNextTuple(BufferStateWrapper bufferStateWrapper) {
        Tuple tuple = getNextTuple();
        if (tuple == null) {
            return false;
        }
        bufferStateWrapper.putTuple(tuple);
        return true;
    }

    /**
     * @return an list of children operation
     */
    public abstract Operator[] getChildren();


    /**
     * Abstract method for accepting PhysicalPlanBuilder visitor,
     * in which the visitor would visit the operator
     * @param visitor PhysicalPlanBuilder visitor to be accepted.
     */
    public abstract void accept(PhysicalPlanBuilder visitor);
}
