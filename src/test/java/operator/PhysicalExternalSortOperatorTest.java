package operator;

import com.sql.interpreter.Handler;
import model.Tuple;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import org.junit.Test;
import util.Catalog;
import util.Constants.SortMethod;

import java.io.StringReader;

import static org.junit.Assert.assertEquals;


public class PhysicalExternalSortOperatorTest {

    public PhysicalExternalSortOperatorTest() throws Exception {
        Handler.init(new String[0]);
        Catalog.getInstance().setSortBlockSize(50);
        Catalog.getInstance().setSortMethod(SortMethod.EXTERNAL);
    }

    @Test
    public void getNextTuple() throws Exception {
        String statement = "SELECT * FROM Boats BT, Sailors S ORDER BY BT.F;";
        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        PlainSelect plainSelect = (PlainSelect) ((Select) parserManager.parse(new StringReader(statement))).getSelectBody();
        Catalog.getInstance().setAttributeOrder(plainSelect);
        PhysicalOperator physSortOp = Handler.constructPhysicalQueryPlan(plainSelect);

        Tuple tuple = physSortOp.getNextTuple();
        long last = Long.MIN_VALUE;
        while (tuple != null) {
            long cur = tuple.getDataAt(physSortOp.getSchema().get("BT.F"));
            assertEquals(true, last <= cur);
            tuple = physSortOp.getNextTuple();
        }
    }
}