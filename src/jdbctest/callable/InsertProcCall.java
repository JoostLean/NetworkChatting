package jdbctest.callable;

import java.sql.SQLException;
import java.sql.Types;

import jdbctest.crud.MyConnection;

public class InsertProcCall extends MyConnection {

	public InsertProcCall() {
		super("study", "1234");
	}
	
	/*
	Java에서 프로시저 호출하기
	-프로시저 호출을 위한 CallableStatement 인스턴스 생성
	-호출시에는 { call 프로시저명(?,? ... N) }
	-return값이 없으므로 함수처럼 ?= 로 시작하지 않는다.
	-파라미터는 in, out 순서없이 작성할 수 있다. 즉 프로시저 정의에
		따라 달라진다.
	*/
	@Override
	public void dbExecute() {
		try {
			csmt = con.prepareCall("{call MyMemberInsert(?,?,?,?)}");
			//인파라미터 설정. 사용자로부터 입력받은 값을 세팅한다.
			csmt.setString(1, inputValue("아이디"));
			csmt.setString(2, inputValue("패스워드"));
			csmt.setString(3, inputValue("이름"));
			//아웃파라미터설정. 반환값에 대한 자료형만 설정하면 된다.
			csmt.registerOutParameter(4, Types.NUMERIC);
			csmt.execute();
			//out 파라미터에 할당된 결과값을 읽어 출력한다.
			int affected = csmt.getInt(4);
			if(affected==0)
				System.out.println("오류발생:입력실패");
			else
				System.out.println(affected +"행 입력성공");
		}
		catch(SQLException e) {
			e.printStackTrace();
		}
		finally {
			dbClose();
		}
	}

	public static void main(String[] args) {
		new InsertProcCall().dbExecute();
	}

}
