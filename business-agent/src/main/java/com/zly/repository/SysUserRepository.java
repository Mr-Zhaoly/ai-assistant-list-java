package com.zly.repository;

import com.zly.model.entity.SysUser;
import com.zly.service.SysUserSchemaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class SysUserRepository {

    @Autowired
    private DataSource dataSource;
    @Autowired
    private SysUserSchemaService schema;

    public Optional<SysUser> findByUsernameOrEmail(String account) {
        String sql = "SELECT " + schema.selectColumns() + " FROM sys_user WHERE (" + schema.col("username") + " = ? OR " + schema.col("email") + " = ?) LIMIT 1";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, account);
            ps.setString(2, account);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    public boolean existsByUsername(String username) {
        String sql = "SELECT 1 FROM sys_user WHERE " + schema.col("username") + " = ? LIMIT 1";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean existsByEmail(String email) {
        String sql = "SELECT 1 FROM sys_user WHERE " + schema.col("email") + " = ? LIMIT 1";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public long insert(SysUser user) {
        String sql = "INSERT INTO sys_user (" + schema.insertColumns() + ") VALUES (" + schema.insertPlaceholders() + ")";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            int idx = 1;
            ps.setString(idx++, user.getUsername());
            ps.setString(idx++, user.getEmail());
            ps.setString(idx++, user.getPasswordHash());
            ps.setString(idx++, user.getStatus());
            ps.setInt(idx++, user.getDeleted() == null ? 0 : user.getDeleted());
            ps.setTimestamp(idx++, Timestamp.valueOf(user.getCreatedAt()));
            ps.setTimestamp(idx++, Timestamp.valueOf(user.getUpdatedAt()));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return 0L;
    }

    public int updatePassword(String account, String passwordHash) {
        String sql = "UPDATE sys_user SET " + schema.col("password_hash") + " = ?, " + schema.col("updated_at") + " = ? WHERE (" + schema.col("username") + " = ? OR " + schema.col("email") + " = ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, passwordHash);
            ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(3, account);
            ps.setString(4, account);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int disableUser(String account) {
        String sql = "UPDATE sys_user SET " + schema.col("status") + " = 'DISABLED', " + schema.col("updated_at") + " = ? WHERE (" + schema.col("username") + " = ? OR " + schema.col("email") + " = ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(2, account);
            ps.setString(3, account);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int logicalDelete(String account) {
        String sql = "UPDATE sys_user SET " + schema.col("deleted") + " = 1, " + schema.col("updated_at") + " = ? WHERE (" + schema.col("username") + " = ? OR " + schema.col("email") + " = ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(2, account);
            ps.setString(3, account);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<SysUser> pageQuery(String name, String email, int page, int size) {
        StringBuilder sql = new StringBuilder("SELECT " + schema.selectColumns() + " FROM sys_user WHERE " + schema.col("deleted") + " = 0");
        List<Object> params = new ArrayList<>();
        if (name != null && !name.isEmpty()) {
            sql.append(" AND " + schema.col("username") + " LIKE ?");
            params.add("%" + name + "%");
        }
        if (email != null && !email.isEmpty()) {
            sql.append(" AND " + schema.col("email") + " LIKE ?");
            params.add("%" + email + "%");
        }
        sql.append(" ORDER BY id DESC LIMIT ? OFFSET ?");
        params.add(size);
        params.add((page - 1) * size);
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            List<SysUser> result = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private SysUser mapRow(ResultSet rs) throws SQLException {
        return SysUser.builder()
                .id(rs.getLong(schema.col("id")))
                .username(rs.getString(schema.col("username")))
                .email(rs.getString(schema.col("email")))
                .passwordHash(rs.getString(schema.col("password_hash")))
                .status(rs.getString(schema.col("status")))
                .deleted(rs.getInt(schema.col("deleted")))
                .createdAt(rs.getTimestamp(schema.col("created_at")) != null ? rs.getTimestamp(schema.col("created_at")).toLocalDateTime() : null)
                .updatedAt(rs.getTimestamp(schema.col("updated_at")) != null ? rs.getTimestamp(schema.col("updated_at")).toLocalDateTime() : null)
                .build();
    }
}
