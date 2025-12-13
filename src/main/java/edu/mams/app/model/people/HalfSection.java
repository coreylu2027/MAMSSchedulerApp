package edu.mams.app.model.people;

public class HalfSection extends Group {
    private Section parentSection; // composition link

    public HalfSection(String name) {
        super(name);
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
        return name + " (of " + parentSection.getName() + ")";
    }
}