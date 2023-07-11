package Client.src;

import Server.src.GameHandler;
import Utilitats.Util;

import java.io.IOException;
import java.util.Random;

public class ClientProtocol {

    private Util util;
    private int id;
    int op_rd;
    int bool;

    public int getOp_rd() {return op_rd;}
    public void setOp_rd(int op_rd) {this.op_rd = op_rd;}
    public void setID(int id){
        this.id = id;
    }
    public int getId(){
        return this.id;
    }
    public ClientProtocol(Util util) {
        this.util = util;
    }

    public GameClient.State sendHello(String playerName, byte opcode){
        try{
            util.writeSingleByte(opcode);
            this.setID(generateRandomNumber());
            util.writeInt32(this.getId());
            util.writeString(playerName);
            System.out.println("HELLO  C -------"+this.getOp_rd()+" "+this.getId()+" "+playerName+" --> S");
            return GameClient.State.HELLO;
        }catch (IOException e) {
            this.sendError((byte) GameClient.State.ERROR.getState_num(), GameClient.Error.ERRCODE1);
            return GameClient.State.ERROR;
        }

    }

    public GameClient.State receiveReady(){
        try{
            this.setOp_rd(util.readSingleByte());
            if(this.getOp_rd() == GameClient.State.ERROR.getState_num()){
                this.reciveError();
                return GameClient.State.HELLO;
            }
            if(this.getOp_rd() != GameClient.State.READY.getState_num()){
                this.sendError((byte) GameClient.State.ERROR.getState_num(), GameClient.Error.ERRCODE6);
                return GameClient.State.ERROR;
            }
            // Leemos la ID que recibimos y comprobamos que sea la misma que enviamos anteriormente.
            if (util.readInt32() != this.getId()) {
                this.sendError((byte) GameClient.State.ERROR.getState_num(), GameClient.Error.ERRCODE4);
                return GameClient.State.ERROR;
            }
            System.out.println("READY  C <------"+this.getOp_rd()+" "+this.getId()+" --------- S");
            return GameClient.State.READY;
        }catch (IOException e) {
            this.sendError((byte) GameClient.State.ERROR.getState_num(), GameClient.Error.ERRCODE1);
            return GameClient.State.ERROR;
        }

    }

    public GameClient.State sendPlay(byte opcode) {
        try{
            util.writeSingleByte(opcode);
            util.writeInt32(this.getId());
            System.out.println("PLAY   C -------"+this.getOp_rd()+" "+this.getId()+" --------> S");
            return GameClient.State.PLAY;
        }catch (IOException e) {
            this.sendError((byte) GameClient.State.ERROR.getState_num(), GameClient.Error.ERRCODE1);
            return GameClient.State.ERROR;
        }

    }

    public GameClient.State receiveAdmit(){
        try{
            this.setOp_rd(util.readSingleByte());
            if(this.getOp_rd() == GameClient.State.ERROR.getState_num()){
                this.reciveError();
                return GameClient.State.HELLO;
            }
            if(this.getOp_rd() != GameClient.State.ADMIT.getState_num()){
                this.sendError((byte) GameClient.State.ERROR.getState_num(), GameClient.Error.ERRCODE6);
                return GameClient.State.PLAY;
            }
            bool = util.readSingleByte();
            if (bool == 0){
                this.sendError((byte) GameClient.State.ERROR.getState_num(), GameClient.Error.ERRCODE4);
                return GameClient.State.ERROR;
            }
            System.out.println("ADMIT  C <------"+this.getOp_rd()+" "+bool+" ------------- S");
            return GameClient.State.ADMIT;
        }catch (IOException e) {
            this.sendError((byte) GameClient.State.ERROR.getState_num(), GameClient.Error.ERRCODE1);
            return GameClient.State.ERROR;
        }


    }

    public GameClient.State sendAction(String action, byte opcode){
        try{
            util.writeSingleByte(opcode);
            util.writeActionResult(action);
            System.out.println("ACTION C -------"+this.getOp_rd()+" "+action+" --------> S");
            return GameClient.State.RESULT;
        }catch (IOException e) {
            this.sendError((byte) GameClient.State.ERROR.getState_num(), GameClient.Error.ERRCODE1);
            return GameClient.State.ERROR;
        }

    }


    public String receiveResult() throws IOException {
        this.setOp_rd(util.readSingleByte());
        if(this.getOp_rd() == GameClient.State.ERROR.getState_num()){
            this.reciveError();
            return "R_ERROR";
        }
        if(this.getOp_rd() != GameClient.State.RESULT.getState_num()){
            this.sendError((byte) GameClient.State.ERROR.getState_num(), GameClient.Error.ERRCODE6);
            return "S_ERROR";
        }
        String result = this.util.readActionResult();
        System.out.println("RESULT C <------"+this.getOp_rd()+" "+result+" --------- S");
        return result;
    }

    public void sendError(byte opcode, GameClient.Error error){
        try {
            util.writeSingleByte(opcode);
            this.setOp_rd(opcode);
            util.writeSingleByte((byte) error.getErr_code());
            util.writeString(error.getMsg());
            System.out.println("ERROR  S <-----"+this.getOp_rd()+" "+ error.getErr_code() + ", "+ error.getMsg() +" ------- C");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reciveError() throws IOException {
         byte errCode = util.readSingleByte();
         String msg = util.readString();
         System.out.println("ERROR  S -------"+this.getOp_rd()+" "+ errCode + ", "+ msg +" -------> C");
    }

    public static int generateRandomNumber() {
        Random rand = new Random();
        int min = 10000, max = 99999;
        return rand.nextInt((max - min) + 1) + min;
    }
}
