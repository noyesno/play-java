/* vim:set sw=4 :*/

package play.java.server;

import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.Future;


public class SocketServerAsync {

  public static void main(String[] args){

    AsynchronousServerSocketChannel server;

    CompletionHandler<Integer, ByteBuffer> read_handler;
    CompletionHandler<AsynchronousSocketChannel, AsynchronousServerSocketChannel> accept_handler;

    read_handler = new CompletionHandler<Integer, ByteBuffer>() {
       public void completed(Integer bytesRead, ByteBuffer byteBuffer) {
	 byteBuffer.flip();

	 byte[] lineBytes = new byte[ bytesRead ];
	 byteBuffer.get( lineBytes, 0, bytesRead );
	 String line = new String( lineBytes );
	 System.out.print(line);

       }
       public void failed(Throwable exc, ByteBuffer att) {
         // ...
       }
    };


    accept_handler = new CompletionHandler<AsynchronousSocketChannel, AsynchronousServerSocketChannel>(){
       public void completed(AsynchronousSocketChannel ch, AsynchronousServerSocketChannel server) {
	 // accept the next connection
	 server.accept(null, this);


	 try {
           InetSocketAddress remote = (InetSocketAddress) ch.getRemoteAddress();

           String remote_host = remote.getHostString();
           int    remote_port = remote.getPort();
	   System.out.println("accept client " + remote_host + ":" + remote_port );

	   boolean do_accept = true;
	   while(do_accept){

	       ByteBuffer byteBuffer = ByteBuffer.allocate( 4096 );

               Future<Integer> read_stat = ch.read(byteBuffer);

	       int bytesRead  = -1;
               int idle_count = 0;
               while(idle_count < 3){
		 try {
		   bytesRead = read_stat.get(5, TimeUnit.SECONDS);
		 } catch (TimeoutException e){
		   idle_count++;
		   System.out.println("read client data timeout " + idle_count);
                   continue;
		 }

                 break;
               }

	       // ch.read( byteBuffer, 5L, TimeUnit.SECONDS, byteBuffer,  read_handler);

	       // System.out.println(bytesRead);
	       if(bytesRead == -1) {
		 do_accept = false;
		 break;
	       }
	       if(bytesRead == 0) {
		 System.out.println("no read");
		 break;
	       }
	       byteBuffer.flip();

	       byte[] lineBytes = new byte[ bytesRead ];
	       byteBuffer.get( lineBytes, 0, bytesRead );
	       String line = new String( lineBytes );
	       System.out.print(line);
	   }

	   System.out.println("close client");
	   ch.close();
	 } catch (Exception e) {
	   System.out.println(e);
	 }
       }

       public void failed(Throwable exc, AsynchronousServerSocketChannel server) {
         // ...
       }
    };


    int port = 9001;
    System.out.println("listen on port " + port);
    try {
      server = AsynchronousServerSocketChannel.open().bind(new InetSocketAddress(port));
      server.accept(server, accept_handler);
    }  catch (Exception e) {

    }


    try {
      System.out.println("will wait some time");
      Thread.sleep( 60000 );
      System.out.println("listen timeout");
    } catch( Exception e ) {
      e.printStackTrace();
    }

    return;
  } // end main
}
