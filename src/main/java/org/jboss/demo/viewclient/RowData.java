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
import org.jboss.as.cli.gui.tables.TableCalculator;
import org.jboss.dmr.ModelNode;

/**
 * I cheated here with the hard-coded column numbers.  Right now this supports up to 10 columns.
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2012 Red Hat Inc.
 */
public class RowData {

    private TableCalculator tableCalc;
    private int row;

    public RowData(TableCalculator tableCalc, int row) {
        this.tableCalc = tableCalc;
        this.row = row;
    }

    public String getCol_0() {
        return getValue(0);
    }

    public String getCol_1() {
        return getValue(1);
    }

    public String getCol_2() {
        return getValue(2);
    }

    public String getCol_3() {
        return getValue(3);
    }

    public String getCol_4() {
        return getValue(4);
    }

    public String getCol_5() {
        return getValue(5);
    }

    public String getCol_6() {
        return getValue(6);
    }

    public String getCol_7() {
        return getValue(7);
    }

    public String getCol_8() {
        return getValue(8);
    }

    public String getCol_9() {
        return getValue(9);
    }

    private String getValue(int column) {
        return tableCalc.getValueAt(row, column).asString();
    }

    public void setCol_0(String value) {
        setValue(0, value);
    }

    public void setCol_1(String value) {
        setValue(1, value);
    }

    public void setCol_2(String value) {
        setValue(2, value);
    }

    public void setCol_3(String value) {
        setValue(3, value);
    }

    public void setCol_4(String value) {
        setValue(4, value);
    }

    public void setCol_5(String value) {
        setValue(5, value);
    }

    public void setCol_6(String value) {
        setValue(6, value);
    }

    public void setCol_7(String value) {
        setValue(7, value);
    }

    public void setCol_8(String value) {
        setValue(8, value);
    }

    public void setCol_9(String value) {
        setValue(9, value);
    }

    private void setValue(int column, String value) {
        String currentValue = tableCalc.getValueAt(row, column).asString();
        if (currentValue.equals(value)) return;

        ModelNode operation = new ModelNode();
        ModelNode address = tableCalc.getFullAddress(row, column);
        operation.get("address").set(address);
        operation.get("operation").set("write-attribute");
        operation.get("name").set(tableCalc.getColumnName(column));
        operation.get("value").set(value);

        try {
            ModelNode response = ViewManager.client.execute(operation);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
