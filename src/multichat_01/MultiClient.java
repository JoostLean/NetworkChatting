package multichat_01;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.Iterator;
import java.util.Scanner;

public class MultiClient {

	public static void main(String[] args) {
		MultiServer multiServer = new MultiServer();
		Iterator<String> userInfo = multiServer.clientMap.keySet().iterator();
		System.out.print("이름을 입력하세요:");
		Scanner scanner = new Scanner(System.in);
		String s_name = scanner.nextLine();
//		while(true) {
//			if(multiServer.clientMap.containsKey(s_name)) {
//				System.out.println("동일한 이름이 존재합니다.");
//				s_name = scanner.nextLine();
//			}
//			else {
//				break;
//			}
//		}
		
		/*
		메세지 송수신을 위한 클래스로 별도로 만들었으므로 해당
		멤버변수는 필요없음
		*/
		//PrintWriter out = null;
		//BufferedReader in = null;
		
		System.setProperty("file.encoding", "UTF-8");
		
		try {
			//서버에 접속 요청
			String ServerIP = "localhost";
			if(args.length > 0) {
				ServerIP = args[0];
			}
			Socket socket = new Socket(ServerIP, 9999);
//			System.out.println("서버와 연결되었습니다...");
			
			/*
			서버가 Echo해준 메세지를 지속적으로 받기위한 리시버 쓰레드
			인스턴스 생성 및 시작 
			*/
			Thread receiver = new Receiver(socket);
			receiver.start();
			
			/*
			서버로 메세지를 전송할 센더 쓰레드 인스턴스 생성 및 시작 
			*/
			Thread sender = new Sender(socket, s_name);
			sender.start();
		}
		catch (Exception e) {
			System.out.println("예외발생[MultiClient]"+ e);
		}
	}
}
