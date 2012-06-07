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
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.dmr.ModelNode;

/**
 * Backing bean to support the ViewTable and view selector.
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2012 Red Hat Inc.
 */
@ManagedBean
@SessionScoped
public class ViewManager {

    public static ModelControllerClient client;

    private static ModelNode getViewNamesOperation;

    static {
        try {
            client = ModelControllerClient.Factory.create(InetAddress.getByName("localhost"), 9999);
        } catch (Exception e) {
            e.printStackTrace();
        }

        ModelNode op = new ModelNode();
        op.get("operation").set("read-children-names");
        op.get("child-type").set("view");

        ModelNode address = new ModelNode();
        ModelNode subsys = new ModelNode();
        subsys.get("subsystem").set("views");
        address.add(subsys);

        op.get("address").set(address);

        getViewNamesOperation = op;
    }

    private String currentView = null;
    private ViewTable viewTable;

    public ViewManager() throws IOException {
        Map<String, String> viewNames = getViewNames();
        if (viewNames.size() > 0) setCurrentView(viewNames.keySet().iterator().next());
        this.viewTable = new ViewTable(this.currentView);
    }

    public String getCurrentView() {
        return this.currentView;
    }

    public final void setCurrentView(String currentView) throws IOException {
        String oldView = this.currentView;
        this.currentView = currentView;

        if (this.viewTable != null && !currentView.equals(oldView)) {
            this.viewTable.update(this.currentView);
        }
    }

    public final Map<String, String> getViewNames() throws IOException {
        ModelNode result = client.execute(getViewNamesOperation);
        Map<String, String> names = new HashMap<String, String>();
        for (ModelNode name : result.get("result").asList()) {
            names.put(name.asString(), name.asString());
        }
        return names;
    }

    public void saveChanges() throws IOException {
        this.viewTable.update(this.currentView);
    }

    public boolean isEditableView() {
        return this.viewTable.hasWriteableColumns();
    }

    public ViewTable getViewTable() {
        return this.viewTable;
    }

    public void setViewTable(ViewTable viewTable) {
        this.viewTable = viewTable;
    }

}
