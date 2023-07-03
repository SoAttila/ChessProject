package gui;

import model.Logic;

import javax.swing.*;
import java.awt.*;

public class ChessFrame extends JFrame {
    private final ChessGamePanel gamePanel;
    private final MoveNotationPanel moveNotationPanel;
    private final JMenuBar menuBar;
    private final ChessMenu menu;

    public ChessFrame() {
        setTitle(GuiConstants.TITLE);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(GuiConstants.FRAME_LENGTH, GuiConstants.FRAME_LENGTH));
        setResizable(false);
        moveNotationPanel=new MoveNotationPanel();
        gamePanel = new ChessGamePanel(new Logic(),moveNotationPanel.getListModel());
        getContentPane().add(gamePanel, BorderLayout.CENTER);
        getContentPane().add(moveNotationPanel,BorderLayout.EAST);
        menuBar=new JMenuBar();
        add(menuBar,BorderLayout.NORTH);
        menu=new ChessMenu(gamePanel);
        menuBar.add(menu);

        revalidate();
        repaint();
        pack();
        setLocationRelativeTo(null);
    }

    public ChessGamePanel getGamePanel() {
        return gamePanel;
    }
}
