package damas;

import damas.misc.CoordinateDamas;
import damas.misc.Damable;
import lib.Data.ListManip;
import proto.Game;
import proto.GamePane;

import java.util.ArrayList;
import java.util.Arrays;

import static lib.Misc.IO.println;
import static lib.Misc.IO.scanInt;

public class Damas implements Game {


    private static String stage = "";
    private DamasBoard table;
    private DamasPlayer PLAYER_1;
    private DamasPlayer PLAYER_2;
    private GamePane gamepane = null;
    private Damable menu;
    private ArrayList<int[]> movables;
    private ArrayList<int[]> moves;
    private ArrayList<int[]> attacks;
    private ArrayList<int[]> movats;

    private int size = 8;

    private int totalWidth = 900;

    public Damas() {
        this(8);
    }

    public Damas(int size) {
        this.size = size;
        PLAYER_1 = DamasPlayer.newPlayer();
        PLAYER_2 = DamasPlayer.newPlayer();
    }

    public static String getStage() {
        return stage;
    }

    private static void setStage(int n) {
        switch (n) {
            case 0:
                stage = "piece";
                break;//pick a piece
            case 1:
                stage = "move";
                break;
        }
    }

    public int getTotalWidth() {
        return totalWidth;
    }

    public void setTotalWidth(int totalWidth) {
        this.totalWidth = totalWidth;
    }

    public void setGamepane(GamePane gamepane) {
        this.gamepane = gamepane;
    }

    @Override
    public void startGame() {
        if (gamepane == null)
            menu = new damasConsole();
        else menu = new damasWindow();

        table = new DamasBoard(size);
        table.setTotalWidth(totalWidth);

        int count = 1;
        while (table.isGameOver() == null) {
            DamasPlayer.setActivePlayer((count++ % 2 == 1) ? PLAYER_1 : PLAYER_2);
            nextTurn();
        }
        menu.gameOver();
    }

    @Override
    public void setGamePane(GamePane gamepane) {
        this.gamepane = gamepane;
        totalWidth = gamepane.getMaxWidth();
    }

    private void nextTurn() {
        //Pick piece to move
        setStage(0);
        movables = table.listOfActionables(DamasPlayer.getActivePlayer());
        if (movables.size() == 0) {
            println("Player " + DamasPlayer.getActivePlayer().getIdQ() + " has no pieces to moves. Check Bugs!");
            return;
        }
        int piece = menu.pickPiece();
        int[] pieceCoords = movables.get(piece);

        //Pick Move
        setStage(1);
        moves = table.listOfMoves(pieceCoords);
        attacks = table.listOfAttackMoves(pieceCoords);
        movats = new ArrayList<>();
        movats.addAll(moves);
        movats.addAll(attacks);
        if (movats.size() == 0) {
            println("Player " + DamasPlayer.getActivePlayer().getIdQ() + " has no available moves. Check Bugs!");
            return;
        }

        do {
            int move = menu.pickMove();
            int[] destination = movats.get(move);

            if (move < moves.size()) {
                table.moveTo(pieceCoords, destination);
                break;
            }
            table.eatOverTo(pieceCoords, destination);
            pieceCoords = destination;

            attacks = movats = table.listOfAttackMoves(pieceCoords);
            moves.clear();
        } while (movats.size() > 0 && table.isGameOver() == null);
    }

    public class damasConsole implements Damable {

        @Override
        public int pickPiece() {
            table.printBoard();
            println("Player's " + DamasPlayer.getActivePlayer().getIdQ() + " turn.");

            ListManip.printList(movables, true, 1);

            //pick piece
            int piece;
            do {
                piece = scanInt("Move piece: ");
            } while (piece < 1 || piece > movables.size());
            return piece - 1;
        }

        @Override
        public int pickMove() {
            if (moves.size() > 0) {
                println("Moves: ");
                ListManip.printList(moves, true, 1);
            }

            if (attacks.size() > 0) {
                println("Attacks: ");
                ListManip.printList(attacks, true, moves.size() + 1);
            }

            int move;
            do {
                move = scanInt("Pick a move:");
            } while (move < 1 || move > movats.size());
            return move - 1;
        }

        @Override
        public void gameOver() {
            table.printBoard();
            System.out.println("Game Over\nWinner is: " + table.isGameOver());
        }
    }

    public class damasWindow implements Damable {

        @Override
        public int pickPiece() {
            String msg = table.toString() +
                    "<table><tr><td class=\"tail\">" +
                    "Player's " + DamasPlayer.getActivePlayer().getUtf() + " turn:" +
                    "</td></tr></table>" +
                    "Pick a piece :";

            CoordinateDamas[] movablesArray = CoordinateDamas.pickAPiece(movables);
            CoordinateDamas pick = (CoordinateDamas) gamepane.showInputDialog(msg, movablesArray);
            return Arrays.binarySearch(movablesArray, pick);
        }

        @Override
        public int pickMove() {
            String msg = table.toString() +
                    "<table><tr><td class=\"tail\">" +
                    "Player's " + DamasPlayer.getActivePlayer().getUtf() + " turn:" +
                    "</td></tr></table>" +
                    "Pick a move :";

            CoordinateDamas[] movatsArray = CoordinateDamas.pickAMove(moves, attacks);
            CoordinateDamas pick = (CoordinateDamas) gamepane.showInputDialog(msg, movatsArray);

            return pick.getIndexOf(movatsArray);
        }

        @Override
        public void gameOver() {
            String msg = table.toString() +
                    "<table><tr><td class=\"tail\">" +
                    "Player's " + DamasPlayer.getActivePlayer().getUtf() + " wins!" +
                    "</td></tr></table>";
            gamepane.showMessageDialog(msg);

        }
    }


}
