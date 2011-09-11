package agentspring.face.model.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import agentspring.face.model.Source;
import agentspring.face.model.Table;
import agentspring.face.model.visual.ChartVisual;
import agentspring.face.model.visual.ScatterVisual;
import agentspring.face.model.visual.Visual;

@Repository
public class VisualDAO {
    private JdbcTemplate jdbcTemplate;

    private HashMap<Integer, Visual> selectedVisuals = null;

    private class VisualSourcesRowMapper implements RowMapper<Integer> {
        @Override
        public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
            return rs.getInt("source");
        }
    }

    private class FullVisualRowMapper implements RowMapper<Visual> {
        public FullVisualRowMapper() {
            VisualDAO.this.selectedVisuals = new HashMap<Integer, Visual>();
        }

        @Override
        public Visual mapRow(ResultSet rs, int rowNum) throws SQLException {
            HashMap<Integer, Visual> map = VisualDAO.this.selectedVisuals;
            Integer id = rs.getInt("id");
            String title = rs.getString("title");
            String clazz = rs.getString("class");
            int source = rs.getInt("source");
            Visual visual = null;
            if (map.get(id) == null) {
                if (clazz.equals(ChartVisual.clazz)) {
                    String type = rs.getString("type");
                    String yaxis = rs.getString("yaxis");
                    visual = new ChartVisual(id, title, type, yaxis);
                    map.put(id, visual);
                } else if (clazz.equals(ScatterVisual.clazz)) {
                    String yaxis = rs.getString("yaxis");
                    visual = new ScatterVisual(id, title, yaxis);
                    map.put(id, visual);
                } else {
                    throw new RuntimeException("Visual type '" + clazz
                            + "' is invalid");
                }
            } else {
                visual = map.get(id);
            }
            visual.addSource(new Source(source, null));
            return null;
        }
    }

    public Visual getVisual(int id) {
        final String sql = "SELECT * FROM " + Table.VISUALS + " JOIN "
                + Table.VISUALS_SOURCES + " ON id = visual WHERE id = ?";
        final Object[] args = new Object[] { id };
        jdbcTemplate.query(sql, args, new FullVisualRowMapper());
        if (this.selectedVisuals.values().size() == 0)
            return null;
        else
            return new ArrayList<Visual>(this.selectedVisuals.values()).get(0);
    }

    public List<Visual> listFullVisuals() {
        final String sql = "SELECT * FROM " + Table.VISUALS + " JOIN "
                + Table.VISUALS_SOURCES + " ON id = visual";
        jdbcTemplate.query(sql, new FullVisualRowMapper());
        return new ArrayList<Visual>(this.selectedVisuals.values());
    }

    private int saveVisual(Visual visual) {
        Integer id = visual.getId();
        if (id == null) {
            final String sql = "INSERT INTO " + Table.VISUALS
                    + " (title) VALUES (?);";
            final Object[] args = new Object[] { visual.getTitle() };
            jdbcTemplate.update(sql, args);
            id = jdbcTemplate.queryForInt("CALL IDENTITY();");
        } else {
            final String sql = "UPDATE " + Table.VISUALS
                    + " SET title = ? WHERE id = ?";
            final Object[] args = new Object[] { visual.getTitle(), id };
            jdbcTemplate.update(sql, args);
        }
        // update visual-sources relationships
        String sql = "SELECT * FROM " + Table.VISUALS_SOURCES
                + " WHERE visual = ?";
        List<Integer> sources = jdbcTemplate.query(sql, new Object[] { id },
                new VisualSourcesRowMapper());
        for (int source : visual.getSourcesIds()) {
            if (!sources.contains(source)) {
                sql = "INSERT INTO " + Table.VISUALS_SOURCES
                        + " (visual, source) VALUES (?, ?);";
                final Object[] args = new Object[] { id, source };
                jdbcTemplate.update(sql, args);
            }
        }
        for (Integer source : sources) {
            if (!visual.getSourcesIds().contains(source)) {
                sql = "DELETE FROM " + Table.VISUALS_SOURCES
                        + " WHERE visual = ? AND source = ?";
                final Object[] args = new Object[] { id, source };
                jdbcTemplate.update(sql, args);
            }
        }
        return id;
    }

    public int saveChartVisual(ChartVisual visual) {
        int id = this.saveVisual(visual);
        final String sql = "UPDATE " + Table.VISUALS
                + " SET class = ?, type = ?, yaxis = ? WHERE id = ?";
        final Object[] args = new Object[] { "chart", visual.getType(),
                visual.getYaxis(), id };
        jdbcTemplate.update(sql, args);
        return id;
    }

    public int saveScatterVisual(ScatterVisual visual) {
        int id = this.saveVisual(visual);
        final String sql = "UPDATE " + Table.VISUALS
                + " SET class = ?, yaxis = ? WHERE id = ?";
        final Object[] args = new Object[] { "scatter", visual.getYaxis(), id };
        jdbcTemplate.update(sql, args);
        return id;
    }

    public void delete(int id) {
        String sql = "DELETE FROM " + Table.VISUALS + " WHERE id = ?";
        final Object[] args = new Object[] { id };
        jdbcTemplate.update(sql, args);
    }

    @Autowired
    public void setDataSource(javax.sql.DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }
}
