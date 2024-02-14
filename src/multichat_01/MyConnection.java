package multichat_01;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Scanner;

public class MyConnection implements IConnect {

	/*
	멤버변수 : JDBC작업을 위해 필요한 여러가지 인스턴스를 정의
		DB연결, select결과의 반환, 정적쿼리문 실행, 동적쿼리문 실행,
		프로시져 실행의 기능을 수행한다.
	*/
	public Connection con;
	public ResultSet rs;
	public Statement stmt;
	public PreparedStatement psmt;
	public CallableStatement csmt;
	
	//생성자 : 사용자계정과 패스워드를 전달받아 DB에 연결한다.
	public MyConnection(String user, String pass) {
		try {
			//드라이버 로드
			Class.forName(ORACLE_DRIVER);
			//오라클 연결(접속)
			con = DriverManager.getConnection(ORACLE_URL,
					user, pass);
		}
		catch(Exception e) {
			System.out.println("DB 커넥션 예외발생");
			e.printStackTrace();
		}
	}
	
	/*
	CRUD에 대한 작업은 하위 클래스에서 구현할 예정이므로 구현부가
	없는 상태로 오버라이딩만 해놓는다.
	*/
	@Override
	public void dbExecute() {}
	
	//DB자원 반납을 위한 메서드
	@Override
	public void dbClose() {
		try {
			if(con!=null) con.close();
			if(rs!=null) rs.close();
			if(stmt!=null) stmt.close();
			if(psmt!=null) psmt.close();
			if(csmt!=null) csmt.close();
			System.out.println("DB 자원 반납");
		}
		catch(Exception e) {
			System.out.println("DB 자원 반납시 예외발생");
			e.printStackTrace();
		}
	}

	//사용자로부터 입력값을 받기 위한 메서드
	@Override
	public String inputValue(String title) {
		Scanner scan = new Scanner(System.in);
		System.out.print(title + "을(를) 입력(exit->종료):");
		String inputStr = scan.nextLine();
		/* 입력값으로 대소문자 구분없이 exit라고 입력하면 즉시
		자원을 반납하고 프로그램을 종료한다. */
		if("EXIT".equalsIgnoreCase(inputStr)) {
			System.out.println("프로그램을 종료합니다.");
			dbClose();
			System.exit(0);
		}
		//종료가 아니라면 입력값을 호출한 지점으로 반환한다. 
		return inputStr;
	}
}
