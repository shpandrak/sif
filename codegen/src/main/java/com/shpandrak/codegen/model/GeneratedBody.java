package com.shpandrak.codegen.model;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: shpandrak
 * Date: 10/13/12
 * Time: 09:27
 */
public class GeneratedBody implements ITypesContainer{
    List<GeneratedCodeLine> lines;

    public GeneratedBody() {
        lines = new ArrayList<GeneratedCodeLine>();
    }

    public GeneratedBody(List<GeneratedCodeLine> lines) {
        this.lines = new ArrayList<GeneratedCodeLine>(lines);
    }

    public GeneratedBody(GeneratedCodeLine singleLine) {
        this(Arrays.asList(singleLine));
    }

    public List<GeneratedCodeLine> getLines() {
        return lines;
    }

    public void addLines(Collection<GeneratedCodeLine> lines){
        this.lines.addAll(lines);
    }
    public void addLine(GeneratedCodeLine line){
        lines.add(line);
    }

    @Override
    public void getImports(Set<String> imports) {
        for (GeneratedCodeLine currLine : lines){
            currLine.getImports(imports);
        }
    }
}
