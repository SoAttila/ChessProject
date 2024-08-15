package model;

import java.util.ArrayList;
import java.util.HashMap;

public class Logic {
    private final Board board;
    private final ArrayList<String> gamePositions;
    private final ArrayList<String> moves = new ArrayList<>();
    private final EngineConnector engineConnector = new EngineConnector();
    private PlayerEnum turn;
    private Position possibleEnPassantPos;
    private Position whiteKingPos;
    private Position blackKingPos;
    private boolean isInCheck;
    private boolean isCheckmate;
    private GameState gameState;
    private int halfmoveClock;
    private int fullmoveNumber;
    private GameMode gameMode;

    public Logic() {
        board = new Board();
        turn = PlayerEnum.WHITE;
        possibleEnPassantPos = null;
        whiteKingPos = new Position(ModelConstants.BOARD_SIZE / 2, ModelConstants.BOARD_SIZE - 1);
        blackKingPos = new Position(ModelConstants.BOARD_SIZE / 2, 0);
        isInCheck = false;
        isCheckmate = false;
        gameState = GameState.IN_PROGRESS;
        halfmoveClock = 0;
        fullmoveNumber = 1;
        gamePositions = new ArrayList<>();
        gamePositions.add(getModifiedFEN());
        moves.clear();
        gameMode = GameMode.OFFLINE;
    }

    public Logic(int skillLevel) {
        this();
        gameMode = GameMode.ENGINE;
        engineConnector.sendCommand("uci");
        engineConnector.sendCommand("setoption name Skill Level value " + skillLevel);
    }

    public Logic(String fen) throws InvalidFenException {
        String[] fields = fen.split(" ");
        //piece placement
        board = new Board(fields[0]);
        //color to move
        switch (fields[1]) {
            case "b" -> turn = PlayerEnum.BLACK;
            case "w" -> this.turn = PlayerEnum.WHITE;
        }

        blackKingPos = null;
        whiteKingPos = null;
        for (int file = 0; file < ModelConstants.BOARD_SIZE; ++file) {
            for (int rank = 0; rank < ModelConstants.BOARD_SIZE; ++rank) {
                Position pos = new Position(file, rank);
                Piece piece = board.getPiece(pos);
                if (piece != null) {
                    piece.setHasMoved(true);
                    if (piece.getType() == PieceType.KING) {
                        switch (piece.getColor()) {
                            case WHITE -> whiteKingPos = pos;
                            case BLACK -> blackKingPos = pos;
                        }
                    }
                }
            }
        }
        if (!whiteKingPos.equals(new Position("e1"))) {
            board.getPiece(whiteKingPos).setHasMoved(true);
        }
        if (!blackKingPos.equals(new Position("e8"))) {
            board.getPiece(blackKingPos).setHasMoved(true);
        }
        //castling rights
        String castlingRights = fields[2];
        for (int charIdx = 0; charIdx < castlingRights.length(); ++charIdx) {
            switch (castlingRights.charAt(charIdx)) {
                case 'k' -> {
                    checkIfNullAndSetHasMovedToFalse(board.getPiece(new Position("h8")));
                    checkIfNullAndSetHasMovedToFalse(board.getPiece(new Position("e8")));
                }
                case 'q' -> {
                    checkIfNullAndSetHasMovedToFalse(board.getPiece(new Position("a8")));
                    checkIfNullAndSetHasMovedToFalse(board.getPiece(new Position("e8")));
                }
                case 'K' -> {
                    checkIfNullAndSetHasMovedToFalse(board.getPiece(new Position("h1")));
                    checkIfNullAndSetHasMovedToFalse(board.getPiece(new Position("e1")));
                }
                case 'Q' -> {
                    checkIfNullAndSetHasMovedToFalse(board.getPiece(new Position("a1")));
                    checkIfNullAndSetHasMovedToFalse(board.getPiece(new Position("e1")));
                }
            }
        }
        //en passant target pos
        possibleEnPassantPos = fields[3].equals("-") ? null : new Position(fields[3]);
        //half-move clock
        halfmoveClock = Integer.parseInt(fields[4]);
        //full-move number
        fullmoveNumber = Integer.parseInt(fields[5]);
        gamePositions = new ArrayList<>();
        gamePositions.add(getModifiedFEN());
        moves.clear();
        isInCheck = (isInCheck(turn == PlayerEnum.BLACK ? blackKingPos : whiteKingPos, board, turn == PlayerEnum.WHITE ? PlayerEnum.BLACK : PlayerEnum.WHITE));
        isCheckmate = isInCheck && !isThereAnyLegalMove();
        updateGameState();
        gameMode = GameMode.OFFLINE;
    }

