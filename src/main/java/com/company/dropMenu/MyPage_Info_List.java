package com.company.dropMenu;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.company.Vo.BoardVo;
import com.company.common.JDBCconn;

@WebServlet("/MyPage_Info_List")
public class MyPage_Info_List extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		request.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");
		
			int page; 
		if(request.getParameter("page")== null)
			page =1;
		else
			page= Integer.parseInt(request.getParameter("page"));
		
		HttpSession session=request.getSession();
		 PrintWriter out = response.getWriter();

		String name=(String)session.getAttribute("name");
		if(name==null) {
			out.print("<script> alert('마이페이지를 이용하시려면 로그인 해주세요'); location.href='index.jsp?filePath=./login_check/Login_main'  </script>");
			out.close();
			return;
		}
		
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			conn = JDBCconn.getConnection();
			String sql = "select * from (select rownum rnum,A.* from "
					+ "(select * from HomeBoard where order by seq desc) A) where rnum between ? and ?";
			pstmt = conn.prepareStatement(sql);
			System.out.println(sql);
			
			//각 페이지에 담기는 rownum값을 정의해준다. 
			pstmt.setInt(1, page*5-4);
			pstmt.setInt(2, page*5);
			
			rs = pstmt.executeQuery();
			ArrayList<BoardVo> list = new ArrayList<BoardVo>();
			while (rs.next()) {
				BoardVo vo = new BoardVo();
				vo.setSeq(rs.getInt("seq"));
				vo.setNickname(rs.getString("nickname"));
				vo.setContent(rs.getString("content"));
				vo.setTitle(rs.getString("title"));
				vo.setRegdate(rs.getDate("regdate"));
				vo.setCnt(rs.getInt("cnt"));
				
				list.add(vo);
			}
			pstmt.close();
			rs.close();
			//재활용하기 위해 사용한자원을 잠시 닫아준다. totalCount 값은 따로 정해야 다시 정의해야 됨
			
			sql="select max(seq) from HomeBoard";
			pstmt=conn.prepareStatement(sql);
			rs=pstmt.executeQuery();
			
			int totalCount=0;
			if(rs.next()) {
				totalCount = rs.getInt(1);
			}
			System.out.println(totalCount);
			
			request.setAttribute("list", list);
			request.setAttribute("totalRows", totalCount);
			
			RequestDispatcher dis = request.getRequestDispatcher("index.jsp?filePath=./boardJSP/GetboardList");
			dis.forward(request, response);

			
			
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		} finally {
			JDBCconn.close(rs, pstmt, conn);
		}
	}
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
}
