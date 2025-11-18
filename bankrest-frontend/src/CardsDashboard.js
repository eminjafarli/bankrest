import React, { useEffect, useState, useCallback } from "react";
import styled, { keyframes, createGlobalStyle, css } from "styled-components";
import axios from "axios";
import { motion, AnimatePresence } from "framer-motion";
import { useNavigate } from "react-router-dom";
import {
    FaPlus,
    FaMinusCircle,
    FaEye,
    FaEyeSlash,
    FaRegCreditCard,
    FaUser,
    FaSignOutAlt,
    FaEdit,
    FaTimes,
    FaCopy,
    FaWallet,
    FaSearch,
} from "react-icons/fa";
import AdminCardModal from "./AdminCardModal";
import EditProfileModal from "./EditProfileModal";
import ConfirmModal from "./ConfirmModal";

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

const maskCardNumber = (cardNumber) => {
    if (!cardNumber || cardNumber.length < 4) return "**** **** **** ****";
    return `**** **** **** ${cardNumber.slice(-4)}`;
};
const formatBalance = (balance) => {
    const num = Number(balance || 0);
    return new Intl.NumberFormat("en-US", { style: "currency", currency: "USD" }).format(num);
};

const shake = keyframes`
    0% { transform: rotate(-1deg); }
    50% { transform: rotate(1deg); }
    100% { transform: rotate(-1deg); }
`;

const Container = styled.div`
    min-height: 100vh;
    padding: 28px 48px;
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

const CardGrid = styled.div`
    display:grid;
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
    gap:28px;
    margin-top:18px;
`;

const CardWrapper = styled(motion.div)`
    perspective: 1400px;
    position: relative;
    ${props => props.shake && css`
        animation: ${shake} 0.25s infinite linear;
        transform-origin: center center;
    `}
`;

const DeleteX = styled.button`
    position:absolute;
    left:-10px;
    top:-10px;
    width:32px;
    height:32px;
    border-radius:999px;
    border:none;
    display:flex;
    align-items:center;
    justify-content:center;
    background:${COLORS.errorRed};
    color:white;
    font-weight:800;
    box-shadow:0 8px 18px rgba(0,0,0,0.12);
    cursor:pointer;
    z-index:5;
`;

const CardInner = styled.div`
    width:100%; height:200px; border-radius:14px; position:relative; transform-style:preserve-3d;
    transition: transform 0.8s cubic-bezier(.2,.9,.2,1);
    transform: rotateY(${(p) => (p.isFlipped ? "180deg" : "0deg")});
`;

const CardFace = styled.div`
    position:absolute; inset:0; border-radius:14px; backface-visibility:hidden; -webkit-backface-visibility:hidden;
    display:flex; flex-direction:column; padding:18px; box-shadow: 0 12px 30px rgba(10,20,40,0.08);
`;

const CardFront = styled(CardFace)`
    background: linear-gradient(135deg, ${COLORS.cardGradientStart}, ${COLORS.cardGradientEnd});
    color:white; justify-content:space-between; overflow:hidden; border:1px solid rgba(255,255,255,0.06);
`;

const CardBack = styled(CardFace)`
    transform: rotateY(180deg);
    background: linear-gradient(180deg, rgba(255,255,255,0.96), rgba(245,247,250,0.96));
    color:${COLORS.textColor}; border:1px solid #e6e9ef;
`;

const StatusPill = styled.span`
    align-self:flex-end; padding:6px 10px; border-radius:999px; font-weight:700; font-size:12px;
    background:${(p) => (p.status === "ACTIVE" ? COLORS.successGreen : p.status === "BLOCKED" ? COLORS.errorRed : COLORS.primaryBlue)};
    color:white;
`;

const Balance = styled.div`
    font-size:22px; font-weight:800; letter-spacing:0.4px;
`;

const NumberMask = styled.div`
    font-family: 'Space Mono', monospace; font-size:16px; opacity:0.95;
`;

const Expiry = styled.div`
    font-size:13px; opacity:0.9;
`;

const BackRow = styled.div`
    display:flex; flex-direction:column; gap:10px; margin-top:6px;
`;

const CVVBox = styled.div`
    display:inline-flex; align-items:center; gap:8px; padding:8px 10px; border-radius:8px; background:#f3f5f8; font-weight:700;
    cursor:pointer;
