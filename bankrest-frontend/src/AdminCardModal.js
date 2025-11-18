import React, { useEffect, useState } from 'react';
import styled from 'styled-components';
import { FaTimes } from 'react-icons/fa';
import { motion } from 'framer-motion';
import axios from 'axios';

const generateRandomDigits = (length) => {
    let result = '';
    for (let i = 0; i < length; i++) {
        if (i === 0 && length > 1) {
            result += Math.floor(Math.random() * 9) + 1;
        } else {
            result += Math.floor(Math.random() * 10);
        }
    }
    return result;
};

const generateExpirationDate = () => {
    const today = new Date();
    const currentMonth = String(today.getMonth() + 1).padStart(2, '0');

    const currentYear = today.getFullYear();
    const expiryYear = String(currentYear + 2).slice(-2);

    return `${currentMonth}/${expiryYear}`;
};


const ModalWrapper = styled(motion.div)`
    background: #ffffff;
    padding: 35px 40px;
    border-radius: 20px;
    width: 95%;
    max-width: 520px;
    box-shadow: 0 25px 70px rgba(0, 0, 0, 0.25);
    position: relative;
    border: 1px solid rgba(0,0,0,0.07);
`;

const CloseButton = styled.button`
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

const ModalTitle = styled.h2`
    color: #1e3c72;
    font-weight: 700;
    font-size: 24px;
    margin-bottom: 25px;
    text-align: center;
`;

const Form = styled.form`
    display: flex;
    flex-direction: column;
    gap: 18px;
`;

const InputGroup = styled.div`
    display: flex;
    flex-direction: column;
`;

const Label = styled.label`
    font-weight: 600;
    color: #333;
    margin-bottom: 6px;
`;

const Input = styled.input`
    padding: 12px;
    border: 1px solid #d0d0d0;
    border-radius: 10px;
    font-size: 15px;
    transition: 0.2s;
    &:focus {
        border-color: #1e3c72;
        box-shadow: 0 0 0 3px rgba(30, 60, 114, 0.15);
        outline: none;
    }
    &:disabled {
        background: #f5f5f5;
        cursor: not-allowed;
    }
`;

const Select = styled.select`
    padding: 12px;
    border: 1px solid #d0d0d0;
    border-radius: 10px;
    font-size: 15px;
    background: white;
    &:focus {
        border-color: #1e3c72;
        box-shadow: 0 0 0 3px rgba(30, 60, 114, 0.15);
        outline: none;
    }
`;

const SubmitButton = styled.button`
    padding: 14px;
    background: linear-gradient(135deg, #1e3c72, #2a5298);
    color: white;
    font-weight: 600;
    font-size: 16px;
    border: none;
    border-radius: 12px;
    cursor: pointer;
    transition: 0.25s;
    margin-top: 10px;
    &:hover {
        filter: brightness(1.1);
    }
    &:disabled {
        background: #b4b4b4;
        cursor: not-allowed;
    }
`;

const ErrorText = styled.p`
    color: #ff3b3b;
    font-size: 14px;
    font-weight: 600;
    text-align: center;
`;

const AdminCardModal = ({ card, onClose, onCardAction }) => {
    const isEditing = !!card;

    const initialCardState = {
        number: generateRandomDigits(16),
        cvv: generateRandomDigits(3),
        balance: '',
        expirationDate: generateExpirationDate(),
        status: 'ACTIVE',
        userId: '',
    };

    const [formData, setFormData] = useState(initialCardState);
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchUsers = async () => {
            try {
                const token = localStorage.getItem("token");

                const response = await axios.get("http://localhost:8080/api/users/admin/all", {
                    headers: { Authorization: `Bearer ${token}` }
                });

                setUsers(response.data);

                if (isEditing) {
                    const initialBalance = card.balance ? parseFloat(card.balance).toFixed(2) : '';

                    setFormData({
                        number: card.number || '',
                        cvv: card.cvv || '',
                        balance: initialBalance,
                        expirationDate: card.expirationDate || '',
                        status: card.status || 'ACTIVE',
                        userId: card.user?.id || card.userId || '',
                    });
                }

            } catch (err) {
                setError("Failed to load users.");
            }
        };

        fetchUsers();
    }, [isEditing, card]);


    const handleBalanceInput = (e) => {
        let value = e.target.value.replace(/[^0-9.]/g, '');
        const parts = value.split('.');
        if (parts.length > 2) parts.pop();
        if (parts[1]) parts[1] = parts[1].slice(0, 2);
        value = parts.join('.');

        setFormData(prev => ({ ...prev, balance: value }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError(null);
        setLoading(true);

        if (!formData.userId) return setError("Please select card owner."), setLoading(false);
        if (isEditing && formData.number.length !== 16) return setError("Card number must be 16 digits."), setLoading(false);
        if (formData.cvv.length !== 3) return setError("CVV must be 3 digits."), setLoading(false);

        const finalBalance = formData.balance === '' || formData.balance === null
            ? 0.00
            : parseFloat(formData.balance);

        if (finalBalance < 0) return setError("Balance cannot be negative."), setLoading(false);
        if (isNaN(finalBalance)) return setError("Invalid balance format."), setLoading(false);


        const token = localStorage.getItem("token");

        const payload = {
            number: formData.number,
            cvv: formData.cvv,
            balance: finalBalance,
            expirationDate: formData.expirationDate,
            status: formData.status,
            userId: Number(formData.userId)
        };

        try {
            if (isEditing) {
                await axios.put(`http://localhost:8080/api/cards/admin/${card.id}`, payload, {
                    headers: { Authorization: `Bearer ${token}` }
                });
                onCardAction("Card updated successfully!", true);
            } else {
                await axios.post("http://localhost:8080/api/cards/admin", payload, {
                    headers: { Authorization: `Bearer ${token}` }
                });
                onCardAction("Card created successfully!", true);
            }
        } catch (err) {
            const errorMessage = isEditing ? "Failed to update card." : "Failed to create card.";
            setError(errorMessage);
            onCardAction(errorMessage, false);
        }

        setLoading(false);
    };

    return (
        <ModalWrapper
            initial={{ opacity: 0, scale: 0.85 }}
            animate={{ opacity: 1, scale: 1 }}
            exit={{ opacity: 0, scale: 0.85 }}
            onClick={(e) => e.stopPropagation()}
        >
            <CloseButton onClick={onClose}>
                <FaTimes />
            </CloseButton>

            <ModalTitle>{isEditing ? "Top Up Balance" : "Create Card"}</ModalTitle>

            <Form onSubmit={handleSubmit}>

                <InputGroup>
                    <Label>Card Owner</Label>
                    <Select
                        name="userId"
                        value={formData.userId}
                        onChange={(e) =>
                            setFormData(prev => ({ ...prev, userId: e.target.value }))
                        }
                        disabled={isEditing}
                    >
                        <option value="">-- Select Owner --</option>
                        {users.map(u => (
                            <option key={u.id} value={u.id}>{u.name}</option>
                        ))}
                    </Select>
                </InputGroup>

                <div style={{ display: 'flex', gap: '15px' }}>

                    <InputGroup style={{ flex: 1 }}>
                        <Label>Balance</Label>
                        <Input
                            name="balance"
                            value={formData.balance}
                            onChange={handleBalanceInput}
                            placeholder="0.00"
                        />
                    </InputGroup>

                </div>

                {error && <ErrorText>{error}</ErrorText>}

                <SubmitButton type="submit" disabled={loading}>
                    {loading ? "Processing..." : isEditing ? "Save Changes" : "Create Card"}
                </SubmitButton>
            </Form>
        </ModalWrapper>
    );
};

export default AdminCardModal;