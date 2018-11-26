package PlanBuilder;

import logical.operator.Operator;
import operator.*;

import java.util.Deque;
import java.util.LinkedList;

public class PhysicalOperatorVisitor {

    private Deque<PhysicalOperator> physOpChildren = new LinkedList<>();
    private StringBuilder phPBTree = new StringBuilder();

    public void visit(PhysicalDuplicateEliminationOperator phDupElimOp, int level) {
        for (int i = 0; i< level; i++) {
            phPBTree.append("-");
        }
        phPBTree.append("DupElim");
        phPBTree.append("\n");
        for (PhysicalOperator child : phDupElimOp.getChildren()) {
            child.accept(this, level + 1);
        }
    }

    public void visit(PhysicalExternalSortOperator phExternalSortOp, int level) {
        for (int i = 0; i< level; i++) {
            phPBTree.append("-");
        }
        phPBTree.append("ExternalSort[");
        // get sort by attribute
        phPBTree.append(phExternalSortOp.getOrder().get(0));
        phPBTree.append("]\n");
        for (PhysicalOperator child : phExternalSortOp.getChildren()) {
            child.accept(this, level + 1);
        }
    }

    public void visit(PhysicalMemorySortOperator memorySortOp, int level) {
        for (int i = 0; i< level; i++) {
            phPBTree.append("-");
        }
        phPBTree.append("MemorySort[");
        phPBTree.append(memorySortOp.getOrder().get(0));
        phPBTree.append("]\n");
        for (PhysicalOperator child : memorySortOp.getChildren()) {
            child.accept(this, level + 1);
        }
    }

    public void visit(PhysicalProjectOperator phProjOp, int level) {
        for (int i = 0; i< level; i++) {
            phPBTree.append("-");
        }
        phPBTree.append("Project[");
        phPBTree.append(phProjOp.getSelectItems().toString());
        phPBTree.append("]\n");
        for (PhysicalOperator child : phProjOp.getChildren()) {
            child.accept(this, level + 1);
        }
    }

    public void visit(PhysicalSortMergeJoinOperator sMJOp, int level) {
        for (int i = 0; i< level; i++) {
            phPBTree.append("-");
        }
        phPBTree.append("SMJ[");
        phPBTree.append(sMJOp.getJoinCondition().toString());
        phPBTree.append("]\n");
        for (PhysicalOperator child : sMJOp.getChildren()) {
            child.accept(this, level + 1);
        }
    }

    public void visit(PhysicalBlockJoinOperator blockJoinOp, int level) {
        for (int i = 0; i< level; i++) {
            phPBTree.append("-");
        }
        phPBTree.append("BNLJ[");
        phPBTree.append(blockJoinOp.getJoinCondition().toString());
        phPBTree.append("]\n");
        for (PhysicalOperator child : blockJoinOp.getChildren()) {
            child.accept(this, level + 1);
        }
    }

    public void visit(PhysicalTupleJoinOperator tupleJoinOp, int level) {
        for (int i = 0; i< level; i++) {
            phPBTree.append("-");
        }
        phPBTree.append("TNLJ[");
        phPBTree.append(tupleJoinOp.getJoinCondition().toString());
        phPBTree.append("]\n");
        for (PhysicalOperator child : tupleJoinOp.getChildren()) {
            child.accept(this, level + 1);
        }
    }

    public void visit(PhysicalSelectOperator phSelectOp, int level) {
        for (int i = 0; i< level; i++) {
            phPBTree.append("-");
        }
        phPBTree.append("Select[");
        phPBTree.append(phSelectOp.getExpression().toString());
        phPBTree.append("]\n");
        for (PhysicalOperator child : phSelectOp.getChildren()) {
            child.accept(this, level + 1);
        }
    }
    public void visit(PhysicalScanOperator phScanOp, int level) {
        for (int i = 0; i< level; i++) {
            phPBTree.append("-");
        }
        phPBTree.append("Select[");
        //phPBTree.append(phScanOp.getExpression().toString());
        phPBTree.append("]\n");
    }
}
