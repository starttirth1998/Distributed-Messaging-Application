import java.net.*;
import java.io.*;
import java.util.Scanner;

public class GreetingServer extends Thread {
   private static Socket server;
   private static ServerSocket serverSocket;

   private static DataInputStream in;
   private static DataOutputStream out;
   private static DatagramSocket dsoc;
   private static Scanner sc;
   private String type;

   public GreetingServer(String str) throws IOException {
      type = str;
   }

   public void run() {
      if(type.equals("Receiver"))
      {
        while(true) {
           try {

              String str;

              System.out.printf("\r>> ");

              while(true)
              {
                str = in.readUTF();
                System.out.println("\rSender: " + str);
                System.out.printf(">> ");
                String[] words = str.split("\\s");
                if(words[0].equals("Send"))
                {
                  if(words.length != 3)
                  {
                    System.out.println("\rFilename or method not provided... Try again with proper format");
                    System.out.printf(">> ");
                  }
                  else
                  {
                    saveFile(server,"output_"+words[1],words[2]);
                    System.out.printf(">> ");
                  }
                }
              }
           }
           catch(IOException e) {
           }
        }
      }
      else if(type.equals("Sender"))
      {
        try
        {
           String str;
           String filename;

           while(true)
           {
             System.out.printf("\r>> ");
             str = sc.nextLine();
             out.writeUTF(str);
             String[] words = str.split("\\s");
             //System.out.println(words[0]);
             if(words[0].equals("ExitExit"))
               break;
             if(words[0].startsWith("Send"))
             {
               try
               {
                 if(words.length != 3)
                 {
                   System.out.println("Filename or method not provided... Try again with proper format");
                 }
                 else
                 {
                   sendFile(words[1],words[2]);
                 }
               }
               catch(Exception e){
               }
             }
           }
        }catch(IOException e) {
        }
      }
   }

   public void printProgressBar(long totalRead, long filesize, String filename){
     long percent = ((totalRead*10)/filesize);
     //System.out.println(percent);
     System.out.printf("\rSending " + filename + " [");
     for(int i=0;i<percent;i++)
     {
       System.out.printf("=");
     }
     if(percent != 10)
     {
       System.out.print(">");
     }
     for(int i=0;i<10-percent;i++)
     {
       System.out.print(" ");
     }
      if(percent == 10)
      {
        System.out.println("] "+10*percent+"%");
      }
      else
      {
       System.out.print("] "+10*percent+"%\r");
      }
   }

   public void printProgressBar(long totalRead, long filesize){
     long percent = ((totalRead*10)/filesize);
     System.out.printf("\rReceiving [");
     for(int i=0;i<percent;i++)
     {
       System.out.printf("=");
     }
     if(percent != 10)
     {
       System.out.printf(">");
     }
     for(int i=0;i<10-percent;i++)
     {
       System.out.printf(" ");
     }
     if(percent == 10)
     {
       System.out.println("] "+10*percent+"%");
     }
     else
     {
       System.out.print("] "+10*percent+"%\r");
     }
   }

   public void saveFile(Socket server,String filename,String method) throws IOException{
     //DataInputStream din = new DataInputStream(server.getInputStream());
     FileOutputStream fout = new FileOutputStream(filename);
     byte[] buffer = new byte[4096];

     long filesize = in.readLong();
     long size = 0;
     long totalRead = 0;
     long remaining = filesize;

     byte[] cnt = new byte[4096];
     DatagramPacket dp = new DatagramPacket(cnt,cnt.length);
     while(totalRead < filesize)
     {
       if(method.equals("UDP"))
       {
         dsoc.receive(dp);
         buffer = dp.getData();
         size = dp.getLength();
       }
       else
       {
         size = in.read(buffer);
       }
       if(size < 0)
      {
        break;
      }
       totalRead += size;
       remaining -= size;
       //System.out.println(totalRead);
       if(remaining < 0)
       {
         remaining = 0;
         totalRead = filesize;
       }
       printProgressBar(totalRead,filesize);
       fout.write(buffer,0,(int)size);
     }
     fout.close();
   }

   public void sendFile(String filename, String method) throws IOException{
      FileInputStream fin = new FileInputStream(filename);
      byte[] buffer = new byte[4096];

      File f = null;
      f = new File(filename);
      long filesize = f.length();
      long remaining = filesize;
      long totalRead = 0;
      long read = 0;
      //System.out.println(f.length());
      out.writeLong(f.length());
      InetAddress host = InetAddress.getByName("localhost");

      while((read = fin.read(buffer)) > 0)
      {
        if(method.equals("UDP"))
        {
          DatagramPacket dp = new DatagramPacket(buffer,(int)read,host,9999);
          dsoc.send(dp);
        }
        else
        {
          out.write(buffer,0,(int)read);
        }
        totalRead += 4096;
        remaining -= 4096;
        if(remaining < 0)
        {
          remaining = 0;
          totalRead = filesize;
        }
        printProgressBar(totalRead,filesize,filename);
      }

      //dout.close();
      fin.close();
   }

   public static void main(String [] args) {
      int port1 = 9001;
      int port2 = 9000;
      try{
        serverSocket = new ServerSocket(port1);
        server = serverSocket.accept();
        dsoc = new DatagramSocket(port2);
        in = new DataInputStream(server.getInputStream());
        out = new DataOutputStream(server.getOutputStream());
      }
      catch(Exception e){
      }
      sc = new Scanner(System.in);

      try{
        Thread t1 = new GreetingServer("Receiver");
        t1.start();
      }
      catch(Exception e){
      }
      try {
         Thread t = new GreetingServer("Sender");
         t.start();
      }catch(Exception e) {
      }

   }
}
