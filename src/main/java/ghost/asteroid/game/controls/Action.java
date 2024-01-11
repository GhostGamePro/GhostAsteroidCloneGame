package ghost.asteroid.game.controls;

import java.util.ArrayList;
import java.util.Arrays;


public enum Action {
	left(65,37), right(68,39), up(87,38), down(83,40), space(32), enter(10), increase(107, 61), decrease(109, 45);
	
	public ArrayList<Integer> keycodes = new ArrayList<>();
	
	
	Action(Integer... keycodes) {
		this.keycodes = new ArrayList<Integer>(Arrays.asList(keycodes));
	}

	
	public boolean isType(Integer code) {
		return keycodes.contains(code);
	}
	
	
	public static Action getType(Integer value) {
		for (Action code : Action.values()) {
			if (code.keycodes.contains(value)){
				return code;
			}
		}
		return null;
	}
}