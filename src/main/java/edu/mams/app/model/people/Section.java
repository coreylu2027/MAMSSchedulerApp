package edu.mams.app.model.people;

import java.util.ArrayList;
import java.util.List;

public class Section extends Group {
    private List<HalfSection> halfSections = new ArrayList<>();

    public Section() {
    }

    public Section(String name) {
        super(name);
    }

    // Add a half section and automatically link it back
    public void addHalfSection(HalfSection halfSection) {
        halfSections.add(halfSection);
        halfSection.setParentSection(this);
    }

    public List<HalfSection> getHalfSections() {
        return halfSections;
    }

    @Override
    public String toString() {
        return name;
    }
}