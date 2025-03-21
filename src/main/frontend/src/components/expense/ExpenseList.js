import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import styled from "styled-components";

const ExpenseList = () => {
  const [expenses, setExpenses] = useState([]);
  const [token, setToken] = useState(null);
  const [username, setUsername] = useState("");
  const navigate = useNavigate();

  // token 관리 useEffect
  useEffect(() => {
    const storedToken = localStorage.getItem("access_token");
    if (storedToken) {
      setToken(storedToken);
    } else {
      navigate("/login");
    }
  }, [navigate]);

  // 경비 목록과 유저 정보 fetch
  useEffect(() => {
    if (!token) return; // 토큰이 없으면 API 요청을 하지 않음

    const fetchData = async () => {
      try {
        // 경비 목록 불러오기
        const expenseResponse = await fetch("/api/expenses", {
          method: "GET",
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        });

        if (expenseResponse.ok) {
          const expenseData = await expenseResponse.json();
          if (Array.isArray(expenseData)) {
            setExpenses(expenseData);
          } else {
            console.error("Invalid expenses data format", expenseData);
          }
        } else {
          console.error("Error fetching expenses:", expenseResponse.status);
        }

        // 유저 정보 불러오기
        const userResponse = await fetch("/api/user", {
          method: "GET",
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        });

        if (userResponse.ok) {
          const userData = await userResponse.json();
          setUsername(userData.username);
        } else {
          console.error("Error fetching username");
        }
      } catch (error) {
        console.error("Error fetching data:", error);
      }
    };

    fetchData();
  }, [token]); // token이 변경될 때마다 fetchData 함수 호출

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
              <ExpenseAuthor>작성자: {username}</ExpenseAuthor>
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
