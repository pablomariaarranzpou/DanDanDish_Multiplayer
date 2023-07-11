package Client.src;

import Server.src.GameHandler;
import Utilitats.Util;

import java.sql.SQLOutput;
import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class GameClient {

    /*
    TO DO.
    Class that encapsulates the game's logic. Sequence of states following the established protocol .
     */

    private final List<String> actions = Arrays.asList("Bloquear", "Re-cargar", "Disparar");
    public Util util;
    private final ClientProtocol clientProtocol;
    private final Scanner scan;
    private boolean serverRedy;
    private State currentState, state_error_prev;
    private int clientMunition;
    private String userName;
    private int menuElection = 0;


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

        private int state_num;

        private State(int num){
            this.state_num = num;
        }

        public int getState_num() {
            return state_num;
        }

        /* public void setState_num(int state_num) {
            this.state_num = state_num;
        }*/

    }
    public enum Error{
        ERRCODE1(1, "CARÀCTER NO RECONEGUT"),
        ERRCODE2(2, "RESULTAT DESCONEGUT"),
        ERRCODE3(3, "MISSATGE FORA DE PROTOCOL"),
        ERRCODE4(4, "INICI DE SESSIÓ INCORRECTE"),
        ERRCODE6(6, "MISSATGE MAL FORMAT"),
        ERRCODE99(99, " ERROR DESCONEGUT");

        private int err_code;
        private String msg;

        private Error(int err_code, String msg){ this.err_code = err_code; this.msg = msg;}
        public  int getErr_code() { return this.err_code;}
        public String getMsg() { return this.msg;}

    }
    public GameClient(Util util) {
        this.util = util;
        this.scan = new Scanner(System.in);
        this.clientProtocol = new ClientProtocol(this.util);
        this.currentState = State.START;
        this.clientMunition = 0;
    }

    public void play() throws Exception {

        while (this.currentState!= State.END) {
            switch (this.currentState) {
                case START:
                    System.out.println("Escribe tu nombre más gamer: ");
                    userName = scan.nextLine();
                    this.currentState = this.clientProtocol.sendHello(userName, (byte)State.HELLO.getState_num());
                    this.state_error_prev = State.START;
                    break;
                case HELLO:
                    this.currentState = this.clientProtocol.receiveReady();
                    this.state_error_prev = State.HELLO;
                    break;
                case READY:
                    this.currentState = this.clientProtocol.sendPlay((byte) State.PLAY.getState_num());
                    this.state_error_prev = State.READY;
                    break;
                case PLAY:
                    this.currentState = this.clientProtocol.receiveAdmit();
                    this.clientMunition = 0;
                    this.state_error_prev = State.PLAY;
                    break;
                case ADMIT, ACTION:
                    this.currentState = this.clientProtocol.sendAction(actionMenu(), (byte) 5);
                    this.state_error_prev = State.ACTION;
                    break;
                case RESULT:
                    this.currentState = this.manageResult(this.clientProtocol.receiveResult());
                    if (this.currentState == State.END){
                        this.currentState = continueMenu();
                    }
                    break;
                case ERROR:
                    this.currentState = this.state_error_prev;
                    break;
                case END:
                    break;

                default:
                    throw new IllegalStateException("Unexpected value");
            }


        }

    }

    public State continueMenu(){
        System.out.println("¿Quieres volver a jugar?");

        while(true){
            System.out.println("[1] Volver a jugar");
            System.out.println("[2] Salir del juego");
            menuElection = this.scan.nextInt();
            if(menuElection == 1){
                return State.READY;
            } else if (menuElection == 2) {
                return State.END;
            }
            System.out.println("Por favor ingrese un número válido");

            }
        }


    public String actionMenu(){

        int action = 0;

        while (true) {
            try {
                System.out.println("[1] Recargar");
                System.out.println("[2] Proteger");

                if (this.clientMunition > 0) {
                    System.out.println("[3] Disparar");
                }
                System.out.println("ESCRIBE QUE QUIERES HACER: ");
                action = this.scan.nextInt();

                if (action < 1 || action > 3) {
                    System.out.println("ACCION NO DISPONIBLE " + action);
                }
                if (action == 3 && (this.clientMunition == 0)) {
                    throw new Exception();
                }
                break;
            } catch (InputMismatchException e) {
                System.err.println("DEBES INGRESAR UN NUMERO ENTERO VALIDO");
            }catch (Exception e){
                System.err.println("NO TIENES BALAS PARA DISPARAR");
            }
        }

        switch (action) {
            case 1 -> {
                this.clientMunition += 1;
                System.out.println("Has elegido recargar. Munición: "+ clientMunition );
                return "CHARG";
            }
            case 2 -> {
                System.out.println("Has elegido bloquear. Munición: "+ clientMunition );
                return "BLOCK";
            }
            case 3 -> {
                this.clientMunition -= 1;
                System.out.println("Has elegido disparar. Munición: "+ clientMunition );
                return "SHOOT";
            }

            default -> {
                return "ERROR";
            }
        }



    }

    public State manageResult(String result){

        switch (result) {
            case "ENDS0" -> {
                System.out.println("¡HAS PERDIDO! ¡GANA TU RIVAL!");
                return State.END;
            }
            case "ENDS1" -> {
                System.out.println("¡HA GANADO "+ userName + "! ¡FELICIDADES!");
                return State.END;
            }
            case "PLUS0" -> {
                System.out.println("TU RIVAL ACABA DE RECARGAR");
                return State.ACTION;
            }
            case "PLUS1" -> {
                System.out.println("TU RIVAL A BLOQUEADO");
                return State.ACTION;
            }
            case "PLUS2" -> {
                System.out.println("LOS DOS HABÉIS RECARGADO");
                return State.ACTION;
            }
            case "DRAW0" -> {
                System.out.println("TU RIVAL TAMBIÉN HA DISPARADO");
                return State.ACTION;
            }
            case "SAFE0" -> {
                System.out.println("EL RIVAL HA BLOQUEADO TU BALA");
                return State.ACTION;
            }
            case "SAFE1" -> {
                System.out.println("HAS BLOQUEADO LA BALA DE TU RIVAL");
                return State.ACTION;
            }
            case "SAFE2" -> {
                System.out.println("EL SERVIDOR HA BLOQUEADO TAMBIÉN");
                return State.ACTION;
            }case "R_ERROR"-> {
                return State.RESULT;
            }case "S_ERROR"-> {
                this.state_error_prev = State.RESULT;
                return State.ERROR;
            }default -> {
                clientProtocol.sendError((byte)State.ERROR.getState_num(), Error.ERRCODE2);
                this.state_error_prev = State.RESULT;
                return State.ERROR;
            }


        }
    }




}