`;

const BankCard = React.memo(({ card, role, onEdit, onDelete, isDeleteMode,onStatusChange }) => {
    const [isFlipped, setIsFlipped] = useState(false);
    const [showCvv, setShowCvv] = useState(false);

    useEffect(() => {
        setIsFlipped(false);
    }, [card.id]);

    useEffect(() => {
        if (isDeleteMode) setIsFlipped(false);
    }, [isDeleteMode]);

    const handleClick = (e) => {
        if (isDeleteMode) return;
        setIsFlipped((p) => !p);
        setShowCvv(false);
    };

    const handleDelete = (e) => {
        e.stopPropagation();
        onDelete(card.id);
    };

    const masked = maskCardNumber(card.number || "0000000000000000");
    const bal = formatBalance(card.balance || 0);
    const expiration = card.expirationDate ? card.expirationDate.split("-").reverse().slice(0,2).join('/') : '00/00';
    const owner = card.user?.name || card.owner || '—';
    const cvv = card.cvv || '000';

    return (
        <CardWrapper shake={isDeleteMode} onClick={handleClick} whileHover={{ scale: isDeleteMode ? 1 : 1.02 }}>
            {isDeleteMode && role === 'ADMIN' && (
                <DeleteX onClick={(e) => { e.stopPropagation(); handleDelete(e); }} aria-label="Delete card">×</DeleteX>
            )}

            <CardInner isFlipped={isFlipped}>
                <CardFront>
                    <div style={{display:'flex', justifyContent:'space-between', alignItems:'center'}}>
                        <div>
                            <div style={{display:'flex', alignItems:'center', gap:8}}>
                                <FaRegCreditCard size={26} style={{opacity:0.95}} />
                                <div style={{fontSize:12, opacity:0.85}}>Credit Card</div>
                            </div>
                        </div>
                        <StatusPill status={card.status}>{(card.status || 'UNKNOWN').replace('_',' ')}</StatusPill>
                    </div>

                    <div style={{display:'flex', justifyContent:'space-between', alignItems:'end'}}>
                        <div>
                            <Balance>{bal}</Balance>
                        </div>
                        <div style={{textAlign:'right'}}>
                            <div style={{fontSize:12, opacity:0.85}}>{owner}</div>
                        </div>
                    </div>

                    <div style={{display:'flex', justifyContent:'space-between', alignItems:'center'}}>
                        <NumberMask>{masked}</NumberMask>
                        <Expiry>{expiration}</Expiry>
                    </div>
                </CardFront>

                <CardBack>
                    <div style={{display:'flex', justifyContent:'space-between', alignItems:'center'}}>
                        <div style={{display:'flex', gap:10, alignItems:'center'}}>
                            <FaRegCreditCard />
                            <div style={{fontWeight:700}}>Full Information</div>
                        </div>
                        <StatusPill status={card.status}>{(card.status || 'UNKNOWN').replace('_',' ')}</StatusPill>
                    </div>

                    <BackRow>
                        <div style={{display:'flex', justifyContent:'space-between', gap:12}}>
                            <div style={{ flex: 1 }}>
                                <strong>Number:</strong>

                                <div style={{
                                    opacity: 0.9,
                                    display:'flex',
                                    alignItems:'center',
                                    gap:8
                                }}>

                                    <span>
                {(card.number || '').replace(/(.{4})/g, '$1 ').trim() || '—'}
            </span>

                                    <FaCopy
                                        style={{
                                            cursor: 'pointer',
                                            opacity: 0.8
                                        }}
                                        onClick={(e) => {
                                            e.stopPropagation();
                                            navigator.clipboard.writeText(card.number);

                                            onStatusChange("Card number copied!", true);
                                        }}
                                    />
                                </div>
                            </div>

                            <div style={{width:100}}>
                                <strong>Expires</strong>
                                <div style={{opacity:0.9}}>{expiration}</div>
                            </div>
                        </div>

                        <div style={{display:'flex', justifyContent:'end', gap:12}}>
                            <CVVBox onClick={(e)=>{ e.stopPropagation(); setShowCvv(s => !s); }}>
                                {showCvv ? cvv : '***'}
                                {showCvv ? <FaEyeSlash /> : <FaEye />}
                            </CVVBox>
                        </div>
                        <div style={{display:'flex', justifyContent:'space-between', alignItems:'center', gap:12}}>
                            {role === 'ADMIN' && (
                                <Btn
                                    bg={card.status === "BLOCKED" ? COLORS.successGreen : COLORS.errorRed}
                                    color='white'
                                    onClick={async (e) => {
                                        e.stopPropagation();
                                        const token = localStorage.getItem("token");
                                        try {
                                            const newStatus = card.status === "BLOCKED" ? "ACTIVE" : "BLOCKED";
                                            await axios.patch(`http://localhost:8080/api/cards/admin/${card.id}/status`, { status: newStatus }, {
                                                headers: { Authorization: `Bearer ${token}` }
                                            });
                                            onStatusChange(`Card ${newStatus.toLowerCase()} successfully`, true);
                                        } catch {
                                            onStatusChange("Failed to update status", false);
                                        }
                                    }}
                                >
                                    {card.status === "BLOCKED" ? "Activate" : "Block"}
                                </Btn>
                            )}
                            {role === 'USER' && card.status !== "BLOCKED" && (
                                <Btn
                                    bg={card.status === "BLOCK_REQUESTED" ? COLORS.primaryBlue : COLORS.cardGradientStart}
                                    color='white'
                                    onClick={async (e) => {
                                        e.stopPropagation();
                                        const token = localStorage.getItem("token");
                                        try {
                                            const newStatus = card.status === "BLOCK_REQUESTED" ? "ACTIVE" : "BLOCK_REQUESTED";
                                            const endpoint = newStatus === "BLOCK_REQUESTED"
                                                ? `http://localhost:8080/api/cards/${card.id}/request-block`
                                                : `http://localhost:8080/api/cards/${card.id}/cancel-request`;

                                            await axios.patch(endpoint, {}, {
                                                headers: { Authorization: `Bearer ${token}` }
                                            });
                                            onStatusChange(`Block request ${newStatus === "BLOCK_REQUESTED" ? "submitted" : "cancelled"} successfully`, true);
                                        } catch (error) {
                                            const message = error.response?.data?.message || "Failed to update status";
                                            onStatusChange(message, false);
                                        }
                                    }}
                                >
                                    {card.status === "BLOCK_REQUESTED" ? "Cancel Block Request" : "Request Blocking"}
                                </Btn>
                            )}

                            {role === 'ADMIN' && (
                                <div style={{display:'flex', justifyContent:'top-end', gap:8, marginTop:0}}>
                                    <Btn onClick={(e)=>{ e.stopPropagation(); onEdit(card); }} bg={COLORS.primaryBlue} color='white'>
                                        <FaWallet /> Top Up
                                    </Btn>
                                </div>
                            )}
                        </div>

                    </BackRow>
                </CardBack>
            </CardInner>
        </CardWrapper>
    );
});

