package Server.src;

import java.util.Random;

public class GameResult {

    public int server_bullets;

    public GameResult(int server_bullets) {
        this.server_bullets = server_bullets;
    }

    public enum Result{

        ENDS0(0, "ENDS0"),
        ENDS1(1, "ENDS1"),
        PLUS0(0, "PLUS0"),
        PLUS1(1, "PLUS1"),
        PLUS2(2, "PLUS2"),
        DRAW0(0, "DRAW0"),
        SAFE0(0, "SAFE0"),
        SAFE1(1, "SAFE1"),
        SAFE2(2, "SAFE2");

        private int flag;
        private String result;

        private Result(int flag, String result){
            this.flag = flag;
            this.result = result;
        }
        public int getFlag() {
            return flag;
        }
        public String getResultString(){ return result; }

    }

    public Result getResult(GameHandler.Action client_action, GameHandler.Action server_action) {
        String action = client_action + "-" + server_action;
        switch (action){
            case "BLOCK-CHARG":
                return Result.PLUS0;
            case "CHARG-BLOCK":
                return Result.PLUS1;
            case "CHARG-CHARG":
                return Result.PLUS2;
            case "CHARG-SHOOT":
                return Result.ENDS0;
            case "SHOOT-CHARG":
                return Result.ENDS1;
            case "SHOOT-BLOCK":
                return Result.SAFE0;
            case "BLOCK-SHOOT":
                return Result.SAFE1;
            case "BLOCK-BLOCK":
                return Result.SAFE2;
            case "SHOOT-SHOOT":
                return Result.DRAW0;
            default:
                throw new RuntimeException();
        }
    }

    //FALTA IMPLEMENTAR HEURISTICA
    public GameHandler.Action serverAction(){
        //Server has got bullets
        if (this.server_bullets>0){
            Random rand = new Random();
            int num = rand.nextInt(10);
            switch (num){
                case 0, 1:
                    return GameHandler.Action.CHARG;
                case 2, 3, 4, 5, 6:
                    return GameHandler.Action.BLOCK;
                case 7,8,9:
                    return GameHandler.Action.SHOOT;
                default:
                    throw new IllegalStateException("ERROR");
            }
        }
        //Server hasn't got bullets
        else {
            Random rand = new Random();
            int num = rand.nextInt(10);

            switch (num) {
                case 0, 1, 2:
                    return GameHandler.Action.CHARG;
                case 3, 4, 5, 6 ,7, 8, 9:
                    return GameHandler.Action.BLOCK;
                default:
                    throw new IllegalStateException("ERROR");
            }
        }
    }

}
