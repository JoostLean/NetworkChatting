package chat7;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MultiServer {
	
	static ServerSocket serverSocket = null;
	static Socket socket = null;
	
	Map<String, PrintWriter> clientMap;
	
	public MultiServer() {
		clientMap = new HashMap<String, PrintWriter>();
		Collections.synchronizedMap(clientMap);
	}
	
	public void init() {
		try {
			serverSocket = new ServerSocket(9999);
			System.out.println("서버가 시작되었습니다.");
			
			while(true) {
				socket = serverSocket.accept();
				System.out.println(
						socket.getInetAddress()+"(클라이언트)의 "
						+socket.getPort()+" 포트를통해 "
						+socket.getLocalAddress()+" (서버)의"
						+socket.getLocalPort()+" 포트로 연결됨");
				Thread mst = new MultiServerT(socket);
				mst.start();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				serverSocket.close();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		MultiServer ms = new MultiServer();
		ms.init();
	}
	
	public void sendAllMsg(String name, String msg)
	{
		Iterator<String> it = clientMap.keySet().iterator();
		
		while(it.hasNext()) {
			try {
				PrintWriter it_out = (PrintWriter)
						clientMap.get(it.next());
				
				if(name.equals("")) {
					it_out.println(msg);
				}
				else {
					it_out.println("["+ name +"]:"+ msg);
				}
			}
			catch(Exception e) {
				System.out.println("예외:"+e);
			}
		}
	}

	//귓속말 전송 : 발신자대화명, 메세지, 수신자대화명
	public void sendAllMsg(String name, String msg,
			String receiveName) {
		Iterator<String> it = clientMap.keySet().iterator();
		
		while(it.hasNext()) {
			try {
				/* HashMap에는 Key로 대화명, Value로 PrintWriter
				인스턴스가 저장되어 있다. */
				String clientName = it.next();
				PrintWriter it_out =
					(PrintWriter) clientMap.get(clientName);
				
				/* 해당 루프에서의 클라이언트 이름과 귓속말을 받을 사람의
				대화명이 일치하는지 확인한다. */
				if(clientName.equals(receiveName)) {
					//일치하면 한 사람에게만 귓속말을 보낸다.
					it_out.println("[귓속말]"+ name +":"+ msg);
				}
			}
			catch(Exception e) {
				System.out.println("예외:"+e);
			}
		}
	}
	
	class MultiServerT extends Thread {
		Socket socket;
		PrintWriter out = null;
		BufferedReader in = null;
		
		public MultiServerT(Socket socket) {
			this.socket = socket;
			try {
				out = new PrintWriter(this.socket.getOutputStream(), true);
				in = new BufferedReader(
						new InputStreamReader(this.socket.getInputStream()));
			}
			catch (Exception e) {
				System.out.println("예외:"+ e);
			}
		}
		
		@Override
		public void run() {
			String name = "";
			String s = "";
			
			try {
				//첫번째 메세지는 대화명이므로 접속을 알린다.
				name = in.readLine();
				sendAllMsg("", name + "님이 입장하셨습니다.");
				clientMap.put(name, out);
				System.out.println(name + " 접속");
				System.out.println("현재 접속자 수는 "+clientMap.size()+"명 입니다.");
				
				//두번째 메세지부터는 "대화내용"
				while (in!=null) {
					s = in.readLine();
					if ( s == null )
						break;
					//서버의 콘솔에는 메세지를 그대로 출력한다.
					System.out.println(name + " >> " + s);
					
					/*
					귓속말형식 => /to 수신자명 대화내용 블라블라
					*/
					if(s.charAt(0)=='/') {
						//슬러쉬로 시작하면 명령어로 판단
						/* split() 으로 문자열을 분리한다. 여기서
						사용하는 구분자는 스페이스 이다. */
						String[] strArr = s.split(" ");
						/*
						문자열을 스페이스로 분리하면 0번 인덱스는 명령어,
						1번 인덱스는 수신자 대화명이 되고
						2번 인덱스부터 끝까지는 대화내용이 되므로 아래와 같이
						문자열 처리를 해야한다. 
						*/
						String msgContent = "";
						for(int i=2 ; i<strArr.length ; i++) {
							msgContent += strArr[i]+" ";
						}
						/* 명령어가 /to가 맞는지 확인한다. 명령어에 대한 오타가
						있을수도 있고, 다른 명령어 일수도 있기 때문이다. */
						if(strArr[0].equals("/to")) {
							//귓속말을 보낸다.
							/* 기존의 메서드를 오버로딩해서 추가 정의한다.
							매개변수는 발신대화명, 메세지, 수신대화명 형태로
							작성한다. */
							sendAllMsg(name, msgContent, strArr[1]);
						}
					}
					else {
						//슬러쉬가 없다면 일반 대화내용
						sendAllMsg(name, s);
					}
				}
			}
			catch (Exception e) {
				System.out.println("예외:"+ e);
			}
			finally {
				clientMap.remove(name);
				sendAllMsg("", name + "님이 퇴장하셨습니다.");
				System.out.println(name + " [" +
				Thread.currentThread().getName() + "] 퇴장");
				System.out.println("현재 접속자 수는"+
				+clientMap.size()+"명 입니다.");
				try {
					in.close();
					out.close();
					socket.close();
				}
				catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
