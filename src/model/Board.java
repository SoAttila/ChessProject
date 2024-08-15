package model;

import java.util.Arrays;

public class Board {
    private final Piece[][] board;

    public Board() {
        board = new Piece[ModelConstants.BOARD_SIZE][ModelConstants.BOARD_SIZE];

        for (int player = 0; player <= 1; ++player) {
            //pawns
            for (int file = 0; file <= ModelConstants.BOARD_SIZE - 1; file++) {
                board[1 + player * (ModelConstants.BOARD_SIZE - 3)][file] = new Piece(PieceType.PAWN, (player == 0 ? PlayerEnum.BLACK : PlayerEnum.WHITE));
            }
            //rooks
            for (int file = 0; file <= ModelConstants.BOARD_SIZE - 1; file += ModelConstants.BOARD_SIZE - 1) {
                board[player * (ModelConstants.BOARD_SIZE - 1)][file] = new Piece(PieceType.ROOK, (player == 0 ? PlayerEnum.BLACK : PlayerEnum.WHITE));
            }
            //knights
            for (int file = 1; file <= ModelConstants.BOARD_SIZE - 2; file += ModelConstants.BOARD_SIZE - 3) {
                board[player * (ModelConstants.BOARD_SIZE - 1)][file] = new Piece(PieceType.KNIGHT, (player == 0 ? PlayerEnum.BLACK : PlayerEnum.WHITE));
            }
            //bishops
            for (int file = 2; file <= ModelConstants.BOARD_SIZE - 3; file += ModelConstants.BOARD_SIZE - 5) {
                board[player * (ModelConstants.BOARD_SIZE - 1)][file] = new Piece(PieceType.BISHOP, (player == 0 ? PlayerEnum.BLACK : PlayerEnum.WHITE));
            }
            //queens
            board[player * (ModelConstants.BOARD_SIZE - 1)][ModelConstants.BOARD_SIZE / 2 - 1] = new Piece(PieceType.QUEEN, (player == 0 ? PlayerEnum.BLACK : PlayerEnum.WHITE));
            //kings
            board[player * (ModelConstants.BOARD_SIZE - 1)][ModelConstants.BOARD_SIZE / 2] = new Piece(PieceType.KING, (player == 0 ? PlayerEnum.BLACK : PlayerEnum.WHITE));

        }
    }

    public Board(Board other) {
        board = new Piece[ModelConstants.BOARD_SIZE][ModelConstants.BOARD_SIZE];

        for (int file = 0; file < ModelConstants.BOARD_SIZE; ++file) {
            for (int rank = 0; rank < ModelConstants.BOARD_SIZE; ++rank) {
                board[rank][file] = other.board[rank][file];
            }
        }
    }

    public Board(String piecePlacement) throws InvalidFenException {
        String[] ranks = piecePlacement.split("/");
        board = new Piece[ModelConstants.BOARD_SIZE][ModelConstants.BOARD_SIZE];
        for (int rank_idx = 0; rank_idx < ModelConstants.BOARD_SIZE; ++rank_idx) {
            int file_idx = 0;
            for (int char_idx = 0; char_idx < ranks[rank_idx].length() && file_idx < ModelConstants.BOARD_SIZE; ++char_idx) {
                switch (ranks[rank_idx].charAt(char_idx)) {
                    case 'p' -> board[rank_idx][file_idx] = new Piece(PieceType.PAWN, PlayerEnum.BLACK);
                    case 'n' -> board[rank_idx][file_idx] = new Piece(PieceType.KNIGHT, PlayerEnum.BLACK);
                    case 'b' -> board[rank_idx][file_idx] = new Piece(PieceType.BISHOP, PlayerEnum.BLACK);
                    case 'r' -> board[rank_idx][file_idx] = new Piece(PieceType.ROOK, PlayerEnum.BLACK);
                    case 'q' -> board[rank_idx][file_idx] = new Piece(PieceType.QUEEN, PlayerEnum.BLACK);
                    case 'k' -> board[rank_idx][file_idx] = new Piece(PieceType.KING, PlayerEnum.BLACK);
                    case 'P' -> board[rank_idx][file_idx] = new Piece(PieceType.PAWN, PlayerEnum.WHITE);
                    case 'N' -> board[rank_idx][file_idx] = new Piece(PieceType.KNIGHT, PlayerEnum.WHITE);
                    case 'B' -> board[rank_idx][file_idx] = new Piece(PieceType.BISHOP, PlayerEnum.WHITE);
                    case 'R' -> board[rank_idx][file_idx] = new Piece(PieceType.ROOK, PlayerEnum.WHITE);
                    case 'Q' -> board[rank_idx][file_idx] = new Piece(PieceType.QUEEN, PlayerEnum.WHITE);
                    case 'K' -> board[rank_idx][file_idx] = new Piece(PieceType.KING, PlayerEnum.WHITE);
                    default -> {
                        int steps = Character.getNumericValue(ranks[rank_idx].charAt(char_idx));
                        file_idx += steps - 1;
                    }
                }
                ++file_idx;
            }
        }
    }

    public Piece getPiece(Position pos) {
        return board[pos.getRank()][pos.getFile()];
    }

    public Piece[][] getBoard() {
        return board;
    }

    public void setPiece(Position pos, Piece piece) {
        board[pos.getRank()][pos.getFile()] = piece;
    }

    public void movePiece(Move move) {
        setPiece(move.getTo(), getPiece(move.getFrom()));
        setPiece(move.getFrom(), null);
    }

    public void printBoard() {
        for (int rank = 0; rank <= ModelConstants.BOARD_SIZE - 1; ++rank) {
            for (int file = 0; file <= ModelConstants.BOARD_SIZE - 1; ++file) {
                Piece piece = board[rank][file];
                System.out.print((piece == null ? " " : piece.getColor() + "" + piece.getType()) + "|");
            }
            System.out.println();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Board board1 = (Board) o;
        return Arrays.deepEquals(board, board1.board);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(board);
    }
}
