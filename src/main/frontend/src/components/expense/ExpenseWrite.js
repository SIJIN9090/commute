import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import styled from "styled-components";
import BackPage from "../BackPage";

// 금액 포맷팅 함수 (쉼표 추가)
const formatAmount = (amount) => {
  if (!amount) return "";
  return amount.replace(/\D/g, "").replace(/\B(?=(\d{3})+(?!\d))/g, ",");
};

const ExpenseWrite = () => {
  const navigate = useNavigate();
  const [previewImages, setPreviewImages] = useState([]);
  const [files, setFiles] = useState([]);
  const [expense, setExpense] = useState({
    title: "",
    content: "",
    category: "",
    date: "",
    photoUrls: [],
    amounts: [{ amount: "" }],
    totalAmount: 0,
  });

  // 금액 값 업데이트
  const handleChange = (e, index) => {
    const { value } = e.target;
    const formattedValue = formatAmount(value);
    const newAmounts = [...expense.amounts];
    newAmounts[index] = { amount: formattedValue };
    setExpense({ ...expense, amounts: newAmounts });
  };

  // 카테고리 변경
  const handleCategoryChange = (category, e) => {
    e.preventDefault();
    setExpense({ ...expense, category });
  };

  // 금액 항목 추가
  const handleAddAmount = () => {
    setExpense({
      ...expense,
      amounts: [...expense.amounts, { amount: "" }],
    });
  };

  // 금액 항목 삭제
  const handleRemoveAmount = (index) => {
    const newAmounts = expense.amounts.filter((_, i) => i !== index);
    setExpense({ ...expense, amounts: newAmounts });
  };

  // 금액 합계 계산
  const calculateTotalAmount = () => {
    const total = expense.amounts.reduce(
      (total, item) => total + (Number(item.amount.replace(/,/g, "")) || 0),
      0
    );
    return total > 0 ? total : 0;
  };

  // 날짜 변경
  const handleChangeDate = (e) => {
    setExpense({ ...expense, date: e.target.value });
  };

  // 파일 선택 핸들러
  const handleFileChange = (e) => {
    const selectedFiles = Array.from(e.target.files);
    const imageUrls = selectedFiles.map((file) => URL.createObjectURL(file));

    // 미리보기 이미지 추가
    setPreviewImages((prevImages) => [...prevImages, ...imageUrls]);

    // 파일 배열 업데이트
    setFiles((prevFiles) => [...prevFiles, ...selectedFiles]);

    // expense 상태 업데이트
    setExpense((prevExpense) => ({
      ...prevExpense,
      photoUrls: [...prevExpense.photoUrls, ...selectedFiles],
    }));
  };

  // 폼 제출
  const handleSubmit = async (event) => {
    event.preventDefault();

    // FormData 생성
    const formData = new FormData();

    // 금액 합계 계산
    const totalAmount = calculateTotalAmount();

    // expenseDto 객체를 formData에 추가
    formData.append(
      "expenseDto",
      JSON.stringify({
        title: expense.title,
        content: expense.content,
        category: expense.category,
        date: expense.date,
        amounts: expense.amounts.map((item) => ({
          amount: item.amount.replace(/,/g, ""), // 금액에서 쉼표 제거
        })),
        totalAmount,
      })
    );

    // 파일이 있으면 formData에 추가
    if (files.length > 0) {
      files.forEach((file) => formData.append("files", file));
    }

    // accessToken을 로컬스토리지에서 가져옴
    const accessToken = localStorage.getItem("access_token");
    if (!accessToken) {
      console.error("No access token found");
      return;
    }

    try {
      const response = await fetch("http://localhost:8080/api/expenses", {
        method: "POST",
        headers: {
          Authorization: `Bearer ${accessToken}`, // Authorization 헤더
        },
        body: formData, // formData를 요청 본문에 첨부
      });

      if (response.ok) {
        const data = await response.json();
        console.log("Expense created successfully", data);
        navigate("/list"); // 성공적으로 생성되면 목록 페이지로 이동
      } else {
        const errorDetails = await response.text();
        console.error("Failed to create expense", errorDetails);
      }
    } catch (error) {
      console.error("Error:", error);
    }
  };

  return (
    <FormContainer>
      <BackPage />
      <h2>경비 관리</h2>
      <Form onSubmit={handleSubmit}>
        <Label>날짜</Label>
        <Input
          type="date"
          value={expense.date}
          onChange={handleChangeDate}
          required
        />

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
              type="button"
              selected={expense.category === category}
              onClick={(e) => handleCategoryChange(category, e)}
            >
              {category}
            </CategoryButton>
          ))}
        </CategoryButtons>

        <Label>사진 URL</Label>
        <FileInputWrapper htmlFor="fileInput">+</FileInputWrapper>
        <HiddenInput
          type="file"
          multiple
          onChange={handleFileChange}
          id="fileInput"
          accept="image/*"
        />

        {previewImages.length > 0 && (
          <ImagePreviewContainer>
            {previewImages.map((src, index) => (
              <PreviewImage
                key={index}
                src={src}
                alt={`미리보기 ${index + 1}`}
              />
            ))}
          </ImagePreviewContainer>
        )}

        <Label>금액</Label>
        {expense.amounts.map((amountItem, index) => (
          <AmountContainer key={index}>
            <Input
              name="amount"
              type="text"
              value={amountItem.amount || ""}
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
          type="text"
          value={formatAmount(String(calculateTotalAmount()))}
          disabled
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

// 스타일링
const FormContainer = styled.div`
  max-width: 400px;
  margin: 0 auto;
  background-color: #f8f9fa;
  padding: 20px;
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
  margin: 5px 0;
  border: 1px solid #ddd;
  border-radius: 6px;
`;

const CategoryButtons = styled.div`
  display: flex;
  justify-content: space-around;
  margin: 10px 0;
`;

const CategoryButton = styled.button`
  padding: 8px 16px;
  font-size: 14px;
  border: none;
  background-color: ${({ selected }) => (selected ? "#007bff" : "#ddd")};
  color: ${({ selected }) => (selected ? "#fff" : "#333")};
  border-radius: 6px;
  cursor: pointer;
`;

const FileInputWrapper = styled.label`
  display: inline-block;
  padding: 8px 12px;
  background-color: #007bff;
  color: #fff;
  border-radius: 6px;
  cursor: pointer;
`;

const HiddenInput = styled.input`
  display: none;
`;

const ImagePreviewContainer = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
`;

const PreviewImage = styled.img`
  width: 80%;
  height: auto;
  object-fit: cover;
  margin: 0 auto;
`;

const AmountContainer = styled.div`
  display: flex;
  gap: 10px;
  margin-top: 5px;
`;

const RemoveButton = styled.button`
  background-color: #f44336;
  color: white;
  border: none;
  border-radius: 6px;
  padding: 4px 10px;
  cursor: pointer;
`;

const AddButton = styled.button`
  margin-top: 10px;
  background-color: #007bff;
  color: white;
  border: none;
  padding: 10px;
  border-radius: 6px;
  cursor: pointer;
`;

const SubmitButton = styled.button`
  margin-top: 20px;
  background-color: #28a745;
  color: white;
  padding: 10px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
`;

export default ExpenseWrite;
