package communicator;

import java.util.*;
import battlecode.common.MapLocation;

public class NegaMax {

	private int negamax(int depth, int alpha, int beta) {
		int score = Integer.MIN_VALUE;

		if (depth == 0) {
			return evaluate();
		}

		generatePossibleActions();

		while (hasActions()) {

			doNextAction();
			int actualvalue = -negamax(depth - 1, -beta, -alpha);
			redoAction();

			if (score < actualvalue) {
				score = actualvalue;
				// TODO: save best move
			}
			if (actualvalue >= beta) {
				// Betacut
				return beta;
			}
			if (score > alpha) {
				// Alphacut
				alpha = score;

			}
		}

		return score;
	}

	// TODO: evaluation function
	public int evaluate() {
		return 0;
	}

	// generate all possible actions the player can make
	public void generatePossibleActions() {

	}

	// looks if there are actions which can be done or not
	public boolean hasActions() {
		return false;
	}

	public void doNextAction() {

	}

	public void redoAction() {

	}

}