const TransferModalWrapper = styled(motion.div)`
    background: #ffffff;
    padding: 35px 40px;
    border-radius: 20px;
    width: 95%;
    max-width: 520px;
    box-shadow: 0 25px 70px rgba(0, 0, 0, 0.25);
    position: relative;
    border: 1px solid rgba(0,0,0,0.07);
`;

const TransferClose = styled.button`
    position: absolute;
    top: 18px;
    right: 18px;
    background: #f3f3f3;
    border: none;
    width: 32px;
    height: 32px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    transition: 0.2s;
    &:hover {
        background: #ff4d4d;
        color: white;
    }
`;

const TransferTitle = styled.h2`
    color: #1e3c72;
    font-weight: 700;
    font-size: 24px;
    margin-bottom: 18px;
    text-align: center;
`;

const TransferForm = styled.form`
    display:flex;
    flex-direction:column;
    gap:16px;
`;

const TransferGroup = styled.div`
    display:flex;
    flex-direction:column;
`;

const TransferLabel = styled.label`
    font-weight:600;
    color:#333;
    margin-bottom:6px;
`;

const TransferSelect = styled.select`
    padding:12px;
    border:1px solid #d0d0d0;
    border-radius:10px;
    font-size:15px;
    background:white;
    &:focus{ border-color:#1e3c72; box-shadow:0 0 0 3px rgba(30,60,114,0.15); outline:none; }
`;

const TransferInput = styled.input`
    padding:12px;
    border:1px solid #d0d0d0;
    border-radius:10px;
    font-size:15px;
    &:focus{ border-color:#1e3c72; box-shadow:0 0 0 3px rgba(30,60,114,0.15); outline:none; }
`;

const TransferSubmit = styled.button`
    padding:14px;
    background: linear-gradient(135deg, #1e3c72, #2a5298);
    color:white;
    font-weight:600;
    font-size:16px;
    border:none;
    border-radius:12px;
    cursor:pointer;
    transition:0.25s;
    &:hover{ filter:brightness(1.05); }
`;

