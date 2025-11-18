import React, { useEffect, useState, useCallback } from "react";
import axios from "axios";
import styled, { createGlobalStyle } from "styled-components";
import { motion, AnimatePresence } from "framer-motion";
import { useNavigate } from "react-router-dom";
import { FaUsers, FaSignOutAlt, FaSearch, FaUser, FaWallet } from "react-icons/fa";
import EditUserModal from "./EditUserModal";

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

const formatBalance = (balance) => {
    const num = Number(balance || 0);
    return new Intl.NumberFormat("en-US", { style: "currency", currency: "USD" }).format(num);
};

const Container = styled.div`
    min-height: 100vh;
    padding: 28px 48px;
`;

const PaginationContainer = styled.div`
    display:flex;
    justify-content:center;
    align-items:center;
    gap: 12px;
    margin-top: 24px;
`;

const TopBar = styled.div`
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 16px;
    margin-bottom: 26px;
`;

const Title = styled.h1`
    display:flex;
    align-items:center;
    gap:12px;
    font-size:24px;
    color: ${COLORS.darkNavy};
    margin:0;
`;

const ButtonGroup = styled.div`
    display:flex; gap:10px; align-items:center;
`;

const Btn = styled(motion.button)`
    background:${(p) => p.bg || "white"};
    color:${(p) => p.color || COLORS.textColor};
    border: ${(p) => p.border || "none"};
    padding:8px 14px;
    border-radius:10px;
    cursor:pointer;
    font-weight:600;
    display:flex; align-items:center; gap:8px;
    box-shadow: 0 6px 18px rgba(12, 20, 40, 0.06);
    transition: background 0.2s, box-shadow 0.2s;
    &:hover {
        filter: brightness(0.98);
        box-shadow: 0 8px 20px rgba(12, 20, 40, 0.1);
    }
    &:disabled {
        opacity: 0.5;
        cursor: not-allowed;
    }
`;

const MainCard = styled.div`
    background: linear-gradient(180deg, rgba(255,255,255,0.75), rgba(255,255,255,0.6));
    border-radius:16px;
    padding:28px;
    box-shadow: ${COLORS.shadowMedium};
`;

const UserInfoBar = styled.div`
    display:flex; justify-content:space-between; align-items:center; gap:12px; margin-bottom:22px;
`;

const SearchWrapper = styled.div`
    display:flex; align-items:center; gap:8px;
`;

const SearchInput = styled.input`
    padding:10px 40px 10px 42px;
    border-radius:12px;
    border:1px solid #e6e9ef;
    font-size:14px;
    width:340px;
    &:focus{ outline:none; box-shadow: 0 6px 18px rgba(55,107,243,0.12); border-color:${COLORS.primaryBlue}; }
`;

const SearchIcon = styled(FaSearch)`
    position:relative; left:34px; color:${COLORS.mediumGrey};
`;

const UserList = styled.div`
    display:flex;
    flex-direction:column;
    gap:16px;
    margin-top:20px;
`;

const UserCard = styled(motion.div)`
    background: white;
    padding: 18px 24px;
    border-radius: 12px;
    box-shadow: 0 4px 15px rgba(0, 0, 0, 0.04);
    display: flex;
    justify-content: space-between;
    align-items: center;
    border: 1px solid #eef3ff;
`;

const UserDetails = styled.div`
    display:flex;
    align-items:center;
    gap: 16px;
`;

const UserText = styled.div`
    line-height: 1.4;
`;

const UserName = styled.div`
    font-weight: 700;
    font-size: 16px;
    color: ${COLORS.darkNavy};
`;

const UserRole = styled.span`
    font-weight: 600;
    color: ${COLORS.mediumGrey};
    font-size: 14px;
    margin-left: 8px;
`;

const BalanceDisplay = styled.div`
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 15px;
    font-weight: 600;
    color: ${COLORS.primaryBlue};
    background: #eef3ff;
    padding: 8px 12px;
    border-radius: 8px;
`;

const ActionButton = styled(Btn)`
    padding: 10px 18px;
    background: ${COLORS.primaryBlue};
    color: white;
`;