    private void checkIfNullAndSetHasMovedToFalse(Piece piece) {
        if (piece != null) {
            piece.setHasMoved(false);
        }
    }

    public Board getBoard() {
        return board;
    }

    public MoveType getPseudoMoveType(Move move, Board board, PlayerEnum turn) {
        Piece piece = board.getPiece(move.getFrom());
        if (piece != null && turn == piece.getColor() && !move.getTo().equals(move.getFrom()) && (board.getPiece(move.getTo()) == null || board.getPiece(move.getTo()).getColor() != turn)) {
            switch (piece.getType()) {
                case PAWN:
                    //move
                    ////single step
                    if (move.getTo().getRank() == move.getFrom().getRank() + (turn == PlayerEnum.WHITE ? -1 : 1) && move.getFrom().getFile() == move.getTo().getFile() && board.getPiece(move.getTo()) == null) {
                        if (move.getTo().getRank() == (turn == PlayerEnum.WHITE ? 0 : ModelConstants.BOARD_SIZE - 1))
                            return MoveType.PROMOTION;
                        return MoveType.NORMAL;
                    }
                    ////double step
                    else if (move.getTo().getRank() == move.getFrom().getRank() + (turn == PlayerEnum.WHITE ? -2 : 2) && move.getFrom().getRank() == (turn == PlayerEnum.WHITE ? ModelConstants.BOARD_SIZE - 2 : 1) && move.getFrom().getFile() == move.getTo().getFile() && board.getPiece(move.getTo()) == null && board.getPiece(new Position(move.getTo().getFile(), move.getTo().getRank() + (turn == PlayerEnum.WHITE ? 1 : -1))) == null)
                        return MoveType.PAWN_DOUBLE;
                        //capture
                        ////normal
                    else if (Math.abs(move.getTo().getFile() - move.getFrom().getFile()) == 1 && move.getTo().getRank() - move.getFrom().getRank() == (turn == PlayerEnum.WHITE ? -1 : 1) && board.getPiece(move.getTo()) != null) {
                        if (move.getTo().getRank() == (turn == PlayerEnum.WHITE ? 0 : ModelConstants.BOARD_SIZE - 1))
                            return MoveType.PROMOTION;
                        return MoveType.NORMAL;
                    }
                    ////en passant
                    else if (possibleEnPassantPos != null && possibleEnPassantPos.equals(move.getTo()) && Math.abs(move.getTo().getFile() - move.getFrom().getFile()) == 1 && move.getTo().getRank() - move.getFrom().getRank() == (turn == PlayerEnum.WHITE ? -1 : 1))
                        return MoveType.EN_PASSANT;
                    return MoveType.ILLEGAL;
                case KNIGHT:
                    if ((Math.abs(move.getTo().getRank() - move.getFrom().getRank()) == 1 && Math.abs(move.getTo().getFile() - move.getFrom().getFile()) == 2) || (Math.abs(move.getTo().getRank() - move.getFrom().getRank()) == 2 && Math.abs(move.getTo().getFile() - move.getFrom().getFile()) == 1))
                        return MoveType.NORMAL;
                    return MoveType.ILLEGAL;
                case BISHOP:
                    int deltaFile = move.getTo().getFile() - move.getFrom().getFile();
                    int deltaRank = move.getTo().getRank() - move.getFrom().getRank();
                    int sFile = (deltaFile > 0 ? 1 : -1);
                    int sRank = (deltaRank > 0 ? 1 : -1);
                    if (Math.abs(deltaFile) == Math.abs(deltaRank)) {
                        int file = move.getFrom().getFile() + sFile;
                        int rank = move.getFrom().getRank() + sRank;
                        while (file != move.getTo().getFile() && rank != move.getTo().getRank()) {
                            if (board.getPiece(new Position(file, rank)) != null)
                                return MoveType.ILLEGAL;
                            file += sFile;
                            rank += sRank;
                        }
                        return MoveType.NORMAL;
                    }
                    return MoveType.ILLEGAL;
                case ROOK:
                    if (move.getTo().getFile() - move.getFrom().getFile() == 0) {
                        sRank = (move.getTo().getRank() - move.getFrom().getRank()) > 0 ? 1 : -1;
                        for (int rank = move.getFrom().getRank() + sRank; rank != move.getTo().getRank(); rank += sRank) {
                            if (board.getPiece(new Position(move.getTo().getFile(), rank)) != null)
                                return MoveType.ILLEGAL;
                        }
                        return MoveType.NORMAL;
                    } else if (move.getTo().getRank() - move.getFrom().getRank() == 0) {
                        sFile = (move.getTo().getFile() - move.getFrom().getFile()) > 0 ? 1 : -1;
                        for (int file = move.getFrom().getFile() + sFile; file != move.getTo().getFile(); file += sFile) {
                            if (board.getPiece(new Position(file, move.getTo().getRank())) != null)
                                return MoveType.ILLEGAL;
                        }
                        return MoveType.NORMAL;
                    }
                    return MoveType.ILLEGAL;
                case QUEEN:
                    if (move.getTo().getFile() - move.getFrom().getFile() == 0) {
                        sRank = (move.getTo().getRank() - move.getFrom().getRank()) > 0 ? 1 : -1;
                        for (int rank = move.getFrom().getRank() + sRank; rank != move.getTo().getRank(); rank += sRank) {
                            if (board.getPiece(new Position(move.getTo().getFile(), rank)) != null)
                                return MoveType.ILLEGAL;
                        }
                        return MoveType.NORMAL;
                    } else if (move.getTo().getRank() - move.getFrom().getRank() == 0) {
                        sFile = (move.getTo().getFile() - move.getFrom().getFile()) > 0 ? 1 : -1;
                        for (int file = move.getFrom().getFile() + sFile; file != move.getTo().getFile(); file += sFile) {
                            if (board.getPiece(new Position(file, move.getTo().getRank())) != null)
                                return MoveType.ILLEGAL;
                        }
                        return MoveType.NORMAL;
                    }
                    deltaFile = move.getTo().getFile() - move.getFrom().getFile();
                    deltaRank = move.getTo().getRank() - move.getFrom().getRank();
                    sFile = (deltaFile > 0 ? 1 : -1);
                    sRank = (deltaRank > 0 ? 1 : -1);
                    if (Math.abs(deltaFile) == Math.abs(deltaRank)) {
                        int file = move.getFrom().getFile() + sFile;
                        int rank = move.getFrom().getRank() + sRank;
                        while (file != move.getTo().getFile() && rank != move.getTo().getRank()) {
                            if (board.getPiece(new Position(file, rank)) != null)
                                return MoveType.ILLEGAL;
                            file += sFile;
                            rank += sRank;
                        }
                        return MoveType.NORMAL;
                    }
                    return MoveType.ILLEGAL;
                case KING:
                    //normal
                    if (Math.abs(move.getTo().getFile() - move.getFrom().getFile()) <= 1 && Math.abs(move.getTo().getRank() - move.getFrom().getRank()) <= 1)
                        return MoveType.KING;
                        //castling
                    else {
                        int rank = turn == PlayerEnum.WHITE ? ModelConstants.BOARD_SIZE - 1 : 0;
                        //long
                        if (move.getTo().equals(new Position(2, rank))
                                && move.getFrom().equals(new Position(4, rank))
                                && !board.getPiece(new Position(4, rank)).hasMoved()
                                && board.getPiece(new Position(3, rank)) == null
                                && board.getPiece(new Position(2, rank)) == null
                                && board.getPiece(new Position(1, rank)) == null
                                && board.getPiece(new Position(0, rank)) != null
                                && board.getPiece(new Position(0, rank)).getType() == PieceType.ROOK
                                && !board.getPiece(new Position(0, rank)).hasMoved())
                            return MoveType.LONG_CASTLING;
                            //short
                        else if (move.getTo().equals(new Position(ModelConstants.BOARD_SIZE - 2, rank))
                                && move.getFrom().equals(new Position(4, rank))
                                && !board.getPiece(new Position(4, rank)).hasMoved()
                                && board.getPiece(new Position(ModelConstants.BOARD_SIZE - 3, rank)) == null
                                && board.getPiece(new Position(ModelConstants.BOARD_SIZE - 2, rank)) == null
                                && board.getPiece(new Position(ModelConstants.BOARD_SIZE - 1, rank)) != null
                                && board.getPiece(new Position(ModelConstants.BOARD_SIZE - 1, rank)).getType() == PieceType.ROOK
                                && !board.getPiece(new Position(ModelConstants.BOARD_SIZE - 1, rank)).hasMoved())
                            return MoveType.SHORT_CASTLING;
                    }

                    return MoveType.ILLEGAL;
            }
        }
        return MoveType.ILLEGAL;
    }

