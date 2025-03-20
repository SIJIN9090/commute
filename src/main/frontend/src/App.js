import React from "react";
import { BrowserRouter, Routes, Route } from "react-router-dom";
import ExpenseList from "../src/components/expense/ExpenseList";
import ExpenseWrite from "../src/components/expense/ExpenseWrite";

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<ExpenseList />} />
        <Route path="/create" element={<ExpenseWrite />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