const TransferError = styled.p`
    color:#ff3b3b;
    font-weight:700;
    text-align:center;
`;


function TransferModal({ cards, onClose, onTransfer }) {
    const [fromId, setFromId] = useState("");
    const [toId, setToId] = useState("");
    const [amount, setAmount] = useState("");
    const [error, setError] = useState(null);

    const mask = (num) => `**** **** **** ${num?.slice(-4) || '----'}`;

    const handleSubmit = (e) => {
        e.preventDefault();
        setError(null);

        if (!fromId || !toId || !amount) {
            setError("Please fill all fields.");
            return;
        }
        if (fromId === toId) {
            setError("Sender and receiver must be different.");
            return;
        }
        const amt = Number(amount);
        if (isNaN(amt) || amt <= 0) {
            setError("Amount must be a positive number.");
            return;
        }

        onTransfer(fromId, toId, amt);
    };

    return (
        <TransferModalWrapper
            initial={{ opacity: 0, scale: 0.9 }}
            animate={{ opacity: 1, scale: 1 }}
            exit={{ opacity: 0, scale: 0.9 }}
            onClick={(e) => e.stopPropagation()}
        >
            <TransferClose onClick={onClose}><FaTimes /></TransferClose>
            <TransferTitle>Transfer Money</TransferTitle>

            <TransferForm onSubmit={handleSubmit}>
                <TransferGroup>
                    <TransferLabel>Sender card</TransferLabel>
                    <TransferSelect value={fromId} onChange={(e) => setFromId(e.target.value)}>
                        <option value="">-- Select sender --</option>
                        {cards.map(c => (
                            <option key={c.id} value={c.id}>
                                {mask(c.number)} - {c.user?.name || '—'}
                            </option>
                        ))}
                    </TransferSelect>
                </TransferGroup>

                <TransferGroup>
                    <TransferLabel>Receiver card</TransferLabel>
                    <TransferSelect value={toId} onChange={(e) => setToId(e.target.value)}>
                        <option value="">-- Select receiver --</option>
                        {cards.map(c => (
                            <option key={c.id} value={c.id}>
                                {mask(c.number)} - {c.user?.name || '—'}
                            </option>
                        ))}
                    </TransferSelect>
                </TransferGroup>

                <TransferGroup>
                    <TransferLabel>Amount (USD)</TransferLabel>
                    <TransferInput
                        type="number"
                        step="1.00"
                        min="0"
                        value={amount}
                        onChange={(e) => setAmount(e.target.value)}
                        placeholder="0.00"
                    />
                </TransferGroup>

                {error && <TransferError>{error}</TransferError>}

                <TransferSubmit type="submit">Send Money</TransferSubmit>
            </TransferForm>
        </TransferModalWrapper>
    );
}

