package edu.stanford.cs108.tetris;

public class TerisBrainLogic extends TetrisLogic {
    private DefaultBrain brain;
    private Brain.Move brainMove;
    private boolean brainMode;

    public TerisBrainLogic(TetrisUIInterface uiInterface) {
        super(uiInterface);
        brain = new DefaultBrain();
    }

    public void setBrainMode(boolean isChecked) {
        brainMode = isChecked;
    }

    @Override
    public void tick(int verb) {
        if (brainMode && verb == DOWN) {
            board.undo();
            brainMove = brain.bestMove(board, currentPiece, board.getHeight() - currentPiece.getHeight(), brainMove);
            if (brainMove != null) {
                if (currentX < brainMove.x) {
                    super.tick(RIGHT);
                } else if (currentX > brainMove.x) {
                    super.tick(LEFT);
                }
                if (!currentPiece.equals(brainMove.piece)) {
                    super.tick(ROTATE);
                }
            }
        }
        super.tick(verb);//keep the rest the same
    }
}
