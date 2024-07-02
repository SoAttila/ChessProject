package gui;

import model.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;

public class ChessGamePanel extends JPanel {
    private final ChessMouseListener chessMouseListener=new ChessMouseListener();
    private final ChessMouseMotionListener chessMouseMotionListener=new ChessMouseMotionListener();
    private Logic logic;
    private final ChessSquare[][] squares;
    private ArrayList<ChessSquare> legalSquares;
    private final PromotionPanel promotionPanel;
    private ChessSquare draggedSquare;
    private PieceType selectedPromotion;
    private boolean isBoardFlipped;
    private final DefaultListModel<String> listModel;
    private final NotationLabel[] fileNotationLabels;
    private final NotationLabel[] rankNotationLabels;
    private GameMode gameMode;
    private ChessGamePanel gamePanel;

    public ChessGamePanel(Logic logic,DefaultListModel<String> listModel) {
        this.logic = logic;
        this.listModel=listModel;
        squares = new ChessSquare[ModelConstants.BOARD_SIZE][ModelConstants.BOARD_SIZE];
        draggedSquare = null;
        legalSquares = new ArrayList<>();
        promotionPanel = new PromotionPanel();
        selectedPromotion = null;
        isBoardFlipped=false;
        fileNotationLabels=new NotationLabel[ModelConstants.BOARD_SIZE];
        rankNotationLabels=new NotationLabel[ModelConstants.BOARD_SIZE];
        gameMode=GameMode.ENGINE;
        gamePanel=this;
        FlowLayout layout = new FlowLayout();
        layout.setHgap(0);
        layout.setVgap(0);
        setLayout(layout);

        for (int rank = 0; rank < ModelConstants.BOARD_SIZE; ++rank) {
            rankNotationLabels[rank]=new NotationLabel((char)(ModelConstants.BOARD_SIZE-rank+'0'));
            add(rankNotationLabels[rank]);
            for (int file = 0; file < ModelConstants.BOARD_SIZE; ++file) {
                squares[rank][file] = new ChessSquare(logic.getBoard().getPiece(new Position(file, rank)), rank, file);
                squares[rank][file].addMouseListener(chessMouseListener);
                squares[rank][file].addMouseMotionListener(chessMouseMotionListener);
                add(squares[rank][file]);
            }
        }
        add(new NotationLabel(' '));
        for (int file=0;file< ModelConstants.BOARD_SIZE;++file) {
            fileNotationLabels[file]=new NotationLabel((char)('a'+file));
            add(fileNotationLabels[file]);
        }
        for (int i = 0; i < ModelConstants.POSSIBLE_PROMOTIONS; ++i) {
            promotionPanel.getPromotionLabel(i).addMouseListener(new ChessPromotionMouseListener());
        }
        setPreferredSize(new Dimension(GuiConstants.FRAME_LENGTH, GuiConstants.FRAME_LENGTH));
    }

    @Override
    public void paintComponent(Graphics graphics) {
        requestFocusInWindow();
        super.paintComponent(graphics);
        Graphics2D graphics2D = (Graphics2D) graphics;
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        for (int rank = 0; rank < ModelConstants.BOARD_SIZE; ++rank) {
            for (int file = 0; file < ModelConstants.BOARD_SIZE; ++file) {
                graphics2D.setPaint((rank + file) % 2 == (isBoardFlipped?1:0) ? Color.WHITE : Color.GRAY);
                graphics2D.fillRect((rank+1) * GuiConstants.SQUARE_SIZE, file * GuiConstants.SQUARE_SIZE, GuiConstants.SQUARE_SIZE, GuiConstants.SQUARE_SIZE);
                squares[rank][file].updateSquare(logic.getBoard().getPiece(new Position(adjustCoordinate(file), adjustCoordinate(rank))), logic.isInCheck(), logic.getTurn());
            }
        }

        for (ChessSquare square : legalSquares) {
            graphics2D.setPaint((square.getFile() + square.getRank()) % 2 == (isBoardFlipped?1:0) ? GuiConstants.LIGHT_RED : GuiConstants.DARK_RED);
            graphics2D.fillRect((adjustCoordinate(square.getFile())+1) * GuiConstants.SQUARE_SIZE, adjustCoordinate(square.getRank()) * GuiConstants.SQUARE_SIZE, GuiConstants.SQUARE_SIZE, GuiConstants.SQUARE_SIZE);
        }

        Toolkit.getDefaultToolkit().sync();
        if (draggedSquare == null) revalidate();
    }

