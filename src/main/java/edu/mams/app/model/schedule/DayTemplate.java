package edu.mams.app.model.schedule;

import java.util.List;

/**
 * Defines the ordered set of blocks that make up a named school day template.
 */
public class DayTemplate {
    private String name;
    private List<BlockDefinition> blocks;

    /**
     * Creates a template with a display name and block definitions.
     *
     * @param name template name
     * @param blocks block definitions in schedule order
     */
    public DayTemplate(String name, List<BlockDefinition> blocks) {
        this.name = name;
        this.blocks = blocks;
    }

    /**
     * Returns the template name.
     *
     * @return template name
     */
    public String getName() { return name; }

    /**
     * Returns the block definitions for this template.
     *
     * @return blocks in schedule order
     */
    public List<BlockDefinition> getBlocks() { return blocks; }
}
