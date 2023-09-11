package org.solution;

public class DetailCollection {
    public String title;
    public String location;
    public String[] description;
    public String[] qualification;
    public String job_type;
    public String postedBy;

    public DetailCollection(String title, String location, String[] description, String[] qualification, String job_type, String postedBy){
        this.title = title;
        this.location = location;
        this.description = description;
        this.qualification = qualification;
        this.job_type = job_type;
        this.postedBy = postedBy;
    }
}
