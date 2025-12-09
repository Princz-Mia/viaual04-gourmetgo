import React, { createContext, useState, useEffect, useContext } from 'react';
import { fetchProfile } from '../api/userService';
import { logout as logoutService } from '../api/authService';
import { toast } from 'react-toastify';
import axiosInstance from '../api/axiosConfig';

export const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        // Try to fetch user profile to check if user is authenticated
        fetchProfile()
            .then(profile => {
                setUser(profile);
            })
            .catch(() => {
                // User not authenticated or token expired
                setUser(null);
            })
            .finally(() => {
                setLoading(false);
            });
    }, []);

    const login = (data) => {
        // After successful login, fetch the full user profile
        fetchProfile()
            .then(profile => {
                setUser(profile);
            })
            .catch(err => {
                toast.error("Error loading user profile");
                setUser(null);
            });
    };

    const logout = async () => {
        try {
            await logoutService();
        } catch (error) {
            console.error("Logout error:", error);
        } finally {
            setUser(null);
        }
    };

    return (
        <AuthContext.Provider value={{ user, login, logout, loading }}>
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error('useAuth must be used within a AuthProvider');
    }
    return context;
};
