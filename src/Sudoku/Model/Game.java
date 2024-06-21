package Sudoku.Model;

import javafx.scene.Node;
import java.util.List;
import Sudoku.Enums.*;

public class Game
{
    // Game data
    private Board board;
    private List<Node> valueInsertHistory;
    private List<Node> hintInsertHistory;
    private List<String> valueInsertHistorySaved;
    private List<String> hintInsertHistorySaved;
    private boolean solvableOnly; // menu
    private boolean unlimitedHints; // menu
    private int hintsAvailable;
    private long userSolveTime; // puzzle
    private int lives;
    private int gameScore;
    private long userSolveTimeLastInsert;
    private long preSaveLoadUserTime;
    private boolean savingGame = false;
    private boolean gameSavedLoaded = false;
    private long savedTimeLoaded;
    private boolean clickedBack = false;
    private boolean soundMuted = false;
    private boolean gamePaused;
    private boolean gameOver;

    // States
    private GameScenes gameScene = GameScenes.MenuScene;
    private GameModes gameMode = GameModes.NormalMode;

    /**
     * @author  Danny
     */
    public int calculateHintsAvailable()
    {
        return !unlimitedHints ? (int) (Math.ceil((board.getAvailableCells() - board.getFilledCells()) * 0.13)) : -1;
    }

    /**
     * @author  Danny, Abinav & Yahya
     */
    public long calculateUserSolvingTime() {
        return board.getBoardSizeRowsColumns() != 1 ? (long) ((Math.ceil(((board.getBoardSizeRowsColumns() * board.getBoardSizeRowsColumns()) / 81.0) * 10.0)) * 60000) : 10000;
    }

    /**
     * @author  Danny, Abinav & Yahya
     */
    public int calculateLivesBasedOnBoardSize() {
        return (int) (Math.ceil(((board.getBoardSizeRowsColumns() / 2.0)) * ((1 - ((double) board.getFilledCells() / board.getAvailableCells())) / 0.63)));
    }

    /**
     * @author  Danny
     */
    public int calculateGameScore(int amount, boolean increaseScore)
    {
        if(increaseScore)
        {
            return gameScore != 0 ? gameScore + (int) Math.ceil(((1 - (userSolveTime - userSolveTimeLastInsert) / (double) userSolveTime) * amount)) : amount;
        }
        else
        {
            return gameScore - amount;
        }
    }

    /**
     * @author Danny
     */
    public void setBoard(Board board)
    {
        this.board = board;
    }

    /**
     * @author Danny
     */
    public void setValueInsertHistory(List<Node> valueInsertHistory)
    {
        this.valueInsertHistory = valueInsertHistory;
    }

    /**
     * @author Danny
     */
    public void setHintInsertHistory(List<Node> hintInsertHistory)
    {
        this.hintInsertHistory = hintInsertHistory;
    }

    /**
     * @author Danny
     */
    public void setValueInsertHistorySaved(List<String> valueInsertHistorySaved)
    {
        this.valueInsertHistorySaved = valueInsertHistorySaved;
    }

    /**
     * @author Danny
     */
    public void setHintInsertHistorySaved(List<String> hintInsertHistorySaved)
    {
        this.hintInsertHistorySaved = hintInsertHistorySaved;
    }

    /**
     * @author Danny
     */
    public void setSolvableOnly(boolean solvableOnly)
    {
        this.solvableOnly = solvableOnly;
    }

    /**
     * @author Danny
     */
    public void setUnlimitedHints(boolean unlimitedHints)
    {
        this.unlimitedHints = unlimitedHints;
    }

    /**
     * @author Danny
     */
    public void setHintsAvailable(int hintsAvailable)
    {
        this.hintsAvailable = hintsAvailable;
    }

    /**
     * @author Danny
     */
    public void setUserSolveTime(long userSolveTime)
    {
        this.userSolveTime = userSolveTime;
    }

    /**
     * @author Danny
     */
    public void setLives(int lives)
    {
        this.lives = lives;
    }

    /**
     * @author Danny & Abinav
     */
    public void setGameScore(int gameScore)
    {
        this.gameScore = gameScore;
    }

