package Server.src;

import Client.src.GameClient;
import Utilitats.Util;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.IllegalBlockingModeException;

public class GameHandler implements Runnable{

    private State current_state, state_error_prev;
    private Error current_error;
    private Action current_action;
    public Util util;
    private GameProtocol gameProtocol;
    public int server_bullets, client_bullets, roll_num;

    public enum Action{
        BLOCK,
        CHARG,
        SHOOT;

        public static Action getAction(String name) {
            for (Action action : Action.values()) {
                if (name.equals(action.toString())) {
                    return action;
                }
            }
            return null;
        }
    }

    public enum State{

        START(0),
        HELLO(1),
        READY(2),
        PLAY(3),
        ADMIT(4),
        ACTION(5),
        RESULT(6),
        ERROR(7),
        END(8);

        private int opcode_num;

        private State(int opcode_num){
            this.opcode_num = opcode_num;
        }
        public int getOpcode_num() {
            return opcode_num;
        }

    }

    public enum Error{
        ERRCODE1(1, "CARÀCTER NO RECONEGUT"),
        ERRCODE2(2, "ACCIÓ DESCONEGUDA"),
        ERRCODE3(3, "MISSATGE FORA DE PROTOCOL"),
        ERRCODE4(4, "INICI DE SESSIÓ INCORRECTE"),
        ERRCODE5(5, "MISSATGE MAL FORMAT"),
        ERRCODE99(99, " ERROR DESCONEGUT");

        private int err_code;
        private String msg;

        private Error(int err_code, String msg){ this.err_code = err_code; this.msg = msg;}
        public  int getErr_code() { return this.err_code;}
        public String getMsg() { return this.msg;}

    }

    public GameHandler(Util util) {
        this.util = util;
        this.gameProtocol = new GameProtocol(this.util);
        this.current_state = State.START;
    }

    public void run() {
        try {
            this.client_bullets = 0;
            this.server_bullets = 0;
            this.roll_num = 0;
            this.play();
        } catch (Exception e) {
            this.current_error = Error.ERRCODE99;
            this.state_error_prev = State.END;
            this.current_state = State.ERROR;
        }
    }

    public State validAction(String action){
        try{
            Action valid = Action.valueOf(action);
            switch (valid){
                case BLOCK:
                    return State.RESULT;
                case CHARG:
                    this.client_bullets+=1;
                    return State.RESULT;
                case SHOOT:
                    if(this.client_bullets>0){
                        this.client_bullets-=1;
                        return State.RESULT;
                    }else{
                        //MISSATGE FORA DE PROTOCOL
                        gameProtocol.sendError((byte)State.ERROR.getOpcode_num(),  Error.ERRCODE3);
                        this.state_error_prev = State.ACTION;
                        return State.ERROR;
                    }

                default:
                    switch (action){
                        case "R_ERROR":
                            return State.ACTION;
                        case "CD_END":
                            return State.END;
                        case "S_ERROR":
                            this.state_error_prev = State.ACTION;
                            return State.ERROR;
                        default:
                            //ACTION DESCONEGUT
                            gameProtocol.sendError((byte)State.ERROR.getOpcode_num(),  Error.ERRCODE2);
                            this.state_error_prev = State.ACTION;
                            return State.ERROR;
                    }
            }
        }catch (IllegalArgumentException e) {
            gameProtocol.sendError((byte)State.ERROR.getOpcode_num(),  Error.ERRCODE99);
            this.state_error_prev = State.ACTION;
            return State.ERROR;
        }

    }

    public void bulletsControll(Action server_action){
        switch (server_action){
            case CHARG:
                this.server_bullets+=1;
                System.out.println("SERVER ACTION: "+ server_action);
                break;
            case SHOOT:
                this.server_bullets-=1;
                System.out.println("SERVER ACTION: "+ server_action);
                break;
            case BLOCK:
                System.out.println("SERVER ACTION: "+ server_action);
                break;
        }

    }

    public void manageEndGame(GameResult.Result result){
        if (result == GameResult.Result.ENDS0 || result == GameResult.Result.ENDS1){
            this.current_state = State.READY;
        }else{
            this.current_state = State.ACTION;
        }
    }

    //game state management method
    public void play() throws Exception{

        while (this.current_state != State.END) {
            String action;
            switch (this.current_state) {
                case START:
                    this.current_state = gameProtocol.reciveHello();
                    this.state_error_prev = State.START;
                    break;
                case HELLO:
                    this.current_state = gameProtocol.sendReady((byte) State.READY.getOpcode_num());
                    this.state_error_prev = State.HELLO;
                    break;
                case READY:
                    this.current_state = gameProtocol.recivePlay();
                    this.roll_num = 0;
                    this.state_error_prev = State.READY;
                    break;
                case PLAY:
                    this.current_state = gameProtocol.sendAdmit((byte) State.ADMIT.getOpcode_num());
                    this.state_error_prev = State.PLAY;
                    break;
                case ADMIT, ACTION:
                    action = gameProtocol.reciveAction();
                    this.current_state = this.validAction(action);
                    this.current_action = Action.getAction(action);
                    break;
                case RESULT:
                    GameResult gameResult = new GameResult(this.server_bullets);

                    Action server_action;
                    if(roll_num == 0){
                        server_action = Action.CHARG;
                    }else{
                        server_action = gameResult.serverAction();
                    }
                    roll_num += 1;
                    this.bulletsControll(server_action);
                    GameResult.Result result = gameResult.getResult(this.current_action,server_action);
                    this.current_state = gameProtocol.sendResult((byte) State.RESULT.getOpcode_num(), result);
                    if (this.current_state != State.ERROR){
                        this.manageEndGame(result);
                    }
                    break;
                case ERROR:
                    this.current_state = this.state_error_prev;
                    break;
                case END:
                    break;
                default:
                    this.current_state = State.ERROR;
            }
        }
    }



}
