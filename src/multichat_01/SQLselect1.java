package multichat_01;

import java.sql.SQLException;

public class SQLselect1 extends MyConnection {
	
	public SQLselect1(String user, String pass) {
		super(user, pass);
	}
	
	String query;
	int result;
	
	@Override
	public void dbExecute() {
		try {
			//Statement 인스턴스 생성
			stmt = con.createStatement();
			//select 쿼리문 작성
			/* to_char() 변환함수를 통해 날짜를 아래 서식에 맞춰서 출력
			한 후 별칭을 부여했다. 차후 별칭을 통해 값을 읽을 수 있다. */
			String query = "SELECT id, pass, name, regidate,"
			  + " to_char(regidate, 'yyyy.mm.dd hh24:mi') d1 "
			  + " FROM member";
			/*
			ResultSet 인터페이스
			: select문 실행시 실행결과를 ResultSet 인스턴스를
			통해 반환받는다. 결과로 반환된 레코드의 처음부터 끝까지 next()를
			통해 확인 후 반복해서 출력한다.
			insert, update, delete와 같은 DML문은 executeUpdate()를
			통해 실행한 후 결과로는 int를 반환받는다.
			하지만 select문은 executeQuery()로 실행해야한다.
			*/
			rs = stmt.executeQuery(query);
			while(rs.next()) {
				//컬럼에 접근시 인덱스와 컬럼명을 모두 사용할 수 있다.
				String id = rs.getString(1);
				String pw = rs.getString("pass");
				String name = rs.getString("name");
				
				/*
				컬럼의 자료형이
					number타입 : getInt()
					char/varchar2타입 : getString()
					date타입 : getDate() 메서드를 통해 출력한다.
				메서드 사용시 인덱스(1부터시작) 혹은 컬럼명(or 별칭)을 사용할 수 있다.
				또한 자료형에 상관없이 getString()으로 모두 가져올 수 있다.
				*/
				/*
				날짜를 getDate()로 인출하면 0000-00-00형태로 출력된다.
				또한 자료형이 date타입이므로 java.sql패키지의 Date클래스를
				사용해야한다.
				*/
				java.sql.Date regidate = rs.getDate("regidate");
				/*
				날짜를 getString()으로 인출하면
				0000-00-00 00:00:00 형식으로 시간까지 포함되어 출력된다.
				따라서 적당한 크기로 자를때는 substring()을 사용한다.
				*/
				String regidate2 = rs.getString("regidate");
				String regidate3 = rs.getString("regidate").substring(0,13);
				/*
				오라클의 변환함수를 통해 출력할때는 원하는 형태로 포매팅한 후
				문자형식으로 출력한다. 별칭을 사용한 경우에는 그대로 사용할 수 있다.
				즉 컬럼명, 인덱스, 별칭을 통해 컬럼을 지정할 수 있다.
				*/
				String regidate4 = rs.getString("d1");
				
				System.out.printf("%s %s %s %s %s %s %s\n",
						id, pw, name, regidate, regidate2, regidate3, regidate4);
			}
		}
		catch (SQLException e) {
			System.out.println("쿼리실행 중 예외발생");
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new SQLselect1("study", "1234").dbExecute();
	}
}
