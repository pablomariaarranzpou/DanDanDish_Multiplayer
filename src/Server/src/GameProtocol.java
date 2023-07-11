package Server.src;

import Utilitats.Util;

import java.io.IOException;

public class GameProtocol {

    private Util util;
    private int id, num_confirmation, op;

    public int getOp() {return op;}
    public void setOp(int op) {this.op = op;}
    public void setId(int id) {
        this.id = id;
    }
    public int getId() {
        return id;
    }

    public GameProtocol(Util util) {
        this.util = util;
    }

    public int getNum_confirmation() {return num_confirmation;}
    public void setNum_confirmation(int num_confirmation) {
        this.num_confirmation = num_confirmation;
    }

    public GameHandler.State reciveHello(){
        try {
            this.setOp(util.readSingleByte());
            if(this.getOp() == GameHandler.State.ERROR.getOpcode_num()){
                this.reciveError();
                return GameHandler.State.START;
            }
        } catch (IOException e) {
            System.out.println("CLIENT DISCONNECTED");
            return GameHandler.State.END;
        }
        if(this.getOp() != GameHandler.State.HELLO.getOpcode_num()){
            this.sendError((byte) GameHandler.State.ERROR.getOpcode_num(), GameHandler.Error.ERRCODE5);
            return GameHandler.State.ERROR;
        }else{
            try {
                int id = util.readInt32();
                this.setId(id);
                String playerName = util.readString();
                System.out.println("HELLO  C -------"+this.getOp()+" "+this.getId()+" "+playerName+" --> S");
                return GameHandler.State.HELLO;
            } catch (IOException e) {
                this.sendError((byte) GameHandler.State.ERROR.getOpcode_num(), GameHandler.Error.ERRCODE1);
                return GameHandler.State.ERROR;
            }
        }
    }

    public GameHandler.State sendReady(byte opcode){
        try {
            util.writeSingleByte(opcode);
            this.setOp(opcode);
            util.writeInt32(this.getId());
            System.out.println("READY  C <------"+this.getOp()+" "+this.getId()+" --------- S");
            return GameHandler.State.READY;
        } catch (IOException e) {
            this.sendError((byte) GameHandler.State.ERROR.getOpcode_num(), GameHandler.Error.ERRCODE1);
            return GameHandler.State.ERROR;
        }
    }

    public GameHandler.State recivePlay(){
        try{
            this.setOp(util.readSingleByte());
            if(this.getOp() == GameHandler.State.ERROR.getOpcode_num()){
                this.reciveError();
                return GameHandler.State.READY;
            }
        } catch (IOException e) {
            System.out.println("CLIENT DISCONNECTED");
            return GameHandler.State.END;
        }
        if(this.getOp() != GameHandler.State.PLAY.getOpcode_num()){
            this.sendError((byte) GameHandler.State.ERROR.getOpcode_num(), GameHandler.Error.ERRCODE5);
            return GameHandler.State.ERROR;
        }else{
            try{
                int id_play = util.readInt32();
                if (id_play == this.getId()){
                    this.setNum_confirmation(1);
                }else{
                    this.setNum_confirmation(0);
                }
                System.out.println("PLAY   C -------"+this.getOp()+" "+this.getId()+" --------> S");
                return GameHandler.State.PLAY;
            }catch (IOException e) {
                this.sendError((byte) GameHandler.State.ERROR.getOpcode_num(), GameHandler.Error.ERRCODE1);
                return GameHandler.State.ERROR;
            }
        }
    }

    public GameHandler.State sendAdmit(byte opcode){
        try {
            util.writeSingleByte(opcode);
            this.setOp(opcode);
            util.writeSingleByte((byte) this.getNum_confirmation());
            System.out.println("ADMIT  C <------"+this.getOp()+" "+this.getNum_confirmation()+" ------------- S");
            if (this.getNum_confirmation() == 0){
                this.sendError((byte) GameHandler.State.ERROR.getOpcode_num(), GameHandler.Error.ERRCODE4);
                return GameHandler.State.ERROR;
            }else{
                return GameHandler.State.ADMIT;
            }
        } catch (IOException e) {
            this.sendError((byte) GameHandler.State.ERROR.getOpcode_num(), GameHandler.Error.ERRCODE1);
            return GameHandler.State.ERROR;
        }
    }

    public String reciveAction() throws IOException {
        try{
            this.setOp(util.readSingleByte());
            if(this.getOp() == GameHandler.State.ERROR.getOpcode_num()){
                this.reciveError();
                return "R_ERROR";
            }
        } catch (IOException e) {
            System.out.println("CLIENT DISCONNECTED");
            return "CD_END";
        }
        if(this.getOp() != GameHandler.State.ACTION.getOpcode_num()){
            this.sendError((byte) GameHandler.State.ERROR.getOpcode_num(), GameHandler.Error.ERRCODE5);
            return "S_ERROR";
        }else{
            String action = util.readActionResult();
            System.out.println("ACTION C -------"+this.getOp()+" "+action+" --------> S");
            return action;
        }
    }

    public GameHandler.State sendResult(byte opcode, GameResult.Result result){
        try {
            util.writeSingleByte(opcode);
            this.setOp(opcode);
            util.writeActionResult(result.getResultString());
            System.out.println("RESULT C <------"+this.getOp()+" "+result+" --------- S");
            return GameHandler.State.ACTION;
        } catch (IOException e) {
            this.sendError((byte) GameHandler.State.ERROR.getOpcode_num(), GameHandler.Error.ERRCODE1);
            return GameHandler.State.ERROR;
        }
    }

    public void sendError(byte opcode, GameHandler.Error error){
        try {
            util.writeSingleByte(opcode);
            this.setOp(opcode);
            util.writeSingleByte((byte) error.getErr_code());
            util.writeString(error.getMsg());
            System.out.println("ERROR  S ------"+this.getOp()+" "+ error.getErr_code() + ", "+ error.getMsg() +" -------> C");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reciveError() throws IOException {
        byte errCode = util.readSingleByte();
        String msg = util.readString();
        System.out.println("ERROR  S <------"+this.getOp()+" "+ errCode + ", "+ msg +" -------- C");

     }
}


