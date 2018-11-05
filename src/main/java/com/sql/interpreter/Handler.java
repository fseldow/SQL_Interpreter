package com.sql.interpreter;

import logical.interpreter.LogicalPlanBuilder;
import logical.operator.Operator;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.ParseException;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import operator.PhysicalOperator;
import util.Catalog;
import util.Constants;
import util.Constants.JoinMethod;
import util.Constants.SortMethod;

import java.io.*;

/**
 * Handler class to parse SQL, construct query plan and handle initialization
 * Created by Yufu Mo
 */
public class Handler {
    /**
     * initialize the file paths and directories
     */
    public static void init(String[] args) throws Exception {
        String outputPath = Constants.OUTPUT_PATH;
        if (args != null && args.length >= 5) {
            if (args[0].charAt(args[0].length() - 1) == '/') {
                args[0] = args[0].substring(0, args[0].length() - 1);
            }
            if (args[1].charAt(args[1].length() - 1) != '/') {
                args[1] = args[1] + "/";
            }
            if (args[2].charAt(args[2].length() - 1) != '/') {
                args[2] = args[2] + "/";
            }
            outputPath = args[1];
            Constants.inputPath = args[0];
            Constants.DATA_PATH = Constants.inputPath + "/db/data/";
            Constants.SCHEMA_PATH = Constants.inputPath + "/db/schema.txt";
            Constants.OUTPUT_PATH = args[1];
            Constants.TEMP_PATH = args[2];
            Constants.SQLQURIES_PATH = Constants.inputPath + "/queries.sql";
            Constants.CONFIG_PATH = Constants.inputPath = "/plan_builder_config.txt";
            System.out.println("Constants.inputPath init");
            System.out.println(Constants.inputPath);
        }
        new File(outputPath).mkdirs();
        new File(Constants.TEMP_PATH).mkdirs();
        final File[] files = new File(outputPath).listFiles();
        for (File f : files) {
            f.delete();
        }
        outputPath += "query";
        Catalog.getInstance().setOutputPath(outputPath);

        Catalog.getInstance().setBuildIndex(args[3]);
        Catalog.getInstance().setEvaluateSQL(args[4]);

        try {
            parserPlanBuilderConfig();
        } catch (Exception e) {
            System.err.println("Plan Builder Config parse failed");
            if (Catalog.getInstance().isEvaluateSQL()) {
                throw e;
            }
        }
        try {
            parserIndexInfo();
        } catch (Exception e) {
            System.err.println("Index Info parse failed");
            if (Catalog.getInstance().isEvaluateSQL() 
                || Catalog.getInstance().isBuildIndex()) 
            {
                throw e;
            }
        }
    }

    public static void buildIndexes() {
        // TODO
    }

    /**
     * called in main function, parse all the queries one by one
     * in the input queries file
     */
    public static void parseSql() {
        try {
            String inputPath = Catalog.getInstance().getSqlQueriesPath();
            CCJSqlParser parser = new CCJSqlParser(new FileReader(inputPath));
            Statement statement;
            int ind = 1;
            while ((statement = parser.Statement()) != null) {
                long startTime = System.currentTimeMillis();

                System.out.println(ind);
                System.out.println("Read statement: " + statement);
                Select select = (Select) statement;
                PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
                PhysicalOperator operator = constructPhysicalQueryPlan(plainSelect);
                operator.dump(ind);
                ind++;

                long endTime = System.currentTimeMillis();
                System.out.println("time: " + (endTime - startTime) + "ms");
            }
        } catch (ParseException e) {
            System.err.println("Exception occurred during parsing");
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }

        deleteTempFiles();
    }

    /**
     * parser the join and sort configuration into the Catalog
     *
     * @return true for no issue
     */
    protected static boolean parserPlanBuilderConfig() throws Exception {
        int[][] ret = new int[2][2];
        File configFile = new File(Constants.CONFIG_PATH);
        try {
            BufferedReader br = new BufferedReader(new FileReader(configFile));
            String join = br.readLine();
            String sort = br.readLine();
            String btree = br.readLine();
            br.close();

            if (!setConfig(ret[0], join)) throw new IOException("Fail to read join config");;
            if (!setConfig(ret[1], sort)) throw new IOException("Fail to read sort config");;

            switch (ret[0][0]) {
                case 0:
                    Catalog.getInstance().setJoinMethod(JoinMethod.TNLJ);
                    break;
                case 1:
                    Catalog.getInstance().setJoinMethod(JoinMethod.BNLJ);
                    Catalog.getInstance().setJoinBlockSize(ret[0][1]);
                    break;
                case 2:
                    Catalog.getInstance().setJoinMethod(JoinMethod.SMJ);
                    break;
                default:
                throw new IOException("Unexpected join method");
            }

            switch (ret[1][0]) {
                case 0:
                    Catalog.getInstance().setSortMethod(SortMethod.IN_MEMORY);
                    break;
                case 1:
                    Catalog.getInstance().setSortMethod(SortMethod.EXTERNAL);
                    Catalog.getInstance().setSortBlockSize(ret[1][1]);
                    break;
                default:
                    throw new IOException("Unexpected sort method");
            }

            Catalog.getInstance().setIndexScan(btree.equals("1"));

        } catch (FileNotFoundException e) {
            System.err.println("Cannot find the target config file");
            throw e;
        } catch (IOException e) {
            System.err.println("Unexpected config file format");
            throw e;
        }
        return true;
    }

    private static boolean setConfig(int[] ret, String config) {
        String[] splitedConfig = config.split("\\s+");
        if (splitedConfig.length == 1) {
            ret[0] = Integer.valueOf(splitedConfig[0]);
            return true;
        }
        if (splitedConfig.length == 2) {
            ret[0] = Integer.valueOf(splitedConfig[0]);
            ret[1] = Integer.valueOf(splitedConfig[1]);
            return true;
        }
        return false;
    }

    public static void parserIndexInfo() throws Exception {
        File file = new File(Catalog.getInstance().getIndexInfoPath());
        BufferedReader br = new BufferedReader(new FileReader(file));
        String config;
        while ((config = br.readLine()) != null) {
            Catalog.getInstance().setIndexConfig(config);
        }
        br.close();
    }

    public static String[] parserInterpreterConfig(String configFile) throws Exception {
        File file = new File(configFile);
        int lineCount = 5;
        String[] ret = new String[lineCount];
        BufferedReader br = new BufferedReader(new FileReader(file));
        for (int i = 0; i < lineCount; i++) {
            ret[i] = br.readLine();
        }
        br.close();
        return ret;
    }

    /**
     * build a logicalPlanTree then convert it to a physical plan
     *
     * @param plainSelect
     * @return the root physical operator
     */
    public static PhysicalOperator constructPhysicalQueryPlan(PlainSelect plainSelect) {
        Operator logicalOperator = LogicalPlanBuilder.constructLogicalPlanTree(plainSelect);
        PhysicalPlanBuilder physPB = new PhysicalPlanBuilder();
        logicalOperator.accept(physPB);
        PhysicalOperator physicalOperator = physPB.getPhysOpChildren().peek();
        return physicalOperator;
    }

    private static void deleteTempFiles() {
        File[] fileList = new File(Catalog.getInstance().getTempPath()).listFiles();
        for (File file : fileList) {
            try {
                deletFile(file);
            } catch (Exception e) {
                //System.out.println(e.getMessage());
            }
        }
    }

    private static void deletFile(File file) throws Exception {
        if (file.getName().contains("temp_")) {
            if (!file.delete()) {
                throw new Exception("Fail to delete temp file: " + file.getName());
            }
        }
    }
}
