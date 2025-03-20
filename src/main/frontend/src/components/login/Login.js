import React, { useState } from "react";
import styled from "styled-components";
import axios from "axios";

const Login = () => {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");

  const handleLogin = async (e) => {
    e.preventDefault();
    try {
      const response = await axios.post("/api/auth/login", {
        username,
        password,
      });
      localStorage.setItem("token", response.data); // JWT 토큰을 로컬 스토리지에 저장
      setError("");
      // 로그인 성공 후, 원하는 페이지로 리디렉션
      window.location.href = "/list";
    } catch (err) {
      setError("Invalid credentials!");
    }
  };

  const handleSignupRedirect = () => {
    // 회원가입 페이지로 리디렉션
    window.location.href = "/signup";
  };

  return (
    <LoginContainer>
      <Form onSubmit={handleLogin}>
        <h2>Login</h2>
        <Input
          type="text"
          placeholder="Username"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
        />
        <Input
          type="password"
          placeholder="Password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
        />
        <Button type="submit">Login</Button>
        {error && <ErrorMessage>{error}</ErrorMessage>}
        <SignupLink onClick={handleSignupRedirect}>
          Don't have an account? Sign up
        </SignupLink>
      </Form>
    </LoginContainer>
  );
};

const LoginContainer = styled.div`
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
`;

const Form = styled.form`
  display: flex;
  flex-direction: column;
  width: 300px;
  padding: 20px;
  border: 1px solid #ddd;
  border-radius: 10px;
`;

const Input = styled.input`
  padding: 10px;
  margin: 10px 0;
  border: 1px solid #ccc;
  border-radius: 5px;
`;

const Button = styled.button`
  padding: 10px;
  margin: 10px 0;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;

  &:hover {
    background-color: #0056b3;
  }
`;

const ErrorMessage = styled.p`
  color: red;
  text-align: center;
`;

const SignupLink = styled.p`
  color: #007bff;
  text-align: center;
  cursor: pointer;

  &:hover {
    text-decoration: underline;
  }
`;

export default Login;
