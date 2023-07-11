package Server.src;

import Utilitats.Util;

import java.io.IOException;
import java.net.Socket;

public class MultiplayerGameHandler implements Runnable {


    private GameHandler.State current_state, state_error_prev;
    private GameHandler.Action current_action_p1, current_action_p2;
    public Util util_p1, util_p2;
    private GameProtocol gameProtocol_p1, gameProtocol_p2;
    public int p1_bullets, p2_bullets;
    boolean player1Turn;



    public void run() {
        try {
            this.p1_bullets = 0;
            this.p2_bullets = 0;
            this.play();
        } catch (Exception e) {
            GameHandler.Error current_error = GameHandler.Error.ERRCODE99;
            this.state_error_prev = GameHandler.State.END;
            this.current_state = GameHandler.State.ERROR;
        }
    }

    public MultiplayerGameHandler(Socket s1, Socket s2) {
        try {
            this.util_p1 = new Util(s1);
            this.util_p2 = new Util(s2);
            this.gameProtocol_p1 = new GameProtocol(this.util_p1);
            this.gameProtocol_p2 = new GameProtocol(this.util_p2);
            this.current_state = GameHandler.State.START;
            this.player1Turn = true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public GameHandler.State validAction(String action){
        try{
            GameHandler.Action valid = GameHandler.Action.valueOf(action);
            switch (valid){
                case BLOCK:
                    return GameHandler.State.RESULT;
                case CHARG:
                    if(this.player1Turn){
                        this.p1_bullets += 1;
                    }else{
                        this.p2_bullets += 1;
                    }
                    return GameHandler.State.RESULT;
                case SHOOT:
                    if(this.player1Turn){
                        if(this.p1_bullets > 0){
                            this.p1_bullets-=1;
                        }else{
                            gameProtocol_p1.sendError((byte) GameHandler.State.ERROR.getOpcode_num(),  GameHandler.Error.ERRCODE3);
                            this.state_error_prev = GameHandler.State.ACTION;
                            return GameHandler.State.ERROR;
                        }
                    }else {
                        if (this.p2_bullets > 0) {
                            this.p2_bullets -= 1;
                        } else {
                            gameProtocol_p2.sendError((byte) GameHandler.State.ERROR.getOpcode_num(), GameHandler.Error.ERRCODE3);
                            this.state_error_prev = GameHandler.State.ACTION;
                            return GameHandler.State.ERROR;
                        }
                    }
                    return GameHandler.State.RESULT;

                default:
                    switch (action) {
                        case "R_ERROR" -> {
                            return GameHandler.State.ACTION;
                        }
                        case "CD_END" -> {
                            return GameHandler.State.END;
                        }
                        case "S_ERROR" -> {
                            this.state_error_prev = GameHandler.State.ACTION;
                            return GameHandler.State.ERROR;
                        }
                        default -> {
                            //ACTION DESCONEGUT
                            if (player1Turn) {
                                gameProtocol_p1.sendError((byte) GameHandler.State.ERROR.getOpcode_num(), GameHandler.Error.ERRCODE2);
                            } else {
                                gameProtocol_p2.sendError((byte) GameHandler.State.ERROR.getOpcode_num(), GameHandler.Error.ERRCODE2);
                            }
                            this.state_error_prev = GameHandler.State.ACTION;
                            return GameHandler.State.ERROR;
                        }
                    }
            }
        }catch (IllegalArgumentException e) {
            if(player1Turn){
                gameProtocol_p1.sendError((byte) GameHandler.State.ERROR.getOpcode_num(),  GameHandler.Error.ERRCODE99);
            }else{
                gameProtocol_p2.sendError((byte) GameHandler.State.ERROR.getOpcode_num(),  GameHandler.Error.ERRCODE99);
            }
        }
            this.state_error_prev = GameHandler.State.ACTION;
            return GameHandler.State.ERROR;
        }


    //game state management method
    public void play() throws Exception {
        while (this.current_state != GameHandler.State.END) {
            switch (this.current_state) {
                case START:
                    this.current_state = gameProtocol_p1.reciveHello();
                    this.current_state = gameProtocol_p2.reciveHello();
                    this.state_error_prev = GameHandler.State.START;
                    break;
                case HELLO:
                    this.current_state = gameProtocol_p1.sendReady((byte) GameHandler.State.READY.getOpcode_num());
                    this.current_state = gameProtocol_p2.sendReady((byte) GameHandler.State.READY.getOpcode_num());
                    this.state_error_prev = GameHandler.State.HELLO;
                    break;
                case READY:
                    this.current_state = gameProtocol_p1.recivePlay();
                    this.current_state = gameProtocol_p2.recivePlay();
                    this.state_error_prev = GameHandler.State.READY;
                    break;
                case PLAY:
                    this.current_state = gameProtocol_p1.sendAdmit((byte) GameHandler.State.ADMIT.getOpcode_num());
                    this.current_state = gameProtocol_p2.sendAdmit((byte) GameHandler.State.ADMIT.getOpcode_num());
                    this.state_error_prev = GameHandler.State.PLAY;
                    break;
                case ADMIT, ACTION:
                    String action = gameProtocol_p1.reciveAction();
                    this.current_state = this.validAction(action);
                    this.current_action_p1 = GameHandler.Action.getAction(action);
                    player1Turn = false;
                    String action_2 = gameProtocol_p2.reciveAction();
                    this.current_state = this.validAction(action_2);
                    this.current_action_p2 = GameHandler.Action.getAction(action_2);
                    player1Turn = true;
                    break;
                case RESULT:
                    GameResult gameResult = new GameResult(0);
                    GameResult.Result result = gameResult.getResult(this.current_action_p1, current_action_p2);
                    this.current_state = gameProtocol_p1.sendResult((byte) GameHandler.State.RESULT.getOpcode_num(), result);
                    GameResult.Result result_2 = gameResult.getResult(this.current_action_p2, current_action_p1);
                    this.current_state = gameProtocol_p2.sendResult((byte) GameHandler.State.RESULT.getOpcode_num(), result_2);
                    if (this.current_state != GameHandler.State.ERROR) {
                        this.manageEndGame(result);
                    }
                    break;
                case ERROR:
                    this.current_state = this.state_error_prev;
                    break;
                case END:
                    break;
                default:
                    this.current_state = GameHandler.State.ERROR;
            }
        }

    }
    public void manageEndGame(GameResult.Result result){
        if (result == GameResult.Result.ENDS0 || result == GameResult.Result.ENDS1){
            this.current_state = GameHandler.State.READY;
        }else{
            this.current_state = GameHandler.State.ACTION;
        }
    }
}

