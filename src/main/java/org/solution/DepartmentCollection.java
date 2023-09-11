package org.solution;

import java.util.List;

public class DepartmentCollection {
    public List<DetailCollection> detailCollections;

    public DepartmentCollection(List<DetailCollection> detailCollections){
        this.detailCollections = detailCollections;
    }

    public List<DetailCollection> getDetailCollection() {
        return detailCollections;
    }
}
