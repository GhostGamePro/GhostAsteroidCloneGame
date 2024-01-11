package ghost.asteroid.game.listeners;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashSet;
import java.util.Set;

import ghost.asteroid.game.controls.Action;

public class KeyboardListener implements KeyListener {

	public Set<Action> pressedKeys = new HashSet<Action>();
	
	

	public void keyPressed(KeyEvent e) {
		Action code = Action.getType(e.getKeyCode());
		System.out.println(e.getKeyCode());
		if (code != null) {
			pressedKeys.add(code);
		}
		
	}

	
	public void keyReleased(KeyEvent e) {
		Action code = Action.getType(e.getKeyCode());
		if (code != null) {
			pressedKeys.remove(code);
		}
	}

	
	public void keyTyped(KeyEvent e) {

	}
}
