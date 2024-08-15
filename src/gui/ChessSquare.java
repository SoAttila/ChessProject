package gui;

import model.Piece;
import model.PlayerEnum;

import javax.swing.*;
import java.awt.*;

public class ChessSquare extends JLabel {
    private final int rank;
    private final int file;

    public ChessSquare(Piece piece, int rank, int file) {
        super("", JLabel.CENTER);
        this.rank = rank;
        this.file = file;
        updateSquare(piece, false, PlayerEnum.WHITE);
        setPreferredSize(new Dimension(GuiConstants.SQUARE_SIZE, GuiConstants.SQUARE_SIZE));
        setBounds((rank + 1) * GuiConstants.SQUARE_SIZE, file * GuiConstants.SQUARE_SIZE, GuiConstants.SQUARE_SIZE, GuiConstants.SQUARE_SIZE);
    }

    public void updateSquare(Piece piece, boolean isInCheck, PlayerEnum turn) {
        ImageIcon icon = null;
        if (piece != null) {
            switch (piece.getType()) {
                case PAWN:
                    icon = piece.getColor() == PlayerEnum.WHITE ? GuiConstants.WHITE_PAWN_ICON : GuiConstants.BLACK_PAWN_ICON;
                    break;
                case KNIGHT:
                    icon = piece.getColor() == PlayerEnum.WHITE ? GuiConstants.WHITE_KNIGHT_ICON : GuiConstants.BLACK_KNIGHT_ICON;
                    break;
                case BISHOP:
                    icon = piece.getColor() == PlayerEnum.WHITE ? GuiConstants.WHITE_BISHOP_ICON : GuiConstants.BLACK_BISHOP_ICON;
                    break;
                case ROOK:
                    icon = piece.getColor() == PlayerEnum.WHITE ? GuiConstants.WHITE_ROOK_ICON : GuiConstants.BLACK_ROOK_ICON;
                    break;
                case QUEEN:
                    icon = piece.getColor() == PlayerEnum.WHITE ? GuiConstants.WHITE_QUEEN_ICON : GuiConstants.BLACK_QUEEN_ICON;
                    break;
                case KING:
                    if (isInCheck && turn == piece.getColor())
                        icon = piece.getColor() == PlayerEnum.WHITE ? GuiConstants.WHITE_KING_IN_CHECK_ICON : GuiConstants.BLACK_KING_IN_CHECK_ICON;
                    else
                        icon = piece.getColor() == PlayerEnum.WHITE ? GuiConstants.WHITE_KING_ICON : GuiConstants.BLACK_KING_ICON;
            }
        }
        setIcon(icon);
    }

    public int getRank() {
        return rank;
    }

    public int getFile() {
        return file;
    }
}
