package multichat_01;

import java.sql.SQLException;

//CRUD 중 입력을 위한 클래스
public class SQLinsert extends MyConnection {
	String name;
	String msg;
	
	/*
	생성자에서 부모클래스의 생성자를 호출하여 DB에 연결한다.
	연결할 계정의 아이디와 비번을 매개변수로 전달하고 있다.
	*/
	public SQLinsert(String user, String pass, String name, String msg) {
		super(user, pass);
		this.name = name;
		this.msg = msg;
	}
	
	//멤버변수
	String query;//쿼리문 저장
	int result;//insert후 결과를 저장
	
	@Override
	public void dbExecute() {
		try {
			//정적쿼리문을 실행하기 위한 Statement 인스턴스를 생성
			stmt = con.createStatement();
			//insert를 위한 동적 쿼리문 작성
			query = "INSERT INTO chat_talking VALUES "
					+ " (chatting_seq.nextval, ?, ?, sysdate)";
			//동적쿼리 실행을 위한 preparedStatement 인스턴스 생성
			psmt = con.prepareStatement(query);
			/*
			동적쿼리문의 ?부분(인파라미터)을 사용자의 입력값으로 채워준다.
			DB에서는 인덱스가 1부터 시작이므로 ?의 갯수만큼 순서대로
			값을 설정하면 된다.
			*/
			psmt.setString(1, inputValue(name));
			psmt.setString(2, inputValue(msg));
			//쿼리문 실행 및 결과 반환
			result = psmt.executeUpdate();
			//insert 쿼리문이므로 성공시 1, 실패시 0이 반환된다.
			System.out.println("[psmt]"+ result +"행 입력됨");
		}
		catch (SQLException e) {
			System.out.println("쿼리실행 중 예외발생");
			e.printStackTrace();
		}
	}
	
//	public static void main(String[] args) {
//		new SQLinsert("study", "1234").dbExecute();
//	}
}