    public MoveType getMoveType(Move move, Board board, PlayerEnum turn) {
        MoveType pseudoMoveType = getPseudoMoveType(move, board, turn);
        if (pseudoMoveType != MoveType.ILLEGAL && !isInCheckAfterMove(move, pseudoMoveType, turn, null, turn)
                && (pseudoMoveType != MoveType.SHORT_CASTLING ||
                (!isInCheck(new Position(ModelConstants.BOARD_SIZE - 3, turn == PlayerEnum.WHITE ? ModelConstants.BOARD_SIZE - 1 : 0), board, turn == PlayerEnum.WHITE ? PlayerEnum.BLACK : PlayerEnum.WHITE) && !isInCheck(new Position(ModelConstants.BOARD_SIZE - 4, turn == PlayerEnum.WHITE ? ModelConstants.BOARD_SIZE - 1 : 0), board, turn == PlayerEnum.WHITE ? PlayerEnum.BLACK : PlayerEnum.WHITE)))
                && (pseudoMoveType != MoveType.LONG_CASTLING ||
                (!isInCheck(new Position(3, turn == PlayerEnum.WHITE ? ModelConstants.BOARD_SIZE - 1 : 0), board, turn == PlayerEnum.WHITE ? PlayerEnum.BLACK : PlayerEnum.WHITE) && !isInCheck(new Position(4, turn == PlayerEnum.WHITE ? ModelConstants.BOARD_SIZE - 1 : 0), board, turn == PlayerEnum.WHITE ? PlayerEnum.BLACK : PlayerEnum.WHITE))))
            return pseudoMoveType;
        return MoveType.ILLEGAL;
    }

