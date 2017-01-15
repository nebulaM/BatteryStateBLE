package com.github.batterystate;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
/**
 * Referred from an example in UBC's CPEN 221 taught by Dr.Gopalakrishnan and Dr.Ripeanu
 */

public class NetworkClient {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;


    public NetworkClient(String hostIP, int port) throws IOException{
        socket=new Socket(InetAddress.getByName(hostIP),port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
    }

    //TODO:CHANGE the identifier for client
    private static final String CORRECT_CLIENT ="222";
    /**
     *
     * @param data "ID:XX;Time:min-hr/month/data/year;Level:xx;Health:xx"
     * @throws IOException exception
     */
    public void sendRequest(String data) throws IOException {
        out.print(CORRECT_CLIENT +'!'+data+'\n');
        out.flush();
    }
    //TODO:CHANGE the identifier for server
    private static final String CORRECT_SERVER ="111";
    public String getReply() throws IOException {
        String reply = in.readLine();
        if(reply!=null){
            String[] dataSet=reply.split("!");
            if(dataSet[0].equals(CORRECT_SERVER)){
                return dataSet[1];
            }else{
                return null;
            }
        }
        return null;
    }

    public void close() throws IOException {
        in.close();
        out.close();
        socket.close();
    }
}