    /**
     * @author Danny
     */
    public void setUserSolveTimeLastInsert()
    {
        this.userSolveTimeLastInsert = userSolveTime;
    }

    /**
     * @author Danny
     */
    public void setPreSaveLoadUserTime(long preSaveLoadUserTime)
    {
        this.preSaveLoadUserTime = preSaveLoadUserTime;
    }

    /**
     * @author Danny
     */
    public void setSavingGame(boolean savingGame)
    {
        this.savingGame = savingGame;
    }

    /**
     * @author Danny
     */
    public void setGameSavedLoaded(boolean gameSavedLoaded)
    {
        this.gameSavedLoaded = gameSavedLoaded;
    }

    /**
     * @author Danny
     */
    public void setSavedTimeLoaded(long savedTimeLoaded)
    {
        this.savedTimeLoaded = savedTimeLoaded;
    }

    /**
     * @author Danny
     */
    public void setClickedBack(boolean clickedBack)
    {
        this.clickedBack = clickedBack;
    }

    /**
     * @author Danny
     */
    public void setSoundMuted(boolean soundMuted)
    {
        this.soundMuted = soundMuted;
    }

    /**
     * @author Danny
     */
    public void setGamePaused(boolean gamePaused)
    {
        this.gamePaused = gamePaused;
    }

    /**
     * @author Danny
     */
    public void setGameOver(boolean gameOver)
    {
        this.gameOver = gameOver;
    }

    /**
     * @author Danny
     */
    public void setGameScene(GameScenes gameScene)
    {
        this.gameScene = gameScene;
    }

    /**
     * @author Danny
     */
    public void setGameMode(GameModes gameMode)
    {
        this.gameMode = gameMode;
    }

    /**
     * @author Danny
     */
    public Board getBoard()
    {
        return board;
    }

    /**
     * @author Danny
     */
    public List<Node> getValueInsertHistory()
    {
        return valueInsertHistory;
    }

    /**
     * @author Danny
     */
    public List<Node> getHintInsertHistory()
    {
        return hintInsertHistory;
    }

    /**
     * @author Danny
     */
    public List<String> getValueInsertHistorySaved()
    {
        return valueInsertHistorySaved;
    }

    /**
     * @author Danny
     */
    public List<String> getHintInsertHistorySaved()
    {
        return hintInsertHistorySaved;
    }

    /**
     * @author Danny
     */
    public boolean getSolvableOnly()
    {
        return solvableOnly;
    }

    /**
     * @author Danny
     */
    public boolean getUnlimitedHints()
    {
        return unlimitedHints;
    }

    /**
     * @author Danny
     */
    public int getHintsAvailable()
    {
        return hintsAvailable;
    }

    /**
     * @author Danny
     */
    public long getUserSolveTime()
    {
        return userSolveTime;
    }

    /**
     * @author Danny
     */
    public int getLives()
    {
        return lives;
    }

    /**
     * @author Danny & Abinav
     */
    public int getGameScore()
    {
        return gameScore;
    }

    /**
     * @author Danny
     */
    public long getPreSaveLoadUserTime()
    {
        return preSaveLoadUserTime;
    }

    /**
     * @author Danny
     */
    public boolean getSavingGame()
    {
        return savingGame;
    }

    /**
     * @author Danny
     */
    public boolean getGameSavedLoaded()
    {
        return gameSavedLoaded;
    }

    /**
     * @author Danny
     */
    public long getSavedTimeLoaded()
    {
        return savedTimeLoaded;
    }

    /**
     * @author Danny
     */
    public boolean getClickedBack()
    {
        return clickedBack;
    }

    /**
     * @author Danny
     */
    public boolean getSoundMuted()
    {
        return soundMuted;
    }

    /**
     * @author Danny
     */
    public boolean getGamePaused()
    {
        return gamePaused;
    }

    /**
     * @author Danny
     */
    public boolean getGameOver()
    {
        return gameOver;
    }

    /**
     * @author Danny
     */
    public GameScenes getGameScene()
    {
        return gameScene;
    }

    /**
     * @author Danny
     */
    public GameModes getGameMode()
    {
        return gameMode;
    }
}
