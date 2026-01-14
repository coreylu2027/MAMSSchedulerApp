package edu.mams.app.model.people;

/**
 * Represents a portion or "half" of a Section, serving as a logical subdivision
 * of a parent Section. A HalfSection is associated with a parent Section through
 * a composition link.
 *
 * This class extends the Group class, inheriting its basic grouping functionality
 * and providing additional behavior to associate a HalfSection with a Section.
 */
public class HalfSection extends Group {
    private Section parentSection; // composition link

    public HalfSection(String name) {
        super(name);
    }

    public HalfSection() {
    }

    public HalfSection(String name, Section parentSection) {
        super(name);
        this.parentSection = parentSection;
    }

    public void setParentSection(Section parent) {
        this.parentSection = parent;
    }

    public Section getParentSection() {
        return parentSection;
    }

    @Override
    public String toString() {
        return name;
    }
}