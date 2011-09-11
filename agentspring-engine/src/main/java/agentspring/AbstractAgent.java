package agentspring;


import org.springframework.data.neo4j.annotation.NodeEntity;

import agentspring.role.Role;

@NodeEntity
public abstract class AbstractAgent implements Agent {

	private String label;

	public AbstractAgent() {
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void act(Role role) {
		role.act(this);
	}

	@Override
	public String toString() {
		if (getLabel() != null) {
			return getLabel();
		} else {
			return super.toString();
		}
	}

}