    public void move(Move move, PieceType promoteTo) {
        MoveType moveType = getMoveType(move, board, turn);
        if (moveType != MoveType.ILLEGAL) {
            String moveNotation = getMoveNotation(move, moveType, promoteTo);
            if (board.getPiece(move.getFrom()).getType() != PieceType.PAWN && board.getPiece(move.getTo()) == null)
                ++halfmoveClock;
            else
                halfmoveClock = 0;
            switch (moveType) {
                case NORMAL -> {
                    board.getPiece(move.getFrom()).setHasMoved(true);
                    board.movePiece(move);
                    possibleEnPassantPos = null;
                }
                case KING -> {
                    board.getPiece(move.getFrom()).setHasMoved(true);
                    board.movePiece(move);
                    if (turn == PlayerEnum.WHITE)
                        whiteKingPos = move.getTo();
                    else
                        blackKingPos = move.getTo();
                    possibleEnPassantPos = null;
                }
                case PAWN_DOUBLE -> {
                    board.movePiece(move);
                    possibleEnPassantPos = new Position(move.getTo().getFile(), move.getTo().getRank() + (turn == PlayerEnum.WHITE ? 1 : -1));
                }
                case EN_PASSANT -> {
                    board.movePiece(move);
                    board.setPiece(new Position(possibleEnPassantPos.getFile(), possibleEnPassantPos.getRank() + (turn == PlayerEnum.WHITE ? 1 : -1)), null);
                    possibleEnPassantPos = null;
                }
                case LONG_CASTLING -> {
                    board.getPiece(move.getFrom()).setHasMoved(true);
                    board.movePiece(move);
                    board.movePiece(new Move(new Position(0, turn == PlayerEnum.WHITE ? ModelConstants.BOARD_SIZE - 1 : 0), new Position(3, turn == PlayerEnum.WHITE ? ModelConstants.BOARD_SIZE - 1 : 0)));
                    if (turn == PlayerEnum.WHITE)
                        whiteKingPos = move.getTo();
                    else
                        blackKingPos = move.getTo();
                    possibleEnPassantPos = null;
                }
                case SHORT_CASTLING -> {
                    board.getPiece(move.getFrom()).setHasMoved(true);
                    board.movePiece(move);
                    board.movePiece(new Move(new Position(ModelConstants.BOARD_SIZE - 1, turn == PlayerEnum.WHITE ? ModelConstants.BOARD_SIZE - 1 : 0), new Position(ModelConstants.BOARD_SIZE - 3, turn == PlayerEnum.WHITE ? ModelConstants.BOARD_SIZE - 1 : 0)));
                    if (turn == PlayerEnum.WHITE)
                        whiteKingPos = move.getTo();
                    else
                        blackKingPos = move.getTo();
                    possibleEnPassantPos = null;
                }
                case PROMOTION -> {
                    board.setPiece(move.getFrom(), null);
                    board.setPiece(move.getTo(), new Piece(promoteTo, turn));
                    possibleEnPassantPos = null;
                }
            }
            if (turn == PlayerEnum.BLACK)
                ++fullmoveNumber;
            isInCheck = (isInCheck(turn == PlayerEnum.WHITE ? blackKingPos : whiteKingPos, board, turn));
            turn = (turn == PlayerEnum.WHITE ? PlayerEnum.BLACK : PlayerEnum.WHITE);
            isCheckmate = isInCheck && !isThereAnyLegalMove();
            moves.add(moveNotation + (isInCheck ? (isCheckmate ? "#" : "+") : ""));
            gamePositions.add(getModifiedFEN());
            updateGameState();
        }
    }

