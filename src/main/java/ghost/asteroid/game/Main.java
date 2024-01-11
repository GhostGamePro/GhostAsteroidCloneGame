package ghost.asteroid.game;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.JFrame;
import ghost.asteroid.game.controls.Action;

public class Main {     
	
	public static void main(String[] args) throws InterruptedException {

		JFrame frame = new JFrame("Java Asteroid Game");
		frame.setSize(new Dimension(500, 500));
		Dimension screenDimension = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setLocation(screenDimension.width/2-frame.getSize().width/2, screenDimension.height/2-frame.getSize().height/2);
		
		// Add Game
		Game game = new Game(new Dimension(500, 500));

		frame.getContentPane().add(game, BorderLayout.CENTER);
		frame.setResizable(false);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		game.setFocusable(true);
		game.requestFocusInWindow();

		// Load and play background music
		try {
			File musicFile = new File("background.wav");
			AudioInputStream audioStream = AudioSystem.getAudioInputStream(musicFile);
			Clip clip = AudioSystem.getClip();
			clip.open(audioStream);
			clip.loop(Clip.LOOP_CONTINUOUSLY);
			clip.start();
		} catch (Exception e) {
			System.out.println("Error loading background music: " + e.getMessage());
		}

		while (true) {
			game.repaint();
			game.tick();
			
			if (game.isGameOver) {
				if (game.keyboard.pressedKeys.contains(Action.space)) {
					frame.remove(game);
					game = new Game(new Dimension(500, 500));
					
					frame.getContentPane().add(game, BorderLayout.CENTER);
					frame.setResizable(false);
					frame.setVisible(true);
					
					game.setFocusable(true);
					game.requestFocusInWindow();
				}
			}

			Thread.sleep(10);
		}
	}
}
