import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import styled from "styled-components";

const ExpenseList = () => {
  const [expenses, setExpenses] = useState([]); // 기본값은 빈 배열로 설정
  const [totalAmount, setTotalAmount] = useState(0);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchExpenses = async () => {
      const token = localStorage.getItem("token"); // 로컬 스토리지에서 JWT 토큰 가져오기

      if (!token) {
        console.error("No token found. Redirecting to login.");
        navigate("/login");
        return;
      }

      try {
        const response = await fetch("/api/expenses", {
          method: "GET",
          headers: {
            Authorization: `Bearer ${token}`, // Authorization 헤더에 토큰 추가
            "Content-Type": "application/json", // JSON 응답을 받기 위한 설정
          },
        });

        if (response.status === 403) {
          console.error("403 Forbidden: You do not have permission.");
          alert("접근 권한이 없습니다. 로그인 후 다시 시도해주세요.");
          navigate("/login");
          return;
        }

        if (!response.ok) {
          throw new Error(`Error fetching expenses: ${response.status}`);
        }

        const data = await response.json();

        if (Array.isArray(data)) {
          setExpenses(data); // 배열이면 바로 설정
          setTotalAmount(
            data.reduce(
              (total, expense) => total + (expense.amount || 0), // `amount`가 숫자라고 가정
              0
            )
          );
        } else {
          console.error("Received data is not in expected format:", data);
        }
      } catch (error) {
        console.error("Error fetching expenses:", error);
      }
    };

    fetchExpenses();
  }, [navigate]); // `navigate`가 변경될 때만 실행

  return (
    <ExpenseContainer>
      <h2>경비 관리</h2>
      <ExpenseListWrapper>
        {expenses && expenses.length > 0 ? (
          expenses.map((expense) => (
            <ExpenseItem key={expense.id}>
              <ExpenseTitle>{expense.title}</ExpenseTitle>
              <ExpenseContent>{expense.content}</ExpenseContent>
              <ExpenseAmount>금액: {expense.amount} 원</ExpenseAmount>
            </ExpenseItem>
          ))
        ) : (
          <p>등록된 경비 항목이 없습니다.</p>
        )}
      </ExpenseListWrapper>
      <TotalAmount>전체 합계: {totalAmount} 원</TotalAmount>
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

const TotalAmount = styled.h3`
  margin-top: 20px;
  font-size: 20px;
  color: #2c3e50;
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
