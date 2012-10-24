/**
 * Name: David Byrne
 * Student Id: 09068783
 * @author david
 */


import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * The actual Tic-Tac-Toe game. Each player starts a separate instance of the game.
 * First initializes all the variables including a double array which represents 
 * the grid within the game. It also sets up a JFrame to represent the gui.
 */

public class TicTacToe implements ActionListener {
	
	private TicPlayer player;
	private int grid[][] = new int[3][3];
	private boolean inProgress;
	public boolean playerTurn;
	private JButton buttons[][] = new JButton[3][3];
	private JFrame frame;
	private JTextArea terminal;
	private String side;
	private String otherSide;
	private int playerNum;
	private int otherPlayer;
	JScrollPane scrollPane;
	
	public TicTacToe(TicPlayer player, boolean turn){
		this.player = player;
		playerTurn = turn;
		if(playerTurn){side="X";playerNum=1;otherSide="O";otherPlayer=2;}
		else{side="O";playerNum=2;otherSide="X";otherPlayer=1;}
		inProgress = true;
		for(int i=0; i<grid.length; i++){
			for(int j=0; j<grid[i].length; j++){
				grid[i][j] = 0;
			}
		}
		startGame();
	}
	
	private void startGame(){
		System.out.println("Game starting...");
		
		frame = new JFrame("TicTacToe");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		setUpBoard(frame.getContentPane());
		
		frame.setSize(450, 575);
		frame.setVisible(true);
		
		terminalPrintln("You are player "+playerNum);
		
		System.out.println("Player 1 turn:");
		terminalPrintln("Game starting...");
		terminalPrintln("Player 1 turn:");
	}
	
	public void setUpBoard(Container pane){
		int buttonSize = 150;
		pane.setLayout(null);
		
		for(int i=0; i<buttons.length; i++){
			for(int j=0; j<buttons[i].length; j++){
				buttons[i][j] = new JButton();
				buttons[i][j].setFont(new Font("Arial", Font.BOLD, 120));
				pane.add(buttons[i][j]);
				buttons[i][j].setBounds(j*buttonSize, i*buttonSize, buttonSize, buttonSize);
				buttons[i][j].addActionListener(this);
			}
		}
		
		terminal = new JTextArea(5, 20);
		terminal.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(terminal);
		pane.add(scrollPane);
		scrollPane.setBounds(0, 450, 450, 100);
	}
	
	public void terminalPrintln(String line){
		terminal.append(line+"\n");
		terminal.setCaretPosition(terminal.getDocument().getLength());
	}
	
	public boolean checkGrid(){
		if(checkLeftDiagonal() || checkRightDiagonal()){return false;}
		for(int i=0; i<grid.length; i++){
			if(checkRow(i)){return false;}
			if(checkColumn(i)){return false;}
		}
		return true;
	}
	
	public boolean checkLeftDiagonal(){
		int temp = grid[0][0];
		for(int i=1, j=1; i<grid.length; i++, j++){
			if(temp == 0){return false;}
			if(grid[i][j] != temp){return false;}
			temp = grid[i][j];
		}
		
		System.out.println("Player " + grid[1][1] + " is the winner");
		terminalPrintln("Player " + grid[1][1] + " is the winner");
		System.out.println("Game ending...");
		terminalPrintln("Game ending...");
		return true;
	}
	
	public boolean checkRightDiagonal(){
		int temp  = grid[0][2];
		for(int i=1, j=1; i<grid.length; i++, j--){
			if(temp == 0){return false;}
			if(grid[i][j] != temp){return false;}
			temp = grid[i][j];
		}
		
		System.out.println("Player " + grid[1][1] + " is the winner");
		terminalPrintln("Player " + grid[1][1] + " is the winner");
		System.out.println("Game ending...");
		terminalPrintln("Game ending...");
		return true;
	}
	
	public boolean checkColumn(int col){
		int temp = grid[0][col];
		for(int i=1; i<grid.length; i++){
			if(temp == 0){return false;}
			if(grid[i][col] != temp){return false;}
			temp = grid[i][col];
		}
		System.out.println("Player " + grid[0][col] + " is the winner");
		terminalPrintln("Player " + grid[0][col] + " is the winner");
		System.out.println("Game ending...");
		terminalPrintln("Game ending...");
		return true;
	}
	
	public boolean checkRow(int row){
		int temp = grid[row][0];
		for(int i=1; i<grid[row].length; i++){
			if(temp == 0){return false;}
			if(grid[row][i] != temp){return false;}
			temp = grid[row][i];
		}
		System.out.println("Player " + grid[row][0] + " is the winner");
		terminalPrintln("Player " + grid[row][0] + " is the winner");
		System.out.println("Game ending...");
		terminalPrintln("Game ending...");
		return true;
	}
	
	public void restartGame(){
		for(int i=0; i<buttons.length; i++){
			for(int j=0; j<buttons[i].length; j++){
				grid[i][j] = 0;
				buttons[i][j].setText("");
				buttons[i][j].setEnabled(true);
			}
		}
		System.out.println("Player 1 turn:");
		terminalPrintln("Player 1 turn:");
	}
	
	public void enterPos(int x, int y, int player){
		grid[x][y] = player;
	}
	
	public boolean gridFilled(){
		for(int i=0; i<grid.length; i++){
			for(int j=0; j<grid[i].length; j++){
				if(grid[i][j] == 0){return false;}
			}
		}
		return true;
	}
	
	public void actionPerformed(ActionEvent a){
		
		if(inProgress){
			
			for(int i=0; i<buttons.length; i++){
				for(int j=0; j<buttons[i].length; j++){
					if(a.getSource() == buttons[i][j]){
						if(playerTurn){
							buttons[i][j].setText(side);
							buttons[i][j].setEnabled(false);
							System.out.println("Row " + i + ", Column " + j + " has been entered");
							terminalPrintln("Row " + i + ", Column " + j + " has been entered");
							enterPos(i, j, playerNum);
							playerTurn = !playerTurn;
							player.sendMove(i+","+j);
							
							if(gridFilled()){
								System.out.println("Game is a draw, restarting...");
								terminalPrintln("Game is a draw, restarting...");
								restartGame();
							}else if(!checkGrid()){
								player.endGame();
								inProgress = false;
								System.out.println("Game Over.");
								terminalPrintln("Game Over.");
							}
						}else{System.out.println("Not your turn mate.");
						terminalPrintln("Not your turn mate");}
					}
				}
			}
			
		}else{System.out.println("Sorry, the game is over.");
		terminalPrintln("Game Over.");}
	}
	
	/*
	 * An method to update the board based on information the
	 * player receives.
	 */
	public void updateBoard(int[] coord){
		buttons[coord[0]][coord[1]].setText(otherSide);
		buttons[coord[0]][coord[1]].setEnabled(false);
		enterPos(coord[0], coord[1], otherPlayer);
		playerTurn = !playerTurn;
		
		if(gridFilled()){
			System.out.println("Game is a draw, restarting...");
			terminalPrintln("Game is a draw, restarting...");
			restartGame();
		}else if(!checkGrid()){
			player.endGame();
			inProgress = false;
			System.out.println("Game Over.");
			terminalPrintln("Game Over.");
		}
		if(inProgress){
			System.out.println("It is player "+playerNum+"'s turn");
			terminalPrintln("It is player "+playerNum+"'s turn");
		}
	}
	
}
