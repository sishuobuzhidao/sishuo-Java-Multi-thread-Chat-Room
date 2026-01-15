package codes;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class Server{
    public static void main(String[] args) throws IOException{
        HashMap<String,String> users = new HashMap<>();
        //防止用户名重复所以使用HashMap
        BufferedReader br = new BufferedReader(new FileReader("files\\users.txt"));
        String str;
        while((str = br.readLine()) != null){
            users.put(str.split("=")[0], str.split("=")[1]);
        } //读取数据IO
        br.close();

        ArrayList<Socket> clients = new ArrayList<>();

        ServerSocket ss = new ServerSocket(10096);
        System.out.println("Waiting for clients to connect...");
        
        while(true){
            Socket socket = ss.accept();
            clients.add(socket);
            System.out.println("Package accepted");

            //一有用户连接就创建线程
            ThreadForClient tfc = new ThreadForClient(socket, users, clients);
            tfc.start();
            //处理用户发送的数据的代码都在ThreadForClient当中。
            //方法我都转移到ThreadForClient当中了，Server这里的我都删除了。
        }
        //ss.close();
    }
}