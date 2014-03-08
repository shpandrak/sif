package com.shpandrak.metadata.model;

/**
 * Copyright (c) 2013, Amit Lieberman
 * All rights reserved.
 * <p/>
 * GNU LESSER GENERAL PUBLIC LICENSE
 * Version 3, 29 June 2007
 * <p/>
 * Copyright (C) 2007 Free Software Foundation, Inc. <http://fsf.org/>
 * Everyone is permitted to copy and distribute verbatim copies
 * of this license document, but changing it is not allowed.
 * <p/>
 * This version of the GNU Lesser General Public License incorporates
 * the terms and conditions of version 3 of the GNU General Public
 * License
 * <p/>
 * Created with love
 * User: shpandrak
 * Date: 11/9/13
 * Time: 15:20
 */
public class CustomProperty {
    private String name;
    private String value;

    protected CustomProperty() {
    }

    public CustomProperty(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
