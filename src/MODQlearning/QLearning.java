package MODQlearning;

import pers.FileIO;

import java.util.HashMap;
import java.util.Random;

/**
 * Created by Peter on 7. 1. 2017.
 */
public class QLearning {

    private final double alpha = 0.1; // learning rate  0 - no learning
    private final double gamma = 0.9; // discount factor (importance of future rewards) 0 - only-short sighted
    private double random = 0.8;

    private State states[];
    private Action actions[];

    private double[][] qMatrix;

    private HashMap<State, Integer> stateIndices = new HashMap<>();
    private HashMap<Action, Integer> actionIndices = new HashMap<>();

    private FileIO qMatrixFile;

    private final Random mProbabilityRandom;
    private final Random mActionIndexRandom;

    public QLearning() {
        initializeStates();
        initializeActions();
        loadMatrixIO();

        if (qMatrixFile.loadFromFile() != null && qMatrixFile.loadFromFile().length == states.length && qMatrixFile.loadFromFile()[0].length == actions.length) {
            this.qMatrix = qMatrixFile.loadFromFile();
        } else {
            buildMatrix();
        }

        buildIndices();

        mProbabilityRandom = new Random();
        mActionIndexRandom = new Random();
    }

    public void initializeStates() {
        System.out.println(":: Initializing states ::");
        states = MatrixBuilder.build();
    }

    public void initializeActions() {
        System.out.println(":: Initializing actions ::");
        actions = new Action[]
                {
                    new SafeAction(),
                    new NormalAction(),
                    new RiskAction()
                };
    }

    public void loadMatrixIO() {
        System.out.println(":: Loading matrix ::");
        qMatrixFile = new FileIO("qMatrix.txt");
    }

    public void saveMatrixIO() {
        System.out.println(":: Saving matrix ::");
        qMatrixFile.saveToFile(getQMatrix());
    }


    public void buildIndices() {

        System.out.println(":: Building indices ::");

        for (int i = 0; i < states.length; i++) {
            stateIndices.put(states[i], i);
        }

        for (int i = 0; i < actions.length; i++) {
            actionIndices.put(actions[i], i);
        }

        System.out.println(":: State indices size = "+stateIndices.size()+" ::");
        System.out.println(":: Action indices size = "+actionIndices.size()+" ::");

    }

    public void buildMatrix() {
        System.out.println("::Building matrix ::");
        qMatrix = new double[states.length][actions.length];

        for(int i = 0; i < states.length; i++)
        {
            for(int j = 0; j < actions.length; j++)
            {
                qMatrix[i][j] = 0.0;
            }
        }
        System.out.println(":: Matrix = ["+states.length+"]x["+actions.length+"]  ::");
    }


    public double maxQ(int stateIndex) {
        double maxValue = Double.MIN_VALUE;

        for (int actionIndex = 0; actionIndex < actions.length; actionIndex++) {
            maxValue = Math.max(qMatrix[stateIndex][actionIndex], maxValue);
        }
        return maxValue;
    }

    public Action estimateBestActionIn(State state) {
        int stateIndex = stateIndices.get(state);
        int bestActionIndex = -1;
        double maxValue = Double.MIN_VALUE;

        for (int actionIndex = 0; actionIndex < actions.length; actionIndex++) {
            double value = qMatrix[stateIndex][actionIndex];

            if (bestActionIndex == -1 || value > maxValue) {
                maxValue = value;
                bestActionIndex = actionIndex;
            }
        }

        if (mProbabilityRandom.nextDouble() < random)
        {
            bestActionIndex = mActionIndexRandom.nextInt(actions.length);
        }
        return actions[bestActionIndex];
    }


    public void experience(State currentState, Action action, State nextState, double paReward) {

        System.out.println(":: Calculating experience ::");

        int currentStateIndex = stateIndices.get(currentState);
        int nextStateIndex = stateIndices.get(nextState);
        int actionIndex = actionIndices.get(action);

        double q = qMatrix[currentStateIndex][actionIndex];
        double r = paReward;

        double maxQ = maxQ(nextStateIndex);

        double value = q + alpha * (r + gamma * maxQ - q);

        qMatrix[currentStateIndex][actionIndex] = value;
    }

       public double[][] getQMatrix() {
        return qMatrix;
    }

    public double getAlpha() {
        return alpha;
    }

    public double getGamma() {
        return gamma;
    }

    public State[] getStates() {
        return states;
    }

    public void setStates(State[] states) {
        this.states = states;
    }

    public Action[] getActions() {
        return actions;
    }

    public void setActions(Action[] actions) {
        this.actions = actions;
    }

    public double[][] getqMatrix() {
        return qMatrix;
    }

    public void setqMatrix(double[][] qMatrix) {
        this.qMatrix = qMatrix;
    }

    public HashMap<State, Integer> getStateIndices() {
        return stateIndices;
    }

    public void setStateIndices(HashMap<State, Integer> stateIndices) {
        this.stateIndices = stateIndices;
    }

    public HashMap<Action, Integer> getActionIndices() {
        return actionIndices;
    }

    public void setActionIndices(HashMap<Action, Integer> actionIndices) {
        this.actionIndices = actionIndices;
    }

    public FileIO getqMatrixFile() {
        return qMatrixFile;
    }

    public void setqMatrixFile(FileIO qMatrixFile) {
        this.qMatrixFile = qMatrixFile;
    }
}
