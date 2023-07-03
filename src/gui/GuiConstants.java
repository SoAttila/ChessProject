package gui;

import javax.swing.*;
import java.awt.*;

import static model.ModelConstants.BOARD_SIZE;

public class GuiConstants {
    public static final String TITLE = "Chess application";
    public static final int FRAME_LENGTH = 900;
    public static final int SQUARE_SIZE = FRAME_LENGTH / (BOARD_SIZE+1);
    public static final int MOVE_NOTATION_PANEL_WIDTH=300;
    public static final Font NOTATION_FONT = new Font("Verdana",Font.BOLD,40);
    public static final int VISIBLE_ROWS=23;
    public static final Font MOVE_NOTATION_LIST_FONT=new Font("Times New Roman",Font.PLAIN,30);
    public static final ImageIcon BLACK_PAWN_ICON = transformIcon(new ImageIcon("images/bp.png"));
    public static final ImageIcon WHITE_PAWN_ICON = transformIcon(new ImageIcon("images/wp.png"));
    public static final ImageIcon BLACK_KNIGHT_ICON = transformIcon(new ImageIcon("images/bn.png"));
    public static final ImageIcon WHITE_KNIGHT_ICON = transformIcon(new ImageIcon("images/wn.png"));
    public static final ImageIcon BLACK_BISHOP_ICON = transformIcon(new ImageIcon("images/bb.png"));
    public static final ImageIcon WHITE_BISHOP_ICON = transformIcon(new ImageIcon("images/wb.png"));
    public static final ImageIcon BLACK_ROOK_ICON = transformIcon(new ImageIcon("images/br.png"));
    public static final ImageIcon WHITE_ROOK_ICON = transformIcon(new ImageIcon("images/wr.png"));
    public static final ImageIcon BLACK_QUEEN_ICON = transformIcon(new ImageIcon("images/bq.png"));
    public static final ImageIcon[] BLACK_PROMOTION_ICONS = {GuiConstants.BLACK_QUEEN_ICON, GuiConstants.BLACK_KNIGHT_ICON, GuiConstants.BLACK_ROOK_ICON, GuiConstants.BLACK_BISHOP_ICON};
    public static final ImageIcon WHITE_QUEEN_ICON = transformIcon(new ImageIcon("images/wq.png"));
    public static final ImageIcon[] WHITE_PROMOTION_ICONS = {GuiConstants.WHITE_QUEEN_ICON, GuiConstants.WHITE_KNIGHT_ICON, GuiConstants.WHITE_ROOK_ICON, GuiConstants.WHITE_BISHOP_ICON};
    public static final ImageIcon BLACK_KING_ICON = transformIcon(new ImageIcon("images/bk.png"));
    public static final ImageIcon WHITE_KING_ICON = transformIcon(new ImageIcon("images/wk.png"));
    public static final ImageIcon BLACK_KING_IN_CHECK_ICON = transformIcon(new ImageIcon("images/bk_c.png"));
    public static final ImageIcon WHITE_KING_IN_CHECK_ICON = transformIcon(new ImageIcon("images/wk_c.png"));
    public static final Color LIGHT_RED = new Color(255, 120, 120);
    public static final Color DARK_RED = new Color(255, 80, 80);

    private static ImageIcon transformIcon(ImageIcon icon) {
        return new ImageIcon((icon.getImage().getScaledInstance(SQUARE_SIZE, SQUARE_SIZE, java.awt.Image.SCALE_SMOOTH)));
    }
}
