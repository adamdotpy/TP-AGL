package fr.umfds.spmanager.repository;

import fr.umfds.spmanager.config.DatabaseConfig;
import fr.umfds.spmanager.model.Subject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SubjectRepository {

    public Optional<Subject> findById(int id) {
        String sql = "SELECT id, title, description, teacher_id FROM subjects WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToSubject(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public List<Subject> findAll() {
        List<Subject> list = new ArrayList<>();
        String sql = "SELECT id, title, description, teacher_id FROM subjects ORDER BY id ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRowToSubject(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Subject save(Subject subject) {
        String sql = "INSERT INTO subjects (title, description, teacher_id) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, subject.getTitle());
            ps.setString(2, subject.getDescription());
            ps.setInt(3, subject.getTeacherId());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    subject.setId(rs.getInt(1));
                }
            }
            return subject;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la sauvegarde du sujet", e);
        }
    }

    public void update(Subject subject) {
        String sql = "UPDATE subjects SET title = ?, description = ?, teacher_id = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, subject.getTitle());
            ps.setString(2, subject.getDescription());
            ps.setInt(3, subject.getTeacherId());
            ps.setInt(4, subject.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la mise a jour du sujet", e);
        }
    }

    public List<Subject> findAllByTeacherId(int teacherId) {
        List<Subject> list = new ArrayList<>();
        String sql = "SELECT id, title, description, teacher_id FROM subjects WHERE teacher_id = ? ORDER BY id ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, teacherId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToSubject(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private Subject mapRowToSubject(ResultSet rs) throws SQLException {
        return new Subject(
                rs.getInt("id"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getInt("teacher_id")
        );
    }
}
