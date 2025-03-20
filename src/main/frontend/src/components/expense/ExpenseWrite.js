import React, { useState, useEffect } from "react";
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
  const [expense, setExpense] = useState({
    title: "",
    content: "",
    category: "",
    date: "",
    photoUrls: [],
    amounts: [{ amount: "" }], // 금액 항목을 빈 값으로 초기화
    totalAmount: 0, // 전체 합계는 수정 불가하도록 설정
  });

  // 금액 값 업데이트
  const handleChange = (e, index) => {
    const { value } = e.target;
    const formattedValue = formatAmount(value); // 포맷팅된 금액 값

    const newAmounts = [...expense.amounts];
    newAmounts[index] = { amount: formattedValue }; // 금액 항목을 업데이트
    setExpense({ ...expense, amounts: newAmounts });
  };

  // 카테고리 변경
  const handleCategoryChange = (category, e) => {
    e.preventDefault(); // 카테고리 클릭 시 폼 제출을 막기 위한 방법
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
    // 금액이 없다면 0을 반환
    const total = expense.amounts.reduce(
      (total, item) => total + (Number(item.amount.replace(/,/g, "")) || 0),
      0
    );
    return total > 0 ? total : 0; // 금액이 0보다 크면 그 값을, 그렇지 않으면 0을 반환
  };

  // 날짜 변경
  const handleChangeDate = (e) => {
    setExpense({ ...expense, date: e.target.value });
  };

  // 파일 선택 핸들러
  const handleFileChange = (e) => {
    const files = Array.from(e.target.files); // 여러 파일을 배열로 처리
    const imageUrls = files.map((file) => URL.createObjectURL(file)); // 미리보기 이미지 URL 생성

    setPreviewImages((prevImages) => [...prevImages, ...imageUrls]); // 기존 이미지를 보존하면서 새로운 이미지 추가
    setExpense((prevExpense) => ({
      ...prevExpense,
      photoUrls: [...prevExpense.photoUrls, ...files], // 기존 파일들을 보존하면서 새로운 파일 추가
    }));
  };

  // 폼 제출
  const handleSubmit = async (e) => {
    e.preventDefault();

    const totalAmount = calculateTotalAmount(); // 금액 합계를 계산
    if (totalAmount === 0) {
      alert("금액이 0이어서는 안 됩니다.");
      return;
    }

    const formData = new FormData();
    formData.append("expenseDto", JSON.stringify({ ...expense, totalAmount })); // amounts를 제외하고 totalAmount만 포함

    // 여러 개의 파일 추가
    expense.photoUrls.forEach((file) => {
      formData.append("files", file);
    });

    console.log("📢 전송 데이터 확인:");
    console.log("Expense DTO:", { ...expense, totalAmount });
    console.log("Photo Files:", expense.photoUrls);

    // 🔥 토큰 가져오기
    const accessToken = localStorage.getItem("access_token");
    console.log("🛠️ accessToken:", accessToken);

    try {
      const response = await fetch("/api/expenses", {
        method: "POST",
        headers: {
          Authorization: `Bearer ${accessToken}`, // ✅ 토큰 추가
        },
        body: formData, // ⚠️ 'Content-Type'을 설정하지 않음 (자동 처리됨)
      });

      console.log("📢 서버 응답 상태 코드:", response.status);
      if (!response.ok) {
        const errorMessage = await response.text();
        console.error("❌ 서버 응답 오류:", errorMessage);
        alert(`서버 오류 발생: ${errorMessage}`);
        return;
      }

      const result = await response.json();
      console.log("✅ 서버 응답 성공:", result);

      alert("게시물이 작성되었습니다.");
      setExpense({
        title: "",
        content: "",
        category: "",
        date: "",
        photoUrls: [],
        amounts: [{ amount: "" }],
        totalAmount: 0,
      });
      setPreviewImages([]);
      navigate("/list");
    } catch (error) {
      console.error("❌ 요청 실패:", error);
      alert("에러가 발생했습니다. 다시 시도해 주세요.");
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
              type="button" // 버튼의 타입을 'button'으로 설정하여 폼 제출 방지
              selected={expense.category === category}
              onClick={(e) => handleCategoryChange(category, e)} // e.preventDefault() 추가
            >
              {category}
            </CategoryButton>
          ))}
        </CategoryButtons>

        <Label>사진 URL</Label>
        <FileInputWrapper htmlFor="fileInput">+</FileInputWrapper>
        <HiddenInput
          type="file"
          multiple // 여러 파일 선택 가능
          onChange={handleFileChange}
          id="fileInput"
          aria-label="파일 선택"
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
  gap: 10px;
  margin-top: 10px;
`;

const PreviewImage = styled.img`
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 6px;
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
  padding: 10px 0;
  border-radius: 6px;
  cursor: pointer;
`;

const SubmitButton = styled.button`
  margin-top: 20px;
  background-color: #28a745;
  color: white;
  border: none;
  padding: 12px;
  border-radius: 6px;
  cursor: pointer;
`;

export default ExpenseWrite;
