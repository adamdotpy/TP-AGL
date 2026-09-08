package fr.umfds.spmanager.model;

public class Preference {
    private int groupId;
    private int rank;
    private int subjectId;

    public Preference() {
    }

    public Preference(int groupId, int rank, int subjectId) {
        this.groupId = groupId;
        this.rank = rank;
        this.subjectId = subjectId;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public int getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(int subjectId) {
        this.subjectId = subjectId;
    }
}