    private class ChessMouseListener extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent event) {
            ChessSquare selectedSquare = (ChessSquare) event.getSource();
            Piece selectedPiece=logic.getBoard().getPiece(new Position(adjustCoordinate(selectedSquare.getFile()), adjustCoordinate(selectedSquare.getRank())));
            if (event.getButton() == MouseEvent.BUTTON1 && selectedPiece!= null && selectedPiece.getColor() == logic.getTurn()) {
                draggedSquare = selectedSquare;
                ArrayList<Position> legalMoves = logic.getLegalMoves(new Position(adjustCoordinate(draggedSquare.getFile()), adjustCoordinate(draggedSquare.getRank())));
                for (Position pos : legalMoves) {
                    legalSquares.add(squares[pos.getRank()][pos.getFile()]);
                }
                repaint();
            }
        }

        @Override
        public void mouseReleased(MouseEvent event) {
            if (event.getButton() == MouseEvent.BUTTON1 && draggedSquare != null) {
                Point destinationPoint = event.getLocationOnScreen();
                SwingUtilities.convertPointFromScreen(destinationPoint, ((ChessSquare) event.getSource()).getParent());
                int destinationFile = (((int) destinationPoint.getX()) / GuiConstants.SQUARE_SIZE) - 1;
                int destinationRank = ((int) destinationPoint.getY()) / GuiConstants.SQUARE_SIZE;
                if (-1 < destinationFile && destinationRank < ModelConstants.BOARD_SIZE) {
                    ChessSquare destinationSquare = squares[destinationRank][destinationFile];
                    MoveType moveType = logic.getMoveType(new Move(new Position(adjustCoordinate(draggedSquare.getFile()), adjustCoordinate(draggedSquare.getRank())), new Position(adjustCoordinate(destinationSquare.getFile()), adjustCoordinate(destinationSquare.getRank()))), logic.getBoard(), logic.getTurn());
                    if (moveType == MoveType.PROMOTION) {
                        promotionPanel.setColor(logic.getTurn());
                        promotionPanel.updateIcons();
                        JOptionPane.showOptionDialog(null, promotionPanel, "Promotion", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, new Object[]{}, null);
                        try {
                            synchronized (logic) {
                                while (selectedPromotion == null) {
                                    logic.wait();
                                }
                            }
                        } catch (InterruptedException e) {
                            System.err.println("Interruption error");
                        }
                        logic.move(new Move(new Position(adjustCoordinate(draggedSquare.getFile()), adjustCoordinate(draggedSquare.getRank())), new Position(adjustCoordinate(destinationSquare.getFile()), adjustCoordinate(destinationSquare.getRank()))), selectedPromotion);
                        selectedPromotion = null;
                    } else
                        logic.move(new Move(new Position(adjustCoordinate(draggedSquare.getFile()), adjustCoordinate(draggedSquare.getRank())), new Position(adjustCoordinate(destinationSquare.getFile()), adjustCoordinate(destinationSquare.getRank()))), null);
                    if (moveType!=MoveType.ILLEGAL){
                        updateList();
                        if (moveType!=MoveType.ILLEGAL && logic.getGameState()==GameState.IN_PROGRESS && logic.getGameMode()==GameMode.ENGINE){
                            logic.moveForEngine();
                            updateList();
                        }
                    }
                    draggedSquare.setLocation(draggedSquare.getFile() * GuiConstants.SQUARE_SIZE, draggedSquare.getRank() * GuiConstants.SQUARE_SIZE);
                    if (logic.getGameState() != GameState.IN_PROGRESS) {
                        for (ChessSquare[] rank : squares) {
                            for (ChessSquare square : rank) {
                                square.removeMouseListener(chessMouseListener);
                                square.removeMouseMotionListener(chessMouseMotionListener);
                            }
                        }
                        JOptionPane.showMessageDialog(null, logic.getGameState(), "Result", JOptionPane.INFORMATION_MESSAGE, null);
                    }
                }
                draggedSquare = null;
                legalSquares.clear();
                repaint();
            }
        }
    }

    private class ChessMouseMotionListener extends MouseMotionAdapter {
        @Override
        public void mouseDragged(MouseEvent event) {
            if (draggedSquare != null)
                draggedSquare.setLocation(draggedSquare.getX() - GuiConstants.SQUARE_SIZE / 2 + event.getX(), draggedSquare.getY() - GuiConstants.SQUARE_SIZE / 2 + event.getY());
        }
    }

    private class ChessPromotionMouseListener extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent event) {
            PromotionLabel selectedLabel = (PromotionLabel) event.getSource();
            synchronized (logic) {
                selectedPromotion = selectedLabel.getPieceType();
                logic.notify();
            }
            Window w = SwingUtilities.getWindowAncestor(selectedLabel.getParent());
            if (w != null) {
                w.setVisible(false);
            }
        }
    }

    public void reset() {
        logic = new Logic();
        draggedSquare = null;
        legalSquares = new ArrayList<>();
        selectedPromotion = null;
        for (int rank = 0; rank < ModelConstants.BOARD_SIZE; ++rank) {
            for (int file = 0; file < ModelConstants.BOARD_SIZE; ++file) {
                squares[rank][file].updateSquare(logic.getBoard().getPiece(new Position(adjustCoordinate(file), adjustCoordinate(rank))),logic.isInCheck(),logic.getTurn());
                squares[rank][file].removeMouseListener(chessMouseListener);
                squares[rank][file].removeMouseMotionListener(chessMouseMotionListener);
                squares[rank][file].addMouseListener(chessMouseListener);
                squares[rank][file].addMouseMotionListener(chessMouseMotionListener);
            }
        }
        listModel.removeAllElements();
        revalidate();
        repaint();
    }

    public void reset(int skillLevel) {
        logic = new Logic(skillLevel);
        draggedSquare = null;
        legalSquares = new ArrayList<>();
        selectedPromotion = null;
        for (int rank = 0; rank < ModelConstants.BOARD_SIZE; ++rank) {
            for (int file = 0; file < ModelConstants.BOARD_SIZE; ++file) {
                squares[rank][file].updateSquare(logic.getBoard().getPiece(new Position(adjustCoordinate(file), adjustCoordinate(rank))),logic.isInCheck(),logic.getTurn());
                squares[rank][file].removeMouseListener(chessMouseListener);
                squares[rank][file].removeMouseMotionListener(chessMouseMotionListener);
                squares[rank][file].addMouseListener(chessMouseListener);
                squares[rank][file].addMouseMotionListener(chessMouseMotionListener);
            }
        }
        listModel.removeAllElements();
        revalidate();
        repaint();
    }

    public void loadFEN(String fen) {
        try {
            logic = new Logic(fen);
            draggedSquare = null;
            legalSquares = new ArrayList<>();
            selectedPromotion = null;
            isBoardFlipped=false;
            for (int rank = 0; rank < ModelConstants.BOARD_SIZE; ++rank) {
                for (int file = 0; file < ModelConstants.BOARD_SIZE; ++file) {
                    squares[rank][file].updateSquare(logic.getBoard().getPiece(new Position(file, rank)), logic.isInCheck(), logic.getTurn());
                    squares[rank][file].removeMouseListener(chessMouseListener);
                    squares[rank][file].removeMouseMotionListener(chessMouseMotionListener);
                    squares[rank][file].addMouseListener(chessMouseListener);
                    squares[rank][file].addMouseMotionListener(chessMouseMotionListener);
                }
            }
            listModel.removeAllElements();
            revalidate();
            repaint();
        }
        catch(InvalidFenException ife) {
            //TBD
        }
    }

    public int adjustCoordinate(int coordinate) {
        if (isBoardFlipped)
            return ModelConstants.BOARD_SIZE-1-coordinate;
        else
            return coordinate;
    }

    public void updateNotationLabels(){
        for (int rank=0;rank<ModelConstants.BOARD_SIZE;++rank) {
            rankNotationLabels[rank].setText(""+(char)(ModelConstants.BOARD_SIZE-adjustCoordinate(rank)+'0'));
        }
        for (int file=0;file<ModelConstants.BOARD_SIZE;++file){
            fileNotationLabels[file].setText(""+(char)('a'+adjustCoordinate(file)));
        }
    }

    public void flipBoard() {
        isBoardFlipped=!isBoardFlipped;
        updateNotationLabels();
        revalidate();
        repaint();
    }

    public void updateList() {
        if (logic.getTurn()==PlayerEnum.BLACK ||listModel.getSize()<=0)
            listModel.addElement(logic.getFullmoveNumber()+". "+logic.getLastMove());
        else {
            int lastIndex=listModel.getSize()-1;
            listModel.setElementAt(listModel.elementAt(lastIndex)+" "+logic.getLastMove(),lastIndex);
        }
    }
}
