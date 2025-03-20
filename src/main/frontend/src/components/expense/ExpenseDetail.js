import React, { useState, useEffect } from "react";
import { useParams, useHistory } from "react-router-dom";

const ExpenseDetailWithForm = () => {
  const { id } = useParams(); // URL에서 id 값을 가져옵니다.
  const history = useHistory(); // 수정 후 페이지 이동을 위한 history 사용
  const [expense, setExpense] = useState(null);
  const [totalAmount, setTotalAmount] = useState(0);
  const [updatedExpense, setUpdatedExpense] = useState({
    title: "",
    content: "",
    amount: 0,
    category: "",
    photoUrl: "",
  });

  useEffect(() => {
    // 서버에서 해당 항목을 가져옵니다.
    fetch(`/api/expenses/${id}`)
      .then((res) => res.json())
      .then((data) => {
        setExpense(data);
        setUpdatedExpense(data); // 기존 항목을 폼에 설정
      });

    // 전체 금액 합산을 서버에서 가져옵니다.
    fetch("/api/expenses/total")
      .then((res) => res.json())
      .then((data) => {
        setTotalAmount(data);
      });
  }, [id]);

  // 입력값 변경 처리
  const handleChange = (e) => {
    const { name, value } = e.target;
    setUpdatedExpense({ ...updatedExpense, [name]: value });
  };

  // 수정된 항목을 서버로 보내기
  const handleSubmit = (e) => {
    e.preventDefault();

    fetch(`/api/expenses/${id}`, {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(updatedExpense),
    })
      .then((res) => res.json())
      .then((data) => {
        setExpense(data);
        history.push(`/expenses/${id}`); // 수정 후 디테일 페이지로 이동
      })
      .catch((error) => console.error("Error:", error));
  };

  if (!expense) {
    return <div>Loading...</div>;
  }

  return (
    <div>
      <h2>비용 항목 수정</h2>
      <form onSubmit={handleSubmit}>
        <div>
          <label>제목</label>
          <input
            type="text"
            name="title"
            value={updatedExpense.title}
            onChange={handleChange}
          />
        </div>
        <div>
          <label>내용</label>
          <textarea
            name="content"
            value={updatedExpense.content}
            onChange={handleChange}
          />
        </div>
        <div>
          <label>금액</label>
          <input
            type="number"
            name="amount"
            value={updatedExpense.amount}
            onChange={handleChange}
          />
        </div>
        <div>
          <label>카테고리</label>
          <input
            type="text"
            name="category"
            value={updatedExpense.category}
            onChange={handleChange}
          />
        </div>
        <div>
          <label>사진 URL</label>
          <input
            type="text"
            name="photoUrl"
            value={updatedExpense.photoUrl}
            onChange={handleChange}
          />
        </div>
        <button type="submit">수정하기</button>
      </form>

      <h3>전체 합계: {totalAmount} 원</h3>
    </div>
  );
};

export default ExpenseDetailWithForm;
