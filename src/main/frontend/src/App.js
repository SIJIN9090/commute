import React from "react";
import { BrowserRouter, Routes, Route } from "react-router-dom";
import ExpenseList from "../src/components/expense/ExpenseList";
import ExpenseWrite from "../src/components/expense/ExpenseWrite";
import BackPage from "./components/BackPage";

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/list" element={<ExpenseList />} />
        <Route path="/create" element={<ExpenseWrite />} />
        <Route path="/back" element={<BackPage />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
