package fr.umfds.spmanager.model;

import java.util.ArrayList;
import java.util.List;

public class StudentGroup {
    private int id;
    private String name;
    private List<User> members = new ArrayList<>();

    public StudentGroup() {
    }

    public StudentGroup(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public StudentGroup(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<User> getMembers() {
        return members;
    }

    public void setMembers(List<User> members) {
        this.members = members;
    }
}