    public void moveForEngine() {
        engineConnector.sendCommand("position fen " + getFEN());
        engineConnector.sendCommand("go depth " + ModelConstants.MAXIMUM_ENGINE_DEPTH);
        String bestMove = null;
        while (bestMove == null) {
            bestMove = engineConnector.getBestMove();
        }
        System.out.println(bestMove);
        Move engineMove = new Move(new Position(bestMove.substring(0, 2)), new Position(bestMove.substring(2, 4)));
        String enginePromoteToString = bestMove.length() == 5 ? bestMove.substring(4) : "";
        PieceType enginePromoteTo;
        switch (enginePromoteToString) {
            case "q" -> enginePromoteTo = PieceType.QUEEN;
            case "n" -> enginePromoteTo = PieceType.KNIGHT;
            case "r" -> enginePromoteTo = PieceType.ROOK;
            case "b" -> enginePromoteTo = PieceType.BISHOP;
            default -> enginePromoteTo = null;
        }
        move(engineMove, enginePromoteTo);
    }

    public String getMoveNotation(Move move, MoveType moveType, PieceType promoteTo) {
        StringBuilder moveNotation = new StringBuilder();
        Piece originalPiece = board.getPiece(move.getFrom());
        boolean isCapture = board.getPiece(move.getTo()) != null || (possibleEnPassantPos != null && possibleEnPassantPos.equals(move.getTo()));
        boolean isAmbiguous = false;
        boolean isAmbiguousByFile = false;
        boolean isAmbiguousByRank = false;
        if (originalPiece.getType() != PieceType.PAWN && originalPiece.getType() != PieceType.KING) {
            for (int file = 0; file < ModelConstants.BOARD_SIZE; ++file) {
                for (int rank = 0; rank < ModelConstants.BOARD_SIZE; ++rank) {
                    if ((file != move.getFrom().getFile() || rank != move.getFrom().getRank())) {
                        Position pos = new Position(file, rank);
                        Piece piece = board.getPiece(pos);
                        if (piece != null && piece.equals(board.getPiece(move.getFrom())) && getMoveType(new Move(pos, move.getTo()), board, turn) != MoveType.ILLEGAL) {
                            isAmbiguous = true;
                            if (pos.getFile() == move.getFrom().getFile())
                                isAmbiguousByFile = true;
                            else if (pos.getRank() == move.getFrom().getRank())
                                isAmbiguousByRank = true;
                        }
                    }
                }
            }
        }
        switch (moveType) {
            case SHORT_CASTLING -> moveNotation.append("0-0");
            case LONG_CASTLING -> moveNotation.append("0-0-0");
            case PROMOTION -> {
                if (isCapture) moveNotation.append(move.getFrom().toString().charAt(0)).append("x");
                moveNotation.append(move.getTo());
                moveNotation.append("=").append(promoteTo);
            }
            default -> {
                moveNotation.append((originalPiece.getType() == PieceType.PAWN && isCapture ? move.getFrom().toString().charAt(0) : originalPiece));
                if (isAmbiguous) {
                    if (isAmbiguousByFile) {
                        if (isAmbiguousByRank)
                            moveNotation.append(move.getFrom());
                        else
                            moveNotation.append(move.getFrom().toString().charAt(1));
                    } else
                        moveNotation.append(move.getFrom().toString().charAt(0));
                }
                moveNotation.append((isCapture ? "x" : ""));
                moveNotation.append(move.getTo());
            }
        }
        return moveNotation.toString();
    }

