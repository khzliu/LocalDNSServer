
/**
 * @version  1.0
 * @author liu.huazhou <khzliu@163.com>
 */
import java.net.*;
import java.io.*;

public class LocalDNSServer extends Thread
{
		private int port = 53;
		private byte[] buffer = new byte[512];
		
		private byte[] Question;
                private String webIP = "192.168.5.1";
		
		private String ClientIP = "";
		private int ClientPort = 0;
		
                
                
		DatagramSocket datagramSocket;
		DatagramPacket datagramPacket;		
	
		public LocalDNSServer()
		{
		}

		public void run(){
                    try{
                        try{     
                            datagramSocket = new DatagramSocket(port);
                            
                            while(true){ 
                                datagramPacket = new DatagramPacket(buffer, buffer.length);
	      			datagramSocket.receive(datagramPacket);
                                
                                ClientIP = datagramPacket.getAddress().getHostAddress();
	      			ClientPort = datagramPacket.getPort();
                                //打印客户端ip和端口
	      			System.out.println("\n>>>Info From " + ClientIP + ":" + String.valueOf(ClientPort) + "\n");
	      			webIP=ClientIP.substring(0,9)+".1";
	      			//System.out.println(datagramPacket.getAddress().getAddress().toString()+"======="+new String(datagramPacket.getData()));
	      			datagramPacket.setLength(datagramPacket.getData().toString().length());
	      			//System.out.println(datagramPacket.getLength());
	      			Decode();
                            }      
                        } catch(IOException e){
                            System.out.println(e);
                        }
                        try{
                            datagramSocket.close();
                        } catch(Exception e){
                            System.out.println(e);
                        }
                    } catch (Exception e){
                        System.out.println(e);
                    }
                    }

		private void Decode()
		{
                        
			String InfoFromClient = new String(datagramPacket.getData());
                        //System.out.println(InfoFromClient);
			String SubInfo = InfoFromClient.substring(12);
			String Name = SubInfo.substring(0, SubInfo.indexOf('\0'));
                        //System.out.println(Name);


			//System.out.append("===============================\n");
						
			Question = InfoFromClient.substring(12, 11+Name.length()+6).getBytes();
                        //byte[] Questions = InfoFromClient.substring(11, InfoFromClient.indexOf('\0')).getBytes();
                        //Question = Name.getBytes();
                        //PrintInfo(Question);
			
			for(int i=0; i<Name.length(); i++)
			{
				if((Name.charAt(i)>='a' && Name.charAt(i)<='z') || 
					(Name.charAt(i)>='A' && Name.charAt(i)<='Z') || 
					(Name.charAt(i)>='0' && Name.charAt(i)<='9'))
				{
					
				}
				else
					Name = Name.substring(0, i) + "." + Name.substring(i+1);
			}
			
			System.out.append("域名为：" + Name + "\n");
			System.out.append("===============================\n");
                        System.out.append("转发到"+webIP+"\n");
                        GetPacketAndSend(webIP);
		}
		
		private void GetPacketAndSend(String IP)
		{
                        byte[] ReturnBuffer = new byte[512];
			int ptr=0;
						
			for(int i=0; i<512; i++)
                            ReturnBuffer[i] = (byte)(ReturnBuffer[i] & (byte)0x00);
			
			// Head Section
			//ReturnBuffer[0] = buffer[0];
                        ReturnBuffer[0] = buffer[0];
			ReturnBuffer[1] = buffer[1];			
			/*
			 * QR = 1;
			 * OPCODE = 0000;
			 * AA = 0;
			 * TC = 0;
			 * RD = 1;
			 * RA = 1;
			 */
			ReturnBuffer[2] = (byte)0x81;
                        ReturnBuffer[3] = (byte)0x80;
			ReturnBuffer[4] = buffer[4];
			ReturnBuffer[5] = buffer[5];
                        ReturnBuffer[6] = (byte)0x00;
			ReturnBuffer[7] = (byte)0x01;			
			ReturnBuffer[8] = buffer[8];
			ReturnBuffer[9] = buffer[9];
			ReturnBuffer[10] = buffer[10];
			ReturnBuffer[11] = buffer[11];
			
			// Question Section
			ptr = 12;
			int i;
			for(i=0; i<Question.length; i++)
			{
				ReturnBuffer[ptr+i] = Question[i];
			}
			ptr = ptr + Question.length;
			
			
			// Recourse Section
			// NAME
			ReturnBuffer[ptr] = (byte)0xC0;
			ptr++;
			ReturnBuffer[ptr] = (byte)0x0C;
			ptr++;
			
			// TYPE
			ReturnBuffer[ptr] = (byte)0x00;
			ptr++;
			ReturnBuffer[ptr] = (byte)0x01;
			ptr++;
			
			// CLASS
			ReturnBuffer[ptr] = (byte)0x00;
			ptr++;
			ReturnBuffer[ptr] = (byte)0x01;
			ptr++;
			
			//TTL
			ReturnBuffer[ptr] = (byte)0x00;
			ptr++;
			ReturnBuffer[ptr] = (byte)0x00;
			ptr++;
			ReturnBuffer[ptr] = (byte)0x00;
			ptr++;
			ReturnBuffer[ptr] = (byte)0x70;
			ptr++;
			
			// RDLENGTH
			ReturnBuffer[ptr] = (byte)0x00;
			ptr++;
			ReturnBuffer[ptr] = (byte)0x04;
			ptr++;
			
			// RDATA
			int a = Integer.parseInt(IP.substring(0, IP.indexOf(".")));
			IP = IP.substring(IP.indexOf(".") + 1);
			int b = Integer.parseInt(IP.substring(0, IP.indexOf(".")));
			IP = IP.substring(IP.indexOf(".") + 1);
			int c = Integer.parseInt(IP.substring(0, IP.indexOf(".")));
			IP = IP.substring(IP.indexOf(".") + 1);
			int d = Integer.parseInt(IP);
			
			ReturnBuffer[ptr] = (byte)a;
			ptr++;
			ReturnBuffer[ptr] = (byte)b;
			ptr++;
			ReturnBuffer[ptr] = (byte)c;
			ptr++;
			ReturnBuffer[ptr] = (byte)d;
			ptr++;
			try{
                        //System.out.print("\nReturnBuffer1:");
                        //PrintInfo(ReturnBuffer);
                        datagramSocket.send(new DatagramPacket(ReturnBuffer, ReturnBuffer.length,InetAddress.getByName(ClientIP), ClientPort));
                        //System.out.print("\nReturnBuffer2:");
                        //PrintInfo(ReturnBuffer);
                               
                        //datagramSocket.close();
                        } catch(IOException e){
                            System.out.println(e);
                        }
		}
               

                
	
		public static void main(String args[])
		{
			LocalDNSServer serverThread = new LocalDNSServer();
                        serverThread.start();
		}
}
