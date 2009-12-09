/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.panayotis.cafeports.filter.portdata;

import com.panayotis.cafeports.db.PortInfo;
import com.panayotis.cafeports.filter.PortData;
import com.panayotis.cafeports.filter.UpdateablePortData;
import com.panayotis.cafeports.filter.operation.Contains;
import com.panayotis.cafeports.filter.operation.Belongs;
import com.panayotis.cafeports.filter.operation.NotContains;
import com.panayotis.cafeports.filter.operation.NotBelongs;

/**
 *
 * @author teras
 */
public class PortCategory extends PortData implements UpdateablePortData {

    {
        addOperation(new Contains());
        addOperation(new NotContains());
        addOperation(new Belongs());
        addOperation(new NotBelongs());
    }

    public PortCategory() {
        super("Category");
    }

    public String getData(PortInfo p) {
        return p.getData(getTag());
    }

    public String getTag() {
        return "categories";
    }
}
