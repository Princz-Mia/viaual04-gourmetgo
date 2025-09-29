import React, { createContext, useState, useEffect, useContext } from 'react';
import { decodeJwt } from '../utils/jwt';
import { fetchProfile } from '../api/userService';
import { toast } from 'react-toastify';

export const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(() => {
        const stored = localStorage.getItem('user');
        return stored ? JSON.parse(stored) : null;
    });
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        if (!user?.token) {
            setLoading(false);
            return;
        }

        fetchProfile()
            .then(profile => {
                setUser(profile);
            })
            .catch(err => {
                toast.error("Error during the loading of personal information")
                localStorage.removeItem('user');
                setUser(null);
            })
            .finally(() => {
                setLoading(false);
            });

    }, [user?.token]);


    const login = (data) => {
        const claims = decodeJwt(data.token);
        const id = claims.id;
        const role = Array.isArray(claims.role) ? claims.role[0] : claims.role;

        const userToStore = { id, role: role.authority, token: data.token };
        localStorage.setItem('user', JSON.stringify(userToStore));
        setUser(userToStore);
    };

    const logout = () => {
        localStorage.removeItem('user');
        setUser(null);
    };

    return (
        <AuthContext.Provider value={{ user, login, logout }}>
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
