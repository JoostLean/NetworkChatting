package jdbctest.callable;

import java.sql.SQLException;
import java.sql.Types;

import jdbctest.crud.MyConnection;

public class FunctionCall extends MyConnection {

	//부모생성자 호출을 통해 study계정에 연결
	public FunctionCall() {
		super("study", "1234");
	}
	
	@Override
	public void dbExecute() {
		try {
			/*
			CallableStatement 인스턴스 생성을 위해 메서드를 호출한다.
			Oracle에서 작성한 함수를 호출하기 위해 call 예약어를 사용한다.
			*/
			csmt = con.prepareCall("{? = call fillAsterik(?)}");
			/*
			첫번째 파라미터는 함수 실행후 반환되는 값을 전달받기 위한
			부분으로 Types클래스의 정적상수를 통해 타입을 지정한다.
			*/
			csmt.registerOutParameter(1, Types.VARCHAR);
			/*
			두번째 파라미터는 사용자로부터 입력받은 아이디를 Oracle로
			전달하기 위해 설정한다. 
			*/
			csmt.setString(2, inputValue("아이디"));
			//여기서 함수를 실행
			csmt.execute();
			//실행 후 반환값은 첫번째 파라미터로 입력되므로 getXX(1)로 출력한다.
			System.out.println("함수의반환값:"+ csmt.getString(1));
		}
		catch(SQLException e) {
			e.printStackTrace();
		}
		finally {
			dbClose();
		}
	}

	public static void main(String[] args) {
		new FunctionCall().dbExecute();
	}

}
