package com.sql.interpreter;

import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import operator.PhysicalOperator;
import org.junit.Assert;
import org.junit.Test;

import java.io.StringReader;

import com.sun.org.apache.xml.internal.resolver.Catalog;

public class HandlerTest extends Handler {

    @Test
    public void parseSqlTest() {
    }

    @Test
    public void constructQueryPlan() throws Exception {
        String statement = "SELECT S.A, S.B, Reserves.G, Boats.D FROM Sailors AS S, Reserves, Boats WHERE Reserves.H = Boats.D And S.A = Reserves.G;";
        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        PlainSelect plainSelect = (PlainSelect) ((Select) parserManager.
                parse(new StringReader(statement))).getSelectBody();
        PhysicalOperator op = Handler.constructPhysicalQueryPlan(plainSelect);
        op.dump(1);
    }

    @Test
    public void parserConfigTest() {
        Assert.assertTrue(parserPlanBuilderConfig());
    }

    @Test
    public void parserIndexInfoTest() throws Exception {
        parserIndexInfo();
    }
}

