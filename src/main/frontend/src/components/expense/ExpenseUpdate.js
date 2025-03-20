import React, { useState, useEffect } from "react";
import { useParams, useHistory } from "react-router-dom";

const ExpenseUpdate = () => {
  const { id } = useParams(); // URL에서 id 값을 가져옵니다.
  const history = useHistory(); // 수정 후 페이지 이동을 위한 history 사용
  const [expense, setExpense] = useState({
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
        setExpense(data); // 가져온 데이터를 state에 저장
      });
  }, [id]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setExpense({ ...expense, [name]: value });
  };

  const handleSubmit = (e) => {
    e.preventDefault();

    // 수정된 데이터를 서버에 전송합니다.
    fetch(`/api/expenses/${id}`, {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(expense),
    })
      .then((res) => res.json())
      .then((data) => {
        // 수정이 성공하면 리스트 페이지로 리다이렉트
        history.push(`/expenses/${id}`);
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
            value={expense.title}
            onChange={handleChange}
          />
        </div>
        <div>
          <label>내용</label>
          <textarea
            name="content"
            value={expense.content}
            onChange={handleChange}
          />
        </div>
        <div>
          <label>금액</label>
          <input
            type="number"
            name="amount"
            value={expense.amount}
            onChange={handleChange}
          />
        </div>
        <div>
          <label>카테고리</label>
          <input
            type="text"
            name="category"
            value={expense.category}
            onChange={handleChange}
          />
        </div>
        <div>
          <label>사진 URL</label>
          <input
            type="text"
            name="photoUrl"
            value={expense.photoUrl}
            onChange={handleChange}
          />
        </div>
        <button type="submit">수정하기</button>
      </form>
    </div>
  );
};

export default ExpenseUpdate;
