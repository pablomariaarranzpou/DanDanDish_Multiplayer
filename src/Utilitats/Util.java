package Utilitats;
import Server.src.GameHandler;

import javax.imageio.IIOException;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class Util {

    private DataInputStream data_input;
    private DataOutputStream data_stream;
    private final int STRSIZE = 5;

    public Util(Socket socket) throws IOException {
        this.data_input = new DataInputStream(socket.getInputStream());
        this.data_stream = new DataOutputStream(socket.getOutputStream());
    }

    // reads a specified number of bytes from the input stream and returns them as a byte array
    private byte[] read_bytes(int numBytes) throws IOException {
        int len = 0;
        byte string_bytes[] = new byte[numBytes];
        int bytesRead = 0;
        do{
            bytesRead = data_input.read(string_bytes, len, numBytes-len);
            len+=bytesRead;
        }while (len < numBytes);

        return string_bytes;
    }

    //reads one byte
    public byte readSingleByte() throws IOException {
        byte byteValue = -1;
        byteValue = data_input.readByte();
        return byteValue;
    }

    //writes one byte
    public void writeSingleByte(byte byteValue) throws IOException {
        data_stream.writeByte(byteValue);
    }

    //method takes a byte array of length 4 and returns the corresponding integer value
    private int bytesToInt32(byte bytes[]) {
        int num;
        num = ((bytes[0] & 0xFF) << 24) | ((bytes[1] & 0xFF) << 16) | ((bytes[2] & 0xFF) << 8) | (bytes[3] & 0xFF);
        return num;
    }

    //method takes an integer and a byte array of length 4 and converts the integer to a byte array in big-endian format
    public byte[] int32ToBytes(int num, byte bytes[]){
        bytes[0] = (byte) ((num >> 24) & 0xFF);
        bytes[1] = (byte) ((num >> 16) & 0xFF);
        bytes[2] = (byte) ((num >> 8) & 0xFF);
        bytes[3] = (byte) (num & 0xFF);
        return bytes;
    }

    //reads int32 previously codified in big endian format
    public int readInt32() throws IOException {
        byte bytes[] = new byte[4];
        bytes = read_bytes(4);
        return bytesToInt32(bytes);
    }

    //writes int32 in big endian format
    public void writeInt32(int intValue) throws IOException {
        byte bytes[] = new byte[4];
        data_stream.write(int32ToBytes(intValue,bytes), 0, 4);
    }

    //returns a string previously codified in UTF8
    public String readString () throws IOException {
        String stringValue = new String();
        while (true) {
            char c = data_input.readChar();
            if (c == 0) {
                break;
            }
            stringValue+=c;
        }
        return stringValue;
    }

    //writes a codified UT8 string with 00 end
    public void writeString(String stringValue) throws IOException {
        for (char c: stringValue.toCharArray()){
            data_stream.writeChar(c);
        }
        data_stream.writeChar((byte)0);
    }

    //returns a string of 5 characters in UTF format
    public String readActionResult () throws IOException {
        String stringValue = new String();
        for (int i = 0; i < STRSIZE; i++) {
            stringValue+=data_input.readChar();
        }
        return stringValue;
    }

    //writes a codified string of 5 characters
    public void writeActionResult (String str) throws IOException {
        for (char c: str.toCharArray()){
            data_stream.writeShort(c);
        }
    }

    //close the input stream
    public void closeInput() throws IOException {
        data_input.close();
    }

    //close the output stream
    public void closeOutput() throws IOException {
        data_stream.close();
    }

    //flush the output stream
    public void flush() throws IOException {
        data_stream.flush();
    }


}
