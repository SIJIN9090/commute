import React, { useState, useEffect } from "react";
import styled from "styled-components";

// 금액 포맷팅 함수 (쉼표 추가)
const formatAmount = (amount) => {
  if (!amount) return "";
  return amount
    .replace(/\D/g, "") // 숫자 외 문자를 제거
    .replace(/\B(?=(\d{3})+(?!\d))/g, ","); // 3자리마다 쉼표 추가
};

const ExpenseWrite = () => {
  const [expense, setExpense] = useState({
    title: "",
    content: "",
    category: "",
    photoUrl: "",
    amounts: [{ amount: "" }], // 금액 항목을 빈 값으로 초기화
    totalAmount: 0, // 전체 합계는 수정 불가하도록 설정
  });

  useEffect(() => {
    // 전체 금액 합산을 서버에서 가져옵니다.
    fetch("/api/expenses/total")
      .then((res) => res.json())
      .then((data) =>
        setExpense((prevExpense) => ({ ...prevExpense, totalAmount: data }))
      );
  }, []);

  // 금액 값 업데이트
  const handleChange = (e, index) => {
    const { value } = e.target;
    const formattedValue = formatAmount(value); // 포맷팅된 금액 값

    const newAmounts = [...expense.amounts];
    newAmounts[index] = { amount: formattedValue }; // 금액 항목을 업데이트
    setExpense({ ...expense, amounts: newAmounts });
  };

  // 카테고리 변경
  const handleCategoryChange = (category) => {
    setExpense({ ...expense, category });
  };

  // 금액 항목 추가
  const handleAddAmount = () => {
    setExpense({
      ...expense,
      amounts: [...expense.amounts, { amount: "" }], // 새 금액 항목을 빈 값으로 추가
    });
  };

  // 금액 항목 삭제
  const handleRemoveAmount = (index) => {
    const newAmounts = expense.amounts.filter((_, i) => i !== index);
    setExpense({ ...expense, amounts: newAmounts });
  };

  // 금액 합계 계산
  const calculateTotalAmount = () => {
    return expense.amounts.reduce(
      (total, item) => total + (Number(item.amount.replace(/,/g, "")) || 0), // 쉼표 제거 후 계산
      0
    );
  };

  // 폼 제출
  const handleSubmit = (e) => {
    e.preventDefault();

    const totalAmount = calculateTotalAmount();

    fetch("/api/expenses", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ ...expense, totalAmount }),
    })
      .then(() => {
        alert("비용이 추가되었습니다.");
        setExpense({
          title: "",
          content: "",
          category: "",
          photoUrl: "",
          amounts: [{ amount: "" }],
          totalAmount: 0,
        });
      })
      .catch((error) => console.error("Error:", error));
  };

  return (
    <FormContainer>
      <h2>비용 작성</h2>
      <Form onSubmit={handleSubmit}>
        <Label>제목</Label>
        <Input
          name="title"
          value={expense.title}
          onChange={(e) => setExpense({ ...expense, title: e.target.value })}
          required
        />

        <Label>카테고리</Label>
        <CategoryButtons>
          {["식비", "교통", "숙박", "경조사", "기타"].map((category) => (
            <CategoryButton
              key={category}
              selected={expense.category === category}
              onClick={() => handleCategoryChange(category)}
            >
              {category}
            </CategoryButton>
          ))}
        </CategoryButtons>

        <Label>금액</Label>
        {expense.amounts.map((amountItem, index) => (
          <AmountContainer key={index}>
            <Input
              name="amount"
              type="text" // 숫자가 아닌 텍스트로 처리
              value={amountItem.amount || ""} // 0이 아닌 빈 값으로 표시
              onChange={(e) => handleChange(e, index)}
              required
            />
            {expense.amounts.length > 1 && (
              <RemoveButton
                type="button"
                onClick={() => handleRemoveAmount(index)}
              >
                삭제
              </RemoveButton>
            )}
          </AmountContainer>
        ))}
        <AddButton type="button" onClick={handleAddAmount}>
          금액 추가
        </AddButton>

        <Label>전체 합계</Label>
        <Input
          name="totalAmount"
          type="number"
          value={calculateTotalAmount()} // 전체 합계 계산 후 값 설정
          disabled
        />

        <Label>사진 URL</Label>
        <Input
          name="photoUrl"
          value={expense.photoUrl}
          onChange={(e) => setExpense({ ...expense, photoUrl: e.target.value })}
        />

        <Label>내용</Label>
        <Input
          name="content"
          value={expense.content}
          onChange={(e) => setExpense({ ...expense, content: e.target.value })}
          required
        />

        <SubmitButton type="submit">작성</SubmitButton>
      </Form>
    </FormContainer>
  );
};

const FormContainer = styled.div`
  max-width: 400px;
  margin: 40px auto;
  padding: 20px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
  text-align: center;
`;

const Form = styled.form`
  display: flex;
  flex-direction: column;
`;

const Label = styled.label`
  margin-top: 10px;
  font-size: 14px;
  color: #333;
  text-align: left;
`;

const Input = styled.input`
  padding: 10px;
  margin-top: 5px;
  border: 1px solid #ccc;
  border-radius: 5px;
  font-size: 16px;
`;

const CategoryButtons = styled.div`
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-top: 10px;
`;

const CategoryButton = styled.button`
  padding: 10px;
  background: ${({ selected }) => (selected ? "#3498db" : "#ccc")};
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-size: 14px;

  &:hover {
    background: #2980b9;
  }
`;

const AmountContainer = styled.div`
  display: flex;
  gap: 10px;
  margin-top: 10px;
`;

const RemoveButton = styled.button`
  padding: 5px;
  background: #e74c3c;
  color: white;
  border: none;
  border-radius: 5px;
  font-size: 12px;
  cursor: pointer;

  &:hover {
    background: #c0392b;
  }
`;

const AddButton = styled.button`
  margin-top: 10px;
  padding: 10px;
  background: #2ecc71;
  color: white;
  border: none;
  border-radius: 5px;
  font-size: 14px;
  cursor: pointer;

  &:hover {
    background: #27ae60;
  }
`;

const SubmitButton = styled.button`
  margin-top: 20px;
  padding: 10px;
  background: #3498db;
  color: white;
  border: none;
  border-radius: 5px;
  font-size: 16px;
  cursor: pointer;
  transition: background 0.3s;

  &:hover {
    background: #2980b9;
  }
`;

export default ExpenseWrite;
