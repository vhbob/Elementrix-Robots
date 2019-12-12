package com.vhbob.elementrixrobots.utils;

public enum RobotTask {

	GRIND("Mob Grinding"), SPAWN("Activating Spawners"), NONE("None");

	private String description;

	public String toString() {
		return description;
	}

	private RobotTask(String description) {
		this.description = description;
	}

	public static RobotTask fromString(String text) {
		for (RobotTask task : RobotTask.values()) {
			if (task.toString().equalsIgnoreCase(text))
				return task;
		}
		return null;
	}

}
