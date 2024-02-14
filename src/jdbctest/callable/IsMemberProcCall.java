package jdbctest.callable;

import java.sql.SQLException;
import java.sql.Types;

import jdbctest.crud.MyConnection;

public class IsMemberProcCall extends MyConnection {
	
	public IsMemberProcCall() {
		super("study", "1234");
	}
	
	@Override
	public void dbExecute() {
		try {
			csmt = con.prepareCall("{ call MyMemberAuth(?,?,?) }");
			//인파라미터는 회원 아이디와 비번을 설정
			csmt.setString(1, inputValue("회원 아이디"));
			csmt.setString(2, inputValue("회원 패스워드"));
			//아웃파라미터는 인증 결과에 따라 0~2까지 반환
			csmt.registerOutParameter(3, Types.NUMERIC);
			csmt.execute();
			//인증 결과 반환
			int outParamResult = csmt.getInt(3);
			switch(outParamResult) {
			case 0:
				System.out.println("회원아이디가 없어요. 회원가입해주삼");
				break;
			case 1:
				System.out.println("패스워드가 일치하지 않아요. "
						+ "비번찾기하삼");
				break;
			case 2:
				System.out.println("회원님 방가방가^^*");
			}
		}
		catch(SQLException e) {
			e.printStackTrace();
		}
		finally {
			dbClose();
		}
	}

	public static void main(String[] args) {
		IsMemberProcCall mapc = new IsMemberProcCall();
		mapc.dbExecute();
	}
}
