import React from "react";
import { useNavigate } from "react-router-dom";
import styled, { createGlobalStyle } from "styled-components";
import { motion } from "framer-motion";
import { FaSignOutAlt, FaRegCreditCard, FaUsers } from "react-icons/fa";

const COLORS = {
    primaryBlue: "#376bf3",
    darkNavy: "#0f172a",
    lightGrey: "#f5f7fb",
    textColor: "#0b2239",
    shadowMedium: "0 12px 40px rgba(15, 20, 42, 0.12)",
    errorRed: "#e55656",
    successGreen: "#22c55e",
    accentGold: "#f6c36b",
    glassWhite: "rgba(255,255,255,0.08)",
    cardGradientStart: "#243b55",
    cardGradientEnd: "#141e30",
    mediumGrey: "#6b7280",
};

const GlobalStyle = createGlobalStyle`
    body {
        margin: 0;
        padding: 0;
        background: linear-gradient(180deg, #eff6ff 0%, ${COLORS.lightGrey} 100%);
        font-family: 'Inter', 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
        color: ${COLORS.textColor};
        -webkit-font-smoothing: antialiased;
        -moz-osx-font-smoothing: grayscale;
    }
`;

const Container = styled.div`
    min-height: 100vh;
    padding: 28px 48px;
    display: flex;
    flex-direction: column;
`;

const TopBar = styled.div`
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 16px;
    margin-bottom: 60px;
`;

const HeaderTitle = styled.h1`
    display:flex;
    align-items:center;
    gap:12px;
    font-size:24px;
    color: ${COLORS.darkNavy};
    margin:0;
`;

const ContentWrapper = styled.div`
    flex-grow: 1;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    margin-top: -80px; 
`;

const MainTitle = styled.h1`
    font-size: 44px;
    font-weight: 800;
    margin-bottom: 50px;
    color: ${COLORS.darkNavy};
    text-shadow: 0 4px 10px rgba(0,0,0,0.05);
`;

const ButtonGroup = styled.div`
    display: flex;
    gap: 20px;
`;

const NavButton = styled(motion.button)`
    padding: 18px 36px;
    font-size: 16px;
    font-weight: 700;
    border: none;
    border-radius: 12px;
    cursor: pointer;
    color: white;
    display: flex;
    align-items: center;
    gap: 10px;
    transition: 0.2s ease;
    
    background: ${COLORS.primaryBlue};
    box-shadow: 0 8px 25px rgba(55, 107, 243, 0.3);

    &:hover {
        filter: brightness(1.1);
        box-shadow: 0 10px 30px rgba(55, 107, 243, 0.4);
    }
`;

const LogoutButton = styled(motion.button)`
    background: ${COLORS.errorRed};
    color: white;
    border: none;
    padding: 8px 14px;
    border-radius: 10px;
    cursor: pointer;
    font-weight: 600;
    display: flex;
    align-items: center;
    gap: 8px;
    box-shadow: 0 6px 18px rgba(229, 86, 86, 0.2);
`;

function HomePage() {
    const navigate = useNavigate();
    const username = localStorage.getItem("username") || "User";

    const handleLogout = () => {
        localStorage.clear();
        navigate("/login");
    };

    return (
        <>
            <GlobalStyle />
            <Container>
                <TopBar>
                    <HeaderTitle>Welcome, {username}!</HeaderTitle>

                    <LogoutButton
                        onClick={handleLogout}
                        whileHover={{ scale: 1.05 }}
                        whileTap={{ scale: 0.95 }}
                    >
                        <FaSignOutAlt /> Logout
                    </LogoutButton>
                </TopBar>

                <ContentWrapper>
                    <MainTitle>Card Management System</MainTitle>
                    <ButtonGroup>
                        <NavButton
                            whileHover={{ scale: 1.05 }}
                            whileTap={{ scale: 0.95 }}
                            onClick={() => navigate("/users-dashboard")}
                        >
                            <FaUsers size={18} /> Manage Users
                        </NavButton>
                        <NavButton
                            whileHover={{ scale: 1.05 }}
                            whileTap={{ scale: 0.95 }}
                            onClick={() => navigate("/cards-dashboard")}
                        >
                            <FaRegCreditCard size={18} /> View Cards
                        </NavButton>
                    </ButtonGroup>
                </ContentWrapper>
            </Container>
        </>
    );
}

export default HomePage;