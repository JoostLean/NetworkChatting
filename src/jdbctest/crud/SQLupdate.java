package jdbctest.crud;

import java.sql.SQLException;

public class SQLupdate extends MyConnection {

	public SQLupdate(String user, String pass) {
		super(user, pass);
	}
	
	String query;
	int result;
	
	@Override
	public void dbExecute() {
		try {
			//Statement 인터페이스 인스턴스 생성
			stmt = con.createStatement();
			//update 쿼리문 작성
			query = "UPDATE member "
					+ " SET "
					+ "   pass='0412',"
					+ "   name='성유겸',"
					+ "   regidate=sysdate "
					+ " WHERE id='test3' ";
			//쿼리문 실행 및 결과반환
			result = stmt.executeUpdate(query);
			//업데이트 된 행의 갯수가 반환됨
			System.out.println("[stmt]"+ result
										+"행 업데이트 됨");
			
			///////////////////////////////////////////////////////////
			//인파라미터가 있는 동적쿼리문 작성
			query = "UPDATE member "
					+ " SET name=?, pass=? "
					+ " WHERE id=?";
			//prepared 인스턴스 생성
			psmt = con.prepareStatement(query);
			//exit를 입력할때까지 반복할 수 있는 구조로 작성
			while(true) {
				/* 인파라미터의 순서대로 입력한 값을 세팅한다. 작성시
				아래와 같이 순서는 마음대로 기술할 수 있다. */
				psmt.setString(3, inputValue("수정할 아이디"));				
				psmt.setString(1, inputValue("이름"));
				psmt.setString(2, inputValue("패스워드"));
				//쿼리문 실행 및 결과반환
				result = psmt.executeUpdate();
				System.out.println("[psmt]"+ result
										+"행 업데이트됨");
			}
		}
		catch (SQLException e) {
			System.out.println("쿼리실행 중 예외발생");
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		new SQLupdate("study", "1234").dbExecute();
	}
}
