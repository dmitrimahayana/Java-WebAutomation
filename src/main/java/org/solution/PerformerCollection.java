package org.solution;

public class PerformerCollection {
    public String departmentName;
    public String title;
    public String location;
    public String[] description;
    public String[] qualification;
    public String job_type;
    public String postedBy;

    public PerformerCollection(String departmentName, String title, String location, String[] description, String[] qualification, String job_type, String postedBy){
        this.departmentName = departmentName;
        this.title = title;
        this.location = location;
        this.description = description;
        this.qualification = qualification;
        this.job_type = job_type;
        this.postedBy = postedBy;
    }
}
