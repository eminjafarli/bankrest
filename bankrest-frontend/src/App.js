import {Routes, Route, Navigate} from "react-router-dom";
import HomePage from "./HomePage";
import UsersDashboard from "./UsersDashboard";
import CardsDashboard from "./CardsDashboard";
import LoginPage from "./LoginPage";
import SignupPage from "./SignupPage";
import ProtectedRoute from "./ProtectedRoute";

function App() {
    return (
        <Routes>
            <Route path="/" element={<Navigate to="/login" replace/>}/>
            <Route path="/login" element={<LoginPage/>}/>
            <Route path="/signup" element={<SignupPage/>}/>
            <Route
                path="/home"
                element={
                    <ProtectedRoute>
                        <HomePage/>
                    </ProtectedRoute>
                }
            />
            <Route
                path="/users-dashboard"
                element={
                    <ProtectedRoute>
                        <UsersDashboard/>
                    </ProtectedRoute>
                }
            />
            <Route
                path="/cards-dashboard"
                element={

                    <CardsDashboard/>

                }
            />
        </Routes>
    );
}

export default App;
