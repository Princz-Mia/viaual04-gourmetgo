import React, { createContext, useState, useEffect } from 'react';
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

    // once token+role+id are known, fetch full profile
    useEffect(() => {
        if (!user?.token) {
            setLoading(false);
            return;
        }

        console.log('⏳ fetchProfile tokennel:', user.token);
        fetchProfile()
            .then(profile => {
                //console.log('✅ profile betöltve:', profile);
                setUser(profile);
            })
            .catch(err => {
                //console.error('❌ fetchProfile hiba:', err);
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
