package play.java.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.OutputStream;

public class SocketServer {

    private static void accept(Socket sock){
        System.out.println("accept " + sock.getInetAddress() + ":" + sock.getPort());

        try {
            InputStream   is = sock.getInputStream();
            OutputStream out = sock.getOutputStream();

            while(true){
                byte[] b=new byte[1024];

                // XXX: blocking reading
                int len = is.read(b);

                if(len==-1){
                    break;
                }

                String str=new String(b);
                System.out.print(str);

            }

            is.close();
            out.close();

            sock.close();
        } catch (Exception e){
            e.printStackTrace();
        }

        return;
    }

    private static ServerSocket listen(int port) throws Exception {
        ServerSocket server=new ServerSocket(port);
        return server;
    }


    public static void main(String[] args){
        int port = 9001;

        try {
            ServerSocket server = listen(port);
            System.out.println("listen on port " + port);

            while(true){
                Socket sock=server.accept();
                accept(sock);
                break;
            }

            server.close();

        } catch (Exception e){
            e.printStackTrace();
        }

    }
}

/* vim:set sw=4 :*/
