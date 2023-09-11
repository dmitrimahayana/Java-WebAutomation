package org.solution;

public class DispatcherCollection {
    public Integer id;
    public String fullText;
    public String selector;
    public String department;

    public DispatcherCollection(Integer id, String fullText, String selector, String department){
        this.id = id;
        this.fullText = fullText;
        this.selector = selector;
        this.department = department;
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFullText() {
        return this.fullText;
    }

    public void setFullText(String fullText) {
        this.fullText = fullText;
    }

    public String getSelector() {
        return this.selector;
    }

    public void setSelector(String selector) {
        this.selector = selector;
    }

    public String getDepartment() {
        return this.department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }
}
