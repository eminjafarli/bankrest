import React, { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import styled, { createGlobalStyle } from "styled-components";
import { AnimatePresence, motion } from "framer-motion";
import { jwtDecode } from "jwt-decode";
import { FaSignInAlt, FaUser, FaLock } from "react-icons/fa";

const COLORS = {
    primaryBlue: "#376bf3",
    darkNavy: "#0f172a",
    lightGrey: "#f5f7fb",
    textColor: "#0b2239",
    shadowMedium: "0 12px 40px rgba(15, 20, 42, 0.12)",
    errorRed: "#e55656",
    successGreen: "#22c55e",
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
    display: flex;
    justify-content: center;
    align-items: center;
    background: transparent;
    font-family: 'Inter', sans-serif;
`;

const FormBox = styled(motion.div)`
    background: linear-gradient(180deg, rgba(255,255,255,0.75), rgba(255,255,255,0.6));
    padding: 40px;
    border-radius: 16px;
    width: 380px;
    text-align: center;
    box-shadow: ${COLORS.shadowMedium};
    border: 1px solid #eef3ff;
    backdrop-filter: blur(5px);
`;

const Notification = styled(motion.div)`
    position: fixed;
    top: 20px;
    padding: 12px 24px;
    border-radius: 8px;
    color: white;
    background: ${props => (props.success ? COLORS.successGreen : COLORS.errorRed)};
    z-index: 1000;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
    font-weight: 600;
`;

const Title = styled.h2`
    display:flex;
    align-items:center;
    justify-content:center;
    gap:10px;
    margin-bottom: 30px;
    font-weight: 700;
    color: ${COLORS.darkNavy};
    font-size: 24px;
`;

const InputWrapper = styled.div`
    position: relative;
    margin-bottom: 20px;
    text-align: left;
`;

const Input = styled.input`
    width: 100%;
    padding: 12px 12px 12px 40px; 
    font-size: 15px;
    border: 1px solid #e6e9ef;
    border-radius: 10px;
    transition: all 0.2s;
    background: white;
    box-sizing: border-box;

    &:focus {
        outline: none;
        border-color: ${COLORS.primaryBlue};
        box-shadow: 0 0 0 3px rgba(55, 107, 243, 0.2);
    }
`;

const InputIcon = styled.div`
    position: absolute;
    top: 50%;
    left: 12px;
    transform: translateY(-50%);
    color: ${COLORS.mediumGrey};
    pointer-events: none;
`;

const Button = styled(motion.button)`
    width: 100%;
    padding: 12px;
    background-color: ${COLORS.primaryBlue};
    color: white;
    font-size: 16px;
    font-weight: 600;
    border: none;
    border-radius: 10px;
    cursor: pointer;
    transition: 0.2s;
    margin-top: 10px;
    box-shadow: 0 8px 15px rgba(55, 107, 243, 0.3);

    &:hover {
        background-color: #2b59d9;
        box-shadow: 0 10px 20px rgba(55, 107, 243, 0.4);
    }
`;

const SignUpLink = styled.div`
    margin-top: 25px;
    font-size: 14px;
    color: ${COLORS.mediumGrey};

    a {
        color: ${COLORS.primaryBlue};
        text-decoration: none;
        font-weight: 600;
    }

    a:hover {
        text-decoration: underline;
    }
`;

function LoginPage() {
    const [notification, setNotification] = useState(null);
    const [formData, setFormData] = useState({ username: "", password: "" });
    const navigate = useNavigate();

    const handleChange = (e) =>
        setFormData({ ...formData, [e.target.name]: e.target.value });

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            const response = await fetch("http://localhost:8080/api/auth/login", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(formData),
            });

            if (response.ok) {
                const data = await response.json();
                const token = data.token;

                const decoded = jwtDecode(token);
                const role = decoded.role;
                const username = decoded.username;
                const userId = decoded.id;

                localStorage.setItem("token", token);
                localStorage.setItem("role", role);
                localStorage.setItem("username", username);
                localStorage.setItem("userId", userId);

                setNotification({ message: "Successful Login!", success: true });

                setTimeout(() => {
                    if (role === "ADMIN") {
                        navigate("/home");
                    } else if (role === "USER") {
                        navigate("/cards-dashboard");
                    } else {
                        navigate("/login");
                    }
                }, 700);

            } else {
                setNotification({ message: "Invalid Credentials.", success: false });
                setTimeout(() => {
                    setNotification(null);
                }, 1500);
            }
        } catch (err) {
            setNotification({ message: "Error Occurred. Please try again.", success: false });
            setTimeout(() => {
                setNotification(null);
            }, 1500);
        }
    };

    return (
        <>
            <GlobalStyle />
            <Container>
                <FormBox
                    initial={{ opacity: 0, y: 20 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ duration: 0.5 }}
                >
                    <form onSubmit={handleSubmit}>
                        <Title>
                            <FaSignInAlt size={22} color={COLORS.primaryBlue}/>
                            Log In
                        </Title>

                        <InputWrapper>
                            <InputIcon><FaUser size={18}/></InputIcon>
                            <Input
                                type="text"
                                name="username"
                                placeholder="Username"
                                value={formData.username}
                                onChange={handleChange}
                                required
                            />
                        </InputWrapper>

                        <InputWrapper>
                            <InputIcon><FaLock size={18}/></InputIcon>
                            <Input
                                type="password"
                                name="password"
                                placeholder="Password"
                                value={formData.password}
                                onChange={handleChange}
                                required
                            />
                        </InputWrapper>

                        <Button type="submit" whileTap={{ scale: 0.98 }}>
                            Login
                        </Button>
                    </form>

                    <SignUpLink>
                        Don't have an account? <Link to="/signup">Sign Up</Link>
                    </SignUpLink>
                </FormBox>

                <AnimatePresence>
                    {notification && (
                        <Notification
                            success={notification.success}
                            initial={{ y: -100, opacity: 0 }}
                            animate={{ y: 0, opacity: 1 }}
                            exit={{ y: -100, opacity: 0 }}
                            transition={{ duration: 0.5 }}
                        >
                            {notification.message}
                        </Notification>
                    )}
                </AnimatePresence>
            </Container>
        </>
    );
}

export default LoginPage;