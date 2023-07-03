package gui;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ChessMenu extends JMenu {
    public ChessMenu(ChessGamePanel gamePanel) {
        super("Options");
        JMenuItem newGame = new JMenuItem(new AbstractAction("New Game") {
            @Override
            public void actionPerformed(ActionEvent e) {
                gamePanel.reset();
            }
        });
        add(newGame);
        JMenuItem loadFEN=new JMenuItem(new AbstractAction("Load FEN") {
            @Override
            public void actionPerformed(ActionEvent e) {
                String fen = (String)JOptionPane.showInputDialog(
                        null,
                        "Please input the FEN Code",
                        "Load FEN",
                        JOptionPane.PLAIN_MESSAGE,
                        null,
                        null,
                        "");
                gamePanel.loadFEN(fen);
            }
        });
        add(loadFEN);
        JMenuItem flipBoard=new JMenuItem(new AbstractAction("Flip Board") {
            @Override
            public void actionPerformed(ActionEvent e) {
                gamePanel.flipBoard();
            }
        });
        add(flipBoard);
    }
}