export default function CardsDashboard() {
    const navigate = useNavigate();
    const [cards, setCards] = useState([]);
    const [showModal, setShowModal] = useState(false);
    const [notification, setNotification] = useState(null);
    const [showEditProfileModal, setShowEditProfileModal] = useState(false);
    const [editCard, setEditCard] = useState(null);
    const [currentUser, setCurrentUser] = useState(null);
    const [searchTerm, setSearchTerm] = useState("");
    const [isDeleteMode, setIsDeleteMode] = useState(false);
    const [showTransferModal, setShowTransferModal] = useState(false);

    const [cardToDeleteId, setCardToDeleteId] = useState(null);

    const role = localStorage.getItem("role");
    const username = localStorage.getItem("username");
    const userId = localStorage.getItem("userId");
    const [page, setPage] = useState(0);
    const [size] = useState(9);
    const [totalPages, setTotalPages] = useState(0);

    const fetchCards = useCallback(async () => {
        try {
            const token = localStorage.getItem("token");
            const isUserRole = role === "USER";
            const endpoint = isUserRole
                ? `http://localhost:8080/api/cards/my?search=${encodeURIComponent(searchTerm)}&page=${page}&size=${size}`
                : `http://localhost:8080/api/cards/admin/all`;

            const response = await axios.get(endpoint, { headers: { Authorization: `Bearer ${token}` } });

            let allCards = [];
            let total = 0;
            let cardsToDisplay = [];

            const rawCards = isUserRole ? response.data.content || [] : response.data || [];

            const enrichedCards = rawCards.map(card => {
                const ownerName = card.user?.name || card.owner || card.userName || 'Self';
                return {
                    ...card,
                    user: {
                        name: ownerName
                    }
                };
            });

            if (isUserRole) {
                cardsToDisplay = enrichedCards;
                total = response.data.totalPages;
            } else {
                allCards = enrichedCards;
                total = Math.ceil(allCards.length / size);

                if (page > 0 && page >= total && allCards.length > 0) {
                    setPage(p => p - 1);
                    return;
                }

                const start = page * size;
                const end = start + size;
                cardsToDisplay = allCards.slice(start, end);

                if(allCards.length === 0) total = 0;
            }

            setCards(cardsToDisplay);
            setTotalPages(total);
        } catch (err) {
            console.error(err);
        }
    }, [role, searchTerm, page, size]);

    useEffect(() => { fetchCards(); }, [fetchCards]);

    const handleEditProfileClick = async () => {
        try {
            const token = localStorage.getItem("token");
            const resp = await axios.get(`http://localhost:8080/api/users/${userId}`, { headers: { Authorization: `Bearer ${token}` } });
            setCurrentUser(resp.data);
            setShowEditProfileModal(true);
        } catch (err) {
            console.error(err);
            setNotification({ message: 'Failed to fetch user', success: false });
            setTimeout(()=>setNotification(null),3000);
        }
    };

    const handleCardAction = (message, success) => {
        setNotification({ message, success });
        fetchCards();
        setShowModal(false);
        setEditCard(null);
        setTimeout(()=>setNotification(null),3000);
    };

    const handleDeleteCard = (cardId) => {
        setCardToDeleteId(cardId);
    };

    const confirmDeleteCard = async () => {
        const cardId = cardToDeleteId;
        setCardToDeleteId(null);

        if (!cardId) return;

        try {
            const token = localStorage.getItem('token');
            await axios.delete(`http://localhost:8080/api/cards/admin/${cardId}`, { headers: { Authorization: `Bearer ${token}` } });
            handleCardAction('Card deleted successfully.', true);
        } catch (err) {
            console.error(err);
            handleCardAction('Failed to delete card.', false);
        }
    };

    const handleTransfer = async (fromId, toId, amount) => {
        try {
            const token = localStorage.getItem("token");

            await axios.post("http://localhost:8080/api/cards/transfer", {
                fromCardId: fromId,
                toCardId: toId,
                amount: Number(amount)
            }, { headers: { Authorization: `Bearer ${token}` } });

            setNotification({ message: "Transfer successful", success: true });
            setTimeout(() => setNotification(null), 3000);
            setShowTransferModal(false);
            fetchCards();
        } catch (err) {
            console.error(err.response?.data?.message || err);
            setNotification({ message: err.response?.data?.message || "Transfer failed", success: false });
            setTimeout(() => setNotification(null), 3000);
        }
    };

    const filtered = cards.filter(c => {
        const s = searchTerm.toLowerCase();
        const last4 = c.number?.slice(-4) || '';
        const owner = (c.user?.name || '—').toLowerCase();
        const status = (c.status || '').toLowerCase();
        return last4.includes(s) || owner.includes(s) || status.includes(s);
    });

    const cardToDelete = cards.find(c => c.id === cardToDeleteId);
    const cardLast4 = cardToDelete?.number?.slice(-4) || '****';


    return (
        <>
            <GlobalStyle />
            <Container>
                <TopBar>
                    <Title><FaRegCreditCard size={28}/> Cards Dashboard</Title>
                    <ButtonGroup>
                        <Btn
                            onClick={() => setShowTransferModal(true)}
                            bg="white"
                            color="black"
                        >
                            <FaRegCreditCard /> Transfer
                        </Btn>
                        <Btn onClick={handleEditProfileClick} bg='white' border='1px solid #eef3ff'><FaEdit/> Edit Profile</Btn>
                        {role === 'ADMIN' && (
                            <>
                                <Btn onClick={() => setIsDeleteMode(s => !s)} bg={isDeleteMode ? COLORS.errorRed : 'white'} color={isDeleteMode ? 'white': COLORS.textColor}>
                                    {isDeleteMode ? <FaTimes/> : <FaMinusCircle/>} {isDeleteMode ? 'Done' : 'Delete'}
                                </Btn>
                                <Btn onClick={() => setShowModal(true)} bg={COLORS.primaryBlue} color='white'><FaPlus/> Add Card</Btn>
                                <Btn onClick={() => {navigate('/home'); }} bg={COLORS.mediumGrey} color='white'><FaSignOutAlt/> Back</Btn>
                            </>
                        )}
                        {role === 'USER' && (
                            <Btn onClick={() => { localStorage.clear(); navigate('/login'); }} bg={COLORS.errorRed} color='white'><FaSignOutAlt/> Logout</Btn>
                        )}
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
                                <SearchInput placeholder='Search by last 4, owner, or status...' value={searchTerm} onChange={(e)=>setSearchTerm(e.target.value)} />
                                <SearchIcon style={{position:'absolute', left:12, top:10}} />
                            </div>
                        </SearchWrapper>
                    </UserInfoBar>

                    {filtered.length === 0 && cards.length === 0 ? (
                        <div style={{padding:'48px 0', textAlign:'center', color:COLORS.mediumGrey}}>No cards found.</div>
                    ) : (
                        <CardGrid>
                            <AnimatePresence>
                                {filtered.map(card => (
                                    <motion.div key={card.id} initial={{opacity:0, y:6}} animate={{opacity:1, y:0}} exit={{opacity:0, y:6}} layout>
                                        <BankCard card={card} role={role} onEdit={setEditCard} onDelete={handleDeleteCard} isDeleteMode={isDeleteMode}  onStatusChange={handleCardAction}  />
                                    </motion.div>
                                ))}
                            </AnimatePresence>
                        </CardGrid>
                    )}

                    {totalPages > 1 && (
                        <div style={{display:'flex', justifyContent:'center', marginTop:24, gap:12}}>
                            <Btn disabled={page === 0} onClick={() => setPage(p => p - 1)}>Prev</Btn>
                            <span style={{padding:'8px 12px'}}>Page {page + 1} of {totalPages}</span>
                            <Btn disabled={page >= totalPages - 1} onClick={() => setPage(p => p + 1)}>Next</Btn>
                        </div>
                    )}
                </MainCard>

                <AnimatePresence>
                    {(showModal || editCard || showEditProfileModal || showTransferModal || cardToDeleteId) && (
                        <motion.div
                            initial={{opacity:0}}
                            animate={{opacity:1}}
                            exit={{opacity:0}}
                            style={{position:'fixed', inset:0, display:'flex', justifyContent:'center', alignItems:'center', zIndex:999}}
                        >

                            <div
                                style={{position:'absolute', inset:0, background:'rgba(8,12,28,0.45)'}}
                                onClick={() => {
                                    if(cardToDeleteId) return;
                                    setShowModal(false);
                                    setEditCard(null);
                                    setShowEditProfileModal(false);
                                    setShowTransferModal(false);
                                }}
                            />

                            <div style={{zIndex:1000}} onClick={(e)=>e.stopPropagation()}>
                                {(showModal || editCard) && role === 'ADMIN' && (
                                    <AdminCardModal card={editCard} onClose={() => { setShowModal(false); setEditCard(null); }} onCardAction={handleCardAction} />
                                )}
                                {showEditProfileModal && currentUser && (
                                    <EditProfileModal user={currentUser} onClose={() => setShowEditProfileModal(false)} onUserDeleted={() => { localStorage.clear(); navigate('/login'); }} />
                                )}
                                {showTransferModal && (
                                    <TransferModal cards={cards} onClose={() => setShowTransferModal(false)} onTransfer={handleTransfer} />
                                )}

                                {cardToDeleteId && (
                                    <ConfirmModal
                                        title="Confirm Card Deletion"
                                        message={`Are you sure you want to delete card ending in ${cardLast4}? This action is permanent.`}
                                        onCancel={() => setCardToDeleteId(null)}
                                        onConfirm={confirmDeleteCard}
                                        confirmText="Delete Card"
                                    />
                                )}
                            </div>
                        </motion.div>
                    )}
                </AnimatePresence>

                <AnimatePresence>
                    {notification && (
                        <motion.div initial={{y:-30, opacity:0}} animate={{y:0, opacity:1}} exit={{y:-30, opacity:0}} style={{position:'fixed', left:'50%', transform:'translateX(-50%)', top:24, zIndex:1200}}>
                            <div style={{padding:'10px 18px', background: notification.success ? COLORS.successGreen : COLORS.errorRed, color:'white', borderRadius:10, boxShadow:'0 8px 24px rgba(12,20,40,0.12)'}}>
                                {notification.message}
                            </div>
                        </motion.div>
                    )}
                </AnimatePresence>
            </Container>
        </>
    );
}