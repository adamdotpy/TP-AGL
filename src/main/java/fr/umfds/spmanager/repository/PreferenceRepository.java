package fr.umfds.spmanager.repository;

import fr.umfds.spmanager.config.DatabaseConfig;
import fr.umfds.spmanager.model.Preference;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PreferenceRepository {

    public void savePreferences(int groupId, List<Integer> subjectIds) {
        String deleteSql = "DELETE FROM preferences WHERE group_id = ?";
        String insertSql = "INSERT INTO preferences (group_id, rank, subject_id) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement deletePs = conn.prepareStatement(deleteSql)) {
                    deletePs.setInt(1, groupId);
                    deletePs.executeUpdate();
                }

                try (PreparedStatement insertPs = conn.prepareStatement(insertSql)) {
                    for (int i = 0; i < subjectIds.size(); i++) {
                        insertPs.setInt(1, groupId);
                        insertPs.setInt(2, i + 1);
                        insertPs.setInt(3, subjectIds.get(i));
                        insertPs.addBatch();
                    }
                    insertPs.executeBatch();
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException("Erreur lors de l'enregistrement des preferences", e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur de connexion a la base", e);
        }
    }

    public List<Preference> findByGroupId(int groupId) {
        List<Preference> list = new ArrayList<>();
        String sql = "SELECT group_id, rank, subject_id FROM preferences WHERE group_id = ? ORDER BY rank ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, groupId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Preference(
                            rs.getInt("group_id"),
                            rs.getInt("rank"),
                            rs.getInt("subject_id")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list; // [] if empty?
    }

    public boolean hasPreferences(int groupId) {
        String sql = "SELECT 1 FROM preferences WHERE group_id = ? LIMIT 1";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, groupId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
