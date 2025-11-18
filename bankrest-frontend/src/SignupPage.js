import React, { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { AnimatePresence, motion } from "framer-motion";

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

const Icon = ({ path, size = 18, color = COLORS.mediumGrey }) => (
    <svg viewBox="0 0 24 24" width={size} height={size} fill="currentColor" style={{ display: 'block' }}>
        <path d={path} fill={color} />
    </svg>
);

const FaUser = (props) => (
    <Icon path="M12 12c-2.21 0-4-1.79-4-4s1.79-4 4-4 4 1.79 4 4-1.79 4-4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z" {...props} />
);
const FaLock = (props) => (
    <Icon path="M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zm-6 9c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zm3.5-9H8.5V6c0-1.93 1.57-3.5 3.5-3.5s3.5 1.57 3.5 3.5v2z" {...props} />
);
const FaUserPlus = (props) => (
    <Icon path="M15 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm-9-2V7H4v3H1v2h3v3h2v-3h3v-2H6zm9 4c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z" {...props} />
);
const FaSignature = (props) => (
    <Icon path="M12.92 10.92l-.18 4.38-1.55-1.55 3.16-3.16c.3-.3.47-.7.47-1.13 0-.43-.17-.83-.47-1.13L11.52 3.8a1.59 1.59 0 0 0-2.26 0L3.8 9.26a1.59 1.59 0 0 0 0 2.26l4.63 4.63-1.55 1.55-4.38.18 1.95 1.95 2.4-2.4 4.34 4.34-1.42 1.42 1.42 1.42 2.84-2.84 1.42 1.42 1.42-1.42-1.42-1.42 2.84-2.84 1.42 1.42 1.42-1.42z" {...props} />
);

const GlobalStyle = () => (
    <style dangerouslySetInnerHTML={{__html: `
        body {
            margin: 0;
            padding: 0;
            background: linear-gradient(180deg, #eff6ff 0%, ${COLORS.lightGrey} 100%);
            font-family: 'Inter', 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            color: ${COLORS.textColor};
            -webkit-font-smoothing: antialiased;
            -moz-osx-font-smoothing: grayscale;
        }
    `}} />
);

const styles = {
    container: {
        minHeight: '100vh',
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        background: 'transparent',
        fontFamily: 'Inter, sans-serif',
    },
    formBox: {
        background: 'linear-gradient(180deg, rgba(255,255,255,0.75), rgba(255,255,255,0.6))',
        padding: '40px',
        borderRadius: '16px',
        width: '380px',
        textAlign: 'center',
        boxShadow: COLORS.shadowMedium,
        border: '1px solid #eef3ff',
        backdropFilter: 'blur(5px)',
    },
    notification: (success) => ({
        position: 'fixed',
        top: '20px',
        padding: '12px 24px',
        borderRadius: '8px',
        color: 'white',
        background: success ? COLORS.successGreen : COLORS.errorRed,
        zIndex: 1000,
        boxShadow: '0 4px 12px rgba(0, 0, 0, 0.3)',
        fontWeight: 600,
    }),
    title: {
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        gap: '10px',
        marginBottom: '30px',
        fontWeight: 700,
        color: COLORS.darkNavy,
        fontSize: '24px',
    },
    inputWrapper: {
        position: 'relative',
        marginBottom: '20px',
        textAlign: 'left',
    },
    input: {
        width: '100%',
        padding: '12px 12px 12px 40px',
        fontSize: '15px',
        border: '1px solid #e6e9ef',
        borderRadius: '10px',
        background: 'white',
        boxSizing: 'border-box',
    },
    inputIcon: {
        position: 'absolute',
        top: '50%',
        left: '12px',
        transform: 'translateY(-50%)',
        color: COLORS.mediumGrey,
        pointerEvents: 'none',
    },
    button: {
        width: '100%',
        padding: '12px',
        backgroundColor: COLORS.primaryBlue,
        color: 'white',
        fontSize: '16px',
        fontWeight: 600,
        border: 'none',
        borderRadius: '10px',
        cursor: 'pointer',
        marginTop: '10px',
        boxShadow: '0 8px 15px rgba(55, 107, 243, 0.3)',
        transition: 'background-color 0.2s, box-shadow 0.2s',
    },
    signUpLink: {
        marginTop: '25px',
        fontSize: '14px',
        color: COLORS.mediumGrey,
    },
    linkStyle: {
        color: COLORS.primaryBlue,
        textDecoration: 'none',
        fontWeight: 600,
    }
};

const InputComponent = ({ icon: IconComponent, name, placeholder, value, onChange, type = "text", required }) => {
    const [isFocused, setIsFocused] = useState(false);
    return (
        <div style={styles.inputWrapper}>
            <div style={styles.inputIcon}>
                <IconComponent color={isFocused ? COLORS.primaryBlue : COLORS.mediumGrey} />
            </div>
            <input
                type={type}
                name={name}
                placeholder={placeholder}
                value={value}
                onChange={onChange}
                required={required}
                style={{
                    ...styles.input,
                    borderColor: isFocused ? COLORS.primaryBlue : '#e6e9ef',
                    boxShadow: isFocused ? `0 0 0 3px rgba(55, 107, 243, 0.2)` : 'none',
                }}
                onFocus={() => setIsFocused(true)}
                onBlur={() => setIsFocused(false)}
            />
        </div>
    );
};

function SignupPage() {
    const [notification, setNotification] = useState(null);
    const [formData, setFormData] = useState({
        username: "",
        password: "",
        name: "",
    });

    const MotionButton = motion.button;
    const MotionFormBox = motion.div;
    const MotionNotification = motion.div;

    const navigate = useNavigate();

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            const response = await fetch("http://localhost:8080/api/auth/signup", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify(formData),
            });

            if (response.ok) {
                setNotification({ message: "Successful Signup!", success: true });
                setTimeout(() => {
                    navigate("/login");
                }, 1500);
            }
            else if (response.status === 409) {
                setNotification({message: "Password must contain at least 8 letters.", success: false});
            }
            else if (response.status === 408) {
                setNotification({ message: "This user already exists.", success: false });
            }
            else {
                setNotification({ message: "Signup Failed.", success: false });
            }
            setTimeout(() => {
                setNotification(null);
            }, 3000);

        } catch (err) {
            console.error("Signup Error:", err);
            setNotification({ message: "Network Error. Could not reach server.", success: false });
            setTimeout(() => {
                setNotification(null);
            }, 3000);
        }
    };

    return (
        <>
            <GlobalStyle />
            <div style={styles.container}>
                <MotionFormBox
                    style={styles.formBox}
                    initial={{ opacity: 0, y: 20 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ duration: 0.5 }}
                >
                    <form onSubmit={handleSubmit}>
                        <h2 style={styles.title}>
                            <FaUserPlus size={22} color={COLORS.primaryBlue}/>
                            Sign Up
                        </h2>

                        <InputComponent
                            icon={FaUser}
                            name="username"
                            placeholder="Username"
                            value={formData.username}
                            onChange={handleChange}
                            required
                        />

                        <InputComponent
                            icon={FaSignature}
                            name="name"
                            placeholder="Full Name"
                            value={formData.name}
                            onChange={handleChange}
                            required
                        />

                        <InputComponent
                            icon={FaLock}
                            name="password"
                            placeholder="Password"
                            value={formData.password}
                            onChange={handleChange}
                            type="password"
                            required
                        />

                        <MotionButton
                            type="submit"
                            style={styles.button}
                            whileTap={{ scale: 0.98 }}
                            onMouseOver={e => e.currentTarget.style.backgroundColor = '#2b59d9'}
                            onMouseOut={e => e.currentTarget.style.backgroundColor = COLORS.primaryBlue}
                        >
                            Sign Up
                        </MotionButton>
                    </form>

                    <div style={styles.signUpLink}>
                        Already have an account? <Link to="/login" style={styles.linkStyle}>Login</Link>
                    </div>
                </MotionFormBox>

                <AnimatePresence>
                    {notification && (
                        <MotionNotification
                            style={styles.notification(notification.success)}
                            initial={{ y: -100, opacity: 0 }}
                            animate={{ y: 0, opacity: 1 }}
                            exit={{ y: -100, opacity: 0 }}
                            transition={{ duration: 0.5 }}
                        >
                            {notification.message}
                        </MotionNotification>
                    )}
                </AnimatePresence>
            </div>
        </>
    );
}

export default SignupPage;