function UsersDashboard() {
    const navigate = useNavigate();
    const [users, setUsers] = useState([]);
    const [userBalances, setUserBalances] = useState({});
    const [selectedUser, setSelectedUser] = useState(null);
    const [showEditModal, setShowEditModal] = useState(false);

    const [searchTerm, setSearchTerm] = useState("");
    const [page, setPage] = useState(0);
    const [size] = useState(10);
    const [totalPages, setTotalPages] = useState(0);

    const username = localStorage.getItem("username");
    const role = localStorage.getItem("role");
    const token = localStorage.getItem("token");

    const handleUserDeleted = (id) => {
        setUsers((prevUsers) => prevUsers.filter((u) => u.id !== id));
        setShowEditModal(false);
        fetchUsers();
    };

    const fetchUserBalance = useCallback(async (userId) => {
        try {
            const res = await axios.get(`http://localhost:8080/api/cards/user/${userId}`, {
                headers: { Authorization: `Bearer ${token}` },
            });
            const cards = res.data;
            const totalBalance = cards.reduce((sum, card) => sum + (Number(card.balance) || 0), 0);
            setUserBalances(prev => ({ ...prev, [userId]: totalBalance }));
        } catch (err) {
            console.error(`Error fetching balance for user ${userId}:`, err);
            setUserBalances(prev => ({ ...prev, [userId]: "ERROR" }));
        }
    }, [token]);

    const fetchUsers = useCallback(async () => {
        try {
            const res = await axios.get(
                `http://localhost:8080/api/users?search=${encodeURIComponent(searchTerm)}&page=${page}&size=${size}`,
                { headers: { Authorization: `Bearer ${token}` } }
            );

            let fetchedUsers = [];
            let totalPagesFromApi = 0;

            if (res.data.content) {
                fetchedUsers = res.data.content;
                totalPagesFromApi = res.data.totalPages || 0;
            } else if (Array.isArray(res.data)) {
                fetchedUsers = res.data;
                totalPagesFromApi = 1;
            } else if (res.data.data && Array.isArray(res.data.data)) {
                fetchedUsers = res.data.data;
                totalPagesFromApi = res.data.totalPages || 1;
            } else {
                fetchedUsers = Array.isArray(res.data) ? res.data : [];
                totalPagesFromApi = 1;
            }

            setUsers(fetchedUsers);
            setTotalPages(totalPagesFromApi);

            setUserBalances({});
            fetchedUsers.forEach(user => fetchUserBalance(user.id));

        } catch (err) {
            console.error("Error fetching users:", err.response?.data || err);
            setUsers([]);
            setTotalPages(0);
        }
    }, [token, fetchUserBalance, searchTerm, page, size]);

    useEffect(() => {
        if (role !== 'ADMIN') {
            navigate('/cards');
            return;
        }
        fetchUsers();
    }, [fetchUsers, role, navigate]);

    const handlePageChange = (newPage) => {
        if (newPage >= 0 && newPage < totalPages) {
            setPage(newPage);
        }
    };

    const handleSearchChange = (e) => {
        setSearchTerm(e.target.value);
        setPage(0);
    };


    return (
        <>
            <GlobalStyle />
            <Container>
                <TopBar>
                    <Title><FaUsers size={28}/> Users Management</Title>
                    <ButtonGroup>
                        <Btn onClick={() => {navigate('/home'); }} bg={COLORS.mediumGrey} color='white'><FaSignOutAlt/> Back</Btn>
                    </ButtonGroup>
                </TopBar>

                <MainCard>
                    <UserInfoBar>
                        <div style={{display:'flex', alignItems:'center', gap:12}}>
                            <FaUser/>
                            <div>
                                <div style={{fontSize:13, color:COLORS.mediumGrey}}>Logged in as</div>
                                <div style={{fontWeight:800}}>{username}</div>
                            </div>
                        </div>

                        <SearchWrapper>
                            <div style={{position:'relative'}}>
                                <SearchInput
                                    placeholder='Search by username or name...'
                                    value={searchTerm}
                                    onChange={handleSearchChange}
                                />
                                <SearchIcon style={{position:'absolute', left:12, top:10}} />
                            </div>
                        </SearchWrapper>
                    </UserInfoBar>

                    <UserList>
                        <AnimatePresence>
                            {users.map((user) => (
                                <UserCard
                                    key={user.id}
                                    initial={{opacity:0, y:10}}
                                    animate={{opacity:1, y:0}}
                                    exit={{opacity:0, x:-50}}
                                    transition={{duration:0.3}}
                                    whileHover={{scale: 1.01, boxShadow: COLORS.shadowMedium}}
                                >
                                    <UserDetails>
                                        <FaUser size={20} color={COLORS.mediumGrey}/>
                                        <UserText>
                                            <UserName>
                                                {user.name}
                                                <UserRole>({user.username})</UserRole>
                                            </UserName>
                                        </UserText>
                                    </UserDetails>

                                    <div style={{display:'flex', alignItems:'center', gap:20}}>
                                        <BalanceDisplay>
                                            <FaWallet size={16}/>
                                            {userBalances[user.id] === "ERROR" ? (
                                                <span>Balance Error</span>
                                            ) : (
                                                <span>Balance: {formatBalance(userBalances[user.id])}</span>
                                            )}
                                        </BalanceDisplay>

                                        <ActionButton
                                            onClick={() => {
                                                setSelectedUser(user);
                                                setShowEditModal(true);
                                            }}
                                            whileTap={{scale:0.95}}
                                        >
                                            View / Edit
                                        </ActionButton>
                                    </div>
                                </UserCard>
                            ))}
                        </AnimatePresence>
                    </UserList>

                    {users.length === 0 && (
                        <div style={{padding:'48px 0', textAlign:'center', color:COLORS.mediumGrey}}>No users found matching "{searchTerm}".</div>
                    )}

                    {totalPages > 1 && (
                        <PaginationContainer>
                            <Btn disabled={page === 0} onClick={() => handlePageChange(page - 1)}>Prev</Btn>
                            <span style={{padding:'8px 12px'}}>Page {page + 1} of {totalPages}</span>
                            <Btn disabled={page >= totalPages - 1} onClick={() => handlePageChange(page + 1)}>Next</Btn>
                        </PaginationContainer>
                    )}
                </MainCard>

                <AnimatePresence>
                    {showEditModal && selectedUser && (
                        <motion.div initial={{opacity:0}} animate={{opacity:1}} exit={{opacity:0}} style={{position:'fixed', inset:0, display:'flex', justifyContent:'center', alignItems:'center', zIndex:999}}>
                            <div style={{position:'absolute', inset:0, background:'rgba(8,12,28,0.45)'}} onClick={() => setShowEditModal(false)} />
                            <div style={{zIndex:1000}} onClick={(e)=>e.stopPropagation()}>
                                <EditUserModal
                                    user={selectedUser}
                                    onClose={() => setShowEditModal(false)}
                                    onUserDeleted={handleUserDeleted}
                                    onUserUpdated={fetchUsers}
                                />
                            </div>
                        </motion.div>
                    )}
                </AnimatePresence>
            </Container>
        </>
    );
}

export default UsersDashboard;