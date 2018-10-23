package operator;

import logical.operator.SortOperator;
import model.Tuple;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

/**
 * PhysicalSortOperator
 * created by Yufu Mo
 */
public class PhysicalMemorySortOperator extends PhysicalSortOperator {

    // stores tuples
    private int currentIndex;
    private int recordIndex;

    /**
     * Constructor
     * read all tuples, store them in a list and sort them
     *
     * @param operator
     * @param plainSelect
     */
    public PhysicalMemorySortOperator(PhysicalOperator operator, PlainSelect plainSelect) {
        super(operator, plainSelect);

        // initialize the list
        currentIndex = 0;
        Tuple tuple = operator.getNextTuple();
        while (tuple != null) {
            tupleList.add(tuple);
            tuple = operator.getNextTuple();
        }

        Collections.sort(tupleList, new TupleComparator());
        operator.reset();
    }

    public PhysicalMemorySortOperator(SortOperator logSortOp, Deque<PhysicalOperator> physChildren) {
        super(logSortOp, physChildren);

        tupleList = new ArrayList<>();
        // initialize the list
        currentIndex = 0;
        Tuple tuple = physChild.getNextTuple();
        while (tuple != null) {
            tupleList.add(tuple);
            tuple = physChild.getNextTuple();
        }

        Collections.sort(tupleList, new TupleComparator());
        physChild.reset();
    }

    public PhysicalMemorySortOperator(List<OrderByElement> order, Deque<PhysicalOperator> physChildren) {
        super(order, physChildren);

        tupleList = new ArrayList<>();
        // initialize the list
        currentIndex = 0;
        Tuple tuple = physChild.getNextTuple();
        while (tuple != null) {
            tupleList.add(tuple);
            tuple = physChild.getNextTuple();
        }

        Collections.sort(tupleList, new TupleComparator());
        physChild.reset();
    }

    /**
     * get the next tuple of the operator.
     */
    @Override
    public Tuple getNextTuple() {
        Tuple tuple = null;
        if (currentIndex < tupleList.size()) {
            tuple = tupleList.get(currentIndex);
        }
        currentIndex++;
        return tuple;

    }

    /**
     * reset the operator.
     */
    @Override
    public void reset() {
        currentIndex = 0;
    }

    /**
     * make a stamp to record tuple reader
     */
    @Override
    public void recordTupleReader() {
        this.recordIndex = currentIndex;
    }

    /**
     * revert to the record tuple reader
     */
    @Override
    public void setRecordTupleReader() {
        currentIndex = recordIndex;
    }
}