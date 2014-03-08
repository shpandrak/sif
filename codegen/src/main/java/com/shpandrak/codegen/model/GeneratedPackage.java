package com.shpandrak.codegen.model;

import java.util.*;

/**
 Copyright (c) 2013, Amit Lieberman
All rights reserved.

                   GNU LESSER GENERAL PUBLIC LICENSE
                       Version 3, 29 June 2007

 Copyright (C) 2007 Free Software Foundation, Inc. <http://fsf.org/>
 Everyone is permitted to copy and distribute verbatim copies
 of this license document, but changing it is not allowed.


  This version of the GNU Lesser General Public License incorporates
the terms and conditions of version 3 of the GNU General Public
License

 * Created with love
 * User: shpandrak
 * Date: 10/13/12
 * Time: 09:48
 */
public class GeneratedPackage {
    private String name;
    private Map<String, GeneratedClass> classes;

    public GeneratedPackage(String name) {
        this.name = name;
        this.classes = new HashMap<String, GeneratedClass>();
    }

    public GeneratedPackage(String name, List<GeneratedClass> classes) {
        this.name = name;
        this.classes  = new HashMap<String, GeneratedClass>(classes.size());
        for (GeneratedClass currClass : classes){
            this.classes.put(currClass.getName(), currClass);
        }
    }

    public void addClass(GeneratedClass generatedClass) {
        classes.put(generatedClass.getName(), generatedClass);
    }

    public void addClasses(Collection<GeneratedClass> generatedClasses) {
        for (GeneratedClass currGeneratedClass : generatedClasses){
            addClass(currGeneratedClass);
        }
    }


    public List<GeneratedClass> getClasses() {
        return new ArrayList<GeneratedClass>(classes.values());
    }

    public String getName() {
        return name;
    }

    public GeneratedClass getClass(String className){
        return this.classes.get(className);
    }
}
