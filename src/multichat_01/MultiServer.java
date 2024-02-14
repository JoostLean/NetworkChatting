package multichat_01;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class MultiServer {
	
	static ServerSocket serverSocket = null;
	static Socket socket = null;
	
	MultiClient multiClient = new MultiClient();
	
	Map<String, PrintWriter> clientMap;
	HashMap<String, String> whsUser = new HashMap<String, String>();
	HashMap<String, String> blkUser = new HashMap<String, String>();
	Set<String> blackList = new HashSet<>();
	Set<String> pWords = new HashSet<>();
	
	public MultiServer() {
		clientMap = new HashMap<String, PrintWriter>();
		Collections.synchronizedMap(clientMap);
		blackList.add("blocked");
		pWords.add("개새끼");
	}
	
	public void init() {
		try {
			serverSocket = new ServerSocket(9999);
			System.out.println("서버가 시작되었습니다.");
			
			while(true) {
				socket = serverSocket.accept();
				System.out.println(
						socket.getInetAddress()+"(클라이언트)의 "
						+socket.getPort()+" 포트를 통해 "
						+socket.getLocalAddress()+" (서버)의 "
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
		String userName;
		
		while(it.hasNext()) {
			try {
				//수신자의 이름
				userName = it.next();
				
				//수신자의 PrintWriter 객체
				PrintWriter it_out = (PrintWriter)clientMap.get(userName);
				
				/* HashMap에 저장된 대화차단 유저를 String에 저장 후
				해당되면 표시되지 않게 한다. 대화차단 유저가 없는 경우
				NullPointerException이 발생하기 때문에
				blockUsers가 null이 아닌 경우에만 if문이 실행될 수 있도록 해준다. */
				String blockUsers = blkUser.get(userName);
				if (blockUsers != null && blockUsers.contains(name)) {
					continue;
				}
				
				if(name.equals("")) {
					try {
						it_out.println(URLDecoder.decode(msg, "UTF-8"));
					}
					catch (UnsupportedEncodingException e1) {}
				}
				else {
					if (pWords.contains(URLDecoder.decode(msg, "UTF-8"))) {
						it_out.println("입력할 수 없는 단어입니다:"+ URLDecoder.decode(msg, "UTF-8"));
					}
					else {
						it_out.println("["+ URLDecoder.decode(name, "UTF-8") +"]:" +
								URLDecoder.decode(msg, "UTF-8"));
					}
				}
			}
			catch(Exception e) {
				System.out.println("예외:"+e);
			}
		}
	}

	//귓속말 전송 : 발신자대화명, 메세지, 수신자대화명
	public void sendWhisperMsg(String name, String msg,
			String receiveName) {
		Iterator<String> it = clientMap.keySet().iterator();
		String userName;
		
		while(it.hasNext()) {
			try {
				//수신자의 이름
				userName = it.next();
				
				/* HashMap에는 Key로 대화명, Value로 PrintWriter
				인스턴스가 저장되어 있다. */
				String clientName = it.next();
				PrintWriter it_out =
					(PrintWriter) clientMap.get(clientName);
				
				/* HashMap에 저장된 대화차단 유저를 String에 저장 후
				해당되면 표시되지 않게 한다. 대화차단 유저가 없는 경우
				NullPointerException이 발생하기 때문에
				blockUsers가 null이 아닌 경우에만 if문이 실행될 수 있도록 해준다. */
				String blockUsers = blkUser.get(userName);
				if (blockUsers != null && blockUsers.contains(name)) {
					continue;
				}
				
				/* 해당 루프에서의 클라이언트 이름과 귓속말을 받을 사람의
				대화명이 일치하는지 확인한다. */
				if(URLDecoder.decode(clientName, "UTF-8").equals(URLDecoder.decode(receiveName, "UTF-8"))) {
					//일치하면 한 사람에게만 귓속말을 보낸다.
					try {
						it_out.println("[귓속말]"+ URLDecoder.decode(name, "UTF-8") +":"
								+ URLDecoder.decode(msg, "UTF-8"));
					}
					catch (UnsupportedEncodingException e1) {}
				}
			}
			catch(Exception e) {
				System.out.println("예외:"+e);
			}
		}
	}
	
	class MultiServerT extends Thread {
		private static final int MAX_CAPACITY = 50;
		Socket socket;
		PrintWriter out = null;
		BufferedReader in = null;
		
		String url = "jdbc:oracle:thin:@localhost:1521:xe";
		String id = "study";
		String pass = "1234";
		
		//블럭처리 : 블럭요청자, 차단할사용자, 추가or삭제
		public void fluctBlock(String user, String blockUser, char sign) {
			if(sign=='+') {
				if(blkUser==null) {
					//비어있다면 그냥 삽입
					blkUser.put(user, blockUser+"|");
				}
				else {
					if(blkUser.containsKey(user)) {
						//차단 내역이 있는경우 : 추가함
						blkUser.put(user, blkUser.get(user)+blockUser+"|");
					}
					else {
						//차단 내역이 없는경우 : 그냥삽입
						blkUser.put(user, blockUser+"|");
					}
				}
			}
			else if(sign=='-') {
				if(blkUser!=null && blkUser.containsKey(user)) {
					String newblockUser = blkUser.get(user).replace(blockUser+"|", "");
					blkUser.put(user, newblockUser);
				}
			}
		}
		
		public MultiServerT(Socket socket) {
			this.socket = socket;
			try {
				out = new PrintWriter(new OutputStreamWriter(
						this.socket.getOutputStream(), StandardCharsets.UTF_8), true);
				in = new BufferedReader(new InputStreamReader(
						this.socket.getInputStream(), "UTF-8"));
			}
			catch (Exception e) {
				System.out.println("예외:"+ e);
			}
		}
		
		private Connection con;
		private String query;
		private int result;
		
		@Override
		public void run() {
			String name = "";
			String s = "";
			
			Iterator<String> blackit = blackList.iterator();
			
			try {
				Class.forName("oracle.jdbc.OracleDriver");
				//오라클 드라이버 지정
				con = DriverManager.getConnection(url, id, pass);
				
				//첫번째 메세지는 대화명이므로 접속을 알린다.
				name = in.readLine();
				while(true) {
					if(clientMap.containsKey(name)) {
						out.println("동일한 이름이 존재합니다.");
//						out.print("이름을 입력하세요:");
						name = in.readLine();
//						if(name==null) {
//							break;
//						}
					}
					else if(clientMap.size()>=MAX_CAPACITY) {
						out.println(clientMap.size()+"명을 초과하여 입장하실 수 없습니다.");
						out.println("[접속오류]실행 가능한 메뉴가 없습니다.");
						out.println("'Q' 또는 'q'를 입력하면 종료됩니다.");
						System.exit(0);
					}
					else if(blackList.contains(name)) {
						out.println("귀하는 차단되었습니다.");
						out.println("[접속오류]실행 가능한 메뉴가 없습니다.");
						out.println("'Q' 또는 'q'를 입력하면 종료됩니다.");
						System.exit(0);
//						if (!socket.isClosed()==false) {
//							socket.close();
//							break;
//						} else {
//							break;
//						}
					}
					else {
						break;
					}
				}
				clientMap.put(name, out);
				out.println("서버와 연결되었습니다...");
				System.out.println(URLDecoder.decode(name, "UTF-8") + " 접속");
				System.out.println("현재 접속자 수는 "+clientMap.size()+"명 입니다.");
				
				//두번째 메세지부터는 "대화내용"
				while (in!=null) {
					s = in.readLine();
					if ( s == null )
						break;
					//서버의 콘솔에는 메세지를 그대로 출력한다.
					if (pWords.contains(URLDecoder.decode(s, "UTF-8"))) {
						System.out.println("[금칙어사용]"+ URLDecoder.decode(name, "UTF-8") + " >> " + URLDecoder.decode(s, "UTF-8"));
					}
					else {
						System.out.println(URLDecoder.decode(name, "UTF-8") + " >> " + URLDecoder.decode(s, "UTF-8"));
					}
					PreparedStatement psmt = null;
					try {
						query = "INSERT INTO chat_talking "
								+ "(idx, client_name, client_msg, send_date) VALUES "
								+ "(chatting_seq.nextval, ?, ?, sysdate)";
						//동적쿼리 실행을 위한 preparedStatement 인스턴스 생성
						psmt = con.prepareStatement(query);
						/*
							동적쿼리문의 ?부분(인파라미터)을 사용자의 입력값으로 채워준다.
							DB에서는 인덱스가 1부터 시작이므로 ?의 갯수만큼 순서대로
							값을 설정하면 된다.
						*/
						psmt.setString(1, URLDecoder.decode(name, "UTF-8"));
						psmt.setString(2, URLDecoder.decode(s, "UTF-8"));
						//쿼리문 실행 및 결과 반환
						result = psmt.executeUpdate();
						//insert 쿼리문이므로 성공시 1, 실패시 0이 반환된다.
						//System.out.println("[psmt]"+ result +"행 입력됨");
					}
					finally {
						if (psmt != null) {
							psmt.close();
						}
					}
					
					if(URLDecoder.decode(s, "UTF-8").equals("/unfixto") ||
						( URLDecoder.decode(s, "UTF-8").contains("/unfixto") &&
						URLDecoder.decode(s, "UTF-8").charAt(0)=='/' )
					) 
					{
						whsUser.remove(name);
						out.println(whsUser.get(name) + " 님에게 귓속말 고정 설정이 해제되었습니다.");
					}
					else {
						if(whsUser!=null && whsUser.containsKey(name)) {
							s = "/to "+ whsUser.get(name) +" "+ s;
							System.out.println("[귓속말고정]"
									+ "(" + name + ">>" + whsUser.get(name) + ")" + " : " 
									+ URLDecoder.decode(s, "UTF-8"));
						}
					}
					
					/*
					귓속말형식 => /to 수신자명 대화내용 블라블라
					*/
					if(URLDecoder.decode(s, "UTF-8").charAt(0)=='/') {
						if(URLDecoder.decode(s, "UTF-8").equals("/list")) {
							//리스트 명령은 명령을 실행한 유저에게만 Echo하면 된다.
							StringBuffer sb = new StringBuffer();
							sb.append(URLEncoder.encode("[접속자리스트]\n", "UTF-8"));
							//Map의 키값이 접속자 이름
							Iterator<String> it = clientMap.keySet().iterator();
							while(it.hasNext()) {
								sb.append(it.next()+"\n");
							}
							sb.append("-----------------");
							
							System.out.println("["+URLDecoder.decode(name, "UTF-8")+"]님이 리스트를 출력하셨습니다.");
							out.println(URLDecoder.decode(sb.toString(), "UTF-8"));
						}
						//슬러쉬로 시작하면 명령어로 판단
						/* split() 으로 문자열을 분리한다. 여기서
						사용하는 구분자는 스페이스 이다. */
						String[] strArr = URLDecoder.decode(s, "UTF-8").split(" ");
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
							sendWhisperMsg(name, msgContent, strArr[1]);
						}
						else if(URLDecoder.decode(s, "UTF-8").equals("/fixto")
								|| ( URLDecoder.decode(s, "UTF-8").contains("/fixto")
								&& URLDecoder.decode(s, "UTF-8").charAt(0)=='/') ) {
							whsUser.put(name, strArr[1]);							
							out.println(strArr[1] + " 님에게 귓속말 고정 설정이 완료되었습니다.");
						}
						else if(URLDecoder.decode(s, "UTF-8").equals("/block")
								|| ( URLDecoder.decode(s, "UTF-8").contains("/block")
								&& URLDecoder.decode(s, "UTF-8").charAt(0)=='/') ) {
							fluctBlock(name, strArr[1], '+');
//							Set<String> blockUser = blkUser.getOrDefault(name, new HashSet<>());
//							blockUser.add(strArr[1]);
//							blkUser.put(name, blockUser);
							out.println(strArr[1] + " 님을 차단하였습니다.");
							out.println(strArr[1] + " 님의 메시지가 수신되지 않습니다.");
						}
						else if(URLDecoder.decode(s, "UTF-8").equals("/unblock")
								|| ( URLDecoder.decode(s, "UTF-8").contains("/unblock")
								&& URLDecoder.decode(s, "UTF-8").charAt(0)=='/') ) {
							fluctBlock(name, strArr[1], '-');
//							Set<String> blockUser = blkUser.get(name);
//							blockUser.remove(strArr[1]);
							out.println(strArr[1] + " 님을 차단해제하였습니다.");
							out.println("앞으로 " + strArr[1] + " 님의 메시지가 수신됩니다.");
						}
					}
					else {
//						//슬러쉬가 없다면 일반 대화내용
						sendAllMsg(name, s);
					}
				}				

			}
			catch (Exception e) {
				System.out.println("예외:"+ e);
			}
			finally {
				clientMap.remove(name);
				try {
					sendAllMsg("", URLDecoder.decode(name, "UTF-8") + "님이 퇴장하셨습니다.");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				try {
					System.out.println(URLDecoder.decode(name, "UTF-8") + " [" +
					Thread.currentThread().getName() + "] 퇴장");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				System.out.println("현재 접속자 수는 "+
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
