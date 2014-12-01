package outpost.group3;

import java.util.*;

import outpost.group3.Loc;

public class Outpost {
	private int id;
	private Loc currentLoc;
	private Loc expectedLoc;
	private Loc targetLoc;
	private boolean updated;
	private int simIndex;
	private String currentStrategy;
	
	public HashMap<String, Object> memory;
	
	Outpost(int id, Loc loc, int simIndex) {
		this.id = id;
		this.currentLoc = loc;
		this.updated = true;
		this.simIndex = simIndex;
		this.currentStrategy = null;
		this.memory = new HashMap<String, Object>();
	}
	
	public int getId() {
		return id;
	}
	
	public Loc getCurrentLoc() {
		return currentLoc;
	}
	
	public void setCurrentLoc(Loc loc) {
		this.currentLoc = loc;
	}
	
	public Loc getExpectedLoc() {
		return expectedLoc;
	}
	
	public void setExpectedLoc(Loc loc) {
		this.expectedLoc = loc;
	}
	
	public Loc getTargetLoc() {
		return targetLoc;
	}
	
	public void setTargetLoc(Loc loc) {
		this.targetLoc = loc;
	}
	
	public int getSimIndex() {
		return simIndex;
	}
	
	public void setSimIndex(int simIndex) {
		this.simIndex = simIndex;
	}
	
	public boolean isUpdated() {
		return updated;
	}
	
	public void setUpdated(boolean updated) {
		this.updated = updated;
	}
	
	public String getStrategy() {
		return currentStrategy;
	}
	
	public void setStrategy(String strategy) {
		this.currentStrategy = strategy;
	}
}
