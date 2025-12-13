package edu.mams.app.model.schedule;

import java.util.List;

public class DayTemplate {
    private String name;
    private List<BlockDefinition> blocks;

    public DayTemplate(String name, List<BlockDefinition> blocks) {
        this.name = name;
        this.blocks = blocks;
    }

    public String getName() { return name; }
    public List<BlockDefinition> getBlocks() { return blocks; }
}