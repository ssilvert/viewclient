/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @author tags. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package org.jboss.demo.viewclient;

import java.io.IOException;
import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlColumn;
import javax.faces.component.html.HtmlDataTable;
import javax.faces.component.html.HtmlInputText;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.context.FacesContext;
import org.jboss.as.cli.gui.tables.TableCalculator;
import org.jboss.as.cli.gui.tables.TableDefinition;
import org.jboss.dmr.ModelNode;

/**
 * HtmlDataTable that dynamically creates its columns from a view definition.
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2012 Red Hat Inc.
 */
public class ViewTable extends HtmlDataTable {

    private TableCalculator tableCalc;

    private Boolean[] writeableColumns;

    public ViewTable(String viewName) throws IOException {
        setStyles();
        update(viewName);
    }

    private void setWritableColumns() throws IOException {
        this.writeableColumns = new Boolean[tableCalc.getColumnCount()];

        for (int i=0; i < tableCalc.getColumnCount(); i++) {
            if (!tableCalc.isAttributeColumn(i)) {
                writeableColumns[i] = Boolean.FALSE;
                continue;
            }

            int row = findNonEmptyRow(i);
            if (row == -1) {
                writeableColumns[i] = Boolean.FALSE;
                continue;
            }

            ModelNode readRscDescOp = makeReadResourceDescOp(row, i);
            ModelNode result = ViewManager.client.execute(readRscDescOp);
            ModelNode accessType = result.get("result", "attributes", tableCalc.getColumnName(i), "access-type");

            if (accessType.asString().equals("read-write")) {
                writeableColumns[i] = Boolean.TRUE;
            } else {
                writeableColumns[i] = Boolean.FALSE;
            }
        }
    }

    private int findNonEmptyRow(int column) {
        int row = 0; // need to find a row that contains a value
        for (int i=0; i < tableCalc.getRowCount(); i++) {
            if (tableCalc.getValueAt(i, column).isDefined()) return row;
        }

        return -1;
    }

    private ModelNode makeReadResourceDescOp(int row, int column) {
        ModelNode op = new ModelNode();
        ModelNode address = tableCalc.getFullAddress(row, column);
        op.get("address").set(address);
        op.get("operation").set("read-resource-description");
        return op;
    }

    public TableCalculator getTableCalculator() {
        return this.tableCalc;
    }

    public final void update(String viewName) throws IOException {
        ModelNode def = ViewManager.client.execute(makeGetDefinitionOperation(viewName));
        this.tableCalc = new TableCalculator();
        populateTableCalc(def);
        setWritableColumns();
        configureTable();
    }

    public boolean hasWriteableColumns() {
        for (Boolean column : writeableColumns) {
            if (column == Boolean.TRUE) return true;
        }

        return false;
    }

    private void setStyles() {
        this.setStyleClass("root");
        this.setHeaderClass("header");
    }

    private void configureTable() {
        getChildren().clear();

        // set all column classes to "column"
        StringBuilder columnClasses = new StringBuilder();
        for (int i=0; i < tableCalc.getColumnCount(); i++) {
            if (writeableColumns[i]) {
                columnClasses.append("attrColumn,");
            } else {
                columnClasses.append("addressColumn,");
            }
        }
        columnClasses.deleteCharAt(columnClasses.length() - 1);
        this.setColumnClasses(columnClasses.toString());

        // create array of row indicies
        RowData[] rowData = new RowData[tableCalc.getRowCount()];
        for (int i=0; i < rowData.length; i++) rowData[i] = new RowData(tableCalc, i);

        setValue(rowData);
        setVar("rowIndex");

        for (int i=0; i < tableCalc.getColumnCount(); i++) {
            String colName = tableCalc.getColumnName(i);

            HtmlOutputText header = new HtmlOutputText();
            header.setValue(colName);

            HtmlColumn column = new HtmlColumn();
            column.setHeader(header);

            UIComponent cellValue;
            if (writeableColumns[i]) {
                cellValue = new HtmlInputText();
            } else {
                cellValue = new HtmlOutputText();
            }

            String expression = "#{rowIndex.col_" + i + "}";
            ValueExpression valExp = makeValueExpression(expression);
            cellValue.setValueExpression("value", valExp);

            column.getChildren().add(cellValue);

            getChildren().add(column);
        }
    }

    private ValueExpression makeValueExpression(String expression) {
        FacesContext facesCtx = FacesContext.getCurrentInstance();
        return facesCtx.getApplication().getExpressionFactory().createValueExpression(facesCtx.getELContext(), expression, String.class);
    }

    private void populateTableCalc(ModelNode def) throws IOException {
        String result = def.get("result").asString();
        ModelNode definition = ModelNode.fromString(result);
        TableDefinition tableDef = new TableDefinition(definition);
        ModelNode tableData = ViewManager.client.execute(tableDef.getCompositeOperation());
        tableCalc.parseRows(tableDef.getBaseAddress(), tableDef.getAttributes(), tableData);
    }

    private static ModelNode makeGetDefinitionOperation(String viewName) {
        ModelNode op = new ModelNode();

        ModelNode subsys = new ModelNode();
        subsys.get("subsystem").set("views");

        ModelNode view = new ModelNode();
        view.get("view").set(viewName);

        ModelNode address = new ModelNode();
        address.add(subsys);
        address.add(view);

        op.get("address").set(address);
        op.get("operation").set("read-attribute");
        op.get("name").set("definition");

        return op;
    }
}