    public boolean isInCheck(Position pos, Board board, PlayerEnum turn) {
        for (int file = 0; file < ModelConstants.BOARD_SIZE; ++file) {
            for (int rank = 0; rank < ModelConstants.BOARD_SIZE; ++rank) {
                Piece piece = board.getPiece(new Position(file, rank));
                if (piece != null && piece.getColor() == turn) {
                    if (getPseudoMoveType(new Move(new Position(file, rank), pos), board, turn) != MoveType.ILLEGAL) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isInCheckAfterMove(Move move, MoveType moveType, PlayerEnum turn, PieceType promoteTo, PlayerEnum promoteToColor) {
        Board tempBoard = new Board(board);
        Position tempWhiteKingPos = new Position(whiteKingPos);
        Position tempBlackKingPos = new Position(blackKingPos);
        PlayerEnum tempTurn = turn == PlayerEnum.WHITE ? PlayerEnum.WHITE : PlayerEnum.BLACK;
        switch (moveType) {
            case NORMAL, PAWN_DOUBLE -> tempBoard.movePiece(move);
            case KING -> {
                tempBoard.movePiece(move);
                if (tempTurn == PlayerEnum.WHITE)
                    tempWhiteKingPos = move.getTo();
                else
                    tempBlackKingPos = move.getTo();
            }
            case EN_PASSANT -> {
                tempBoard.movePiece(move);
                tempBoard.setPiece(new Position(possibleEnPassantPos.getFile(), possibleEnPassantPos.getRank() + (tempTurn == PlayerEnum.WHITE ? 1 : -1)), null);
            }
            case LONG_CASTLING -> {
                tempBoard.movePiece(move);
                tempBoard.movePiece(new Move(new Position(0, tempTurn == PlayerEnum.WHITE ? ModelConstants.BOARD_SIZE - 1 : 0), new Position(3, tempTurn == PlayerEnum.WHITE ? ModelConstants.BOARD_SIZE - 1 : 0)));
                if (tempTurn == PlayerEnum.WHITE)
                    tempWhiteKingPos = move.getTo();
                else
                    tempBlackKingPos = move.getTo();
            }
            case SHORT_CASTLING -> {
                tempBoard.movePiece(move);
                tempBoard.movePiece(new Move(new Position(ModelConstants.BOARD_SIZE - 1, tempTurn == PlayerEnum.WHITE ? ModelConstants.BOARD_SIZE - 1 : 0), new Position(ModelConstants.BOARD_SIZE - 3, tempTurn == PlayerEnum.WHITE ? ModelConstants.BOARD_SIZE - 1 : 0)));
                if (tempTurn == PlayerEnum.WHITE)
                    tempWhiteKingPos = move.getTo();
                else
                    tempBlackKingPos = move.getTo();
            }
            case PROMOTION -> {
                tempBoard.setPiece(move.getFrom(), null);
                tempBoard.setPiece(move.getTo(), new Piece(promoteTo, promoteToColor));
            }
        }
        tempTurn = tempTurn == PlayerEnum.WHITE ? PlayerEnum.BLACK : PlayerEnum.WHITE;
        return isInCheck(tempTurn == PlayerEnum.BLACK ? tempWhiteKingPos : tempBlackKingPos, tempBoard, tempTurn);
    }

    public ArrayList<Position> getLegalMoves(Position from) {
        ArrayList<Position> legalMoves = new ArrayList<>();
        Piece piece = board.getPiece(from);
        if (piece != null) {
            for (int file = 0; file < ModelConstants.BOARD_SIZE; ++file) {
                for (int rank = 0; rank < ModelConstants.BOARD_SIZE; ++rank) {
                    Position to = new Position(file, rank);
                    if (getMoveType(new Move(from, to), board, turn) != MoveType.ILLEGAL)
                        legalMoves.add(to);
                }
            }
        }
        return legalMoves;
    }

    private boolean isThereAnyLegalMove() {
        for (int file = 0; file < ModelConstants.BOARD_SIZE; ++file) {
            for (int rank = 0; rank < ModelConstants.BOARD_SIZE; ++rank) {
                Position pos = new Position(file, rank);
                Piece piece = board.getPiece(pos);
                if (piece != null && piece.getColor() == turn) {
                    if (!getLegalMoves(pos).isEmpty())
                        return true;
                }
            }
        }
        return false;
    }

    public String getFEN() {
        String FEN = "";
        StringBuilder piecePlacement = new StringBuilder();
        for (int rank = 0; rank < ModelConstants.BOARD_SIZE; ++rank) {
            int steps = 0;
            for (int file = 0; file < ModelConstants.BOARD_SIZE; ++file) {
                Piece piece = board.getPiece(new Position(file, rank));
                if (piece != null) {
                    if (steps != 0)
                        piecePlacement.append(steps);
                    steps = 0;
                    switch (piece.getColor()) {
                        case WHITE -> {
                            switch (piece.getType()) {
                                case KING -> piecePlacement.append('K');
                                case QUEEN -> piecePlacement.append('Q');
                                case ROOK -> piecePlacement.append('R');
                                case BISHOP -> piecePlacement.append('B');
                                case KNIGHT -> piecePlacement.append('N');
                                case PAWN -> piecePlacement.append('P');
                            }
                        }
                        case BLACK -> {
                            switch (piece.getType()) {
                                case KING -> piecePlacement.append('k');
                                case QUEEN -> piecePlacement.append('q');
                                case ROOK -> piecePlacement.append('r');
                                case BISHOP -> piecePlacement.append('b');
                                case KNIGHT -> piecePlacement.append('n');
                                case PAWN -> piecePlacement.append('p');
                            }
                        }
                    }
                } else {
                    ++steps;
                    if (file == ModelConstants.BOARD_SIZE - 1)
                        piecePlacement.append(steps);
                }
            }
            if (rank != ModelConstants.BOARD_SIZE - 1)
                piecePlacement.append('/');
        }
        FEN += piecePlacement + " ";
        FEN += (turn == PlayerEnum.WHITE ? "w" : "b") + " ";
        String castlingRights = "";
        Piece piece = board.getPiece(new Position("e1"));
        if (piece != null && piece.getType() == PieceType.KING && !piece.hasMoved()) {
            piece = board.getPiece(new Position("h1"));
            if (piece != null && piece.getType() == PieceType.ROOK && !piece.hasMoved())
                castlingRights += 'K';
            piece = board.getPiece(new Position("a1"));
            if (piece != null && piece.getType() == PieceType.ROOK && !piece.hasMoved())
                castlingRights += 'Q';
        }
        piece = board.getPiece(new Position("e8"));
        if (piece != null && piece.getType() == PieceType.KING && !piece.hasMoved()) {
            piece = board.getPiece(new Position("h8"));
            if (piece != null && piece.getType() == PieceType.ROOK && !piece.hasMoved())
                castlingRights += 'k';
            piece = board.getPiece(new Position("a8"));
            if (piece != null && piece.getType() == PieceType.ROOK && !piece.hasMoved())
                castlingRights += 'q';
        }
        if (castlingRights.equals(""))
            castlingRights = "-";
        FEN += castlingRights + " ";
        FEN += (possibleEnPassantPos == null ? "-" : possibleEnPassantPos) + " ";
        FEN += halfmoveClock + " " + fullmoveNumber;
        return FEN;
    }

    //Modified to enable comparing positions for threefold repetition
    public String getModifiedFEN() {
        String FEN = "";
        StringBuilder piecePlacement = new StringBuilder();
        for (int rank = 0; rank < ModelConstants.BOARD_SIZE; ++rank) {
            int steps = 0;
            for (int file = 0; file < ModelConstants.BOARD_SIZE; ++file) {
                Piece piece = board.getPiece(new Position(file, rank));
                if (piece != null) {
                    if (steps != 0)
                        piecePlacement.append(steps);
                    steps = 0;
                    switch (piece.getColor()) {
                        case WHITE -> {
                            switch (piece.getType()) {
                                case KING -> piecePlacement.append('K');
                                case QUEEN -> piecePlacement.append('Q');
                                case ROOK -> piecePlacement.append('R');
                                case BISHOP -> piecePlacement.append('B');
                                case KNIGHT -> piecePlacement.append('N');
                                case PAWN -> piecePlacement.append('P');
                            }
                        }
                        case BLACK -> {
                            switch (piece.getType()) {
                                case KING -> piecePlacement.append('k');
                                case QUEEN -> piecePlacement.append('q');
                                case ROOK -> piecePlacement.append('r');
                                case BISHOP -> piecePlacement.append('b');
                                case KNIGHT -> piecePlacement.append('n');
                                case PAWN -> piecePlacement.append('p');
                            }
                        }
                    }
                } else {
                    ++steps;
                    if (file == ModelConstants.BOARD_SIZE - 1)
                        piecePlacement.append(steps);
                }
            }
            if (rank != ModelConstants.BOARD_SIZE - 1)
                piecePlacement.append('/');
        }
        FEN += piecePlacement + " ";
        FEN += (turn == PlayerEnum.WHITE ? "w" : "b") + " ";
        String castlingRights = "";
        Piece piece = board.getPiece(new Position("e1"));
        if (piece != null && piece.getType() == PieceType.KING && !piece.hasMoved()) {
            piece = board.getPiece(new Position("h1"));
            if (piece != null && piece.getType() == PieceType.ROOK && !piece.hasMoved())
                castlingRights += 'K';
            piece = board.getPiece(new Position("a1"));
            if (piece != null && piece.getType() == PieceType.ROOK && !piece.hasMoved())
                castlingRights += 'Q';
        }
        piece = board.getPiece(new Position("e8"));
        if (piece != null && piece.getType() == PieceType.KING && !piece.hasMoved()) {
            piece = board.getPiece(new Position("h8"));
            if (piece != null && piece.getType() == PieceType.ROOK && !piece.hasMoved())
                castlingRights += 'k';
            piece = board.getPiece(new Position("a8"));
            if (piece != null && piece.getType() == PieceType.ROOK && !piece.hasMoved())
                castlingRights += 'q';
        }
        if (castlingRights.equals(""))
            castlingRights = "-";
        FEN += castlingRights + " ";
        if (possibleEnPassantPos != null) {
            for (int file = 0; file < ModelConstants.BOARD_SIZE; ++file) {
                for (int rank = 0; rank < ModelConstants.BOARD_SIZE; ++rank) {
                    Position pos = new Position(file, rank);
                    piece = board.getPiece(pos);
                    if (piece != null && piece.getColor() == turn && piece.getType() == PieceType.PAWN) {
                        if (getLegalMoves(pos).contains(possibleEnPassantPos)) {
                            FEN += possibleEnPassantPos;
                            return FEN;
                        }
                    }
                }
            }
        }
        return FEN;
    }

    private boolean isThreefoldRepetition() {
        HashMap<String, Integer> gamePositionOccurrences = new HashMap<>();
        for (String gamePosition : gamePositions) {
            if (gamePositionOccurrences.containsKey(gamePosition))
                gamePositionOccurrences.put(gamePosition, gamePositionOccurrences.get(gamePosition) + 1);
            else
                gamePositionOccurrences.put(gamePosition, 1);
        }
        return gamePositionOccurrences.containsValue(3);
    }

    private void updateGameState() {
        if (isCheckmate)
            gameState = turn == PlayerEnum.WHITE ? GameState.BLACK_WON : GameState.WHITE_WON;
        else if (!isThereAnyLegalMove())
            gameState = GameState.DRAW;
        else if (halfmoveClock == 2 * ModelConstants.FIFTY_MOVE || isThreefoldRepetition())
            gameState = GameState.DRAW;
        else
            gameState = GameState.IN_PROGRESS;
    }

    public PlayerEnum getTurn() {
        return turn;
    }

    public boolean isInCheck() {
        return isInCheck;
    }

    public GameState getGameState() {
        return gameState;
    }

    public String getLastMove() {
        return moves.get(moves.size() - 1);
    }

    public int getFullmoveNumber() {
        return fullmoveNumber;
    }

    public GameMode getGameMode() {
        return gameMode;
    }
}
