package edu.mams.app.model.people;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a student section that can contain optional half-sections.
 */
public class Section extends Group {
    private List<HalfSection> halfSections = new ArrayList<>();

    /**
     * Creates an empty section for serialization frameworks.
     */
    public Section() {
    }

    /**
     * Creates a section with the given name.
     *
     * @param name section name
     */
    public Section(String name) {
        super(name);
    }

    /**
     * Adds a half section and sets its parent reference to this section.
     *
     * @param halfSection half section to add
     */
    public void addHalfSection(HalfSection halfSection) {
        halfSections.add(halfSection);
        halfSection.setParentSection(this);
    }

    /**
     * Returns all half sections assigned to this section.
     *
     * @return mutable list of half sections
     */
    public List<HalfSection> getHalfSections() {
        return halfSections;
    }

    @Override
    public String toString() {
        return name;
    }
}
