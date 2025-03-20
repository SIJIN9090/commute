import React from "react";
import { useNavigate } from "react-router-dom";
import styled from "styled-components";

const BackPage = () => {
  const navigate = useNavigate();

  return (
    <Header>
      <BackButton onClick={() => navigate(-1)}>←</BackButton>
    </Header>
  );
};

const Header = styled.div`
  display: flex;
  align-items: center;
  justify-content: flex-start;
  width: 90%;
  padding: 8px 20px;
  background-color: #f8f9fa;
  height: 10px;
`;

const BackButton = styled.button`
  background: none;
  border: none;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  color: #3498db;
  padding: 0;
  margin: 0;
`;
export default BackPage;
