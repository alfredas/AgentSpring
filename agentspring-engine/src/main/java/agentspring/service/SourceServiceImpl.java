package agentspring.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import agentspring.db.Table;
import agentspring.facade.SourceService;
import agentspring.facade.db.Source;

public class SourceServiceImpl implements SourceService {

    private JdbcTemplate jdbcTemplate;

    private class SourceRowMapper implements RowMapper<Source> {

        @Override
        public Source mapRow(ResultSet rs, int rowNum) throws SQLException {
            int id = rs.getInt("id");
            String start = rs.getString("start");
            String script = rs.getString("script");
            String title = rs.getString("title");
            return new Source(id, title, start, script);
        }

    }

    @Autowired
    public void setDataSource(javax.sql.DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void delete(int id) {
        String sql = "DELETE FROM " + Table.SOURCES + " WHERE id = ?";
        final Object[] args = new Object[] { id };
        jdbcTemplate.update(sql, args);
    }

    public int saveSource(Source source) {
        Integer id = source.getId();
        String title = source.getTitle();
        if (id == null) {
            // create new data source
            String sql = "INSERT INTO " + Table.SOURCES + " (id, title, start, script) VALUES (?, ?, ?, ?);";
            final Object[] args = new Object[] { id, title, source.getStart(), source.getScript() };
            this.jdbcTemplate.update(sql, args);
            return this.jdbcTemplate.queryForInt("CALL IDENTITY();");
        } else {
            // update data source
            String sql = "UPDATE " + Table.SOURCES + " SET title = ?, start = ?, script = ? WHERE id = ?";
            final Object[] args = new Object[] { source.getTitle(), source.getStart(), source.getScript(), source.getId() };
            this.jdbcTemplate.update(sql, args);
        }
        return id;
    }

    public Source getSource(int id) {
        String sql = "SELECT * FROM " + Table.SOURCES + " WHERE id = ?";
        final Object[] args = new Object[] { id };
        List<Source> m = jdbcTemplate.query(sql, args, new SourceRowMapper());
        if (m.size() == 0)
            return null;
        else
            return m.get(0);
    }

    public List<Source> listSources() {
        final String sql = "SELECT * FROM " + Table.SOURCES;
        final Object[] args = new Object[] {};
        return jdbcTemplate.query(sql, args, new SourceRowMapper());
    }

}
