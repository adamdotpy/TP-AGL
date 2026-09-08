package fr.umfds.spmanager.repository;

import fr.umfds.spmanager.config.DatabaseConfig;
import fr.umfds.spmanager.model.Role;
import fr.umfds.spmanager.model.StudentGroup;
import fr.umfds.spmanager.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GroupRepository {

    public Optional<StudentGroup> findById(int id) {
        String sql = "SELECT id, name FROM student_groups WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    StudentGroup group = new StudentGroup(rs.getInt("id"), rs.getString("name"));
                    group.setMembers(getMembers(group.getId()));
                    return Optional.of(group);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public Optional<StudentGroup> findByName(String name) {
        String sql = "SELECT id, name FROM student_groups WHERE name = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    StudentGroup group = new StudentGroup(rs.getInt("id"), rs.getString("name"));
                    group.setMembers(getMembers(group.getId()));
                    return Optional.of(group);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public StudentGroup save(StudentGroup group) {
        String sql = "INSERT INTO student_groups (name) VALUES (?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, group.getName());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    group.setId(rs.getInt(1));
                }
            }
            return group;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la sauvegarde du groupe", e);
        }
    }

    public void addMember(int groupId, int studentId) {
        String sql = "INSERT INTO group_members (group_id, student_id) VALUES (?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, groupId);
            ps.setInt(2, studentId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'ajout du membre au groupe", e);
        }
    }

    public List<User> getMembers(int groupId) {
        List<User> members = new ArrayList<>();
        String sql = "SELECT u.id, u.login, u.password_hash, u.role, u.first_name, u.last_name " +
                "FROM users u INNER JOIN group_members gm ON u.id = gm.student_id " +
                "WHERE gm.group_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, groupId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    members.add(new User(
                            rs.getInt("id"),
                            rs.getString("login"),
                            rs.getString("password_hash"),
                            Role.valueOf(rs.getString("role")),
                            rs.getString("first_name"),
                            rs.getString("last_name")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return members;
    }

    public Optional<StudentGroup> findGroupByStudentId(int studentId) {
        String sql = "SELECT g.id, g.name FROM student_groups g " +
                "INNER JOIN group_members gm ON g.id = gm.group_id " +
                "WHERE gm.student_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    StudentGroup group = new StudentGroup(rs.getInt("id"), rs.getString("name"));
                    group.setMembers(getMembers(group.getId()));
                    return Optional.of(group);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public int getMembersCount(int groupId) {
        String sql = "SELECT COUNT(*) FROM group_members WHERE group_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, groupId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<StudentGroup> findAll() {
        List<StudentGroup> groups = new ArrayList<>();
        String sql = "SELECT id, name FROM student_groups ORDER BY id ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                StudentGroup group = new StudentGroup(rs.getInt("id"), rs.getString("name"));
                group.setMembers(getMembers(group.getId()));
                groups.add(group);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return groups;
    }
}
