import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import styled from "styled-components";

const ExpenseList = () => {
  const [expenses, setExpenses] = useState([]); // 기본값은 빈 배열
  const [token, setToken] = useState(null);
  const [username, setUsername] = useState(""); // 유저 이름 상태 추가
  const navigate = useNavigate();

  useEffect(() => {
    const storedToken = localStorage.getItem("access_token");
    if (storedToken) {
      setToken(storedToken); // 토큰을 state에 설정
    } else {
      // 토큰이 없으면 로그인 페이지로 리디렉션
      navigate("/login");
    }
  }, [navigate]);

  useEffect(() => {
    const fetchExpenses = async () => {
      if (!token) {
        console.error("No token found.");
        return;
      }

      try {
        const response = await fetch("/api/expenses", {
          method: "GET",
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        });

        if (response.status === 403) {
          alert("접근 권한이 없습니다.");
          return;
        }

        if (!response.ok) {
          throw new Error(`Error fetching expenses: ${response.status}`);
        }

        const data = await response.json();

        if (Array.isArray(data)) {
          setExpenses(data); // 경비 항목들 설정
        } else {
          console.error("Received data is not in expected format:", data);
        }
      } catch (error) {
        console.error("Error fetching expenses:", error);
      }
    };

    fetchExpenses();
  }, [token]); // token 변경될 때마다 fetchExpenses 실행

  useEffect(() => {
    const storedToken = localStorage.getItem("access_token");
    if (storedToken) {
      setToken(storedToken);
    }
  }, []);

  useEffect(() => {
    const fetchUsername = async () => {
      if (!token) return;

      try {
        const response = await fetch("/api/user", {
          method: "GET",
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        });

        if (response.ok) {
          const userData = await response.json();
          setUsername(userData.username); // 유저 이름 설정
        } else {
          console.error("Error fetching username");
        }
      } catch (error) {
        console.error("Error fetching username:", error);
      }
    };

    fetchUsername();
  }, [token]);

  return (
    <ExpenseContainer>
      <h2>경비 관리</h2>
      <ExpenseListWrapper>
        {expenses.length > 0 ? (
          expenses.map((expense) => (
            <ExpenseItem key={expense.id}>
              <ExpenseTitle>{expense.title}</ExpenseTitle>
              <ExpenseContent>{expense.content}</ExpenseContent>
              <ExpenseAmount>금액: {expense.amount} 원</ExpenseAmount>
              <ExpenseAuthor>작성자: {username}</ExpenseAuthor>{" "}
              {/* 작성자 추가 */}
            </ExpenseItem>
          ))
        ) : (
          <p>등록된 게시글이 없습니다.</p>
        )}
      </ExpenseListWrapper>
      <AddButton onClick={() => navigate("/create")}>+</AddButton>
    </ExpenseContainer>
  );
};

const ExpenseContainer = styled.div`
  max-width: 600px;
  margin: 40px auto;
  padding: 20px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
  text-align: center;
`;

const ExpenseItem = styled.li`
  list-style: none;
  background: #f9f9f9;
  margin: 10px 0;
  padding: 15px;
  border-radius: 8px;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
  text-align: left;
`;

const ExpenseTitle = styled.h3`
  margin: 0;
  color: #333;
`;

const ExpenseContent = styled.p`
  margin: 5px 0;
  color: #666;
  font-size: 14px;
`;

const ExpenseAmount = styled.p`
  font-weight: bold;
  color: #e74c3c;
  font-size: 16px;
`;

const ExpenseAuthor = styled.p`
  margin-top: 5px;
  color: #34495e;
  font-size: 14px;
  font-style: italic;
`;

const ExpenseListWrapper = styled.ul`
  padding: 0;
`;

const AddButton = styled.button`
  margin-top: 20px;
  padding: 10px 15px;
  background: #3498db;
  color: white;
  border: none;
  border-radius: 100%;
  font-size: 16px;
  cursor: pointer;
  transition: background 0.3s;

  &:hover {
    background: #2980b9;
  }
`;

export default ExpenseList;
