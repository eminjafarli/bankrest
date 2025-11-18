import React from "react";
import styled from "styled-components";
import { AnimatePresence, motion } from "framer-motion";
import { FaExclamationTriangle } from "react-icons/fa";

const COLORS = {
    primaryBlue: "#376bf3",
    darkNavy: "#0f172a",
    textColor: "#0b2239",
    shadowDeep: "0 25px 70px rgba(0, 0, 0, 0.25)",
    errorRed: "#e55656",
    mediumGrey: "#6b7280",
};

const Backdrop = styled(motion.div)`
    position: fixed;
    inset: 0;
    background: rgba(8, 12, 28, 0.45);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 999;
`;

const ModalWrapper = styled(motion.div)`
    background: #ffffff;
    padding: 35px 40px;
    border-radius: 20px;
    width: 95%;
    max-width: 420px;
    box-shadow: ${COLORS.shadowDeep};
    position: relative;
    border: 1px solid rgba(0, 0, 0, 0.07);
    color: ${COLORS.textColor};
`;

const Title = styled.h3`
    display: flex;
    align-items: center;
    gap: 12px;
    font-size: 22px;
    margin-bottom: 8px;
    color: ${COLORS.darkNavy};
    font-weight: 700;
`;

const Message = styled.p`
    font-size: 15px;
    color: ${COLORS.mediumGrey};
    margin-bottom: 25px;
    line-height: 1.5;
`;

const ButtonGroup = styled.div`
    display: flex;
    justify-content: flex-end;
    gap: 12px;
    margin-top: 20px;
`;

const Button = styled(motion.button)`
    padding: 10px 18px;
    border: none;
    border-radius: 10px;
    font-size: 15px;
    font-weight: 600;
    cursor: pointer;
    transition: background-color 0.2s, box-shadow 0.2s;
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.05);

    &.cancel {
        background: #eef1f6;
        color: ${COLORS.textColor};
        &:hover {
            background: #dfe2e7;
        }
    }

    &.confirm {
        background: ${COLORS.errorRed};
        color: white;
        box-shadow: 0 4px 10px rgba(229, 86, 86, 0.3);
        &:hover {
            background: #c94040;
        }
    }
`;

const ConfirmModal = ({ title, onCancel, onConfirm, confirmText = 'Delete', message }) => {
    return (
        <AnimatePresence>
            <Backdrop
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                exit={{ opacity: 0 }}
                onClick={onCancel}
            >
                <ModalWrapper
                    initial={{ scale: 0.9, opacity: 0 }}
                    animate={{ scale: 1, opacity: 1 }}
                    exit={{ scale: 0.9, opacity: 0 }}
                    transition={{ duration: 0.2 }}
                    onClick={(e) => e.stopPropagation()}
                >
                    <Title>
                        <FaExclamationTriangle size={24} color={COLORS.errorRed} />
                        {title}
                    </Title>

                    <Message>
                        {message || "Are you sure you want to proceed with this action? This operation cannot be undone."}
                    </Message>

                    <ButtonGroup>
                        <Button
                            className="cancel"
                            onClick={onCancel}
                            whileTap={{ scale: 0.98 }}
                        >
                            Cancel
                        </Button>
                        <Button
                            className="confirm"
                            onClick={onConfirm}
                            whileTap={{ scale: 0.98 }}
                        >
                            {confirmText}
                        </Button>
                    </ButtonGroup>
                </ModalWrapper>
            </Backdrop>
        </AnimatePresence>
    );
};

export default ConfirmModal;