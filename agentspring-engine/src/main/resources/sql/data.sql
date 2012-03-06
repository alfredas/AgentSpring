set schema agentspring_face;

INSERT INTO sources (id, title, start, script) VALUES (0, 'Cash', 'agent','[v.label, v.cash]');

INSERT INTO visuals (id, title, class, type, yaxis) VALUES (0, 'Cash', 'chart', 'line', 'Euro');

INSERT INTO visuals_sources (visual, source) VALUES (0, 0);